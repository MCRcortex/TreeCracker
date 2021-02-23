package me.cortex.TreeCracker.program.opencl;

import org.jocl.*;

import static org.jocl.CL.*;
import static org.jocl.CL.CL_PLATFORM_PROFILE;

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





    public static OpenCLContext createNewContext() {
        // The platform, device type and device number
        // that will be used
        final int platformIndex = 0;
        final long deviceType = CL_DEVICE_TYPE_ALL;
        final int deviceIndex = 0;

        // Enable exceptions and subsequently omit error checks in this sample
        CL.setExceptionsEnabled(true);

        // Obtain the number of platforms
        int numPlatformsArray[] = new int[1];
        clGetPlatformIDs(0, null, numPlatformsArray);
        int numPlatforms = numPlatformsArray[0];

        // Obtain a platform ID
        cl_platform_id platforms[] = new cl_platform_id[numPlatforms];
        clGetPlatformIDs(platforms.length, platforms, null);
        cl_platform_id platform = platforms[platformIndex];

        // Initialize the context properties
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);

        // Obtain the number of devices for the platform
        int numDevicesArray[] = new int[1];
        clGetDeviceIDs(platform, deviceType, 0, null, numDevicesArray);
        int numDevices = numDevicesArray[0];

        // Obtain a device ID
        cl_device_id devices[] = new cl_device_id[numDevices];
        clGetDeviceIDs(platform, deviceType, numDevices, devices, null);
        cl_device_id device = devices[deviceIndex];

        // Create a context for the selected device
        cl_context context = clCreateContext(
                contextProperties, 1, new cl_device_id[]{device},
                null, null, null);

        // Create a command-queue for the selected device
        cl_queue_properties properties = new cl_queue_properties();
        cl_command_queue commandQueue = clCreateCommandQueueWithProperties(
                context, device, properties, null);

        /*
        long[] size_of_result = new long[1];
        clGetPlatformInfo(platform, CL_PLATFORM_PROFILE, 0, Pointer.to(new char[199999]),size_of_result);
        System.out.println(size_of_result[0]);
         */

        return new OpenCLContext(context, device, properties, commandQueue);
    }
}
