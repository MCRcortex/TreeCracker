package me.cortex.TreeCracker.program;

import org.jocl.*;

import java.util.Arrays;

import static org.jocl.CL.*;

public class OpenCLProgram {
    private String source;

    private final OpenCLContextHolder contextHolder;

    private cl_kernel kernel;
    private final String kernelName;
    private final int argCount;

    public OpenCLProgram(OpenCLContextHolder context, String kernelName, int kernelArgumentCount, String source) {
        this.contextHolder = context;
        this.source = source;
        this.kernelName = kernelName;
        this.argCount = kernelArgumentCount;
    }


    public void compileKernel() {
        String[] programSources = new String[]{source};

        // Create the program from the sources
        cl_program program = clCreateProgramWithSource(contextHolder.context, programSources.length, programSources, null, null);

        // Build the program
        clBuildProgram(program, 0, null, null, null, null);

        // Create the kernel
        kernel = clCreateKernel(program, kernelName, null);

        //TODO: Check if i can actually do this
        clReleaseProgram(program);
    }

    //TODO: make it so that you can pass in stuff like normal integers and longs
    public void enqueue(long[] global_work_size, long[] local_work_size, Pointer... arguments) {
        enqueue(global_work_size, local_work_size, new long[global_work_size.length], arguments);
    }
    public void enqueue(long[] global_work_size, long[] local_work_size, long[] global_work_offset, Pointer... arguments) {
        if (arguments.length != argCount)
            throw new IllegalArgumentException("The number of arguments given dont match the expected argument count");

        for(int i = 0; i < argCount; i++) {
            int errorCode;
            if ((errorCode = clSetKernelArg(kernel, i, Sizeof.cl_mem, arguments[i])) != 0) {
                throw new IllegalStateException("Got error code " + errorCode + " while setting arguments");
            }
        }

        clEnqueueNDRangeKernel(contextHolder.commandQueue, kernel, global_work_size.length, global_work_offset,
                global_work_size, local_work_size, 0, null, null);
    }


    public static OpenCLContextHolder createOpenCLContextAndQueue() {
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

        long[] size_of_result = new long[1];
        clGetPlatformInfo(platform, CL_PLATFORM_PROFILE, 0, Pointer.to(new char[199999]),size_of_result);
        System.out.println(size_of_result[0]);
        return new OpenCLContextHolder(context, device, properties, commandQueue);
    }

    public static class OpenCLContextHolder {
        public final cl_context context;
        public final cl_device_id device;
        public final cl_queue_properties properties;
        public final cl_command_queue commandQueue;
        public OpenCLContextHolder(cl_context context, cl_device_id device, cl_queue_properties properties, cl_command_queue commandQueue) {
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


    public static void main(String[] args) {
        OpenCLContextHolder holder = createOpenCLContextAndQueue();
        OpenCLProgram program = new OpenCLProgram(holder,"sampleKernel",1,
                "#pragma OPENCL EXTENSION cl_khr_int64_base_atomics: enable\n__kernel void "+
                "sampleKernel(__global ulong *a)"+
                "{"+
                "    ulong seed = (((ulong)get_global_id(0))<<31) + get_global_id(1);"+
                        "if ((((seed *     25214903917LU +              11LU) >> 44) & 15) != 15) return;\n" +
                        "if ((((seed)*120950523281469LU + 102626409374399LU)&(((1LU<<1)-1)<<(48-1))) != (0LU<<(48-1))) return;\n" +
                        "\tif ((((seed)*76790647859193LU + 25707281917278LU)&(((1LU<<1)-1)<<(48-1))) != (0LU<<(48-1))) return;\n" +
                        "\tif ((((seed)*61282721086213LU + 25979478236433LU)&(((1LU<<1)-1)<<(48-1))) != (1LU<<(48-1))) return;\n" +
                        "\tif ((((seed)*177269950146317LU + 148267022728371LU)&(((1LU<<1)-1)<<(48-1))) != (1LU<<(48-1))) return;\n" +
                        "\tif ((((seed)*19927021227657LU + 127911637363266LU)&(((1LU<<1)-1)<<(48-1))) != (1LU<<(48-1))) return;\n" +
                        "\tif ((((seed)*92070806603349LU + 65633894156837LU)&(((1LU<<1)-1)<<(48-1))) != (0LU<<(48-1))) return;\n" +
                        "\tif ((((seed)*118637304785629LU + 262259097190887LU)&(((1LU<<1)-1)<<(48-1))) != (1LU<<(48-1))) return;\n" +
                        "\tif ((((seed)*12659659028133LU + 156526639281273LU)&(((1LU<<1)-1)<<(48-1))) != (1LU<<(48-1))) return;\n" +
                "    atomic_inc(a);"+
                "}");

        program = new OpenCLProgram(holder, "TEST", 1, "#pragma OPENCL EXTENSION cl_khr_int64_base_atomics: enable\n"+
                "#define TREE_X 2\n" +
                "#define TREE_Z 2\n" +
                "\n" +
                "\n" +
                "\n" +
                "#define signed_seed_t long\n" +
                "\n" +
                "#define MODULUS (1L << 48)\n" +
                "#define SQUARE_SIDE (MODULUS / 16)\n" +
                "#define X_TRANSLATE 0\n" +
                "#define Z_TRANSLATE 11\n" +
                "#define L00 7847617L\n" +
                "#define L01 (-18218081L)\n" +
                "#define L10 4824621L\n" +
                "#define L11 24667315L\n" +
                "#define LI00 (24667315.0 / 16)\n" +
                "#define LI01 (18218081.0 / 16)\n" +
                "#define LI10 (-4824621.0 / 16)\n" +
                "#define LI11 (7847617.0 / 16)\n" +
                "\n" +
                "#define CONST_MIN(a, b) ((a) < (b) ? (a) : (b))\n" +
                "#define CONST_MIN4(a, b, c, d) CONST_MIN(CONST_MIN(a, b), CONST_MIN(c, d))\n" +
                "#define CONST_MAX(a, b) ((a) > (b) ? (a) : (b))\n" +
                "#define CONST_MAX4(a, b, c, d) CONST_MAX(CONST_MAX(a, b), CONST_MAX(c, d))\n" +
                "#define CONST_FLOOR(x) ((x) < (signed_seed_t) (x) ? (signed_seed_t) (x) - 1 : (signed_seed_t) (x))\n" +
                "#define CONST_CEIL(x) ((x) == (signed_seed_t) (x) ? (signed_seed_t) (x) : CONST_FLOOR((x) + 1))\n" +
                "#define CONST_LOWER(x, m, c) ((m) < 0 ? ((x) + 1 - (double) (c) / MODULUS) * (m) : ((x) - (double) (c) / MODULUS) * (m))\n" +
                "#define CONST_UPPER(x, m, c) ((m) < 0 ? ((x) - (double) (c) / MODULUS) * (m) : ((x) + 1 - (double) (c) / MODULUS) * (m))\n" +
                "\n" +
                "// for a parallelogram ABCD https://media.discordapp.net/attachments/668607204009574411/671018577561649163/unknown.png\n" +
                "#define B_X LI00\n" +
                "#define B_Z LI10\n" +
                "#define C_X (LI00 + LI01)\n" +
                "#define C_Z (LI10 + LI11)\n" +
                "#define D_X LI01\n" +
                "#define D_Z LI11\n" +
                "#define LOWER_X CONST_MIN4(0, B_X, C_X, D_X)\n" +
                "#define LOWER_Z CONST_MIN4(0, B_Z, C_Z, D_Z)\n" +
                "#define UPPER_X CONST_MAX4(0, B_X, C_X, D_X)\n" +
                "#define UPPER_Z CONST_MAX4(0, B_Z, C_Z, D_Z)\n" +
                "#define ORIG_SIZE_X (UPPER_X - LOWER_X + 1)\n" +
                "#define SIZE_X CONST_CEIL(ORIG_SIZE_X - D_X)\n" +
                "#define SIZE_Z CONST_CEIL(UPPER_Z - LOWER_Z + 1)\n" +
                "#define TOTAL_WORK_SIZE (SIZE_X * SIZE_Z)\n" +
                "\n" +
                "#define SEED_SPACE TOTAL_WORK_SIZE\n"+
                "__kernel void "+
                "TEST(__global ulong *a)"+
                "{"+
                "signed_seed_t lattice_x = (long)(get_global_id(0)) + LOWER_X;\n" +
                "    signed_seed_t lattice_z = (long)(get_global_id(1)) + LOWER_Z;\n" +
                "\n" +
                "    lattice_z += (B_X * lattice_z < B_Z * lattice_x) * SIZE_Z;\n" +
                "    if (D_X * lattice_z > D_Z * lattice_x) {\n" +
                "        lattice_x += B_X;\n" +
                "        lattice_z += B_Z;\n" +
                "    }\n" +
                "\n" +
                "    lattice_x += (signed_seed_t) (TREE_X * LI00 + TREE_Z * LI01);\n" +
                "    lattice_z += (signed_seed_t) (TREE_X * LI10 + TREE_Z * LI11);\n" +
                "\n" +
                "    ulong seed = (lattice_x * L00 + lattice_z * L01 + X_TRANSLATE)  & (MODULUS-1);" +
                "   if ((((seed)*120950523281469LU + 102626409374399LU)&(((1LU<<1)-1)<<(48-1))) != (0LU<<(48-1))) return;\n" +
                "\tif ((((seed)*76790647859193LU + 25707281917278LU)&(((1LU<<1)-1)<<(48-1))) != (0LU<<(48-1))) return;\n" +
                "\tif ((((seed)*61282721086213LU + 25979478236433LU)&(((1LU<<1)-1)<<(48-1))) != (1LU<<(48-1))) return;\n" +
                "\tif ((((seed)*177269950146317LU + 148267022728371LU)&(((1LU<<1)-1)<<(48-1))) != (1LU<<(48-1))) return;\n" +
                "\tif ((((seed)*19927021227657LU + 127911637363266LU)&(((1LU<<1)-1)<<(48-1))) != (1LU<<(48-1))) return;\n" +
                "\tif ((((seed)*92070806603349LU + 65633894156837LU)&(((1LU<<1)-1)<<(48-1))) != (0LU<<(48-1))) return;\n" +
                "\tif ((((seed)*118637304785629LU + 262259097190887LU)&(((1LU<<1)-1)<<(48-1))) != (1LU<<(48-1))) return;\n" +
                "\tif ((((seed)*12659659028133LU + 156526639281273LU)&(((1LU<<1)-1)<<(48-1))) != (1LU<<(48-1))) return;\n" +
                "\tif ((((((seed)*205749139540585LU + 277363943098LU)>>(48-31))&((1LU<<31)-1)) % 5) != 0) return;\n" +
                "\tif ((((((seed)*55986898099985LU + 49720483695876LU)>>(48-31))&((1LU<<31)-1)) % 3) != 0) return;\n" +
                "\tif ((((((seed)*233752471717045LU + 11718085204285LU)>>(48-31))&((1LU<<31)-1)) % 10) == 0) return;"+
                "    atomic_inc(a);"+
                "}");
        program.compileKernel();

        long dstArray[] = new long[1];
        Pointer dst = Pointer.to(dstArray);

        cl_mem srcMemA = clCreateBuffer(holder.context,
                CL_MEM_READ_WRITE,
                Sizeof.cl_ulong, null, null);

        long start = System.nanoTime();
        //program.enqueue(new long[]{1<<9,(1L<<31) - 256}, new long[]{1<<0, 1<<8}, new long[]{0,0}, Pointer.to(srcMemA));
        program.enqueue(new long[]{(1<<15), 792064}, new long[]{1<<0, 1<<8}, new long[]{0,0}, Pointer.to(srcMemA));
        clEnqueueReadBuffer(holder.commandQueue, srcMemA, CL_TRUE, 0,
                Sizeof.cl_ulong, dst, 0, null, null);
        System.out.println((double)((System.nanoTime() - start)/1000/1000)/1000);

        System.out.println(Arrays.toString(dstArray));
        holder.release();
        program.release();
    }


    public void release() {
        clReleaseKernel(kernel);
    }
}
