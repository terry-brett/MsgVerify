package com.terrydroid.msgverify.benchmark

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import android.os.Debug
import android.os.PowerManager
import java.io.File

/** Set by MainActivity before a benchmark run is started. */
@SuppressLint("StaticFieldLeak")
lateinit var benchmarkAppContext: Context

actual fun benchmarkDeviceInfo(): DeviceInfo = DeviceInfo(
    platform = "android",
    osVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
    model = "${Build.MANUFACTURER} ${Build.MODEL}",
    isSimulator = Build.FINGERPRINT.contains("generic") || Build.HARDWARE.contains("ranchu") ||
        Build.HARDWARE.contains("goldfish"),
    isDebugBuild = (benchmarkAppContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0,
)

actual fun benchmarkMemorySnapshot(): MemorySnapshot {
  val info = Debug.MemoryInfo()
  Debug.getMemoryInfo(info)
  val runtime = Runtime.getRuntime()
  return MemorySnapshot(
      footprintBytes = info.totalPss.toLong() * 1024,
      residentBytes = readVmRssBytes(),
      managedHeapBytes = runtime.totalMemory() - runtime.freeMemory(),
      nativeHeapBytes = Debug.getNativeHeapAllocatedSize(),
  )
}

private fun readVmRssBytes(): Long? = runCatching {
  File("/proc/self/status").useLines { lines ->
    lines.firstOrNull { it.startsWith("VmRSS:") }
        ?.substringAfter(":")?.trim()?.substringBefore(" ")?.toLong()?.times(1024)
  }
}.getOrNull()

actual fun benchmarkThermalState(): String {
  val power = benchmarkAppContext.getSystemService(Context.POWER_SERVICE) as PowerManager
  return when (power.currentThermalStatus) {
    PowerManager.THERMAL_STATUS_NONE -> "none"
    PowerManager.THERMAL_STATUS_LIGHT -> "light"
    PowerManager.THERMAL_STATUS_MODERATE -> "moderate"
    PowerManager.THERMAL_STATUS_SEVERE -> "severe"
    PowerManager.THERMAL_STATUS_CRITICAL -> "critical"
    PowerManager.THERMAL_STATUS_EMERGENCY -> "emergency"
    PowerManager.THERMAL_STATUS_SHUTDOWN -> "shutdown"
    else -> "unknown"
  }
}

/** Writes to the app-specific external files directory so `adb pull` can retrieve the file. */
actual fun writeBenchmarkFile(fileName: String, contents: String): String {
  val dir = benchmarkAppContext.getExternalFilesDir(null) ?: benchmarkAppContext.filesDir
  val target = File(dir, fileName)
  val partial = File(dir, "$fileName.partial")
  partial.writeText(contents)
  partial.renameTo(target)
  return target.absolutePath
}
