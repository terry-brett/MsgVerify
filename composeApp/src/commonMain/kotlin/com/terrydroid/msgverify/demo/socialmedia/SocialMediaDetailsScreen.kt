package com.terrydroid.msgverify.demo.socialmedia

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.VideoCall
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.terrydroid.msgverify.demo.emaildetails.VerificationCard
import com.terrydroid.msgverify.demo.socialmedia.model.SocialMediaUiState
import org.koin.compose.viewmodel.koinViewModel
import io.github.feliperce.avatarkt.Avatar
import io.github.feliperce.avatarkt.AvatarVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SocialMediaDetailsScreen(
    id: Int,
    navigateBack: () -> Unit,
    viewModel: SocialMediaViewModel = koinViewModel(),
) {
    val state = viewModel.uiState
        .collectAsStateWithLifecycle()
        .value

    val message = when (state) {
        SocialMediaUiState.Loading -> null

        is SocialMediaUiState.Success -> {
            state.message.firstOrNull { it.id == id }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),

        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = navigateBack,
                    ) {
                        Icon(
                            imageVector =
                                Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {},
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.VideoCall,
                            contentDescription = "Video call",
                        )
                    }

                    IconButton(
                        onClick = {},
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Call,
                            contentDescription = "Call",
                        )
                    }

                    IconButton(
                        onClick = {},
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = "More options",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor =
                        MaterialTheme.colorScheme.background,
                ),
            )
        },

        bottomBar = {
            if (message != null) {
                MessageComposer()
            }
        },
    ) { innerPadding ->

        if (message == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text("Loading...")
            }
        } else {
            Conversation(
                message = message,
                contentPadding = innerPadding,
            )
        }
    }
}

@Composable
private fun Conversation(
    message: Message,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.background,
            ),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ConversationHeader(
                name = message.title,
            )
        }

        item {
            Spacer(
                modifier = Modifier.size(20.dp),
            )
        }

        item {
            IncomingBubble(
                text = message.message,
            )
        }

        if (message.trafficLight != null) {
            item {
                VerificationCard(
                    trafficLight = message.trafficLight,
                    reasons = message.reasons,
                )
            }
        }
    }
}

@Composable
private fun MessageComposer() {
    var text by rememberSaveable {
        mutableStateOf("")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.background,
            )
            .navigationBarsPadding()
            .padding(
                horizontal = 12.dp,
                vertical = 8.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = {
                text = it
            },
            placeholder = {
                Text("Message")
            },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.weight(1f),
        )

        Spacer(
            modifier = Modifier.width(8.dp),
        )

        IconButton(
            onClick = {
                // Demo only
            },
        ) {
            Text(
                text = "➤",
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}
@Composable
private fun ConversationHeader(
    name: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Avatar(
            name = name,
            variant = AvatarVariant.BEAM,
            size = 52.dp,
            shape = CircleShape,
        )

        Spacer(
            modifier = Modifier.size(8.dp),
        )

        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        Text(
            text = "Active now",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
@Composable
private fun IncomingBubble(
    text: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(0.75f),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Bottom,
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 4.dp,
                        topEnd = 20.dp,
                        bottomStart = 20.dp,
                        bottomEnd = 20.dp,
                    )
                )
                .background(
                    MaterialTheme.colorScheme.primary
                )
                .padding(
                    horizontal = 16.dp,
                    vertical = 11.dp,
                ),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}