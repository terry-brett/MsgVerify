package com.terrydroid.msgverify.di

import com.terrydroid.msgverify.data.MsgVerifyRepository
import org.koin.dsl.module

val appModules = module {
    single<MsgVerifyRepository> {
        MsgVerifyRepository()
    }
}