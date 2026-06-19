package com.example.ui.chat

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.PRESET_MODELS
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.GenerationConfig
import com.example.data.api.ThinkingConfig
import com.example.data.api.GeminiStreamer
import com.example.data.database.AppDatabase
import com.example.data.database.ChatConversation
import com.example.data.database.ChatMessage
import com.example.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(
    application: Application,
    private val repository: ChatRepository
) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("gemini_prefs", Context.MODE_PRIVATE)

    // Api config states
    private val _apiKey = MutableStateFlow(sharedPrefs.getString("api_key", "") ?: "")
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    private val _modelId = MutableStateFlow(sharedPrefs.getString("model_id", "gemini-2.0-flash-thinking-exp-01-21") ?: "gemini-2.0-flash-thinking-exp-01-21")
    val modelId: StateFlow<String> = _modelId.asStateFlow()

    private val _customModelId = MutableStateFlow(sharedPrefs.getString("custom_model_id", "") ?: "")
    val customModelId: StateFlow<String> = _customModelId.asStateFlow()

    private val _isCustomModelSelected = MutableStateFlow(sharedPrefs.getBoolean("is_custom_model_selected", false))
    val isCustomModelSelected: StateFlow<Boolean> = _isCustomModelSelected.asStateFlow()

    private val _temperature = MutableStateFlow(sharedPrefs.getFloat("temperature", 0.7f))
    val temperature: StateFlow<Float> = _temperature.asStateFlow()

    private val _thinkingMode = MutableStateFlow(sharedPrefs.getString("thinking_mode", "auto") ?: "auto")
    val thinkingMode: StateFlow<String> = _thinkingMode.asStateFlow()

    private val _thinkingBudgetLevel = MutableStateFlow(sharedPrefs.getString("thinking_budget_level", "basic") ?: "basic")
    val thinkingBudgetLevel: StateFlow<String> = _thinkingBudgetLevel.asStateFlow()

    // Workspace refresh triggers
    private val _workspaceTrigger = MutableStateFlow(0)
    val workspaceTrigger: StateFlow<Int> = _workspaceTrigger.asStateFlow()

    fun triggerWorkspaceRefresh() {
        _workspaceTrigger.value++
    }

    // Workspace & Chat Navigation states
    val conversations: StateFlow<List<ChatConversation>> = repository.allConversations
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentConversationId = MutableStateFlow<Long?>(null)
    val currentConversationId: StateFlow<Long?> = _currentConversationId.asStateFlow()

    // Messages flow for chosen conversation
    val currentMessages: StateFlow<List<ChatMessage>> = _currentConversationId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(emptyList())
            } else {
                repository.getMessagesForConversation(id)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Live Streaming states
    private val _isStreaming = MutableStateFlow(false)
    val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()

    private val _currentStreamingThought = MutableStateFlow("")
    val currentStreamingThought: StateFlow<String> = _currentStreamingThought.asStateFlow()

    private val _currentStreamingAnswer = MutableStateFlow("")
    val currentStreamingAnswer: StateFlow<String> = _currentStreamingAnswer.asStateFlow()

    init {
        // Fallback to BuildConfig if sharedPrefs is empty
        if (_apiKey.value.isEmpty() && BuildConfig.GEMINI_API_KEY.isNotEmpty() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY") {
            updateApiKey(BuildConfig.GEMINI_API_KEY)
        }
    }

    // Config setters
    fun updateApiKey(newKey: String) {
        _apiKey.value = newKey
        sharedPrefs.edit().putString("api_key", newKey).apply()
    }

    fun updateModelId(newId: String) {
        _modelId.value = newId
        sharedPrefs.edit().putString("model_id", newId).apply()
    }

    fun updateCustomModelId(newId: String) {
        _customModelId.value = newId
        sharedPrefs.edit().putString("custom_model_id", newId).apply()
    }

    fun setCustomModelSelected(selected: Boolean) {
        _isCustomModelSelected.value = selected
        sharedPrefs.edit().putBoolean("is_custom_model_selected", selected).apply()
    }

    fun updateTemperature(newTemp: Float) {
        _temperature.value = newTemp
        sharedPrefs.edit().putFloat("temperature", newTemp).apply()
    }

    fun updateThinkingMode(newMode: String) {
        _thinkingMode.value = newMode
        sharedPrefs.edit().putString("thinking_mode", newMode).apply()
    }

    fun updateThinkingBudgetLevel(newLevel: String) {
        _thinkingBudgetLevel.value = newLevel
        sharedPrefs.edit().putString("thinking_budget_level", newLevel).apply()
    }

    // Conversation management
    fun selectConversation(id: Long?) {
        _currentConversationId.value = id
        // Cancel any active streaming values
        _currentStreamingThought.value = ""
        _currentStreamingAnswer.value = ""
        _isStreaming.value = false
    }

    fun startNewConversation() {
        selectConversation(null)
    }

    fun deleteConversation(id: Long) {
        viewModelScope.launch {
            repository.deleteConversation(id)
            if (_currentConversationId.value == id) {
                _currentConversationId.value = null
            }
        }
    }

    // Main action: Send message and stream reply from Gemini API using a ReAct-styled multi-turn loop
    fun sendMessage(promptText: String) {
        if (promptText.trim().isEmpty() || _isStreaming.value) return

        val activeModelId = if (_isCustomModelSelected.value) {
            _customModelId.value.trim()
        } else {
            _modelId.value
        }

        viewModelScope.launch {
            // 1. Ensure conversation exists, otherwise generate a new thread
            val convId = _currentConversationId.value ?: run {
                val newConvId = repository.insertConversation(
                    ChatConversation(
                        title = if (promptText.length > 30) promptText.take(30) + "..." else promptText,
                        modelId = activeModelId
                    )
                )
                _currentConversationId.value = newConvId
                newConvId
            }

            // 2. Insert User prompt
            val userMsgId = repository.insertMessage(
                ChatMessage(
                    conversationId = convId,
                    role = "user",
                    text = promptText
                )
            )

            val sanitizedModelName = activeModelId.trim().removePrefix("models/")

            val isThinkingSupportedByModel = if (_isCustomModelSelected.value) {
                false
            } else {
                PRESET_MODELS.find { it.id == activeModelId }?.supportsThinking ?: false
            }

            val shouldSendThinkingConfig = when (_thinkingMode.value) {
                "on" -> true
                "off" -> false
                else -> isThinkingSupportedByModel
            }

            val thinkingTokens = if (shouldSendThinkingConfig) {
                when (_thinkingBudgetLevel.value) {
                    "basic" -> 2048
                    "hard" -> 8192
                    "max" -> 16384
                    "ultra" -> 32768
                    "even_more" -> 65535
                    else -> 2048
                }
            } else {
                null
            }

            val config = GenerationConfig(
                temperature = _temperature.value,
                thinkingConfig = thinkingTokens?.let { ThinkingConfig(thinkingBudget = it) }
            )

            var stepIndex = 0
            val maxSteps = 5
            var continueReActLoop = true

            while (continueReActLoop && stepIndex < maxSteps) {
                stepIndex++

                _isStreaming.value = true
                _currentStreamingThought.value = ""
                _currentStreamingAnswer.value = ""

                var thinkingStartTime = 0L
                var thinkingEndTime = 0L

                // 3. Fetch latest history (updated multi-turn!)
                val history = repository.getMessagesForConversationDirect(convId)

                // Build the contents array for Gemini
                val apiContents = history.filter { !it.isError }.map { msg ->
                    Content(
                        role = if (msg.role == "model") "model" else "user",
                        parts = listOf(com.example.data.api.Part(text = msg.text))
                    )
                }.toMutableList()

                // Safeguard: Ensure we never send an empty contents list to Gemini, and it must end with a user prompt.
                if (apiContents.isEmpty() || apiContents.last().role != "user") {
                    apiContents.add(
                        Content(
                            role = "user",
                            parts = listOf(com.example.data.api.Part(text = promptText))
                        )
                    )
                }

                val treeSummary = com.example.data.workspace.WorkspaceManager.getWorkspaceTreeSummary(getApplication())
                val systemInstructionsWithWorkspace = SYSTEM_INSTRUCTION_WORKSPACE + "\n\n" +
                        "Current files in the active developer workspace:\n" +
                        treeSummary + "\n\n" +
                        "Remember: You are a ReAct-styled Coding Agent. If you need to write or change files, output the XML-like workspace tags. " +
                        "You can perform multiple tasks over multiple turns (e.g., creating files first, seeing the feedback, and modifying other files). " +
                        "When you are completely done with the user's request and no further code changes are needed, write your final response and do NOT output any XML tags. " +
                        "This will end your execution loop."

                val request = GenerateContentRequest(
                    contents = apiContents,
                    generationConfig = config,
                    systemInstruction = Content(
                        parts = listOf(com.example.data.api.Part(text = systemInstructionsWithWorkspace))
                    )
                )

                try {
                    GeminiStreamer.streamContent(
                        model = sanitizedModelName,
                        apiKey = _apiKey.value,
                        requestBody = request,
                        onRetry = {
                            _currentStreamingThought.value = ""
                            _currentStreamingAnswer.value = ""
                            thinkingStartTime = 0L
                            thinkingEndTime = 0L
                        },
                        onChunk = { thoughtChunk, answerChunk ->
                            if (thoughtChunk != null && thoughtChunk.isNotEmpty()) {
                                if (thinkingStartTime == 0L) {
                                    thinkingStartTime = System.currentTimeMillis()
                                }
                                _currentStreamingThought.value += thoughtChunk
                            }
                            if (answerChunk != null && answerChunk.isNotEmpty()) {
                                if (thinkingStartTime != 0L && thinkingEndTime == 0L) {
                                    thinkingEndTime = System.currentTimeMillis()
                                }
                                _currentStreamingAnswer.value += answerChunk
                            }
                        }
                    )

                    // Streaming completed for this turn
                    val finalThought = _currentStreamingThought.value.trim()
                    val finalAnswer = _currentStreamingAnswer.value.trim()

                    if (thinkingStartTime != 0L && thinkingEndTime == 0L) {
                        thinkingEndTime = System.currentTimeMillis()
                    }
                    val durationSeconds = if (thinkingStartTime != 0L && thinkingEndTime >= thinkingStartTime) {
                        val computed = (thinkingEndTime - thinkingStartTime) / 1000L
                        maxOf(1L, computed)
                    } else {
                        null
                    }

                    if (finalAnswer.isNotEmpty() || finalThought.isNotEmpty()) {
                        // Insert model's message in the DB
                        repository.insertMessage(
                            ChatMessage(
                                conversationId = convId,
                                role = "model",
                                text = finalAnswer,
                                thought = if (finalThought.isNotEmpty()) finalThought else null,
                                thinkingDuration = durationSeconds
                            )
                        )

                        // Check for tool execution calls in the model's answer
                        val hasTools = hasWorkspaceTools(finalAnswer)
                        if (hasTools) {
                            // Execute advanced multi-turn tools and collect stdout/feedback
                            val toolOutputsSummary = executeToolsAndCollectResults(getApplication(), finalAnswer)

                            // Insert rich system/action simulation feedback into timeline
                            repository.insertMessage(
                                ChatMessage(
                                    conversationId = convId,
                                    role = "system",
                                    text = toolOutputsSummary
                                )
                            )

                            // Continue ReAct loop as tools were successfully run and output feedback is ready
                            continueReActLoop = true
                        } else {
                            // No workspace tools, ReAct loop complete!
                            continueReActLoop = false
                        }
                    } else {
                        continueReActLoop = false
                    }
                } catch (e: Exception) {
                    repository.insertMessage(
                        ChatMessage(
                            conversationId = convId,
                            role = "model",
                            text = "Connection Failed: ${e.message}",
                            isError = true
                        )
                    )
                    continueReActLoop = false
                } finally {
                    _isStreaming.value = false
                    _currentStreamingThought.value = ""
                    _currentStreamingAnswer.value = ""
                }
            }
        }
    }

    private fun hasWorkspaceTools(text: String): Boolean {
        return text.contains("<workspace", ignoreCase = true)
    }

    class InternalToolCall(
        val type: String,
        val attributes: Map<String, String>,
        val body: String
    )

    private fun parseWorkspaceTools(text: String): List<InternalToolCall> {
        val list = mutableListOf<InternalToolCall>()
        try {
            // Match both standard body forms like <workspace_x attrs>body</workspace_x> and self-closing <workspace_x attrs />
            // Support tag names with optional separators or typos (e.g. <workspace_deletepath ... /> or <workspacedeletepath...>)
            val tagRegex = Regex("""<(workspace[a-zA-Z0-9_-]*)([^>]*)>([\s\S]*?)</\1>|<(workspace[a-zA-Z0-9_-]*)([^>]*)\s*/>""", RegexOption.IGNORE_CASE)
            tagRegex.findAll(text).forEach { match ->
                val isSelfClosing = match.groups[4] != null
                val rawType = if (isSelfClosing) match.groups[4]?.value ?: "" else match.groups[1]?.value ?: ""
                val attrString = if (isSelfClosing) match.groups[5]?.value ?: "" else match.groups[2]?.value ?: ""
                val body = if (isSelfClosing) "" else match.groups[3]?.value ?: ""

                // Normalize action name (remove 'workspace' header, strip separators and format)
                val type = rawType.lowercase()
                    .removePrefix("workspace")
                    .removePrefix("_")
                    .replace("_", "")

                val attrs = mutableMapOf<String, String>()
                val attrRegex = Regex("""(\w+)\s*=\s*(?:"([^"]*)"|'([^']*)')""")
                attrRegex.findAll(attrString).forEach { attrMatch ->
                    val key = attrMatch.groups[1]?.value ?: ""
                    val value = attrMatch.groups[2]?.value ?: attrMatch.groups[3]?.value ?: ""
                    if (key.isNotEmpty()) {
                        attrs[key] = value
                    }
                }

                if (type.isNotEmpty()) {
                    list.add(InternalToolCall(type, attrs, body))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun executeToolsAndCollectResults(context: Context, text: String): String {
        val calls = parseWorkspaceTools(text)
        if (calls.isEmpty()) {
            return "[System Action Result] Verified workspace state"
        }

        val runSummaries = mutableListOf<String>()
        val debugDetails = StringBuilder()

        for (call in calls) {
            val rawType = call.type.lowercase().trim()
            val attrs = call.attributes
            val body = call.body

            // Normalize type back to standard handler strings
            val type = when (rawType) {
                "createfile" -> "create_file"
                "editfile" -> "edit_file"
                "viewfile" -> "view_file"
                "listdir", "list" -> "list_dir"
                "createdirectory", "createdir" -> "create_directory"
                "deletepath", "delete" -> "delete_path"
                "move" -> "move"
                "shellexec", "shell" -> "shell_exec"
                else -> rawType
            }

            when (type) {
                "create_file" -> {
                    val path = attrs["path"] ?: ""
                    if (path.isNotEmpty()) {
                        try {
                            com.example.data.workspace.WorkspaceManager.writeFile(context, path, body)
                            runSummaries.add("Created File: $path")
                            debugDetails.append("• Success: Created file '$path' (${body.length} characters)\n")
                        } catch (e: Exception) {
                            runSummaries.add("Failed Create File: $path")
                            debugDetails.append("• Error: Failed to create file '$path': ${e.message}\n")
                        }
                    } else {
                        debugDetails.append("• Error: Missing 'path' attribute in create_file\n")
                    }
                }
                "edit_file" -> {
                    val path = attrs["path"] ?: ""
                    if (path.isNotEmpty()) {
                        try {
                            val findRegex = Regex("""<find>([\s\S]*?)</find>""")
                            val replaceRegex = Regex("""<replace>([\s\S]*?)</replace>""")
                            val findMatch = findRegex.find(body)
                            val replaceMatch = replaceRegex.find(body)

                            if (findMatch != null && replaceMatch != null) {
                                val target = findMatch.groups[1]?.value ?: ""
                                val replacement = replaceMatch.groups[1]?.value ?: ""
                                val success = com.example.data.workspace.WorkspaceManager.replaceCode(context, path, target, replacement)
                                if (success) {
                                    runSummaries.add("Surgically Modified File: $path")
                                    debugDetails.append("• Success: Surgically modified file '$path' (replaced search-block)\n")
                                } else {
                                    runSummaries.add("Failed Edit Block: $path")
                                    debugDetails.append("• Error: Could not find target code block in '$path' for surgical replace. Verify lines exactly.\n")
                                }
                            } else {
                                // Full overwrite fallback
                                com.example.data.workspace.WorkspaceManager.writeFile(context, path, body)
                                runSummaries.add("Overwrote File: $path")
                                debugDetails.append("• Success: Overwrote file '$path' completely\n")
                            }
                        } catch (e: Exception) {
                            runSummaries.add("Failed Edit File: $path")
                            debugDetails.append("• Error: Failed to edit file '$path': ${e.message}\n")
                        }
                    } else {
                        debugDetails.append("• Error: Missing 'path' attribute in edit_file\n")
                    }
                }
                "view_file" -> {
                    val path = attrs["path"] ?: ""
                    if (path.isNotEmpty()) {
                        try {
                            val content = com.example.data.workspace.WorkspaceManager.readFile(context, path)
                            if (content.isNotEmpty()) {
                                runSummaries.add("Viewed File: $path")
                                debugDetails.append("• Success: View contents of file '$path':\n---START---\n$content\n---END---\n")
                            } else {
                                runSummaries.add("Failed View File: $path (Empty/not found)")
                                debugDetails.append("• Error: File '$path' is empty or doesn't exist.\n")
                            }
                        } catch (e: Exception) {
                            runSummaries.add("Failed View File: $path")
                            debugDetails.append("• Error: Could not read file '$path': ${e.message}\n")
                        }
                    } else {
                        debugDetails.append("• Error: Missing 'path' attribute in view_file\n")
                    }
                }
                "list_dir" -> {
                    val path = attrs["path"] ?: ""
                    try {
                        val items = com.example.data.workspace.WorkspaceManager.listFilesAndFolders(context, path)
                        runSummaries.add("Listed Directory: ${if(path.isEmpty()) "Root" else path}")
                        debugDetails.append("• Success: Listed directory '${if(path.isEmpty()) "Root" else path}':\n")
                        if (items.isEmpty()) {
                            debugDetails.append("  (Directory is empty or doesn't exist)\n")
                        } else {
                            items.forEach { item ->
                                val typeLabel = if (item.isDirectory) "Directory" else "File"
                                debugDetails.append("  - $typeLabel: ${item.name} (${item.relativePath})\n")
                            }
                        }
                    } catch (e: Exception) {
                        runSummaries.add("Failed List Directory: $path")
                        debugDetails.append("• Error listing directory '$path': ${e.message}\n")
                    }
                }
                "create_directory" -> {
                    val path = attrs["path"] ?: ""
                    if (path.isNotEmpty()) {
                        try {
                            com.example.data.workspace.WorkspaceManager.createDirectory(context, path)
                            runSummaries.add("Created Directory: $path")
                            debugDetails.append("• Success: Created directory '$path'\n")
                        } catch (e: Exception) {
                            runSummaries.add("Failed Create Directory: $path")
                            debugDetails.append("• Error building directory '$path': ${e.message}\n")
                        }
                    } else {
                        debugDetails.append("• Error: Missing 'path' attribute in create_directory\n")
                    }
                }
                "delete_path" -> {
                    val path = attrs["path"] ?: ""
                    if (path.isNotEmpty()) {
                        try {
                            val ok = com.example.data.workspace.WorkspaceManager.deletePath(context, path)
                            if (ok) {
                                runSummaries.add("Deleted Path: $path")
                                debugDetails.append("• Success: Deleted path '$path'\n")
                            } else {
                                runSummaries.add("Failed Delete Path: $path (Not found)")
                                debugDetails.append("• Error: Path '$path' does not exist.\n")
                            }
                        } catch (e: Exception) {
                            runSummaries.add("Failed Delete Path: $path")
                            debugDetails.append("• Error deleting path '$path': ${e.message}\n")
                        }
                    } else {
                        debugDetails.append("• Error: Missing 'path' attribute in delete_path\n")
                    }
                }
                "move" -> {
                    val source = attrs["source"] ?: ""
                    val destination = attrs["destination"] ?: ""
                    if (source.isNotEmpty() && destination.isNotEmpty()) {
                        try {
                            val ok = com.example.data.workspace.WorkspaceManager.movePath(context, source, destination)
                            if (ok) {
                                runSummaries.add("Moved Path: $source -> $destination")
                                debugDetails.append("• Success: Moved path from '$source' to '$destination'\n")
                            } else {
                                runSummaries.add("Failed Move Path: $source")
                                debugDetails.append("• Error: Failed to rename/move '$source' to '$destination'\n")
                            }
                        } catch (e: Exception) {
                            runSummaries.add("Failed Move Path: $source")
                            debugDetails.append("• Error moving path: ${e.message}\n")
                        }
                    } else {
                        debugDetails.append("• Error: Missing 'source' or 'destination' in move\n")
                    }
                }
                "shell_exec" -> {
                    val command = attrs["command"] ?: ""
                    if (command.isNotEmpty()) {
                        try {
                            runSummaries.add("Executed Command: $command")
                            val out = runLocalCommand(context, command)
                            debugDetails.append("• Success: Executed terminal command '$command':\n$out\n")
                        } catch (e: Exception) {
                            runSummaries.add("Failed Executing: $command")
                            debugDetails.append("• Error executing command '$command': ${e.message}\n")
                        }
                    } else {
                        debugDetails.append("• Error: Missing 'command' attribute in shell_exec\n")
                    }
                }
            }
        }

        triggerWorkspaceRefresh()

        return "[System Action Result] Executed workspace tasks: \n" +
                runSummaries.joinToString("\n") { "• $it" } +
                "\n\n=== VERIFIED TOOL EXECUTION LOGS AND STDOUT EXPOSED FOR VERIFICATION ===\n" +
                debugDetails.toString()
    }

    private fun runLocalCommand(context: Context, commandLine: String): String {
        val root = com.example.data.workspace.WorkspaceManager.getWorkspaceRoot(context)
        return try {
            val process = Runtime.getRuntime().exec(
                arrayOf("sh", "-c", commandLine),
                null,
                root
            )
            
            // Wait up to 8 seconds to prevent lingering block of streaming loop
            val finished = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                process.waitFor(8, java.util.concurrent.TimeUnit.SECONDS)
            } else {
                process.waitFor()
                true
            }
            
            if (!finished) {
                process.destroy()
                return "Error: Command timed out after 8 seconds"
            }
            
            val stdout = process.inputStream.bufferedReader().use { it.readText() }
            val stderr = process.errorStream.bufferedReader().use { it.readText() }
            val exitCode = process.exitValue()
            
            buildString {
                append("Exit code: $exitCode\n")
                if (stdout.isNotEmpty()) {
                    append("STDOUT:\n$stdout\n")
                }
                if (stderr.isNotEmpty()) {
                    append("STDERR:\n$stderr\n")
                }
                if (stdout.isEmpty() && stderr.isEmpty()) {
                    append("(No output)\n")
                }
            }
        } catch (e: Exception) {
            "Error executing shell command: ${e.message}"
        }
    }
}

private val SYSTEM_INSTRUCTION_WORKSPACE = """
You are an expert, universal AI Coding Agent with access to an active local development workspace directory.
You can view, list, create, edit surgically, delete, move, or run shell / terminal commands inside the workspace.
To interact with the workspace, you MUST output specific XML-like tool calls embedded in your response. You can output multiple tool calls in a single response.

When making code edits, you MUST prioritize surgical search-and-replace edits over full file rewrites. This is extremely critical to save budget, reduce latency, and ensure precision.

Tool Calls format:

1. To view a file's contents (View File):
<workspace_view_file path="relative/path/to/file.ext" />

2. To list files and folders in a directory (List Directory):
<workspace_list_dir path="relative/path/to/folder" /> (can leave path empty or use "" for root directory)

3. To create a new file (Create File):
<workspace_create_file path="relative/path/to/file.ext">
file contents here
</workspace_create_file>

4. To surgically edit parts of an existing file (Surgical Edit - HIGHEST PRIORITY FOR EDITS):
<workspace_edit_file path="relative/path/to/file.ext">
<find>
exact lines of code in the existing file that you want to replace
</find>
<replace>
the updated lines of code to substitute
</replace>
</workspace_edit_file>
(If you need to make a full overwrite format with no find/replace blocks, simply put the full content inside without find and replace tags.)

5. To create a folder (Create Folder):
<workspace_create_directory path="relative/path/to/folder" />

6. To delete a file or folder (Delete Path):
<workspace_delete_path path="relative/path/to/item" />

7. To move or rename a file or folder (Move/Rename):
<workspace_move source="relative/path/src" destination="relative/path/destination" />

8. To execute any system shell command (Shell Exec):
<workspace_shell_exec command="any linux command or script execution" />
Example: <workspace_shell_exec command="python main.py" /> or <workspace_shell_exec command="grep -rn 'todo' ." />

Agent Interaction Guidelines:
- You operate in a live ReAct Loop (ReAct Agent Framework). If you invoke any tools, the system executes them and returns their output (including stdout/stderr/results/file contents) in a system message in the next turn of the loop.
- You can inspect a folder using <workspace_list_dir>, inspect a file's code using <workspace_view_file>, make surgical changes, and then run a command like <workspace_shell_exec command="python script.py"> immediately after to verify compilation, test outputs, or runtime.
- For code development tasks (e.g. Node, Python, C++, Web, scriptings), write robust code and run tests with terminal shell execution.
- Always explain what actions you are taking and why, before/after the tool calls.
- When you are completely done and no further actions are required, write your final response to user WITHOUT any XML tags.
"""

class ChatViewModelFactory(
    private val application: Application,
    private val repository: ChatRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
