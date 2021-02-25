package me.cortex.TreeCracker.program.opencl;

import org.jocl.Pointer;

interface IGPUMemory {
    Pointer getGPUMemory();
}
