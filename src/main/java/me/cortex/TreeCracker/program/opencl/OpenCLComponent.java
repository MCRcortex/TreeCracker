package me.cortex.TreeCracker.program.opencl;

public abstract class OpenCLComponent {
    protected final OpenCLContext context;

    public OpenCLComponent(OpenCLContext context) {
        this.context = context;
    }


    public void release() {}
}
