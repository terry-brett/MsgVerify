package com.terrydroid.msgverify.benchmark

import kotlin.experimental.ExperimentalNativeApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSProcessInfo
import platform.Foundation.NSProcessInfoThermalState
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.thermalState
import platform.Foundation.writeToFile
import platform.UIKit.UIDevice
import platform.darwin.KERN_SUCCESS
import platform.darwin.TASK_VM_INFO
import platform.darwin.integer_tVar
import platform.darwin.mach_msg_type_number_tVar
import platform.darwin.mach_task_self_
import platform.darwin.task_info
import platform.darwin.task_vm_info_data_t
import platform.posix.uname
import platform.posix.utsname

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
actual fun benchmarkDeviceInfo(): DeviceInfo {
  val environment = NSProcessInfo.processInfo.environment
  return DeviceInfo(
      platform = "ios",
      osVersion = "${UIDevice.currentDevice.systemName} ${UIDevice.currentDevice.systemVersion}",
      model = (environment["SIMULATOR_MODEL_IDENTIFIER"] as? String) ?: hardwareMachine(),
      isSimulator = environment["SIMULATOR_DEVICE_NAME"] != null,
      isDebugBuild = Platform.isDebugBinary,
  )
}

@OptIn(ExperimentalForeignApi::class)
private fun hardwareMachine(): String = memScoped {
  val name = alloc<utsname>()
  uname(name.ptr)
  name.machine.toKString()
}

@OptIn(ExperimentalForeignApi::class)
actual fun benchmarkMemorySnapshot(): MemorySnapshot = memScoped {
  val info = alloc<task_vm_info_data_t>()
  val count = alloc<mach_msg_type_number_tVar>()
  // TASK_VM_INFO_COUNT: size of task_vm_info_data_t in natural_t (32-bit) units.
  count.value = (sizeOf<task_vm_info_data_t>() / sizeOf<integer_tVar>()).toUInt()
  val status = task_info(mach_task_self_, TASK_VM_INFO.toUInt(), info.ptr.reinterpret(), count.ptr)
  if (status != KERN_SUCCESS) {
    MemorySnapshot(null, null, null, null)
  } else {
    MemorySnapshot(
        footprintBytes = info.phys_footprint.toLong(),
        residentBytes = info.resident_size.toLong(),
        managedHeapBytes = null,
        nativeHeapBytes = null,
    )
  }
}

actual fun benchmarkThermalState(): String = when (NSProcessInfo.processInfo.thermalState) {
  NSProcessInfoThermalState.NSProcessInfoThermalStateNominal -> "nominal"
  NSProcessInfoThermalState.NSProcessInfoThermalStateFair -> "fair"
  NSProcessInfoThermalState.NSProcessInfoThermalStateSerious -> "serious"
  NSProcessInfoThermalState.NSProcessInfoThermalStateCritical -> "critical"
}

/** Writes to the app's Documents directory, which `xcrun devicectl device copy from` can read. */
@OptIn(ExperimentalForeignApi::class)
actual fun writeBenchmarkFile(fileName: String, contents: String): String {
  val documents = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
      .first() as String
  val target = "$documents/$fileName"
  val partial = "$target.partial"
  @Suppress("CAST_NEVER_SUCCEEDS")
  (contents as NSString).writeToFile(partial, atomically = true, encoding = NSUTF8StringEncoding, error = null)
  NSFileManager.defaultManager.removeItemAtPath(target, error = null)
  NSFileManager.defaultManager.moveItemAtPath(partial, toPath = target, error = null)
  return target
}
