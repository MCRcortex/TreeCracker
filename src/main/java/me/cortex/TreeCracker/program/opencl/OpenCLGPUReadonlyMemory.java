package me.cortex.TreeCracker.program.opencl;

import org.jocl.Pointer;
import org.jocl.cl_mem;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import static org.jocl.CL.*;

public class OpenCLGPUReadonlyMemory extends OpenCLComponent {
    public final cl_mem gpu_memory_raw;
    public final Pointer gpu_memory;
    public OpenCLGPUReadonlyMemory(OpenCLContext context, int size_of_type, Buffer buffer) {
        super(context);
        this.gpu_memory_raw =  clCreateBuffer(context.context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, (long) size_of_type * buffer.capacity(), Pointer.to(buffer), null);
        this.gpu_memory = Pointer.to(gpu_memory_raw);
    }

    public OpenCLGPUReadonlyMemory(OpenCLContext context, long[] data) {
        this(context, 8, LongBuffer.wrap(data));
    }
    public OpenCLGPUReadonlyMemory(OpenCLContext context, int[] data) {
        this(context, 4, IntBuffer.wrap(data));
    }
    public OpenCLGPUReadonlyMemory(OpenCLContext context, byte[] data) {
        this(context, 1, ByteBuffer.wrap(data));
    }


    public void release() {
        clReleaseMemObject(gpu_memory_raw);
    }
}
