package com.terrydroid.msgverify.overlay

import android.app.ActivityManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowInsets
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Timer
import kotlin.concurrent.fixedRateTimer

class OverlayService: Service() {
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
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
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
    }

    private data class DialogTag(
        val visible: Boolean
    )
}