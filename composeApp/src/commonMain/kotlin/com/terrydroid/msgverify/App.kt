package com.terrydroid.msgverify

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.terrydroid.msgverify.compose.MsgVerifyScaffold
import com.terrydroid.msgverify.demo.emaildetails.DemoEmailDetailsScreen
import com.terrydroid.msgverify.demo.emailoverview.DemoEmailOverview
import com.terrydroid.msgverify.demo.overview.DemoOverviewScreen
import com.terrydroid.msgverify.demo.smsdetails.DemoMessageDetailsScreen
import com.terrydroid.msgverify.demo.smsoverview.DemoMessagesScreen
import com.terrydroid.msgverify.demo.socialmedia.SocialMediaScreen
import com.terrydroid.msgverify.home.HomeScreen
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
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = Route.Home) {
                composable<Route.Home> {
                    val scrollBehavior =
                        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
                    MsgVerifyScaffold(
                        scrollBehavior = scrollBehavior,
                        title = "MsgVerify",
                        actions = {
                            TextButton(
                                onClick = { navController.navigate(Route.DemoOverview) },
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.secondary,
                                ),
                            ) {
                                Text(text = "Demos")
                            }
                        },
                        onClick = { navController.navigate(Route.DemoOverview) }
                    ) { innerPadding ->
                        HomeScreen(
                            paddingValues = innerPadding,
                            recievedText = recievedText,
                            onTextConsumed = onTextConsumed,
                        )
                    }
                }

                composable<Route.DemoOverview> {
                    val scrollBehavior =
                        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
                    MsgVerifyScaffold(
                        scrollBehavior = scrollBehavior,
                        title = "Demo Overview",
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        iconDescription = "Navigate Back",
                        onClick = { navController.navigateUp() }
                    ) {
                        DemoOverviewScreen(
                            onSmsDemoClicked = { navController.navigate(Route.DemoSmsOverview) },
                            onEmailDemoClicked = { navController.navigate(Route.DemoEmailOverview) },
                            onSocialMediaDemoClicked = { navController.navigate(Route.SocialMediaDemo) },
                        )
                    }
                }

                composable<Route.DemoSmsOverview> {
                    val scrollBehavior =
                        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
                    MsgVerifyScaffold(
                        scrollBehavior = scrollBehavior,
                        title = "Demo Messages",
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        iconDescription = "Navigate Back",
                        onClick = { navController.navigateUp() }
                    ) { innerPadding ->
                        DemoMessagesScreen(
                            paddingValues = innerPadding,
                            navigateToDetails = { id ->
                                navController.navigate(Route.DemoSmsDetails(id))
                            },
                        )
                    }
                }

                composable<Route.DemoSmsDetails> { backStackEntry ->
                    val route = backStackEntry.toRoute<Route.DemoSmsDetails>()
                    val scrollBehavior =
                        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
                    MsgVerifyScaffold(
                        scrollBehavior = scrollBehavior,
                        title = "Demo Message",
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        iconDescription = "Navigate Back",
                        onClick = { navController.navigateUp() }
                    ) { innerPadding ->
                        DemoMessageDetailsScreen(
                            paddingValues = innerPadding,
                            id = route.id,
                        )
                    }
                }

                composable<Route.DemoEmailOverview> {
                    val scrollBehavior =
                        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
                    MsgVerifyScaffold(
                        scrollBehavior = scrollBehavior,
                        title = "Email Client Demo",
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        iconDescription = "Navigate Back",
                        onClick = { navController.navigateUp() }
                    ) { innerPadding ->
                        DemoEmailOverview(
                            padding = innerPadding,
                            navigateToDetails = { id ->
                                navController.navigate(Route.DemoEmailDetails(id))
                            },
                        )
                    }
                }

                composable<Route.DemoEmailDetails> { backStackEntry ->
                    val route = backStackEntry.toRoute<Route.DemoEmailDetails>()
                    val scrollBehavior =
                        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
                    MsgVerifyScaffold(
                        scrollBehavior = scrollBehavior,
                        title = "Email",
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        iconDescription = "Navigate Back",
                        onClick = { navController.navigateUp() }
                    ) { innerPadding ->
                        DemoEmailDetailsScreen(
                            paddingValues = innerPadding,
                            id = route.id,
                        )
                    }
                }

                composable<Route.SocialMediaDemo> {
                    val scrollBehavior =
                        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
                    MsgVerifyScaffold(
                        scrollBehavior = scrollBehavior,
                        title = "Social Media Demo",
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        iconDescription = "Navigate Back",
                        onClick = { navController.navigateUp() }
                    ) { innerPadding ->
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
