package me.cortex.TreeCracker.program.opencl;

import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_mem;
import sun.nio.ch.DirectBuffer;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import static org.jocl.CL.*;

public class OpenCLSyncableMemory {
    private cl_mem gpu_memory;
    private Buffer cpu_memory;
    private int size_of_type;

    private final OpenCLContext context;


    public OpenCLSyncableMemory(OpenCLContext context, int size_of_type, Buffer buffer) {
        if (buffer.capacity() % size_of_type != 0)
            throw new IllegalArgumentException("Buffer capacity couldnt contain size of element");
        this.size_of_type = size_of_type;
        this.cpu_memory = buffer;
        this.context = context;
        this.gpu_memory =  clCreateBuffer(context.context, CL_MEM_READ_WRITE, buffer.capacity(), Pointer.to(buffer), null);
    }

    public OpenCLSyncableMemory(OpenCLContext context, int size_of_type, int number_of_elements) {
        this(context, size_of_type, ByteBuffer.allocateDirect(size_of_type * number_of_elements));
    }

    public OpenCLSyncableMemory(OpenCLContext context, byte[] array) {
        this(context, 1, ByteBuffer.wrap(array));
    }

    public OpenCLSyncableMemory(OpenCLContext context, int[] array) {
        this(context, 4, IntBuffer.wrap(array));
    }

    public OpenCLSyncableMemory(OpenCLContext context, long[] array) {
        this(context, 8, LongBuffer.wrap(array));
    }




    public void synchronizeToHost() {
        this.synchronizeToHost(cpu_memory.capacity() / size_of_type);
    }
    public void synchronizeToGPU() {
        this.synchronizeToGPU(cpu_memory.capacity() / size_of_type);
    }

    public void synchronizeToHost(int count) {
        this.synchronizeToHost(count, 0);
    }
    public void synchronizeToGPU(int count) {
        this.synchronizeToGPU(count, 0);
    }



    public int synchronizeToHost(int count, int offset) {
        return synchronizeToHost((long) count * size_of_type, offset, gpu_memory, Pointer.to(cpu_memory));
    }

    public int synchronizeToGPU(int count, int offset) {
        return this.synchronizeToGPU((long) count * size_of_type, offset, gpu_memory, Pointer.to(cpu_memory));
    }

    private int synchronizeToHost(long byte_count, int offset, cl_mem gpu, Pointer cpu) {
        return  clEnqueueReadBuffer(context.commandQueue, gpu, CL_TRUE, offset,
                byte_count, cpu,
                0, null, null);
    }

    private int synchronizeToGPU(long byte_count, int offset, cl_mem gpu, Pointer cpu) {
        return clEnqueueWriteBuffer(context.commandQueue, gpu, CL_TRUE, offset,
                byte_count, cpu,
                0, null, null);
    }


    public void release() {
        clReleaseMemObject(gpu_memory);
    }
}
