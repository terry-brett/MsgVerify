package com.terrydroid.msgverify.benchmark

/** Memory readings in bytes. A null field means the platform has no equivalent metric. */
data class MemorySnapshot(
    /**
     * Whole-process footprint, the primary metric for cross-platform comparison.
     * Android: total proportional set size (PSS). iOS: `phys_footprint` from TASK_VM_INFO
     * (the value shown by Xcode's memory gauge and used by iOS for memory limits).
     */
    val footprintBytes: Long?,
    /** Resident set size. Android: VmRSS from /proc/self/status. iOS: `resident_size`. */
    val residentBytes: Long?,
    /** Android only: managed (Java) heap in use, Runtime.totalMemory() - Runtime.freeMemory(). */
    val managedHeapBytes: Long?,
    /** Android only: native heap allocated, Debug.getNativeHeapAllocatedSize(). */
    val nativeHeapBytes: Long?,
)

data class DeviceInfo(
    val platform: String,
    val osVersion: String,
    /** Hardware model identifier, e.g. "Pixel 9 Pro XL" or "iPhone17,5". */
    val model: String,
    val isSimulator: Boolean,
    val isDebugBuild: Boolean,
)

expect fun benchmarkDeviceInfo(): DeviceInfo

expect fun benchmarkMemorySnapshot(): MemorySnapshot

/** Platform thermal state as reported by the OS (iOS: nominal/fair/serious/critical; Android: thermal status). */
expect fun benchmarkThermalState(): String

/** Writes [contents] to the platform's benchmark output directory and returns the absolute path. */
expect fun writeBenchmarkFile(fileName: String, contents: String): String
