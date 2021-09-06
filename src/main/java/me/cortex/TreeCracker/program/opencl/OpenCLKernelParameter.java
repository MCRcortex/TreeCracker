package me.cortex.TreeCracker.program.opencl;

import me.cortex.TreeCracker.NotImplementedException;

import java.nio.Buffer;

public class OpenCLKernelParameter {
    public final Class<?> type;
    public final int kernel_arg_index;

    public final boolean isConstant;
    public final boolean shouldBeSynced;

    public OpenCLKernelParameter(Class<?> type, int arg_index, boolean constant, boolean shouldBeSynced) {
        this.type = type;
        this.kernel_arg_index = arg_index;
        this.isConstant = constant;
        this.shouldBeSynced = shouldBeSynced;
    }

    public OpenCLKernelParameter(Class<?> type, int arg_index) {
        this(type, arg_index, true, false);
    }

    public boolean objectCanBeThisParameter(Object object) {
        return type == object.getClass();
    }

    public OpenCLGPUReadonlyMemory objectToConstantMemory(OpenCLContext context, Object object) {
        if (!objectCanBeThisParameter(object))
            throw new IllegalArgumentException("Object cant be of the parameter type");

        if (object instanceof OpenCLSyncableMemory)
            throw new NotImplementedException("Syncable memory as a constant not yet implmented");

        if (shouldBeSynced)
            throw new IllegalStateException("Tried to get a pointer to a syncable parameter, should call objectToSyncableMemory");

        if (!isConstant)
            throw new NotImplementedException("Non constant non synced objectToPointer not implemented");

        if (object instanceof OpenCLGPUReadonlyMemory)
            return ((OpenCLGPUReadonlyMemory)object);


        if (object instanceof long[])
            return new OpenCLGPUReadonlyMemory(context, (long[]) object);
        if (object instanceof int[])
            return new OpenCLGPUReadonlyMemory(context, (int[]) object);
        if (object instanceof byte[])
            return new OpenCLGPUReadonlyMemory(context, (byte[]) object);


        throw new NotImplementedException("Constant of given object not yet implemented");
    }


    public boolean shouldRelease(Object object) {
        if (!objectCanBeThisParameter(object))
            throw new IllegalArgumentException("Object cant be of the parameter type");

        if (object instanceof OpenCLGPUReadonlyMemory)
            return false;

        if (object instanceof OpenCLSyncableMemory)
            return false;

        if (object instanceof OpenCLGPUOnlyMemory)
            return false;


        return true;
    }


    public OpenCLSyncableMemory objectToSyncableMemory(OpenCLContext context, Object object) {
        if (!objectCanBeThisParameter(object))
            throw new IllegalArgumentException("Object cant be of the parameter type");

        if (!shouldBeSynced)
            throw new IllegalStateException("Tried to create syncable memory for parameter that should not be synced");

        if (isConstant)
            throw new IllegalStateException("No need to synchronise memory if its constant");

        if (!objectCanBeThisParameter(object))
            throw new IllegalArgumentException("Object cant be of the parameter type");

        if (object instanceof OpenCLSyncableMemory)
            return ((OpenCLSyncableMemory)object);

        if (object instanceof OpenCLGPUReadonlyMemory)
            throw new IllegalStateException("Cannot have synchronised readonly memory");

        if (object instanceof OpenCLGPUOnlyMemory)
            throw new IllegalStateException("Cannot have synchronised gpu memory, it kinda already is");



        if (object instanceof byte[])
            return new OpenCLSyncableMemory(context, (byte[]) object);
        if (object instanceof int[])
            return new OpenCLSyncableMemory(context, (int[]) object);
        if (object instanceof long[])
            return new OpenCLSyncableMemory(context, (long[]) object);

        if (object instanceof Buffer)
            throw new NotImplementedException("seamless synchroization of object is not implmented cause its not easy to get the size_of_type.");

        throw new NotImplementedException("Object type of syncable memory not implemented");
    }
}
