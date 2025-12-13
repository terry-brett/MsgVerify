package com.terrydroid.msgverify.di

import com.terrydroid.msgverify.PlatformContext
import com.terrydroid.msgverify.data.MsgVerifyRepository
import com.terrydroid.msgverify.demo.DemoMessageOverviewViewModel
import com.terrydroid.msgverify.home.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module


fun initKoin(
    platformContext: PlatformContext,
    appDeclaration: KoinAppDeclaration = {}
) = startKoin {
        modules(commonModule(platformContext))
        appDeclaration()
    }

// called by iOS client
fun initKoin(platformContext: PlatformContext) = initKoin(platformContext) {}

fun commonModule(platformContext: PlatformContext) = module {
    single { platformContext }
    singleOf(::MsgVerifyRepository)
    viewModel { HomeViewModel(msgVerifyRepository = get()) }
    viewModel { DemoMessageOverviewViewModel(msgVerifyRepository = get(), dispatcher = Dispatchers.IO) }
}
