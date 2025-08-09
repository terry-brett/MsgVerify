package com.terrydroid.msgverify.di

import com.terrydroid.msgverify.PlatformContext
import com.terrydroid.msgverify.data.MsgVerifyRepository
import org.koin.dsl.module

fun appModules(platformContext: PlatformContext) = module {
    single<MsgVerifyRepository> {
        MsgVerifyRepository(platformContext)
    }
}