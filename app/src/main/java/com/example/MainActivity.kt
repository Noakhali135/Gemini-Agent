package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.database.AppDatabase
import com.example.data.database.ChatConversation
import com.example.data.database.ChatMessage
import com.example.data.repository.ChatRepository
import com.example.ui.chat.ChatViewModel
import com.example.ui.chat.ChatViewModelFactory
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = AppDatabase.getDatabase(this)
        val repository = ChatRepository(database.chatDao())
        
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: ChatViewModel = viewModel(
                    factory = ChatViewModelFactory(application, repository)
                )
                ChatScreen(viewModel = viewModel)
            }
        }
    }
}

// Predefined catalog of famous model selections
data class ModelPreset(
    val id: String,
    val name: String,
    val description: String,
    val supportsThinking: Boolean
)

val PRESET_MODELS = listOf(
    ModelPreset(
        id = "gemini-2.0-flash-thinking-exp-01-21",
        name = "Gemini 2.0 Flash Thinking",
        description = "Streams comprehensive, granular thinking before spitting answers.",
        supportsThinking = true
    ),
    ModelPreset(
        id = "gemini-2.5-flash",
        name = "Gemini 2.5 Flash",
        description = "Lightweight, super-fast model optimal for basic day-to-day work.",
        supportsThinking = false
    ),
    ModelPreset(
        id = "gemini-2.5-pro",
        name = "Gemini 2.5 Pro",
        description = "Ultra intelligence for mathematics, deep research, & complex coding.",
        supportsThinking = true
    ),
    ModelPreset(
        id = "gemini-3.5-flash",
        name = "Gemini 3.5 Flash",
        description = "Default standard for simple summaries or general Q&A.",
        supportsThinking = false
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Chat streams & preferences states
    val apiKey by viewModel.apiKey.collectAsState()
    val modelId by viewModel.modelId.collectAsState()
    val customModelId by viewModel.customModelId.collectAsState()
    val isCustomModelSelected by viewModel.isCustomModelSelected.collectAsState()
    val temperature by viewModel.temperature.collectAsState()
    val thinkingMode by viewModel.thinkingMode.collectAsState()
    val thinkingBudgetLevel by viewModel.thinkingBudgetLevel.collectAsState()

    val conversations by viewModel.conversations.collectAsState()
    val currentConvId by viewModel.currentConversationId.collectAsState()
    val messages by viewModel.currentMessages.collectAsState()

    val isStreaming by viewModel.isStreaming.collectAsState()
    val streamingThought by viewModel.currentStreamingThought.collectAsState()
    val streamingAnswer by viewModel.currentStreamingAnswer.collectAsState()

    // Navigation and Sheets triggers
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showFormattingGuide by remember { mutableStateOf(false) }

    // Dynamic label for current active model ID
    val activeModelLabel = if (isCustomModelSelected) {
        if (customModelId.isNotEmpty()) customModelId else "Custom Model"
    } else {
        PRESET_MODELS.find { it.id == modelId }?.name ?: modelId
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Surface(
                modifier = Modifier
                    .width(320.dp)
                    .fillMaxHeight(),
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header with visual icon
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "Gemini Brain AI",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Gemini Streamer",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Deep Thinking Console",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Start New Chat primary action
                    Button(
                        onClick = {
                            viewModel.startNewConversation()
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("new_chat_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Icon")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("New Conversation", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Conversations",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                    )

                    Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

                    // Scrolling Conversations history List
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (conversations.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No history yet",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        } else {
                            items(conversations) { conv ->
                                val isSelected = conv.id == currentConvId
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            viewModel.selectConversation(conv.id)
                                            coroutineScope.launch { drawerState.close() }
                                        },
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "Chat",
                                            modifier = Modifier.size(18.dp),
                                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = conv.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = conv.modelId,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 9.sp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.deleteConversation(conv.id) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

                    // Drawer footer with Settings reveal button
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                showSettingsDialog = true
                                coroutineScope.launch { drawerState.close() }
                            },
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings Icon",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "API Settings",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Configure Key & Model ID",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Arrow right",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = if (messages.isEmpty() && !isStreaming) "Gemini Client" else (conversations.find { it.id == currentConvId }?.title ?: "Chatting"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = activeModelLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu Icon")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showFormattingGuide = true }) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = "Formatting help icon")
                        }
                        IconButton(onClick = { showSettingsDialog = true }) {
                            Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings icon")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // If API Key is missing: display high-contrast guided setup immediately
                if (apiKey.trim().isEmpty()) {
                    MissingApiKeyWizard(
                        context = context,
                        onKeyEntered = { enteredKey ->
                            viewModel.updateApiKey(enteredKey)
                        },
                        onOpenSettings = {
                            showSettingsDialog = true
                        }
                    )
                } else {
                     val listState = rememberLazyListState()

                    // Pinned-to-bottom state governs automatic stream scrolls
                    var userPinnedToBottom by remember { mutableStateOf(true) }
                    var isProgrammaticScroll by remember { mutableStateOf(false) }

                    // Pure state-driven list item count to avoid layout phase lag
                    val totalItemsCount by remember {
                        derivedStateOf {
                            messages.size + if (isStreaming && (streamingThought.isNotEmpty() || streamingAnswer.isNotEmpty())) 1 else 0
                        }
                    }

                    // Track whether the list is scrolled near bottom using exact layoutInfo info
                    val isNearBottom by remember {
                        derivedStateOf {
                            val visibleItems = listState.layoutInfo.visibleItemsInfo
                            if (visibleItems.isEmpty()) {
                                true
                            } else {
                                val lastVisibleItem = visibleItems.last()
                                lastVisibleItem.index >= totalItemsCount - 2
                            }
                        }
                    }

                    // Sync pinning state with user manual scrolling
                    LaunchedEffect(isNearBottom) {
                        if (isNearBottom) {
                            userPinnedToBottom = true
                        } else {
                            if (listState.isScrollInProgress && !isProgrammaticScroll) {
                                userPinnedToBottom = false
                            }
                        }
                    }

                    // Scroll to bottom when new messages are added
                    LaunchedEffect(messages.size) {
                        if (messages.isNotEmpty()) {
                            delay(50L) // Wait a brief tick for compose tree update
                            val lastIndex = maxOf(0, totalItemsCount - 1)
                            userPinnedToBottom = true
                            isProgrammaticScroll = true
                            try {
                                listState.animateScrollToItem(lastIndex)
                            } finally {
                                isProgrammaticScroll = false
                            }
                        }
                    }

                    // Scroll down stably without interruption during active streaming content chunks
                    LaunchedEffect(isStreaming) {
                        if (isStreaming) {
                            snapshotFlow { streamingThought.length + streamingAnswer.length }
                                .collect {
                                    if (userPinnedToBottom) {
                                        val lastIndex = maxOf(0, totalItemsCount - 1)
                                        if (lastIndex >= 0) {
                                            isProgrammaticScroll = true
                                            try {
                                                listState.scrollToItem(lastIndex)
                                            } finally {
                                                isProgrammaticScroll = false
                                            }
                                        }
                                    }
                                }
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        var activeTab by remember { mutableStateOf(0) } // 0 = Chat, 1 = Workspace

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .imePadding()
                        ) {
                            // High-fidelity tab picker row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Chat Console Tab
                                Surface(
                                    onClick = { activeTab = 0 },
                                    modifier = Modifier.weight(1f),
                                    color = if (activeTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                                    contentColor = if (activeTab == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    shape = RoundedCornerShape(12.dp),
                                    border = if (activeTab == 0) null else BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = Icons.Default.Psychology, contentDescription = "Active chat thread panel", modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Chat Console", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Workspace Tab
                                Surface(
                                    onClick = { activeTab = 1 },
                                    modifier = Modifier.weight(1f),
                                    color = if (activeTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                                    contentColor = if (activeTab == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    shape = RoundedCornerShape(12.dp),
                                    border = if (activeTab == 1) null else BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Workspace tree listing view", modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Workspace", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            if (activeTab == 1) {
                                WorkspaceView(viewModel = viewModel, modifier = Modifier.weight(1f))
                            } else {
                                if (messages.isEmpty() && !isStreaming) {
                                    // Empty chat timeline: display suggestions
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                    ) {
                                        EmptyTimelineSplash(
                                            activeModel = activeModelLabel,
                                            onSuggestionSelected = { suggestion ->
                                                viewModel.sendMessage(suggestion)
                                            }
                                        )
                                    }
                                } else {
                                    // Scrolling messages container
                                    LazyColumn(
                                        state = listState,
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth(),
                                        contentPadding = PaddingValues(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        items(messages, key = { it.id }) { message ->
                                            ChatMessageBubble(
                                                message = message,
                                                clipboardManager = clipboardManager,
                                                context = context
                                            )
                                        }

                                        // Streaming response indicator bubble
                                        if (isStreaming && (streamingThought.isNotEmpty() || streamingAnswer.isNotEmpty())) {
                                            item {
                                                StreamingMsgBubble(
                                                    thoughtText = streamingThought,
                                                    answerText = streamingAnswer,
                                                    modelLabel = activeModelLabel
                                                )
                                            }
                                        }
                                    }
                                }

                                // Interactive typing bar (Only shown on Chat Console tab!)
                                TypingBar(
                                    isStreaming = isStreaming,
                                    onSendMessage = { prompt ->
                                        viewModel.sendMessage(prompt)
                                    }
                                )
                            }
                        }

                        // Floating scroll-to-bottom arrow button
                        val showScrollToBottomButton by remember {
                            derivedStateOf {
                                activeTab == 0 && !isNearBottom
                            }
                        }

                        if (showScrollToBottomButton) {
                            Card(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 20.dp, bottom = 84.dp)
                                    .size(42.dp)
                                    .clickable {
                                        val totalCount = listState.layoutInfo.totalItemsCount
                                        val lastIndex = maxOf(0, totalCount - 1)
                                        userPinnedToBottom = true
                                        isProgrammaticScroll = true
                                        coroutineScope.launch {
                                            try {
                                                if (totalCount > 0) {
                                                    listState.animateScrollToItem(lastIndex)
                                                }
                                            } finally {
                                                isProgrammaticScroll = false
                                            }
                                        }
                                    },
                                shape = CircleShape,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = "Scroll to bottom",
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal API Config Panel Settings Dialog
    if (showSettingsDialog) {
        SettingsDialog(
            apiKey = apiKey,
            modelId = modelId,
            customModelId = customModelId,
            isCustomModelSelected = isCustomModelSelected,
            temperature = temperature,
            thinkingMode = thinkingMode,
            thinkingBudgetLevel = thinkingBudgetLevel,
            onClose = { showSettingsDialog = false },
            onSave = { updatedKey, updatedModel, updatedCustom, useCustom, updatedTemp, updatedThinkingMode, updatedThinkingBudgetLevel ->
                viewModel.updateApiKey(updatedKey)
                viewModel.updateModelId(updatedModel)
                viewModel.updateCustomModelId(updatedCustom)
                viewModel.setCustomModelSelected(useCustom)
                viewModel.updateTemperature(updatedTemp)
                viewModel.updateThinkingMode(updatedThinkingMode)
                viewModel.updateThinkingBudgetLevel(updatedThinkingBudgetLevel)
                showSettingsDialog = false
            },
            context = context
        )
    }

    if (showFormattingGuide) {
        FormattingGuideDialog(onClose = { showFormattingGuide = false })
    }
}

@Composable
fun MissingApiKeyWizard(
    context: Context,
    onKeyEntered: (String) -> Unit,
    onOpenSettings: () -> Unit
) {
    var inputKey by remember { mutableStateOf("") }
    var isVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // High-contrast, clean illustration banner representing AI awaiting connection
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Key,
                contentDescription = "API Key lock icon",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Gemini API Key Required",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Provide a Gemini API Key to stream reasoning, deep thinking, and answers directly from Google models.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Direct shortcut link banner for quick key creation
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                    val intent =
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/"))
                    context.startActivity(intent)
                },
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Get Key Info",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Don't have a Gemini key?",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Tap here to obtain one free from Google AI Studio",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Navigate link",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = inputKey,
            onValueChange = { inputKey = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("api_key_input"),
            label = { Text("Enter Gemini API Key") },
            placeholder = { Text("AIzaSy...") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isVisible = !isVisible }) {
                    Icon(
                        imageVector = if (isVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Visibility Toggle"
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                if (inputKey.trim().isNotEmpty()) {
                    onKeyEntered(inputKey)
                }
            })
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text("Advanced Configuration")
            }

            Button(
                onClick = {
                    if (inputKey.trim().isNotEmpty()) {
                        onKeyEntered(inputKey)
                    }
                },
                enabled = inputKey.trim().isNotEmpty(),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Get Started", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EmptyTimelineSplash(
    activeModel: String,
    onSuggestionSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Aesthetic brain outline canvas represent artificial intelligence connection
        Canvas(modifier = Modifier.size(120.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val radius = size.minDimension / 3f

            // Drawing central glowing orb lines
            drawCircle(
                color = Color(0xFF6750A4).copy(alpha = 0.1f),
                radius = radius * 1.5f,
                center = Offset(cx, cy)
            )
            drawCircle(
                color = Color(0xFF6750A4).copy(alpha = 0.2f),
                radius = radius,
                center = Offset(cx, cy)
            )

            // Dynamic intelligence connection node circles
            val nodes = listOf(
                Offset(cx - radius * 0.4f, cy - radius * 0.4f),
                Offset(cx + radius * 0.5f, cy - radius * 0.2f),
                Offset(cx - radius * 0.5f, cy + radius * 0.3f),
                Offset(cx + radius * 0.3f, cy + radius * 0.4f),
                Offset(cx, cy - radius * 0.7f)
            )

            nodes.forEach { node ->
                drawCircle(color = Color(0xFF6750A4), radius = 6.dp.toPx(), center = node)
                nodes.forEach { other ->
                    if (node != other) {
                        drawLine(
                            color = Color(0xFF6750A4).copy(alpha = 0.4f),
                            start = node,
                            end = other,
                            strokeWidth = 1.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Conversation Active",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Using Model: $activeModel",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Ask any intricate, deep reasoning questions. The streaming output will separate thoughts and actual results accurately.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Try these thinking challenges:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Onboarding suggestions
        val suggestions = listOf(
            "Explain string theory mathematically in 3 short points.",
            "Write a complex riddle about shadows and write its deep rationale.",
            "If a train leaves NYC at 60mph and Boston at 80mph, when do they meet?"
        )

        suggestions.forEach { suggest ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onSuggestionSelected(suggest) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "Think Prompt",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = suggest,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Choose suggest",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

fun formatThinkingDuration(seconds: Long): String {
    if (seconds <= 0) return "1s"
    val m = seconds / 60
    val s = seconds % 60
    return when {
        m > 0 && s > 0 -> "${m}m ${s}s"
        m > 0 -> "${m}m"
        else -> "${seconds}s"
    }
}

@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager,
    context: Context
) {
    if (message.role == "system") {
        val cleanText = message.text.replace("[System Action Result] ", "").trim()
        val lines = cleanText.lines()
        val isTasksRun = cleanText.contains("Executed workspace tasks")

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1E1E) // Dark developer workspace/terminal aesthetic
                ),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFF333333))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Title Bar / Log Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "Intelligence logs",
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SYSTEM LOG",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFFB0BEC5)
                                )
                            )
                        }
                        Text(
                            text = "SUCCESS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF00E676),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = Color(0xFF37474F), thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (!isTasksRun) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Active task successful",
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = cleanText,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFFECEFF1)
                                )
                            )
                        }
                    } else {
                        Text(
                            text = "Executed workspace changes:",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFCFD8DC)
                            ),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        lines.drop(1).forEach { line ->
                            val trimmedLine = line.trim().removePrefix("•").trim()
                            if (trimmedLine.isNotEmpty()) {
                                val parts = trimmedLine.split(":", limit = 2)
                                if (parts.size == 2) {
                                    val action = parts[0].trim()
                                    val filepath = parts[1].trim()

                                    val (actionColor, actionIcon) = when {
                                        action.contains("Created") -> Pair(Color(0xFF69F0AE), Icons.Default.Check)
                                        action.contains("Modified") -> Pair(Color(0xFFFFD740), Icons.Default.Settings)
                                        action.contains("Deleted") -> Pair(Color(0xFFFF5252), Icons.Default.Delete)
                                        else -> Pair(Color(0xFFB0BEC5), Icons.Default.Info)
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = actionIcon,
                                            contentDescription = null,
                                            tint = actionColor,
                                            modifier = Modifier
                                                .padding(top = 2.dp)
                                                .size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Row {
                                                Text(
                                                    text = "$action: ",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontFamily = FontFamily.Monospace,
                                                        fontWeight = FontWeight.Bold,
                                                        color = actionColor
                                                    )
                                                )
                                                Text(
                                                    text = filepath,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontFamily = FontFamily.Monospace,
                                                        color = Color(0xFFECEFF1),
                                                        lineHeight = 16.sp
                                                    )
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Text(
                                        text = line,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            color = Color(0xFFCFD8DC)
                                        ),
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        return
    }

    val isUser = message.role == "user"
    var expandedThought by remember { mutableStateOf(false) }
    var showRawText by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "AI icon",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(top = 4.dp, end = 8.dp)
                    .size(24.dp)
            )
        }

        Column(
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            // Label metadata for bubbles
            Text(
                text = if (isUser) "You" else "Gemini Model Thread",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )

            Surface(
                color = when {
                    isUser -> MaterialTheme.colorScheme.primary
                    message.isError -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.surfaceContainerHigh
                },
                contentColor = when {
                    isUser -> MaterialTheme.colorScheme.onPrimary
                    message.isError -> MaterialTheme.colorScheme.onErrorContainer
                    else -> MaterialTheme.colorScheme.onSurface
                },
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                border = if (message.isError) BorderStroke(1.dp, MaterialTheme.colorScheme.error) else null
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    // Display internal thinking reasoning blocks if saved
                    if (!message.thought.isNullOrEmpty()) {
                        val rotationState by animateFloatAsState(
                            targetValue = if (expandedThought) 180f else 0f,
                            label = "chevron_rotation"
                        )

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLowest,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expandedThought = !expandedThought },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Psychology,
                                            contentDescription = "Thoughts",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        val durationSuffix = message.thinkingDuration?.let { " (${formatThinkingDuration(it)})" } ?: ""
                                        Text(
                                            text = "Reasoned Thoughts$durationSuffix",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ExpandMore,
                                        contentDescription = "Expand thinking icon",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .rotate(rotationState)
                                    )
                                }

                                AnimatedVisibility(
                                    visible = expandedThought,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically()
                                ) {
                                    Column(modifier = Modifier.padding(top = 8.dp)) {
                                        Divider(
                                            color = MaterialTheme.colorScheme.outlineVariant,
                                            thickness = 0.5.dp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = message.thought,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontFamily = FontFamily.Monospace,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Main response textual content (using our custom markdown elements)
                    if (isUser) {
                        Text(text = message.text, style = MaterialTheme.typography.bodyMedium)
                    } else {
                        if (message.isError) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Error",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = message.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        } else {
                            if (showRawText) {
                                MarkdownText(text = message.text)
                            } else {
                                val toolCalls = remember(message.text) { extractWorkspaceToolCalls(message.text) }
                                val cleanText = remember(message.text) { stripWorkspaceXmlTags(message.text) }

                                if (toolCalls.isNotEmpty()) {
                                    WorkspaceToolCallBlock(toolCalls = toolCalls)
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                if (cleanText.isNotEmpty()) {
                                    MarkdownText(text = cleanText)
                                }
                            }
                        }
                    }

                    // Action tools (Copy/Share) below rich generated bubbles
                    if (!isUser && !message.isError) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val hasXmlTags = remember(message.text) { message.text.contains("<workspace", ignoreCase = true) }
                            if (hasXmlTags) {
                                TextButton(
                                    onClick = { showRawText = !showRawText },
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = if (showRawText) "✨ Visual View" else "📄 Raw Output",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(message.text))
                                    android.widget.Toast.makeText(context, "Copied response to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy message",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, message.text)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Gemini Response"))
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share text",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Live, glowing animated bubble for real-time incoming streaming
@Composable
fun StreamingMsgBubble(
    thoughtText: String,
    answerText: String,
    modelLabel: String
) {
    var expandedThought by remember { mutableStateOf(false) }
    var secondsElapsed by remember { mutableStateOf(0) }
    var streamShowRawText by remember { mutableStateOf(false) }
    val isThinkingActive = thoughtText.isNotEmpty() && answerText.isEmpty()

    LaunchedEffect(isThinkingActive) {
        if (isThinkingActive) {
            expandedThought = false
            secondsElapsed = 0
            while (true) {
                delay(1000L)
                secondsElapsed++
            }
        } else {
            expandedThought = false
        }
    }

    val rotationState by animateFloatAsState(
        targetValue = if (expandedThought) 180f else 0f,
        label = "chevron_rotation"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = "AI Streaming icon",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = 4.dp, end = 8.dp)
                .size(24.dp)
        )

        Column(
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            Text(
                text = "$modelLabel (Streaming)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )

            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = 4.dp,
                    bottomEnd = 16.dp
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .animateContentSize()
                ) {
                    // 1. Thinking step
                    if (thoughtText.isNotEmpty()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLowest,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expandedThought = !expandedThought },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Psychology,
                                            contentDescription = "Thoughts outline",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isThinkingActive) {
                                                "Thinking... (${formatThinkingDuration(secondsElapsed.toLong())})"
                                            } else {
                                                "Reasoned Thoughts (${formatThinkingDuration(secondsElapsed.toLong())})"
                                            },
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ExpandMore,
                                        contentDescription = "Minimize/expand thinking icon",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .rotate(rotationState)
                                    )
                                }

                                AnimatedVisibility(
                                    visible = expandedThought,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically()
                                ) {
                                    Column {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Divider(
                                            color = MaterialTheme.colorScheme.outlineVariant,
                                            thickness = 0.5.dp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = thoughtText,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontFamily = FontFamily.Monospace,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Final response step
                    if (answerText.isNotEmpty()) {
                        if (streamShowRawText) {
                            MarkdownText(text = answerText)
                        } else {
                            val toolCalls = remember(answerText) { extractWorkspaceToolCalls(answerText) }
                            val cleanText = remember(answerText) { stripWorkspaceXmlTags(answerText) }

                            if (toolCalls.isNotEmpty()) {
                                WorkspaceToolCallBlock(toolCalls = toolCalls)
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            if (cleanText.isNotEmpty()) {
                                MarkdownText(text = cleanText)
                            }
                        }

                        val hasXmlTags = remember(answerText) { answerText.contains("<workspace", ignoreCase = true) }
                        if (hasXmlTags) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { streamShowRawText = !streamShowRawText }) {
                                    Text(
                                        text = if (streamShowRawText) "✨ Visual View" else "📄 Raw View",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    } else {
                        // Empty pulsing indicator awaiting final answer stream
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "Active processing",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Formulating response...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontFamily = FontFamily.SansSerif
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RenderCodeBlock(section: String) {
    val lines = section.trim().lines()
    val language = if (lines.isNotEmpty() && !lines.first().contains(" ")) lines.first() else ""
    val codeContent = if (language.isNotEmpty()) lines.drop(1).joinToString("\n") else section
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        color = Color(0xFF1E1E1E),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFF333333))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (language.isNotEmpty()) language.uppercase() else "CODE",
                    color = Color(0xFF888888),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                )
                val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                val context = androidx.compose.ui.platform.LocalContext.current
                Text(
                    text = "COPY",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .clickable {
                            clipboardManager.setText(AnnotatedString(codeContent.trim()))
                            android.widget.Toast.makeText(context, "Copied code block", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        .padding(4.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = codeContent.trim(),
                color = Color(0xFFD4D4D4),
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.horizontalScroll(rememberScrollState())
            )
        }
    }
}

@Composable
fun MathFormulaBlock(formula: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "∑ EQUATION",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 9.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = formula.trim(),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

// Fully Featured Markdown Block and Inline Text Formatter
@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = TextStyle.Default
) {
    // 1. Normalize LaTeX delimiters
    val normalized = text
        .replace("\\[", "$$")
        .replace("\\]", "$$")
        .replace("\\(", "$")
        .replace("\\)", "$")

    // 2. Parse code blocks and math blocks!
    Column(modifier = modifier) {
        val codeSections = normalized.split("```")
        codeSections.forEachIndexed { codeIndex, codeSection ->
            if (codeIndex % 2 == 1) {
                RenderCodeBlock(codeSection)
            } else {
                val mathSections = codeSection.split("$$")
                mathSections.forEachIndexed { mathIndex, mathSection ->
                    if (mathIndex % 2 == 1) {
                        MathFormulaBlock(formula = mathSection)
                    } else {
                        if (mathSection.isNotEmpty()) {
                            MarkdownBlocks(text = mathSection, color = color, style = style)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderBlock(level: Int, text: String) {
    val style = when (level) {
        1 -> MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        2 -> MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        3 -> MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        4 -> MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        5 -> MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        else -> MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
    }
    val topPadding = when (level) {
        1 -> 14.dp
        2 -> 12.dp
        3 -> 10.dp
        else -> 8.dp
    }
    val bottomPadding = 4.dp
    val color = when (level) {
        1 -> MaterialTheme.colorScheme.primary
        2 -> MaterialTheme.colorScheme.secondary
        3 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Spacer(modifier = Modifier.height(topPadding))
    PartiallyStyledText(
        text = text,
        color = color,
        style = style,
        modifier = Modifier.padding(bottom = bottomPadding)
    )
}

@Composable
fun SeparatorBlock() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Divider(
            modifier = Modifier.fillMaxWidth(0.95f),
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 1.dp
        )
    }
}

@Composable
fun QuoteBlock(text: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(topStart = 0.dp, topEnd = 8.dp, bottomEnd = 8.dp, bottomStart = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)) {
                PartiallyStyledText(
                    text = text,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontStyle = FontStyle.Italic,
                        lineHeight = 22.sp
                    )
                )
            }
        }
    }
}

@Composable
fun BulletListBlock(items: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items.forEach { content ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Box(modifier = Modifier.weight(1f)) {
                    PartiallyStyledText(text = content)
                }
            }
        }
    }
}

@Composable
fun OrderedListBlock(items: List<Pair<String, String>>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items.forEach { (num, content) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = num,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Box(modifier = Modifier.weight(1f)) {
                    PartiallyStyledText(text = content)
                }
            }
        }
    }
}

@Composable
fun TableBlock(headers: List<String>, rows: List<List<String>>) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            if (headers.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
                        )
                        .padding(vertical = 10.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    headers.forEach { h ->
                        Text(
                            text = h,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                              ),
                              modifier = Modifier.widthIn(min = 100.dp, max = 250.dp)
                        )
                    }
                }
                Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 2.dp)
            }

            rows.forEachIndexed { rowIndex, row ->
                val isEven = rowIndex % 2 == 0
                val bgColor = if (isEven) {
                    MaterialTheme.colorScheme.surfaceContainerLowest
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLow
                }
                Row(
                    modifier = Modifier
                        .background(color = bgColor)
                        .padding(vertical = 10.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    row.forEach { cell ->
                        Box(modifier = Modifier.widthIn(min = 100.dp, max = 250.dp)) {
                            PartiallyStyledText(
                                text = cell,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                if (rowIndex < rows.lastIndex) {
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
fun TaskListBlock(items: List<Pair<Boolean, String>>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items.forEach { (isChecked, text) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .border(
                            width = 2.dp,
                            color = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .background(
                            color = if (isChecked) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isChecked) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Checked",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                val textStyle = if (isChecked) {
                    MaterialTheme.typography.bodyMedium.copy(
                        textDecoration = TextDecoration.LineThrough,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                } else {
                    MaterialTheme.typography.bodyMedium
                }

                Box(modifier = Modifier.weight(1f)) {
                    PartiallyStyledText(
                        text = text,
                        style = textStyle
                    )
                }
            }
        }
    }
}

sealed class Block {
    data class Header(val level: Int, val text: String) : Block()
    data class Paragraph(val text: String) : Block()
    data class Quote(val text: String) : Block()
    data class BulletList(val items: List<String>) : Block()
    data class OrderedList(val items: List<Pair<String, String>>) : Block()
    data class Table(val headers: List<String>, val rows: List<List<String>>) : Block()
    data class Separator(val raw: String) : Block()
    data class TaskList(val items: List<Pair<Boolean, String>>) : Block()
}

fun parseBlocks(text: String): List<Block> {
    val lines = text.lines()
    val blocks = mutableListOf<Block>()
    var i = 0
    val total = lines.size

    val taskRegex = Regex("^[-+*]\\s+\\[([ xX])\\]\\s+(.*)")
    val bulletRegex = Regex("^[-+*•]\\s+(.*)")
    val orderedRegex = Regex("^(\\d+)\\.\\s+(.*)")

    while (i < total) {
        val line = lines[i]
        val trimmed = line.trim()

        if (trimmed.startsWith("#") && trimmed.contains(" ")) {
            val level = trimmed.takeWhile { it == '#' }.length
            if (level in 1..6) {
                val headerText = trimmed.drop(level).trim()
                blocks.add(Block.Header(level, headerText))
                i++
                continue
            }
        }

        if (trimmed == "---" || trimmed == "***" || trimmed == "___") {
            blocks.add(Block.Separator(trimmed))
            i++
            continue
        }

        if (trimmed.startsWith(">")) {
            val quoteLines = mutableListOf<String>()
            while (i < total && lines[i].trimStart().startsWith(">")) {
                val quoteLine = lines[i].trimStart()
                val parsed = if (quoteLine.startsWith("> ")) quoteLine.drop(2) else quoteLine.drop(1)
                quoteLines.add(parsed)
                i++
            }
            blocks.add(Block.Quote(quoteLines.joinToString("\n")))
            continue
        }

        if (trimmed.startsWith("|") && trimmed.contains("|")) {
            val tableLines = mutableListOf<String>()
            while (i < total && lines[i].trim().startsWith("|")) {
                tableLines.add(lines[i].trim())
                i++
            }

            if (tableLines.isNotEmpty()) {
                val cleanLines = tableLines.filter { !it.matches(Regex("\\|\\s*[-:]+\\s*(\\|\\s*[-:]+\\s*)*\\|")) }
                if (cleanLines.isNotEmpty()) {
                    val parsedRows = cleanLines.map { r ->
                        r.split("|")
                            .map { it.trim() }
                            .filterIndexed { idx, _ -> idx > 0 && idx < r.split("|").lastIndex }
                    }

                    val headers = if (parsedRows.isNotEmpty()) parsedRows.first() else emptyList()
                    val rows = if (parsedRows.size > 1) parsedRows.drop(1) else emptyList()
                    blocks.add(Block.Table(headers, rows))
                }
            }
            continue
        }

        if (taskRegex.matches(trimmed)) {
            val taskItems = mutableListOf<Pair<Boolean, String>>()
            while (i < total && taskRegex.matches(lines[i].trim())) {
                val m = taskRegex.find(lines[i].trim())!!
                val checked = m.groupValues[1].lowercase() == "x"
                val content = m.groupValues[2]
                taskItems.add(Pair(checked, content))
                i++
            }
            blocks.add(Block.TaskList(taskItems))
            continue
        }

        if (bulletRegex.matches(trimmed)) {
            val bulletItems = mutableListOf<String>()
            while (i < total && bulletRegex.matches(lines[i].trim()) && !taskRegex.matches(lines[i].trim())) {
                val m = bulletRegex.find(lines[i].trim())!!
                bulletItems.add(m.groupValues[1])
                i++
            }
            blocks.add(Block.BulletList(bulletItems))
            continue
        }

        if (orderedRegex.matches(trimmed)) {
            val orderedItems = mutableListOf<Pair<String, String>>()
            while (i < total && orderedRegex.matches(lines[i].trim())) {
                val m = orderedRegex.find(lines[i].trim())!!
                val num = m.groupValues[1]
                val content = m.groupValues[2]
                orderedItems.add(Pair("$num.", content))
                i++
            }
            blocks.add(Block.OrderedList(orderedItems))
            continue
        }

        if (trimmed.isEmpty()) {
            i++
            continue
        }

        val paraLines = mutableListOf<String>()
        while (i < total) {
            val nextLine = lines[i]
            val nextTrimmed = nextLine.trim()
            if (nextTrimmed.isEmpty() ||
                (nextTrimmed.startsWith("#") && nextTrimmed.contains(" ")) ||
                nextTrimmed == "---" || nextTrimmed == "***" || nextTrimmed == "___" ||
                nextTrimmed.startsWith(">") ||
                (nextTrimmed.startsWith("|") && nextTrimmed.contains("|")) ||
                taskRegex.matches(nextTrimmed) ||
                bulletRegex.matches(nextTrimmed) ||
                orderedRegex.matches(nextTrimmed)
            ) {
                break
            }
            paraLines.add(nextLine)
            i++
        }
        if (paraLines.isNotEmpty()) {
            blocks.add(Block.Paragraph(paraLines.joinToString("\n")))
        }
    }

    return blocks
}

@Composable
fun MarkdownBlocks(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = TextStyle.Default
) {
    val blocks = remember(text) { parseBlocks(text) }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        blocks.forEach { block ->
            when (block) {
                is Block.Header -> HeaderBlock(level = block.level, text = block.text)
                is Block.Paragraph -> {
                    PartiallyStyledText(
                        text = block.text,
                        color = color,
                        style = style.copy(lineHeight = 22.sp),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                is Block.Quote -> QuoteBlock(text = block.text)
                is Block.BulletList -> BulletListBlock(items = block.items)
                is Block.OrderedList -> OrderedListBlock(items = block.items)
                is Block.Table -> TableBlock(headers = block.headers, rows = block.rows)
                is Block.Separator -> SeparatorBlock()
                is Block.TaskList -> TaskListBlock(items = block.items)
            }
        }
    }
}

@Composable
fun PartiallyStyledText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = TextStyle.Default
) {
    val annotatedString = androidx.compose.ui.text.buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            when {
                text.startsWith("***", i) -> {
                    val end = text.indexOf("***", i + 3)
                    if (end != -1) {
                        pushStyle(
                            androidx.compose.ui.text.SpanStyle(
                                fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Italic
                            )
                        )
                        append(text.substring(i + 3, end))
                        pop()
                        i = end + 3
                    } else {
                        append("***")
                        i += 3
                    }
                }
                text.startsWith("**", i) -> {
                    val end = text.indexOf("**", i + 2)
                    if (end != -1) {
                        pushStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold))
                        append(text.substring(i + 2, end))
                        pop()
                        i = end + 2
                    } else {
                        append("**")
                        i += 2
                    }
                }
                text.startsWith("__", i) -> {
                    val end = text.indexOf("__", i + 2)
                    if (end != -1) {
                        pushStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold))
                        append(text.substring(i + 2, end))
                        pop()
                        i = end + 2
                    } else {
                        append("__")
                        i += 2
                    }
                }
                text.startsWith("*", i) -> {
                    val end = text.indexOf("*", i + 1)
                    if (end != -1) {
                        pushStyle(androidx.compose.ui.text.SpanStyle(fontStyle = FontStyle.Italic))
                        append(text.substring(i + 1, end))
                        pop()
                        i = end + 1
                    } else {
                        append("*")
                        i++
                    }
                }
                text.startsWith("_", i) -> {
                    val end = text.indexOf("_", i + 1)
                    if (end != -1) {
                        pushStyle(androidx.compose.ui.text.SpanStyle(fontStyle = FontStyle.Italic))
                        append(text.substring(i + 1, end))
                        pop()
                        i = end + 1
                    } else {
                        append("_")
                        i++
                    }
                }
                text.startsWith("~~", i) -> {
                    val end = text.indexOf("~~", i + 2)
                    if (end != -1) {
                        pushStyle(
                            androidx.compose.ui.text.SpanStyle(
                                textDecoration = TextDecoration.LineThrough
                            )
                        )
                        append(text.substring(i + 2, end))
                        pop()
                        i = end + 2
                    } else {
                        append("~~")
                        i += 2
                    }
                }
                text.startsWith("`", i) -> {
                    val end = text.indexOf("`", i + 1)
                    if (end != -1) {
                        pushStyle(
                            androidx.compose.ui.text.SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                background = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        append(text.substring(i + 1, end))
                        pop()
                        i = end + 1
                    } else {
                        append("`")
                        i++
                    }
                }
                text.startsWith("$", i) -> {
                    val end = text.indexOf("$", i + 1)
                    if (end != -1) {
                        pushStyle(
                            androidx.compose.ui.text.SpanStyle(
                                fontFamily = FontFamily.Serif,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        )
                        append(text.substring(i + 1, end).trim())
                        pop()
                        i = end + 1
                    } else {
                        append("$")
                        i++
                    }
                }
                text.startsWith("[", i) -> {
                    val bracketClose = text.indexOf("]", i + 1)
                    if (bracketClose != -1 && text.startsWith("(", bracketClose + 1)) {
                        val parenClose = text.indexOf(")", bracketClose + 2)
                        if (parenClose != -1) {
                            val linkText = text.substring(i + 1, bracketClose)
                            val linkUrl = text.substring(bracketClose + 2, parenClose)
                            pushStyle(
                                androidx.compose.ui.text.SpanStyle(
                                    color = MaterialTheme.colorScheme.primary,
                                    textDecoration = TextDecoration.Underline,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            pushStringAnnotation(tag = "URL", annotation = linkUrl)
                            append(linkText)
                            pop()
                            pop()
                            i = parenClose + 1
                        } else {
                            append("[")
                            i++
                        }
                    } else {
                        append("[")
                        i++
                    }
                }
                else -> {
                    append(text[i].toString())
                    i++
                }
            }
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

    androidx.compose.foundation.text.ClickableText(
        text = annotatedString,
        modifier = modifier,
        style = style.copy(color = if (color != Color.Unspecified) color else MaterialTheme.colorScheme.onSurface),
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    try {
                        uriHandler.openUri(annotation.item)
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(context, "Link: ${annotation.item}", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
        }
    )
}

@Composable
fun TypingBar(
    isStreaming: Boolean,
    onSendMessage: (String) -> Unit
) {
    var rawText by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = rawText,
                onValueChange = { rawText = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                placeholder = { Text("Ask Gemini model...") },
                maxLines = 6,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Default
                )
            )

            IconButton(
                onClick = {
                    if (rawText.trim().isNotEmpty() && !isStreaming) {
                        onSendMessage(rawText)
                        rawText = ""
                    }
                },
                enabled = rawText.trim().isNotEmpty() && !isStreaming,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (rawText.trim().isNotEmpty() && !isStreaming) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
                    .testTag("send_msg_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send prompt button",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun FormattingGuideDialog(onClose: () -> Unit) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onClose) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically, 
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Syntax & Styles Guide",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = onClose) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close guide")
                    }
                }

                Divider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // List of items
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "This application fully parses rich content natively including markdown, science, math, and code syntax: ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val guideItems = listOf(
                        Pair("Headers (Level 1-6)", "# Primary Heading\n## Secondary Subheading\n### Level 3 Title"),
                        Pair("Bold & Italic Emphasis", "You can combine **bold text** with *italicized emphasis* or use __alternative bold__ and _alternative italic_."),
                        Pair("Strikethrough", "Correct outdated info with ~~strikethrough~~ lines."),
                        Pair("Code Styling", "Inline `print(\"Hello\")` and multi-line code blocks:\n```kotlin\nval list = remember { mutableStateListOf<String>() }\n```"),
                        Pair("LaTeX / Scientific Math", "Perform inline formulas like \$E = mc^2\$ or full display equations:\n\$\$\\int_{a}^{b} f(x) dx = F(b) - F(a)\$\$"),
                        Pair("Checklists & Task Items", "- [ ] Complete math assignment\n- [x] Integrate high-fidelity markdown renderer"),
                        Pair("Bullet & Numbered Lists", "- Bullet group item A\n- Bullet group item B\n\n1. First procedural step\n2. Second procedural step"),
                        Pair("Markdown Tables", "| Target Variable | Mathematical Constant |\n|---|---|\n| Acceleration | 9.81 m/s^2 |\n| Pi Approximation| 3.14159 |"),
                        Pair("Blockquotes", "> \"The only way to do great work is to love what you do.\"\n> -- Steve Jobs"),
                        Pair("Horizontal Divider", "Separate sections elegantly:\n---"),
                        Pair("Interactive Markdown Links", "Reference external resources via [Google Search](https://google.com) directly.")
                    )

                    guideItems.forEach { (title, mdText) ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                // Raw input representation:
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = mdText,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "LIVE PREVIEW RENDER:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                // Actual rendering via our Markdown parser:
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp)
                                ) {
                                    MarkdownText(text = mdText)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Got It, Close Guide")
                }
            }
        }
    }
}

// Detailed Modal Dialog for advanced setting tweaks
@Composable
fun SettingsDialog(
    apiKey: String,
    modelId: String,
    customModelId: String,
    isCustomModelSelected: Boolean,
    temperature: Float,
    thinkingMode: String,
    thinkingBudgetLevel: String,
    onClose: () -> Unit,
    onSave: (String, String, String, Boolean, Float, String, String) -> Unit,
    context: Context
) {
    var keyField by remember { mutableStateOf(apiKey) }
    var keyVisible by remember { mutableStateOf(false) }

    var selectedPresetId by remember { mutableStateOf(modelId) }
    var customIdField by remember { mutableStateOf(customModelId) }
    var useCustomModel by remember { mutableStateOf(isCustomModelSelected) }
    var tempVal by remember { mutableStateOf(temperature) }
    var thinkingModeVal by remember { mutableStateOf(thinkingMode) }
    var thinkingBudgetLevelVal by remember { mutableStateOf(thinkingBudgetLevel) }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Settings Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Config icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Engine Settings",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black
                        )
                    }
                    IconButton(onClick = onClose) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close dialog")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. API Key Box
                    Column {
                        Text(
                            text = "Gemini API Key",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = keyField,
                            onValueChange = { keyField = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter your Gemini API Key") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { keyVisible = !keyVisible }) {
                                    Icon(
                                        imageVector = if (keyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle visibility"
                                    )
                                }
                            }
                        )
                        TextButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Get free key in Google AI Studio", fontSize = 12.sp)
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

                    // 2. Model Selection Block
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "AI Reasoning Model",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // Grid List of Preset Models styled as clickable cards
                        PRESET_MODELS.forEach { preset ->
                            val isPresetSelected = !useCustomModel && preset.id == selectedPresetId
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        useCustomModel = false
                                        selectedPresetId = preset.id
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isPresetSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
                                ),
                                border = if (isPresetSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = preset.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isPresetSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (preset.supportsThinking) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                                    shape = CircleShape
                                                ) {
                                                    Text(
                                                        text = "THINKING",
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = preset.description,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isPresetSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                    if (isPresetSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Custom Model ID alternative toggle card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { useCustomModel = true },
                            colors = CardDefaults.cardColors(
                                containerColor = if (useCustomModel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            border = if (useCustomModel) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Custom Model ID",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (useCustomModel) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (useCustomModel) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Enter any raw alternative Gemini Model ID below directly.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (useCustomModel) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )

                                if (useCustomModel) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedTextField(
                                        value = customIdField,
                                        onValueChange = { customIdField = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = { Text("e.g. gemini-1.5-pro-002") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

                    // 3. Temperature Slider Block
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Temperature (Creativity)",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = String.format("%.2f", tempVal),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Slider(
                            value = tempVal,
                            onValueChange = { tempVal = it },
                            valueRange = 0.0f..2.0f
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "0.0 (Precise/Deterministic)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "2.0 (Creative/Random)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

                    // 4. Thinking Configuration Block
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "Intelligence / Thinking settings",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gemini Thinking Effort",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Set the depth of reasoning. Choose a level structure from basic up to maximum ability/ultra, even for custom models.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Thinking Behavior",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "auto" to "Auto-Detect",
                                "on" to "Force On",
                                "off" to "Force Off"
                            ).forEach { (value, label) ->
                                val isSelected = thinkingModeVal == value
                                OutlinedButton(
                                    onClick = { thinkingModeVal = value },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    ),
                                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(label, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }

                        if (thinkingModeVal != "off") {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Effort / Token Budget",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(
                                    "basic" to Pair("Basic Reasoning (2,048 Tokens)", "Light analytical tasks and quick standard replies."),
                                    "hard" to Pair("Hard Analytical (8,192 Tokens)", "Rigorous multi-step logical tasks or robust code debugs."),
                                    "max" to Pair("Maximum Ability (16,384 Tokens)", "Full intellectual depth for complex science / math formulas."),
                                    "ultra" to Pair("Ultra Cognition (32,768 Tokens)", "Advanced system reasoning designed for deep problem solving."),
                                    "even_more" to Pair("Extreme Cognition (65,536 Tokens)", "The absolute maximum intelligence pool, designed to crack extreme proofs.")
                                ).forEach { (level, levelInfo) ->
                                    val (title, infoDesc) = levelInfo
                                    val isSelected = thinkingBudgetLevelVal == level
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { thinkingBudgetLevelVal = level },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.surfaceContainerLow
                                        ),
                                        border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = title,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                                )
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Selected",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = infoDesc,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom actions buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onClose) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(keyField, selectedPresetId, customIdField, useCustomModel, tempVal, thinkingModeVal, thinkingBudgetLevelVal)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Changes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// WORKSPACE VISUAL SYSTEM & AGENT TOOL LOGS
// ==========================================

data class ExecutedToolCall(
    val type: String,
    val path: String,
    val content: String? = null,
    val status: String = "Completed"
)

fun extractWorkspaceToolCalls(text: String): List<ExecutedToolCall> {
    val list = mutableListOf<ExecutedToolCall>()
    try {
        val openingRegex = Regex("""<(workspace[a-zA-Z0-9_-]*)([^>]*)>""", RegexOption.IGNORE_CASE)
        val matches = openingRegex.findAll(text).toList()
        
        matches.forEach { match ->
            val tagName = match.groups[1]?.value ?: ""
            val attrString = match.groups[2]?.value ?: ""
            val startIndex = match.range.last + 1
            
            val isSelfClosing = attrString.trim().endsWith("/")
            val cleanAttrString = if (isSelfClosing) attrString.trim().dropLast(1) else attrString
            
            val attrs = mutableMapOf<String, String>()
            val attrRegex = Regex("""(\w+)\s*=\s*(?:"([^"]*)"|'([^']*)')""")
            attrRegex.findAll(cleanAttrString).forEach { attrMatch ->
                val key = attrMatch.groups[1]?.value ?: ""
                val value = attrMatch.groups[2]?.value ?: attrMatch.groups[3]?.value ?: ""
                if (key.isNotEmpty()) {
                    attrs[key] = value
                }
            }
            
            val path = attrs["path"] ?: ""
            val source = attrs["source"] ?: ""
            val destination = attrs["destination"] ?: ""
            val command = attrs["command"] ?: ""
            
            val normTag = tagName.lowercase()
                .removePrefix("workspace")
                .removePrefix("_")
                .replace("_", "")
            
            var body = ""
            var status = "Completed"
            
            if (!isSelfClosing) {
                val closingTag = "</$tagName>"
                val closingIndex = text.indexOf(closingTag, startIndex, ignoreCase = true)
                if (closingIndex != -1) {
                    body = text.substring(startIndex, closingIndex)
                    status = "Completed"
                } else {
                    val nextTagIndex = text.indexOf("<workspace", startIndex, ignoreCase = true)
                    body = if (nextTagIndex != -1) {
                        text.substring(startIndex, nextTagIndex)
                    } else {
                        text.substring(startIndex)
                    }
                    status = "Streaming..."
                }
            }
            
            when (normTag) {
                "createfile" -> {
                    if (path.isNotEmpty()) {
                        list.add(ExecutedToolCall("Create File", path, body, status))
                    }
                }
                "editfile" -> {
                    if (path.isNotEmpty()) {
                        list.add(ExecutedToolCall("Edit File", path, body, status))
                    }
                }
                "viewfile" -> {
                    if (path.isNotEmpty()) {
                        list.add(ExecutedToolCall("View File", path, null, status))
                    }
                }
                "listdir", "list" -> {
                    list.add(ExecutedToolCall("List Directory", if (path.isEmpty()) "Root" else path, null, status))
                }
                "createdirectory", "createdir" -> {
                    if (path.isNotEmpty()) {
                        list.add(ExecutedToolCall("Create Folder", path, null, status))
                    }
                }
                "deletepath", "delete" -> {
                    if (path.isNotEmpty()) {
                        list.add(ExecutedToolCall("Delete Item", path, null, status))
                    }
                }
                "move" -> {
                    if (source.isNotEmpty() && destination.isNotEmpty()) {
                        list.add(ExecutedToolCall("Move/Rename", "$source -> $destination", null, status))
                    }
                }
                "shellexec", "shell" -> {
                    if (command.isNotEmpty()) {
                        list.add(ExecutedToolCall("Run Command", command, null, status))
                    }
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return list
}

fun stripWorkspaceXmlTags(text: String): String {
    var result = text
    try {
        // Step 1: Strip complete XML tags and multi-line body contents matching any 'workspace' variation (blocks out drafts)
        val blockRegex = Regex("""<(workspace[a-zA-Z0-9_-]*)\b[^>]*>([\s\S]*?)(?:<\/\1>|$)""", RegexOption.IGNORE_CASE)
        result = result.replace(blockRegex, "")
        
        // Step 2: Strip any unclosed, stray or self-closing 'workspace' tags
        val tagRegex = Regex("""<[/]?(workspace[a-zA-Z0-9_-]*)\b[^>]*[/]?>""", RegexOption.IGNORE_CASE)
        result = result.replace(tagRegex, "")
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return result.trim()
}

@Composable
fun WorkspaceToolCallBlock(
    toolCalls: List<ExecutedToolCall>,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }
    val rotationState by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "chevron_rotation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("workspace_tool_call_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Clickable Header to Expand/Collapse
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Agent action icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (toolCalls.any { it.status == "Streaming..." }) "Agent performing actions..." else "Agent Actions",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val completedCount = toolCalls.count { it.status == "Completed" }
                    val totalCount = toolCalls.size
                    Text(
                        text = "$completedCount/$totalCount done",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = "Collapse/Expand tools",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(18.dp)
                            .rotate(rotationState)
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        thickness = 0.5.dp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    toolCalls.forEachIndexed { index, call ->
                        if (index > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (call.status == "Streaming...") MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
                                    else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(vertical = 6.dp, horizontal = 6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            if (call.status == "Streaming...") {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .padding(top = 2.dp, start = 2.dp, end = 2.dp)
                                        .size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Success tick",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "${call.type}: ${call.path}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (call.status == "Streaming...") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (call.status == "Streaming...") {
                                        Text(
                                            text = "writing...",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.W600,
                                            fontStyle = FontStyle.Italic,
                                            color = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.padding(start = 4.dp)
                                        )
                                    }
                                }
                                
                                if (!call.content.isNullOrEmpty()) {
                                    var showContentPreview by remember { mutableStateOf(false) }
                                    Text(
                                        text = if (showContentPreview) "Hide Code Content" else "View Written Content",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .clickable { showContentPreview = !showContentPreview }
                                    )
                                    AnimatedVisibility(visible = showContentPreview) {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 6.dp, bottom = 4.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                            ),
                                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                        ) {
                                            Text(
                                                text = call.content,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontFamily = FontFamily.Monospace,
                                                modifier = Modifier
                                                    .padding(10.dp)
                                                    .horizontalScroll(rememberScrollState())
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WorkspaceView(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentSubPath by remember { mutableStateOf("") }
    val workspaceRefreshCount by viewModel.workspaceTrigger.collectAsState()

    var fileList by remember { mutableStateOf<List<com.example.data.workspace.WorkspaceItem>>(emptyList()) }

    LaunchedEffect(workspaceRefreshCount, currentSubPath) {
        fileList = com.example.data.workspace.WorkspaceManager.listFilesAndFolders(context, currentSubPath)
    }

    // Modal states
    var showCreateFileDialog by remember { mutableStateOf(false) }
    var showCreateDirDialog by remember { mutableStateOf(false) }
    var selectedFileForEditing by remember { mutableStateOf<com.example.data.workspace.WorkspaceItem.FileItem?>(null) }
    var editingFileContent by remember { mutableStateOf("") }

    // State inputs
    var newFileName by remember { mutableStateOf("") }
    var newFileContent by remember { mutableStateOf("") }
    var newDirName by remember { mutableStateOf("") }

    val prefs = remember { context.getSharedPreferences("workspace_prefs", Context.MODE_PRIVATE) }
    var customDirInput by remember { mutableStateOf(prefs.getString("custom_root_path", "") ?: "") }
    var showPathSettings by remember { mutableStateOf(false) }
    var showDirectoryPicker by remember { mutableStateOf(false) }

    val safLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            try {
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            com.example.data.workspace.WorkspaceManager.setCustomWorkspaceUri(context, uri.toString())
            currentSubPath = ""
            viewModel.triggerWorkspaceRefresh()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("workspace_view_container")
    ) {
        // Custom Workspace Folder Configuration Panel
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .testTag("workspace_paths_panel"),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
            ),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📁",
                            fontSize = 20.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Active Workspace Folder",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            val label = com.example.data.workspace.WorkspaceManager.getWorkspaceRootLabel(context)
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    TextButton(
                        onClick = { showPathSettings = !showPathSettings }
                    ) {
                        Text(
                            text = if (showPathSettings) "Hide Panel" else "Change Folder",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                AnimatedVisibility(visible = showPathSettings) {
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        Text(
                            text = "Redirect the agent console's file system to any local folder on your device. All subsequent tool read, write, and directory listings will occur live at that selected location.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // SAF Choose Directory Button (Primary)
                        Button(
                            onClick = {
                                try {
                                    safLauncher.launch(null)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("workspace_saf_select_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text("📁 Select Folder (Android System Picker)", fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Spacer(modifier = Modifier.height(12.dp))

                        // Advanced manual fallback options
                        Text(
                            text = "Advanced: Enter manual absolute path fallback",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = customDirInput,
                                onValueChange = { customDirInput = it },
                                placeholder = { Text("/storage/emulated/0/Download/workspace") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("workspace_custom_path_input"),
                                singleLine = true,
                                textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                                trailingIcon = {
                                    IconButton(
                                        onClick = { showDirectoryPicker = true },
                                        modifier = Modifier.testTag("workspace_select_folder_icon")
                                    ) {
                                        Text("📁", fontSize = 18.sp)
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                )
                            )

                            Button(
                                onClick = {
                                    if (customDirInput.trim().isNotEmpty()) {
                                        com.example.data.workspace.WorkspaceManager.setCustomWorkspaceRoot(
                                            context,
                                            customDirInput.trim()
                                        )
                                        currentSubPath = ""
                                        viewModel.triggerWorkspaceRefresh()
                                    }
                                },
                                enabled = customDirInput.trim().isNotEmpty(),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text("Apply")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    com.example.data.workspace.WorkspaceManager.resetWorkspaceRoot(context)
                                    customDirInput = ""
                                    currentSubPath = ""
                                    viewModel.triggerWorkspaceRefresh()
                                }
                            ) {
                                Text("Reset to default local sandbox", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
        // Workspace Path Header & Breadcrumb
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (currentSubPath.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                val parent = currentSubPath.substringBeforeLast("/", "")
                                currentSubPath = parent
                            },
                            modifier = Modifier.size(36.dp).testTag("workspace_parent_dir_back")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Back directory",
                                modifier = Modifier.rotate(90f).size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = if (currentSubPath.isEmpty()) "Root Workspace" else "Workspace / $currentSubPath",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Export entire workspace ZIP
                IconButton(
                    onClick = {
                        val file = com.example.data.workspace.WorkspaceManager.exportToZip(context)
                        if (file != null) {
                            shareFile(context, file)
                        }
                    },
                    modifier = Modifier.size(36.dp).testTag("workspace_export_zip_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Export workspace files as ZIP",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Actions segment
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    newFileName = ""
                    newFileContent = ""
                    showCreateFileDialog = true
                },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("workspace_new_file_button"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Icon", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("New File", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {
                    newDirName = ""
                    showCreateDirDialog = true
                },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("workspace_new_directory_button"),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Icon Outlined", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("New Folder", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Files/Folders List Frame
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (fileList.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Empty Folder Information",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "This directory is empty",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Add some files or ask Gemini to build them!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(fileList, key = { it.relativePath }) { item ->
                        WorkspaceItemRow(
                            item = item,
                            onClick = {
                                if (item.isDirectory) {
                                    currentSubPath = item.relativePath
                                } else {
                                    val fItem = item as com.example.data.workspace.WorkspaceItem.FileItem
                                    selectedFileForEditing = fItem
                                    editingFileContent = com.example.data.workspace.WorkspaceManager.readFile(context, fItem.relativePath)
                                }
                            },
                            onDelete = {
                                com.example.data.workspace.WorkspaceManager.deletePath(context, item.relativePath)
                                viewModel.triggerWorkspaceRefresh()
                            }
                        )
                    }
                }
            }
        }
    }

    // Interactive Dialogs
    if (showCreateFileDialog) {
        Dialog(onDismissRequest = { showCreateFileDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Create / Upload New File",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newFileName,
                        onValueChange = { newFileName = it },
                        label = { Text("File Name (e.g. script.js, math.py)") },
                        modifier = Modifier.fillMaxWidth().testTag("dialog_new_file_name_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newFileContent,
                        onValueChange = { newFileContent = it },
                        label = { Text("File Content") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .testTag("dialog_new_file_content_input"),
                        maxLines = 12
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showCreateFileDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newFileName.trim().isNotEmpty()) {
                                    val targetRelPath = if (currentSubPath.isEmpty()) newFileName.trim() else "$currentSubPath/${newFileName.trim()}"
                                    com.example.data.workspace.WorkspaceManager.writeFile(context, targetRelPath, newFileContent)
                                    viewModel.triggerWorkspaceRefresh()
                                    showCreateFileDialog = false
                                }
                            },
                            enabled = newFileName.trim().isNotEmpty(),
                            modifier = Modifier.testTag("dialog_new_file_confirm")
                        ) {
                            Text("Create File")
                        }
                    }
                }
            }
        }
    }

    if (showCreateDirDialog) {
        Dialog(onDismissRequest = { showCreateDirDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Create New Directory",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newDirName,
                        onValueChange = { newDirName = it },
                        label = { Text("Folder Name (e.g., src, utils)") },
                        modifier = Modifier.fillMaxWidth().testTag("dialog_new_dir_name_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showCreateDirDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newDirName.trim().isNotEmpty()) {
                                    val targetRelPath = if (currentSubPath.isEmpty()) newDirName.trim() else "$currentSubPath/${newDirName.trim()}"
                                    com.example.data.workspace.WorkspaceManager.createDirectory(context, targetRelPath)
                                    viewModel.triggerWorkspaceRefresh()
                                    showCreateDirDialog = false
                                }
                            },
                            enabled = newDirName.trim().isNotEmpty(),
                            modifier = Modifier.testTag("dialog_new_dir_confirm")
                        ) {
                            Text("Create Folder")
                        }
                    }
                }
            }
        }
    }

    if (showDirectoryPicker) {
        DirectoryPickerDialog(
            initialDir = customDirInput,
            onDismiss = { showDirectoryPicker = false },
            onSelect = { selectedPath ->
                customDirInput = selectedPath
                com.example.data.workspace.WorkspaceManager.setCustomWorkspaceRoot(context, selectedPath)
                currentSubPath = ""
                viewModel.triggerWorkspaceRefresh()
                showDirectoryPicker = false
            }
        )
    }

    if (selectedFileForEditing != null) {
        val fItem = selectedFileForEditing!!
        var showFullScreenPreview by remember { mutableStateOf(false) }
        var previewRefreshKey by remember { mutableStateOf(0) }

        Dialog(
            onDismissRequest = { selectedFileForEditing = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Editing: ${fItem.name}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = fItem.relativePath,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val isWebbyFile = fItem.name.endsWith(".html", ignoreCase = true) || fItem.name.endsWith(".htm", ignoreCase = true)
                            if (isWebbyFile) {
                                Button(
                                    onClick = { showFullScreenPreview = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.padding(end = 8.dp).height(36.dp).testTag("editor_run_fullscreen_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Run App Fullscreen",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Run Page", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                            IconButton(onClick = { selectedFileForEditing = null }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close editor")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    var editorTab by remember { mutableStateOf(0) } // 0 = Code, 1 = HTML WebView Preview

                    androidx.compose.material3.TabRow(
                        selectedTabIndex = editorTab,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        containerColor = Color.Transparent
                    ) {
                        androidx.compose.material3.Tab(
                            selected = editorTab == 0,
                            onClick = { editorTab = 0 },
                            text = { Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Code Editor")
                            }}
                        )
                        androidx.compose.material3.Tab(
                            selected = editorTab == 1,
                            onClick = { editorTab = 1 },
                            text = { Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("HTML Live Preview")
                            }}
                        )
                    }

                    if (editorTab == 1) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "📐 Embed view (tight layout)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Button(
                                onClick = { showFullScreenPreview = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(28.dp).testTag("editor_tab_fullscreen_button")
                            ) {
                                Icon(Icons.Default.Fullscreen, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Launch Full Screen Run", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            androidx.compose.ui.viewinterop.AndroidView(
                                factory = { ctx ->
                                    android.webkit.WebView(ctx).apply {
                                        settings.apply {
                                            javaScriptEnabled = true
                                            domStorageEnabled = true
                                            allowFileAccess = true
                                            allowContentAccess = true
                                            builtInZoomControls = true
                                            displayZoomControls = false
                                        }
                                        webViewClient = object : android.webkit.WebViewClient() {
                                            override fun shouldInterceptRequest(
                                                view: android.webkit.WebView?,
                                                request: android.webkit.WebResourceRequest?
                                            ): android.webkit.WebResourceResponse? {
                                                val url = request?.url ?: return null
                                                if (url.host == "app.workspace") {
                                                    val rawPath = url.path?.removePrefix("/") ?: ""
                                                    val cleanPath = try {
                                                        java.io.File("/", rawPath).canonicalPath.removePrefix("/")
                                                    } catch (e: Exception) {
                                                        rawPath
                                                    }
                                                    
                                                    if (cleanPath == fItem.relativePath) {
                                                        val dataBytes = editingFileContent.toByteArray(Charsets.UTF_8)
                                                        return android.webkit.WebResourceResponse(
                                                            "text/html",
                                                            "UTF-8",
                                                            java.io.ByteArrayInputStream(dataBytes)
                                                        )
                                                    }
                                                    
                                                    val mimeType = when (cleanPath.substringAfterLast('.', "").lowercase()) {
                                                        "css" -> "text/css"
                                                        "js" -> "application/javascript"
                                                        "json" -> "application/json"
                                                        "png" -> "image/png"
                                                        "jpg", "jpeg" -> "image/jpeg"
                                                        "gif" -> "image/gif"
                                                        "svg" -> "image/svg+xml"
                                                        "html", "htm" -> "text/html"
                                                        else -> "text/plain"
                                                    }
                                                    
                                                    try {
                                                        if (com.example.data.workspace.WorkspaceManager.isUsingSaf(context)) {
                                                            val rootDoc = com.example.data.workspace.WorkspaceManager.getSafRoot(context)
                                                            if (rootDoc != null) {
                                                                val doc = com.example.data.workspace.WorkspaceManager.findDocumentByPath(rootDoc, cleanPath)
                                                                if (doc != null && doc.isFile) {
                                                                    val inStream = context.contentResolver.openInputStream(doc.uri)
                                                                    if (inStream != null) {
                                                                        return android.webkit.WebResourceResponse(mimeType, "UTF-8", inStream)
                                                                    }
                                                                }
                                                            }
                                                        } else {
                                                            val rootFolder = com.example.data.workspace.WorkspaceManager.getWorkspaceRoot(context)
                                                            val file = java.io.File(rootFolder, cleanPath)
                                                            if (file.exists() && file.isFile) {
                                                                return android.webkit.WebResourceResponse(mimeType, "UTF-8", java.io.FileInputStream(file))
                                                            }
                                                        }
                                                    } catch (e: Exception) {
                                                        e.printStackTrace()
                                                    }
                                                }
                                                return super.shouldInterceptRequest(view, request)
                                            }
                                        }
                                    }
                                },
                                update = { webView ->
                                    webView.loadDataWithBaseURL(
                                        "https://app.workspace/${fItem.relativePath}",
                                        editingFileContent,
                                        "text/html",
                                        "UTF-8",
                                        null
                                    )
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else {
                        OutlinedTextField(
                            value = editingFileContent,
                            onValueChange = { editingFileContent = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .testTag("code_editor_textarea"),
                            textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                            maxLines = 1000
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val clipboardManager = LocalClipboardManager.current
                        TextButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(editingFileContent))
                            }
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy full code", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Code")
                        }

                        Row {
                            TextButton(onClick = { selectedFileForEditing = null }) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    com.example.data.workspace.WorkspaceManager.writeFile(context, fItem.relativePath, editingFileContent)
                                    viewModel.triggerWorkspaceRefresh()
                                    selectedFileForEditing = null
                                },
                                modifier = Modifier.testTag("dialog_edit_file_save")
                            ) {
                                Text("Save Changes")
                            }
                        }
                    }
                }
            }
        }

        if (showFullScreenPreview) {
            Dialog(
                onDismissRequest = { showFullScreenPreview = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🌐", fontSize = 20.sp, modifier = Modifier.padding(end = 6.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Full Screen Run / Live Preview",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = fItem.relativePath,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { previewRefreshKey++ },
                                    modifier = Modifier.testTag("full_preview_refresh_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Reload Webview Preview",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = { showFullScreenPreview = false },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(34.dp).testTag("full_preview_close_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Exit fullscreen preview",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Exit Run", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant)

                        androidx.compose.runtime.key(previewRefreshKey) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .background(Color.White)
                            ) {
                                androidx.compose.ui.viewinterop.AndroidView(
                                    factory = { ctx ->
                                        android.webkit.WebView(ctx).apply {
                                            settings.apply {
                                                javaScriptEnabled = true
                                                domStorageEnabled = true
                                                allowFileAccess = true
                                                allowContentAccess = true
                                                builtInZoomControls = true
                                                displayZoomControls = false
                                                useWideViewPort = true
                                                loadWithOverviewMode = true
                                            }
                                            webViewClient = object : android.webkit.WebViewClient() {
                                                override fun shouldInterceptRequest(
                                                    view: android.webkit.WebView?,
                                                    request: android.webkit.WebResourceRequest?
                                                ): android.webkit.WebResourceResponse? {
                                                    val url = request?.url ?: return null
                                                    if (url.host == "app.workspace") {
                                                        val rawPath = url.path?.removePrefix("/") ?: ""
                                                        val cleanPath = try {
                                                            java.io.File("/", rawPath).canonicalPath.removePrefix("/")
                                                        } catch (e: Exception) {
                                                            rawPath
                                                        }

                                                        if (cleanPath == fItem.relativePath) {
                                                            val dataBytes = editingFileContent.toByteArray(Charsets.UTF_8)
                                                            return android.webkit.WebResourceResponse(
                                                                "text/html",
                                                                "UTF-8",
                                                                java.io.ByteArrayInputStream(dataBytes)
                                                            )
                                                        }

                                                        val mimeType = when (cleanPath.substringAfterLast('.', "").lowercase()) {
                                                            "css" -> "text/css"
                                                            "js" -> "application/javascript"
                                                            "json" -> "application/json"
                                                            "png" -> "image/png"
                                                            "jpg", "jpeg" -> "image/jpeg"
                                                            "gif" -> "image/gif"
                                                            "svg" -> "image/svg+xml"
                                                            "html", "htm" -> "text/html"
                                                            else -> "text/plain"
                                                        }

                                                        try {
                                                            if (com.example.data.workspace.WorkspaceManager.isUsingSaf(context)) {
                                                                val rootDoc = com.example.data.workspace.WorkspaceManager.getSafRoot(context)
                                                                if (rootDoc != null) {
                                                                    val doc = com.example.data.workspace.WorkspaceManager.findDocumentByPath(rootDoc, cleanPath)
                                                                    if (doc != null && doc.isFile) {
                                                                        val inStream = context.contentResolver.openInputStream(doc.uri)
                                                                        if (inStream != null) {
                                                                            return android.webkit.WebResourceResponse(mimeType, "UTF-8", inStream)
                                                                        }
                                                                    }
                                                                }
                                                            } else {
                                                                val rootFolder = com.example.data.workspace.WorkspaceManager.getWorkspaceRoot(context)
                                                                val file = java.io.File(rootFolder, cleanPath)
                                                                if (file.exists() && file.isFile) {
                                                                    return android.webkit.WebResourceResponse(mimeType, "UTF-8", java.io.FileInputStream(file))
                                                                }
                                                            }
                                                        } catch (e: Exception) {
                                                            e.printStackTrace()
                                                        }
                                                    }
                                                    return super.shouldInterceptRequest(view, request)
                                                }
                                            }
                                        }
                                    },
                                    update = { webView ->
                                        webView.loadDataWithBaseURL(
                                            "https://app.workspace/${fItem.relativePath}",
                                            editingFileContent,
                                            "text/html",
                                            "UTF-8",
                                            null
                                        )
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WorkspaceItemRow(
    item: com.example.data.workspace.WorkspaceItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("workspace_item_${item.name}"),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (item.isDirectory) Icons.Default.Menu else Icons.Default.Info,
                    contentDescription = if (item.isDirectory) "Directory icon" else "File icon",
                    tint = if (item.isDirectory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!item.isDirectory) {
                        val f = item as com.example.data.workspace.WorkspaceItem.FileItem
                        Text(
                            text = "${f.size} bytes",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showDeleteConfirm) {
                    IconButton(
                        onClick = { showDeleteConfirm = false },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel deletion", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = {
                            onDelete()
                            showDeleteConfirm = false
                        },
                        modifier = Modifier.size(28.dp).testTag("confirm_delete_${item.name}")
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Confirm deletion icon", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                } else {
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.size(32.dp).testTag("delete_${item.name}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Trash delete item",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun shareFile(context: Context, file: java.io.File) {
    try {
        val builder = android.os.StrictMode.VmPolicy.Builder()
        android.os.StrictMode.setVmPolicy(builder.build())

        val uri = android.net.Uri.fromFile(file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Agent Workspace Export")
            putExtra(Intent.EXTRA_TEXT, "Exported workspace files and folders from Gemini AI Agent Client.")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Sharing Workspace ZIP").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
fun DirectoryPickerDialog(
    initialDir: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    var currentFile by remember {
        val path = initialDir.trim()
        val f = if (path.isNotEmpty()) java.io.File(path) else android.os.Environment.getExternalStorageDirectory()
        mutableStateOf(if (f.exists() && f.isDirectory) f else java.io.File("/"))
    }

    var directoryList by remember(currentFile) {
        val list = try {
            currentFile.listFiles { file -> file.isDirectory && !file.name.startsWith(".") }?.toList() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        mutableStateOf(list.sortedBy { it.name.lowercase() })
    }

    var createDirName by remember { mutableStateOf("") }
    var showCreateDirInput by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Text(
                    text = "Select Workspace Folder",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Path breadcrumb
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val parent = currentFile.parentFile
                            if (parent != null && parent.exists() && parent.isDirectory) {
                                currentFile = parent
                            }
                        },
                        enabled = currentFile.parentFile != null,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Up a directory",
                            modifier = Modifier.rotate(180f).size(18.dp),
                            tint = if (currentFile.parentFile != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = currentFile.absolutePath,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable List of Subfolders
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    if (directoryList.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No accessible subfolders",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            items(directoryList) { dir ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { currentFile = dir }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("📁", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = dir.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Create folder options
                if (showCreateDirInput) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = createDirName,
                            onValueChange = { createDirName = it },
                            placeholder = { Text("New Folder Name") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (createDirName.trim().isNotEmpty()) {
                                    val newFolder = java.io.File(currentFile, createDirName.trim())
                                    if (!newFolder.exists()) {
                                        newFolder.mkdirs()
                                    }
                                    createDirName = ""
                                    showCreateDirInput = false
                                    // reload directory list
                                    directoryList = try {
                                        currentFile.listFiles { file -> file.isDirectory && !file.name.startsWith(".") }?.toList() ?: emptyList()
                                    } catch (e: Exception) {
                                        emptyList()
                                    }.sortedBy { it.name.lowercase() }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Create folder", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(
                            onClick = { showCreateDirInput = false }
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { showCreateDirInput = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Create Subfolder Here", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Select and Cancel Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSelect(currentFile.absolutePath) },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Select this Folder")
                    }
                }
            }
        }
    }
}
