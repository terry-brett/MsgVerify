package com.terrydroid.msgverify

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import com.terrydroid.msgverify.overlay.OverlayService

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            App(onOverlayEnable = { checked ->
                if (checked) askDrawOverlayPermissionIfNotGiven(
                    context
                )

                if (Settings.canDrawOverlays(context)) {
                    val serviceIntent = Intent(context, OverlayService::class.java)

                    if (checked) {
                        context.startService(serviceIntent)
                    } else context.stopService(serviceIntent)
                }
            })
        }
    }
}
private fun askDrawOverlayPermissionIfNotGiven(context: Context) {
    val canDrawOverlays = Settings.canDrawOverlays(context)

    if (!canDrawOverlays) {
        val permissionIntent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            "package:${context.packageName}".toUri()
        )
        context.startActivity(permissionIntent)
    }
}
@Preview
@Composable
fun AppAndroidPreview() {
    App(onOverlayEnable = {})
}