package com.terrydroid.msgverify.di

import com.terrydroid.msgverify.PlatformContext
import com.terrydroid.msgverify.data.MsgVerifyRepository
import com.terrydroid.msgverify.home.HomeViewModel
import com.terrydroid.msgverify.settings.SettingsViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module


fun initKoin(contextProvider: PlatformContext, appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        modules(commonModule(contextProvider))
        appDeclaration()
    }

// called by iOS client
fun initKoin(contextProvider: PlatformContext) = initKoin(contextProvider) {
}

fun commonModule(contextProvider: PlatformContext) = module {
    single { contextProvider }
    singleOf(::MsgVerifyRepository)
    viewModel { HomeViewModel(msgVerifyRepository = get()) }
    viewModel { SettingsViewModel(msgVerifyRepository = get()) }
}
