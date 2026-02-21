package com.terrydroid.msgverify

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.terrydroid.msgverify.compose.MsgVerifyScaffold
import com.terrydroid.msgverify.demo.emailoverview.DemoEmailOverview
import com.terrydroid.msgverify.demo.overview.DemoOverviewScreen
import com.terrydroid.msgverify.demo.smsdetails.DemoMessageDetailsScreen
import com.terrydroid.msgverify.demo.smsoverview.DemoMessagesScreen
import com.terrydroid.msgverify.demo.socialmedia.SocialMediaScreen
import com.terrydroid.msgverify.home.HomeScreen
import com.terrydroid.msgverify.settings.SettingsScreen
import com.terrydroid.msgverify.theme.MsgVerifyTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun App(recievedText: String? = null, onTextConsumed: () -> Unit = {}) {
    AppScreen(recievedText, onTextConsumed)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppScreen(recievedText: String?, onTextConsumed: () -> Unit) {
    MsgVerifyTheme {
        Column(
            modifier = Modifier.background(color = MaterialTheme.colorScheme.background)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val scrollBehavior =
                TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

            // TODO: Improve navigation with decompose or something when it gets a bit more complex
            val route = rememberSaveable { mutableStateOf(Routes.Home) }
            val demoDetailsId = rememberSaveable { mutableStateOf(0) }

            when (route.value) {
                Routes.Settings -> {
                    MsgVerifyScaffold(
                        scrollBehavior = scrollBehavior,
                        title = "Settings",
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        iconDescription = "Navigate Back",
                        onClick = { route.value = Routes.Home }
                    ) { innerPadding ->
                        SettingsScreen(
                            paddingValues = innerPadding
                        )
                    }
                }

                Routes.Home -> {
                    MsgVerifyScaffold(
                        scrollBehavior = scrollBehavior,
                        title = "MsgVerify",
                        actions = {
                            TextButton(
                                onClick = { route.value = Routes.DemoOverview },
                                border =
                                    BorderStroke(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.secondary,
                                    ),
                            ) {
                                Text(text = "Demos")
                            }
                            IconButton(onClick = { route.value = Routes.Settings }) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Go to settings",
                                )
                            }
                        },
                        onClick = { route.value = Routes.Settings }
                    ) { innerPadding ->
                        HomeScreen(
                            paddingValues = innerPadding,
                            recievedText = recievedText,
                            onTextConsumed = onTextConsumed,
                        )
                    }
                }

                Routes.DemoSmsOverview -> {
                    MsgVerifyScaffold(
                        scrollBehavior = scrollBehavior,
                        title = "Demo Messages",
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        iconDescription = "Navigate Back",
                        onClick = { route.value = Routes.DemoOverview }
                    ) { innerPadding ->
                        DemoMessagesScreen(
                            paddingValues = innerPadding,
                            navigateToDetails = { id ->
                                demoDetailsId.value = id
                                route.value = Routes.DemoSmsDetails
                            },
                        )
                    }
                }

                Routes.DemoSmsDetails -> {
                    MsgVerifyScaffold(
                        scrollBehavior = scrollBehavior,
                        title = "Demo Message",
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        iconDescription = "Navigate Back",
                        onClick = { route.value = Routes.DemoSmsOverview }
                    ){ innerPadding ->
                        DemoMessageDetailsScreen(
                            paddingValues = innerPadding,
                            id = demoDetailsId.value
                        )
                    }
                }

                Routes.DemoOverview -> {
                    MsgVerifyScaffold(
                        scrollBehavior = scrollBehavior,
                        title = "Demo Overview",
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        iconDescription = "Navigate Back",
                        onClick = { route.value = Routes.Home }
                    ){
                        DemoOverviewScreen(
                            onSmsDemoClicked = { route.value = Routes.DemoSmsOverview },
                            onEmailDemoClicked = { route.value = Routes.DemoEmailOverview },
                            onSocialMediaDemoClicked = { route.value = Routes.SocialMediaDemo },
                        )
                    }
                }

                Routes.DemoEmailOverview -> {
                    MsgVerifyScaffold(
                        scrollBehavior = scrollBehavior,
                        title = "Email Client Demo",
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        iconDescription = "Navigate Back",
                        onClick = { route.value = Routes.DemoOverview }
                    ){ innerPadding ->
                        DemoEmailOverview(innerPadding)
                    }
                }

                Routes.SocialMediaDemo -> {
                    MsgVerifyScaffold(
                        scrollBehavior = scrollBehavior,
                        title = "Social Media Demo",
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        iconDescription =  "Navigate Back",
                        onClick = { route.value = Routes.DemoOverview }
                    ){ innerPadding ->
                        SocialMediaScreen(
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
    MsgVerifyTheme { AppScreen("", {}) }
}
