package com.terrydroid.msgverify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import io.ktor.http.Url
import org.contextguard.UrlVerifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val urlVerifier = UrlVerifier(
                context = applicationContext,
                url = "https://www.dnb.no"
                    // "https://www.google.com"
                    // "https://l.wl.co/l?u=https://cpcalendars.64-226-105-5.cprapid.com/short/?Verification=a97trrybrett@yahoo.co.uk"
            )
            LaunchedEffect("init") {
                urlVerifier.makePrediction()
            }

            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}