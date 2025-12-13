package com.terrydroid.msgverify.overlay

import android.app.Activity
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.WindowInsets
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Timer
import kotlin.concurrent.fixedRateTimer
import androidx.core.graphics.createBitmap

class OverlayService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private val uiScope = CoroutineScope(Job() + Dispatchers.Main)

    private val windowManager
        get() = getSystemService(WINDOW_SERVICE) as WindowManager

    private var dialog: ComposeView? = null
    private var timer: Timer? = null

    // Default layout parameters to be used when adding a view to the window.
    private val defaultLayoutParams = LayoutParams(
        LayoutParams.WRAP_CONTENT,
        LayoutParams.WRAP_CONTENT,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("deprecation")
            LayoutParams.TYPE_PHONE
        },
        LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.TOP or Gravity.START
    }

    private val viewModelStoreOwner = OverlayViewModelStoreOwner()
    private val lifecycleOwner = OverlayLifecycleOwner()

    override fun onCreate() {
        super.onCreate()
        listenAppState()
        addDialogToWindowIfNotAddedAlready()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.getStringExtra("action")) {
            "START_PROJECTION" -> {
                val resultCode = intent.getIntExtra("resultCode", Activity.RESULT_CANCELED)
                val data = intent.getParcelableExtra<Intent>("data")
                if (resultCode == Activity.RESULT_OK && data != null) {
                    startAsMediaProjectionForeground()
                    startProjection(resultCode, data)
                }

            }

            "TAKE_SCREENSHOT" -> {
                takeScreenshot()
            }
        }
        return START_STICKY
    }
    private val NOTIF_ID = 42
    private val CHANNEL_ID = "screen_capture"


    private fun startAsMediaProjectionForeground() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentTitle("Screen capture active")
            .setContentText("Capturing screen for text recognition")
            .setOngoing(true)
            .build()

        startForeground(
            NOTIF_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val mgr = getSystemService(NotificationManager::class.java)
            val ch = NotificationChannel(
                CHANNEL_ID,
                "Screen capture",
                NotificationManager.IMPORTANCE_LOW
            )
            mgr.createNotificationChannel(ch)
        }
    }


    @Suppress("LongMethod")
    private fun addOverlay() {
        dialog = ComposeView(baseContext).apply {
            setContent {
                OverlayScreen(
                    onCancel = { /* No-op */ },
                    onDragStarted = {
                        // When the drag started, we need to have the full screen so that
                        // we can calculate the coordinates of the dragged item correctly.
                        windowManager.updateViewLayout(
                            this,
                            defaultLayoutParams.also {
                                it.width = LayoutParams.MATCH_PARENT
                                it.height = LayoutParams.MATCH_PARENT
                            }
                        )

                        val offset = IntArray(2)
                        getLocationOnScreen(offset)

                        Offset(
                            x = offset[0].toFloat(),
                            y = offset[1].toFloat(),
                        ) to getStatusBarHeight(windowManager.currentWindowMetrics.windowInsets)
                    },
                    takeScreenShot = {
                        Log.d("CAPTURE","Capturing")
                        takeScreenshot()
                    },
                    onDragEnd = { offset, shouldRemoveView ->
                        if (shouldRemoveView) {
                            // Stopping service will remove the dialog as well
                            stopSelf()
                        } else {
                            // If the user has dragged to the area which is outside of the
                            // remove area then we need to update the size of this view to
                            // wrap content again so that we don't cover the whole screen and
                            // block the user interaction with the underlying content.
                            windowManager.updateViewLayout(
                                this,
                                defaultLayoutParams.also {
                                    it.width = LayoutParams.WRAP_CONTENT
                                    it.height = LayoutParams.WRAP_CONTENT
                                    it.x = offset.x.toInt()
                                    it.y = offset.y.toInt()
                                }
                            )
                        }
                    }
                )
            }
        }

        dialog?.setViewTreeLifecycleOwner(lifecycleOwner)
        dialog?.setViewTreeSavedStateRegistryOwner(lifecycleOwner)
        dialog?.setViewTreeViewModelStoreOwner(viewModelStoreOwner)

        // Trigger lifecycle events to make compose work
        if (!lifecycleOwner.savedStateRegistry.isRestored) {
            lifecycleOwner.performRestore(null)
        }
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)

        windowManager.addView(
            dialog,
            defaultLayoutParams,
        )
    }

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null

    fun startProjection(resultCode: Int, data: Intent) {
        val mpm = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = mpm.getMediaProjection(resultCode, data)
    }

    private fun takeScreenshot() {
        val projection = mediaProjection ?: return

        val metrics = resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val density = metrics.densityDpi

        // Create (or recreate) ImageReader
        imageReader?.close()
        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)

        // Create (or recreate) VirtualDisplay
        virtualDisplay?.release()
        virtualDisplay = projection.createVirtualDisplay(
            "screen-capture",
            width, height, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader!!.surface,
            null,
            null
        )

        // Wait briefly for a frame, then acquire it.
        // In practice you may need to try a couple times.
        val handlerThread = HandlerThread("ImageReaderThread").apply { start() }
        val handler = Handler(handlerThread.looper)

        handler.post {
            var image: Image? = null
            try {
                // Try a few times to get a non-null frame
                repeat(10) {
                    image = imageReader?.acquireLatestImage()
                    if (image != null) return@repeat
                    Thread.sleep(50)
                }

                val img = image ?: return@post
                val bitmap = imageToBitmap(img)

                // IMPORTANT: close the image ASAP
                img.close()

                // Optional: release display if you only need one frame
                virtualDisplay?.release()
                virtualDisplay = null
                imageReader?.close()
                imageReader = null

                // OCR
                ocr(
                    bitmap,
                    onResult = { text ->
                        // Use your overlay/UI update here
                        Log.d("OCR", text)
                    },
                    onError = { e ->
                        Log.e("OCR", "Failed", e)
                    }
                )
            } catch (e: Exception) {
                Log.e("CAPTURE", "takeScreenshot error", e)
                image?.close()
            } finally {
                handlerThread.quitSafely()
            }
        }
    }

    private fun imageToBitmap(image: Image): Bitmap {
        val width = image.width
        val height = image.height
        val plane = image.planes[0]
        val buffer = plane.buffer

        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride
        val rowPadding = rowStride - pixelStride * width

        // Create a bitmap accounting for row padding
        val bitmap = createBitmap(width + rowPadding / pixelStride, height)
        bitmap.copyPixelsFromBuffer(buffer)

        // Crop to the actual content size
        return Bitmap.createBitmap(bitmap, 0, 0, width, height)
    }

    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun ocr(bitmap: Bitmap, onResult: (String) -> Unit, onError: (Exception) -> Unit) {
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image)
            .addOnSuccessListener { result -> onResult(result.text) } // <- your string
            .addOnFailureListener(onError)
    }

    private fun getStatusBarHeight(windowInsets: WindowInsets): Int {
        return windowInsets.getInsets(WindowInsets.Type.systemBars()).top
    }

    /**
     * Since we do not want to touch any activity or application class for getting
     * lifecycle callbacks to cancel or update the visibility of the view in this
     * service, we listen the app state (if its in foreground or not).
     */
    private fun listenAppState() {
        timer = fixedRateTimer(
            period = 500L,
            action = {
                uiScope.launch {
                    //if (isAppInForeground()) {
                    addDialogToWindowIfNotAddedAlready()
                    //} else {
                    //    lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
//
                    //    removeDialogFromWindowIfVisible()
                    //}
                }
            }
        )
    }

    /**
     * Checks the dialog view tag for the visibility.
     */
    private fun isDialogVisible(): Boolean {
        val visible = (dialog?.tag as? DialogTag)?.visible

        return visible == true
    }

    private fun setDialogVisibilityTag(visible: Boolean) {
        dialog?.tag = DialogTag(visible)
    }

    /**
     * Checks if the apps is in foreground from the current process.
     */
    private fun isAppInForeground(): Boolean {
        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val runningAppProcesses = activityManager.runningAppProcesses

        if (runningAppProcesses != null) {
            for (processInfo in runningAppProcesses) {
                // Find the app from the process with its package name
                if (processInfo.processName == packageName) {
                    return processInfo.importance == ActivityManager
                        .RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                }
            }
        }

        // If the app is not in the current process then its not in foreground
        return false
    }

    private fun removeViewAndCancelTimer() {
        removeDialogFromWindowIfVisible()
        timer?.cancel()
    }

    /**
     * Use this function to add the dialog to a window. It handles the visibility tag accordingly.
     */
    private fun addDialogToWindowIfNotAddedAlready() {
        if (!isDialogVisible()) {
            addOverlay()
            setDialogVisibilityTag(true)
        }
    }

    /**
     * Use this function to remove the dialog to a window.
     * It handles the visibility tag accordingly.
     */
    private fun removeDialogFromWindowIfVisible() {
        if (isDialogVisible()) {
            setDialogVisibilityTag(false)
            windowManager.removeViewImmediate(dialog)
        }
    }

    override fun onDestroy() {
        removeViewAndCancelTimer()
        super.onDestroy()
        imageReader?.close()
        virtualDisplay?.release()
        mediaProjection?.stop()
    }

    private data class DialogTag(
        val visible: Boolean
    )
}