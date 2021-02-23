package me.cortex.TreeCracker.program.opencl;

import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_kernel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.jocl.CL.*;

public class OpenCLKernel extends OpenCLComponent {
    private final String kernel_name;
    private final cl_kernel kernel;
    private final OpenCLKernelParameter[] kernel_parameters;
    private final boolean mustBeSynchronous;

    private List<OpenCLSyncableMemory> params_to_sync = new ArrayList<>();
    private List<OpenCLComponent> params_to_release = new ArrayList<>();

    protected OpenCLKernel(OpenCLContext context, String name, cl_kernel compiled_kernel, OpenCLKernelParameter[] parameters) {
        super(context);
        kernel_name = name;
        kernel = compiled_kernel;
        kernel_parameters = parameters;

        boolean mustBeSync = false;
        for (OpenCLKernelParameter param : kernel_parameters) {
            mustBeSync |= param.shouldBeSynced;
        }
        mustBeSynchronous = mustBeSync;
    }

    public void call(long[] global_work_size, Object... args) {
        call(global_work_size, null, args);
    }
    public void callEx(long[] global_work_size, long[] local_work_size, long[] global_work_offset, Object... args) {
        setKernelParametersSynchronizedCall(args);
        enqueue(global_work_size, local_work_size, global_work_offset);
        context.flushAndFinish();
        synchronizeSynchronousParameters();
        context.flushAndFinish();
    }



    private void setKernelParametersSynchronizedCall(Object... args) {
        params_to_sync.clear();
        params_to_release.clear();

        if (args.length != kernel_parameters.length)
            throw new IllegalArgumentException("Length of provided arguments not equal to the length of expected paramaters");

        for (int i = 0; i < args.length; i++) {
            OpenCLKernelParameter param = kernel_parameters[i];
            Object arg = args[i];
            if (!param.objectCanBeThisParameter(arg))
                throw new IllegalArgumentException("Parameter provided could not be a kernel parameter");

            if (param.shouldBeSynced) {

                OpenCLSyncableMemory syncMemory = param.objectToSyncableMemory(context, arg);
                syncMemory.synchronizeToGPU();
                params_to_sync.add(syncMemory);
                setArg(param.kernel_arg_index, syncMemory.getGPUMemory());
                if (param.shouldRelease(arg))
                    params_to_release.add(syncMemory);

            } else if (param.isConstant) {
                OpenCLGPUReadonlyMemory readonlyMemory = param.objectToConstantMemory(context, args[i]);
                setArg(param.kernel_arg_index, readonlyMemory.gpu_memory);
                if (param.shouldRelease(arg))
                    params_to_release.add(readonlyMemory);
            } else {
                if (!(arg instanceof OpenCLSyncableMemory))
                    throw new IllegalStateException("non constant non synced memory cannot not be syncable memory");
                setArg(param.kernel_arg_index, ((OpenCLSyncableMemory)arg).getGPUMemory());
            }


        }
    }

    private void synchronizeSynchronousParameters() {
        for(OpenCLSyncableMemory memory : params_to_sync) {
            memory.synchronizeToHost();
        }
        for (OpenCLComponent param : params_to_release) {
            param.release();
        }
    }

    public void enqueue(long[] global_work_size, Object... args) {
        if (mustBeSynchronous)
            throw new IllegalStateException("Tried to enqueue a kernel that has to be synchronous");
        setKernelParametersEnqueuedCall(args);
        enqueue(global_work_size, null, null);
    }

    private void setKernelParametersEnqueuedCall(Object[] args) {
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (!kernel_parameters[i].objectCanBeThisParameter(arg)) {
                throw new IllegalArgumentException("Got unexpected arg type");
            }
            if (arg instanceof OpenCLSyncableMemory) {
                setArg(kernel_parameters[i].kernel_arg_index, ((OpenCLSyncableMemory)arg).getGPUMemory());
            } else if (arg instanceof OpenCLGPUReadonlyMemory) {
                setArg(kernel_parameters[i].kernel_arg_index, ((OpenCLGPUReadonlyMemory)arg).gpu_memory);
            } else {
                throw new IllegalArgumentException("Argument passed in not of OpenCLSyncableMemory or OpenCLGPUReadonlyMemory type");
            }
        }
    }


    private void setArg(int index, Pointer pointer) {
        setArg(index, pointer, Sizeof.cl_mem);
    }
    private void setArg(int index, Pointer pointer, long size) {
        clSetKernelArg(kernel, index, size, pointer);
    }

    private void enqueue(long[] global_work_size, long[] local_work_size, long[] global_work_offset) {
        clEnqueueNDRangeKernel(context.commandQueue, kernel, global_work_size.length, global_work_offset,
                global_work_size, local_work_size, 0, null, null);
    }

    public void release() {
        clReleaseKernel(kernel);
    }
}
