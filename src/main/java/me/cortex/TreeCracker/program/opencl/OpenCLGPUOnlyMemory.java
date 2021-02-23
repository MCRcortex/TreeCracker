package me.cortex.TreeCracker.program.opencl;

import org.jocl.Pointer;
import org.jocl.*;

import static org.jocl.CL.*;

public class OpenCLGPUOnlyMemory extends OpenCLComponent{
    public final cl_mem gpu_memory;
    public OpenCLGPUOnlyMemory(OpenCLContext context, long size) {
        super(context);
        gpu_memory = clCreateBuffer(context.context, CL_MEM_HOST_NO_ACCESS, size, null, null);
    }

    public void release() {
        clReleaseMemObject(gpu_memory);
    }
}
