package com.terrydroid.msgverify

import android.app.Application
import android.content.Context
import com.terrydroid.msgverify.di.appModules
import com.terrydroid.msgverify.di.initKoin
import org.koin.android.ext.koin.androidContext

class MsgVerifyApplication : Application() {

    companion object {
        lateinit var appContext: Context
    }

    override fun onCreate() {
        super.onCreate()

        appContext = applicationContext

        initKoin {
            androidContext(this@MsgVerifyApplication)

            modules(appModules)
        }
        AppContext.apply { set(applicationContext) }
    }
}
