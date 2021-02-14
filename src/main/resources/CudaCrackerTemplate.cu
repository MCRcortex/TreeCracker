#include <thread>
#include <vector>
#include <iostream>
#include <string>
#include <fstream>
#include <stdlib.h>
#include <utility>
#include <mutex>
#include <map>
#include <algorithm>
#define _USE_MATH_DEFINES
#include <math.h>
#include <chrono>


uint64_t millis() {return (std::chrono::duration_cast< std::chrono::milliseconds >(std::chrono::system_clock::now().time_since_epoch())).count();}

#define GPU_ASSERT(code) gpuAssert((code), __FILE__, __LINE__)
inline void gpuAssert(cudaError_t code, const char *file, int line) {
  if (code != cudaSuccess) {
	fprintf(stderr, "GPUassert: %s (code %d) %s %d\n", cudaGetErrorString(code), code, file, line);
	exit(code);
  }
}

#define SETGPU(gpuId) cudaSetDevice(gpuId);\
	GPU_ASSERT(cudaPeekAtLastError());\
	GPU_ASSERT(cudaDeviceSynchronize());\
	GPU_ASSERT(cudaPeekAtLastError());

#define DEVICEABLE __host__ __device__



#define THREAD_SIZE 256LLU
#define BLOCK_SIZE (1LLU<<28) //(1LLU<<29)
#define BATCH_SIZE (THREAD_SIZE * BLOCK_SIZE)



__managed__ uint32_t count = 0;
__managed__ uint64_t seedBuff[60000000];


__managed__ uint32_t countOut = 0;
__managed__ uint64_t outputSeedBuff[6000];//Max seed output for the secondary filter


__global__ __launch_bounds__(THREAD_SIZE) void InitalFilter(const uint64_t offset) {
	uint64_t seed = (((uint64_t)blockIdx.x * (uint64_t)blockDim.x + (uint64_t)threadIdx.x)) + offset;

	PRIMARY_TREE_FILTER

	//TODO: Have different seed buffers per thread or somthing, so that the atomicAdd isnt a bottleneck
	seedBuff[atomicAdd(&count, 1)] = (((uint64_t)blockIdx.x * (uint64_t)blockDim.x + (uint64_t)threadIdx.x)) + offset;
}





AUX_TREE_FUNCTIONS_REPLACEMENT



#define NEXT_INT_16(seed) (((seed = ((seed * 0x5DEECE66DLLU + 0xBLLU)&((1LLU<<48)-1)))>>(48-4)))

#define TREE_TEST(testMethod, index, expected_x, expected_z, IF_TYPE) IF_TYPE ((!(mask & (1<<index))) && x_pos == expected_x && z_pos == expected_z) mask |= ((uint8_t)testMethod(seed))<<index;
#define TARGET_MASK ((1<<AUXILIARY_TREE_COUNT)-1)
__global__ __launch_bounds__(THREAD_SIZE) void SecondaryFilter() {
	uint64_t idx = ((((uint64_t)blockIdx.x * (uint64_t)blockDim.x + (uint64_t)threadIdx.x)));
	if (idx >= count)
		return;
	uint64_t seed = seedBuff[idx];
	seed = LCG_REVERSE_STAGE_2_REPLACEMENT;
	
	uint8_t mask = 0;
	int32_t x_pos;
	int32_t z_pos = NEXT_INT_16(seed);
	for (int32_t index = 0; index < MAX_TREE_RNG_RANGE_REPLACEMENT * 2 && mask != TARGET_MASK; index++) {
        x_pos = z_pos;
		z_pos = NEXT_INT_16(seed);
		
        AUX_TREE_TEST_INNER_LOOP_CALL_REPLACEMENT
		

	}
	
	
	if (mask != TARGET_MASK)
		return;
	
	outputSeedBuff[atomicAdd(&countOut, 1)] = seedBuff[idx];
}











int main() {
	SETGPU(0);
	std::ofstream outfile("output_seeds.dat", std::ofstream::binary);
	for (uint64_t offset = 0; offset < (1LLU<<44); offset += BATCH_SIZE) {
		uint64_t start = millis();
		
		count = 0;
		countOut = 0;
		InitalFilter<<<BLOCK_SIZE, THREAD_SIZE>>>((((uint64_t)INIT_TREE_INNER_X) << 44) | offset);
		GPU_ASSERT(cudaPeekAtLastError());	
		GPU_ASSERT(cudaDeviceSynchronize());
		GPU_ASSERT(cudaPeekAtLastError());
		uint64_t step1 = millis()-start;
		start = millis();

        uint64_t step2 = 0;
        uint64_t step3 = 0;
		if (count != 0) {
            SecondaryFilter<<<ceil((double)count/THREAD_SIZE), THREAD_SIZE>>>();
            GPU_ASSERT(cudaPeekAtLastError());
            GPU_ASSERT(cudaDeviceSynchronize());
            GPU_ASSERT(cudaPeekAtLastError());
            step2 = millis()-start;
            start = millis();

            for (uint64_t index = 0; index < countOut; index++) {
                outfile << outputSeedBuff[index] << std::endl;
                outfile.flush();
            }
            step3 = millis()-start;
		}

		std::cout << "Finished gpu: " << (step1+step2+step3) << ", " << step1 << ", " << step2 << ", " << step3 << ", " << count << ", " << countOut << ", " << (((1LLU<<44) - offset)/BATCH_SIZE)  << std::endl;
	}
	outfile.close();
	return 1;
}