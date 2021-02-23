package me.cortex.TreeCracker.program.opencl;

import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_kernel;
import org.jocl.cl_program;

import static org.jocl.CL.*;
import static org.jocl.CL.clReleaseProgram;

public class OpenCLKernel extends OpenCLComponent {
    private final String kernel_name;
    private final cl_kernel kernel;
    private final OpenCLKernelParameter[] kernel_parameters;

    protected OpenCLKernel(OpenCLContext context, String name, cl_kernel compiled_kernel, OpenCLKernelParameter[] parameters) {
        super(context);
        kernel_name = name;
        kernel = compiled_kernel;
        kernel_parameters = parameters;
    }

    public void enqueue(long[] global_work_size, Object... args) {
        setKernelParameter(args);
        enqueueExecution(global_work_size, null, null);
    }

    private void setKernelParameter(Object... args) {
        if (args.length != kernel_parameters.length)
            throw new IllegalArgumentException("Length of provided arguments not equal to the length of expected paramaters");

        for (int i = 0; i < args.length; i++) {
            if (!kernel_parameters[i].objectCanBeThisParameter(args[i]))
                throw new IllegalArgumentException("Parameter provided could not be a kernel parameter");

            Pointer arg_pointer = kernel_parameters[i].objectToPointer(args[i]);
            setArg(kernel_parameters[i].kernel_arg_index, arg_pointer);
        }
    }

    private void setArg(int index, Pointer pointer) {
        setArg(index, pointer, Sizeof.cl_mem);
    }
    private void setArg(int index, Pointer pointer, long size) {
        clSetKernelArg(kernel, index, size, pointer);
    }

    private int enqueueExecution(long[] global_work_size, long[] local_work_size, long[] global_work_offset) {
        return clEnqueueNDRangeKernel(context.commandQueue, kernel, global_work_size.length, global_work_offset,
                global_work_size, local_work_size, 0, null, null);
    }

    public void release() {
        clReleaseKernel(kernel);
    }
}
