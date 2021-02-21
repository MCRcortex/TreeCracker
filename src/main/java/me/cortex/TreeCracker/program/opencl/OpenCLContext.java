package me.cortex.TreeCracker.program.opencl;

import org.jocl.cl_command_queue;
import org.jocl.cl_context;
import org.jocl.cl_device_id;
import org.jocl.cl_queue_properties;

import static org.jocl.CL.clReleaseCommandQueue;
import static org.jocl.CL.clReleaseContext;

public class OpenCLContext {
    public final cl_context context;
    public final cl_device_id device;
    public final cl_queue_properties properties;
    public final cl_command_queue commandQueue;
    public OpenCLContext(cl_context context, cl_device_id device, cl_queue_properties properties, cl_command_queue commandQueue) {
        this.context = context;
        this.device = device;
        this.properties = properties;
        this.commandQueue = commandQueue;
    }

    public void release() {
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);
    }
}
