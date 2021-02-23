package me.cortex.TreeCracker.program.opencl;

import org.jocl.Pointer;

public class OpenCLKernelParameter {
    public final Class<?> type;
    public final int kernel_arg_index;


    public OpenCLKernelParameter(Class<?> type, int arg_index) {
        this.type = type;
        this.kernel_arg_index = arg_index;
    }

    public boolean objectCanBeThisParameter(Object object) {
        return type == object.getClass();
    }

    public Pointer objectToPointer(Object object) {
        if (!objectCanBeThisParameter(object))
            throw new IllegalArgumentException("Object cant be of the parameter type");

        return null;
    }
}
