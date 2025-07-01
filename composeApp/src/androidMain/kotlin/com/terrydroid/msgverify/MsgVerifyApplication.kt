package com.terrydroid.msgverify

import android.app.Application
import com.terrydroid.msgverify.di.appModules
import com.terrydroid.msgverify.di.initKoin
import org.koin.android.ext.koin.androidContext

class MsgVerifyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@MsgVerifyApplication)

            modules(appModules)
        }
    }
}
