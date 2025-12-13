package com.terrydroid.msgverify.overlay

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.core.content.ContextCompat

class CaptureImagePermissionActivity : Activity() {

    private val REQ_CAPTURE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mpm = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(mpm.createScreenCaptureIntent(), REQ_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQ_CAPTURE && resultCode == RESULT_OK && data != null) {
            val svc = Intent(this, OverlayService::class.java).apply {
                putExtra("resultCode", resultCode)
                putExtra("data", data) // Intent is Parcelable
                putExtra("action", "START_PROJECTION")
            }
            ContextCompat.startForegroundService(this, svc)
        }

        finish()
    }
}