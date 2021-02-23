package me.cortex.TreeCracker.program.opencl;

import org.jocl.Pointer;
import org.jocl.cl_program;

import java.util.Arrays;

import static org.jocl.CL.*;

public class OpenCLProgram extends OpenCLComponent {
    protected cl_program program;
    private String[] raw_source;

    private OpenCLProgram(OpenCLContext context, cl_program program) {
        super(context);
        this.program = program;
        this.raw_source = null;
    }

    public OpenCLProgram(OpenCLContext context, String[] sources, String compileOptions) {
        super(context);

        raw_source = sources;
        program = clCreateProgramWithSource(context.context, sources.length, raw_source, null, null);

        // Build the program
        clBuildProgram(program, 0, null, compileOptions, null, null);
    }

    public OpenCLProgram(OpenCLContext context, String[] sources) {
        this(context, sources, null);
    }
    public OpenCLProgram(OpenCLContext context, String source) {
        this(context, new String[]{source});
    }

    public OpenCLKernel createKernel(String kernel_name, Object... argTypes) {
        OpenCLKernelParameter[] params = new OpenCLKernelParameter[argTypes.length];
        for (int i = 0; i < params.length; i++) {
            if (argTypes[i] instanceof OpenCLKernelParameter) {
                params[i] = (OpenCLKernelParameter) argTypes[i];
                continue;
            }
            if (argTypes[i] instanceof OpenCLSyncableMemory) {
                params[i] = new OpenCLKernelParameter(OpenCLSyncableMemory.class, i, false, false);
            }
            if (argTypes[i] instanceof Class<?>) {
                params[i] = new OpenCLKernelParameter((Class<?>) argTypes[i], i);
                continue;
            }
            throw new IllegalArgumentException("Argument parameter description");
        }
        return createKernel(kernel_name, params);
    }

    public OpenCLKernel createKernel(String kernel_name, OpenCLKernelParameter[] args) {
        return new OpenCLKernel(context, kernel_name, clCreateKernel(program, kernel_name, null), args);
    }



    public OpenCLProgram linkWith(OpenCLProgram[] others, String options) {
        int[] returnCode = new int[1];
        cl_program[] programs = new cl_program[others.length];
        for (int i = 0; i < others.length; i++) {
            programs[i] = others[i].program;
        }
        cl_program linked_program = clLinkProgram(context.context, 0, null, options, programs.length, programs, null, null, returnCode);

        return new OpenCLProgram(context, linked_program);
    }

    public OpenCLProgram linkWith(OpenCLProgram other) {
        return linkWith(new OpenCLProgram[]{other}, null);
    }

    public void release() {
        clReleaseProgram(program);
    }
}
