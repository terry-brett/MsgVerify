package com.terrydroid.msgverify

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.terrydroid.msgverify.di.commonModule
import com.terrydroid.msgverify.home.HomeScreen
import com.terrydroid.msgverify.settings.SettingsScreen
import com.terrydroid.msgverify.theme.MsgVerifyTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication

@Composable
fun App() {
    KoinApplication(application = {
        modules(commonModule())
    }) {
        AppScreen()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppScreen() {
    MsgVerifyTheme {
        Column(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.background)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val scrollBehavior =
                TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

            //TODO: Improve navigation with decompose or something when it gets a bit more complex
            val route = rememberSaveable { mutableStateOf(Routes.Home) }
            when (route.value) {
                Routes.Settings -> {
                    Scaffold(
                        topBar = {
                            CenterAlignedTopAppBar(
                                modifier = Modifier.nestedScroll(
                                    scrollBehavior.nestedScrollConnection
                                ),
                                title = {
                                    Text(
                                        text = "Settings",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.onBackground,
                                    )
                                },
                                scrollBehavior = scrollBehavior,
                                navigationIcon = {
                                    IconButton(
                                        onClick = {
                                            route.value = Routes.Home
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Navigate Back"
                                        )
                                    }
                                }
                            )
                        },
                    ) { innerPadding ->
                        SettingsScreen(
                            paddingValues = innerPadding
                        )
                    }
                }

                Routes.Home -> {
                    Scaffold(
                        contentWindowInsets = WindowInsets(0.dp),
                        topBar = {
                            CenterAlignedTopAppBar(
                                modifier = Modifier.nestedScroll(
                                    scrollBehavior.nestedScrollConnection
                                ),
                                title = {
                                    Text(
                                        text = "MsgVerify",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.onBackground,
                                    )
                                },
                                scrollBehavior = scrollBehavior,
                                actions = {
                                    IconButton(
                                        onClick = {
                                            route.value = Routes.Settings
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = "Go to settings"
                                        )
                                    }
                                }
                            )
                        }
                    ) { innerPadding ->
                        HomeScreen(
                            paddingValues = innerPadding,
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun AppPreview() {
    MsgVerifyTheme {
        AppScreen()
    }
}
