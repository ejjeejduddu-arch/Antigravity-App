package com.example.gemini.data.agent

import android.content.Context
import android.os.Environment
import android.util.Log
import com.example.gemini.data.local.LocalEnvironmentManager
import com.example.gemini.domain.model.ChatMessage
import com.example.gemini.domain.model.Conversation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap

@Serializable
data class ConversationBackupPayload(
    val conversation: Conversation,
    val messages: List<ChatMessage>,
    val savedAt: Long = System.currentTimeMillis()
)

@Serializable
data class ChatHistoryIndex(
    val version: Int = 1,
    val lastUpdated: Long = System.currentTimeMillis(),
    val conversations: List<Conversation> = emptyList()
)

/**
 * PersistentChatHistoryManager ensures chatting history (conversations, messages,
 * trajectories, brain transcripts, summaries DB) survives APK deletion (uninstall/reinstall)
 * and Android Settings "Clear Data".
 *
 * It stores and mirrors data in public external storage:
 *   /storage/emulated/0/Documents/Antigravity/chat_history/
 * (with automatic fallback to /storage/emulated/0/Download/Antigravity/chat_history/).
 */
object PersistentChatHistoryManager {

    private const val TAG = "PersistentChatHistory"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
        encodeDefaults = true
    }

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val indexMutex = Mutex()
    private val debounceJobs = ConcurrentHashMap<String, Job>()

    private val _isRestoring = MutableStateFlow(false)
    val isRestoring = _isRestoring.asStateFlow()

    private val _lastBackupTime = MutableStateFlow(0L)
    val lastBackupTime = _lastBackupTime.asStateFlow()

    private val _savedCount = MutableStateFlow(0)
    val savedCount = _savedCount.asStateFlow()

    /**
     * Resolves the primary writable persistent storage root directory.
     */
    fun getStorageDir(context: Context? = null): File {
        // 1. Primary: Documents/Antigravity/chat_history
        val docDir = runCatching {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        }.getOrNull() ?: File("/sdcard/Documents")
        val docTarget = File(docDir, "Antigravity/chat_history")
        if (docTarget.exists() || docTarget.mkdirs() || (docTarget.canWrite() && docTarget.isDirectory)) {
            ensureSubdirs(docTarget)
            return docTarget
        }

        // 2. Secondary: Download/Antigravity/chat_history
        val downDir = runCatching {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        }.getOrNull() ?: File("/sdcard/Download")
        val downTarget = File(downDir, "Antigravity/chat_history")
        if (downTarget.exists() || downTarget.mkdirs()) {
            ensureSubdirs(downTarget)
            return downTarget
        }

        // 3. Fallback: /sdcard/Antigravity/chat_history
        val sdcardTarget = File("/sdcard/Antigravity/chat_history")
        sdcardTarget.mkdirs()
        ensureSubdirs(sdcardTarget)
        return sdcardTarget
    }

    private fun ensureSubdirs(root: File) {
        runCatching {
            File(root, "conversations").mkdirs()
            File(root, "brains").mkdirs()
            File(root, "summaries").mkdirs()
        }
    }

    /**
     * Returns candidate directories in search priority order.
     */
    fun getAllCandidateDirs(context: Context? = null): List<File> {
        val list = mutableListOf<File>()
        runCatching {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)?.let {
                list.add(File(it, "Antigravity/chat_history"))
            }
        }
        runCatching {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)?.let {
                list.add(File(it, "Antigravity/chat_history"))
                list.add(File(it, "Antigem/chat_history"))
            }
        }
        list.add(File("/sdcard/Documents/Antigravity/chat_history"))
        list.add(File("/sdcard/Download/Antigravity/chat_history"))
        list.add(File("/sdcard/Download/Antigem/chat_history"))
        list.add(File("/sdcard/Antigravity/chat_history"))
        return list.distinctBy { it.absolutePath }
    }

    /**
     * Finds the best directory containing existing persistent chat history.
     */
    fun findActiveBackupDir(context: Context? = null): File? {
        val candidates = getAllCandidateDirs(context)
        for (candidate in candidates) {
            if (candidate.exists() && candidate.isDirectory) {
                val indexFile = File(candidate, "index.json")
                val convDir = File(candidate, "conversations")
                val brainDir = File(candidate, "brains")
                val summariesDir = File(candidate, "summaries")
                val hasIndex = indexFile.exists() && indexFile.length() > 0
                val hasConvs = convDir.exists() && (convDir.listFiles()?.isNotEmpty() == true)
                val hasBrains = brainDir.exists() && (brainDir.listFiles()?.isNotEmpty() == true)
                val hasSummaries = summariesDir.exists() && (summariesDir.listFiles()?.isNotEmpty() == true)
                if (hasIndex || hasConvs || hasBrains || hasSummaries) {
                    return candidate
                }
            }
        }
        return null
    }

    /**
     * Checks if internal AGY storage is empty (clean install or data cleared) and restores
     * all conversation history, brain transcripts, and SQLite summaries automatically.
     */
    fun checkAndRestoreIfFresh(context: Context): List<Conversation> {
        return try {
            val homeDir = LocalEnvironmentManager.getHomeDir(context)
            val geminiDir = File(homeDir, ".gemini/antigravity-cli")
            val dbFile = File(geminiDir, "conversation_summaries.db")
            val brainDir = File(geminiDir, "brain")

            val internalHasData = (dbFile.exists() && dbFile.length() > 0) ||
                    (brainDir.exists() && brainDir.listFiles()?.isNotEmpty() == true)

            val backupDir = findActiveBackupDir(context)
            if (backupDir == null) {
                Log.d(TAG, "No external chat backup found to restore.")
                return emptyList()
            }

            // If internal has no data, restore everything!
            if (!internalHasData) {
                Log.i(TAG, "Fresh install or cleared data detected! Restoring chat history from: ${backupDir.absolutePath}")
                _isRestoring.value = true
                try {
                    restoreInternalAgyFiles(geminiDir, backupDir)
                } finally {
                    _isRestoring.value = false
                }
            } else {
                Log.d(TAG, "Internal chat data exists. Synchronizing external chat index if needed.")
            }

            val restored = loadRestoredConversationsFromDir(backupDir)
            _savedCount.value = restored.size
            restored
        } catch (e: Exception) {
            Log.e(TAG, "checkAndRestoreIfFresh failed", e)
            emptyList()
        }
    }

    /**
     * Restores SQLite database, proto summary, and brains into the internal ~/.gemini/antigravity-cli directory.
     */
    private fun restoreInternalAgyFiles(geminiDir: File, backupDir: File) {
        runCatching {
            geminiDir.mkdirs()

            // 1. Restore summaries DB & proto
            val extSummaries = File(backupDir, "summaries")
            if (extSummaries.exists()) {
                val extDb = File(extSummaries, "conversation_summaries.db")
                if (extDb.exists() && extDb.length() > 0) {
                    val destDb = File(geminiDir, "conversation_summaries.db")
                    copyFile(extDb, destDb)
                    Log.i(TAG, "Restored conversation_summaries.db (${destDb.length()} bytes)")
                }

                val extProto = File(extSummaries, "agyhub_summaries_proto.pb")
                if (extProto.exists() && extProto.length() > 0) {
                    val destProto = File(geminiDir, "agyhub_summaries_proto.pb")
                    copyFile(extProto, destProto)
                    Log.i(TAG, "Restored agyhub_summaries_proto.pb (${destProto.length()} bytes)")
                }
            }

            // 2. Restore brains (trajectories & transcripts)
            val extBrains = File(backupDir, "brains")
            val destBrains = File(geminiDir, "brain")
            if (extBrains.exists() && extBrains.isDirectory) {
                destBrains.mkdirs()
                extBrains.listFiles()?.forEach { brainFolder ->
                    if (brainFolder.isDirectory) {
                        val targetFolder = File(destBrains, brainFolder.name)
                        copyDirectory(brainFolder, targetFolder)
                        Log.i(TAG, "Restored brain folder: ${brainFolder.name}")
                    }
                }
            }
        }.onFailure {
            Log.e(TAG, "Failed restoring internal AGY files", it)
        }
    }

    /**
     * Reads all saved conversations from persistent storage.
     */
    fun getRestoredConversations(context: Context? = null): List<Conversation> {
        val dir = findActiveBackupDir(context) ?: getStorageDir(context)
        return loadRestoredConversationsFromDir(dir)
    }

    private fun loadRestoredConversationsFromDir(dir: File): List<Conversation> {
        return try {
            val indexFile = File(dir, "index.json")
            if (indexFile.exists() && indexFile.length() > 0) {
                val content = indexFile.readText()
                val parsedIndex = json.decodeFromString<ChatHistoryIndex>(content)
                if (parsedIndex.conversations.isNotEmpty()) {
                    return parsedIndex.conversations.sortedByDescending { it.updatedAt }
                }
            }

            // Fallback: iterate conversations/*.json
            val convDir = File(dir, "conversations")
            val list = mutableListOf<Conversation>()
            if (convDir.exists() && convDir.isDirectory) {
                convDir.listFiles { f -> f.extension.equals("json", ignoreCase = true) }?.forEach { f ->
                    runCatching {
                        val payload = json.decodeFromString<ConversationBackupPayload>(f.readText())
                        list.add(payload.conversation)
                    }
                }
            }
            list.sortedByDescending { it.updatedAt }
        } catch (e: Exception) {
            Log.e(TAG, "loadRestoredConversationsFromDir failed", e)
            emptyList()
        }
    }

    /**
     * Loads the complete saved messages for a conversation from external persistent storage.
     */
    fun getRestoredMessages(context: Context? = null, conversationId: String): List<ChatMessage> {
        if (conversationId.isBlank()) return emptyList()
        val candidates = getAllCandidateDirs(context)
        for (candidate in candidates) {
            val convFile = File(candidate, "conversations/$conversationId.json")
            if (convFile.exists() && convFile.length() > 0) {
                try {
                    val payload = json.decodeFromString<ConversationBackupPayload>(convFile.readText())
                    if (payload.messages.isNotEmpty()) {
                        return payload.messages
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error decoding $convFile: ${e.message}")
                }
            }
        }
        return emptyList()
    }

    /**
     * Debounced background backup for a single conversation and its messages.
     */
    fun backupConversationDebounced(
        context: Context,
        conversation: Conversation,
        messages: List<ChatMessage>
    ) {
        if (conversation.id.isBlank()) return
        val convId = conversation.id

        debounceJobs[convId]?.cancel()
        debounceJobs[convId] = scope.launch {
            delay(1200) // 1.2s debounce to aggregate rapid streaming chunks
            backupConversationSync(context, conversation, messages)
        }
    }

    /**
     * Synchronous backup of a conversation, its messages, and corresponding internal AGY files.
     */
    fun backupConversationSync(
        context: Context,
        conversation: Conversation,
        messages: List<ChatMessage>
    ) {
        if (conversation.id.isBlank()) return
        try {
            val root = getStorageDir(context)
            val convDir = File(root, "conversations")
            convDir.mkdirs()

            // 1. Write conversation JSON atomically
            val targetFile = File(convDir, "${conversation.id}.json")
            val tmpFile = File(convDir, "${conversation.id}.json.tmp")
            val payload = ConversationBackupPayload(
                conversation = conversation,
                messages = messages,
                savedAt = System.currentTimeMillis()
            )
            val jsonString = json.encodeToString(payload)
            tmpFile.writeText(jsonString)
            if (tmpFile.renameTo(targetFile) || !targetFile.exists()) {
                if (tmpFile.exists()) {
                    tmpFile.copyTo(targetFile, overwrite = true)
                    tmpFile.delete()
                }
            }

            // 2. Mirror brain directory if present
            runCatching {
                val homeDir = LocalEnvironmentManager.getHomeDir(context)
                val geminiBrainDir = File(homeDir, ".gemini/antigravity-cli/brain/${conversation.id}")
                if (geminiBrainDir.exists() && geminiBrainDir.isDirectory) {
                    val extBrainTarget = File(root, "brains/${conversation.id}")
                    copyDirectory(geminiBrainDir, extBrainTarget)
                }
            }

            // 3. Mirror SQLite DB and proto
            runCatching {
                val homeDir = LocalEnvironmentManager.getHomeDir(context)
                val geminiDir = File(homeDir, ".gemini/antigravity-cli")
                val dbFile = File(geminiDir, "conversation_summaries.db")
                if (dbFile.exists() && dbFile.length() > 0) {
                    val extSummaries = File(root, "summaries")
                    extSummaries.mkdirs()
                    copyFile(dbFile, File(extSummaries, "conversation_summaries.db"))
                }
                val protoFile = File(geminiDir, "agyhub_summaries_proto.pb")
                if (protoFile.exists() && protoFile.length() > 0) {
                    val extSummaries = File(root, "summaries")
                    extSummaries.mkdirs()
                    copyFile(protoFile, File(extSummaries, "agyhub_summaries_proto.pb"))
                }
            }

            // 4. Update index.json atomically
            updateIndex(root, conversation)

            _lastBackupTime.value = System.currentTimeMillis()
        } catch (e: Exception) {
            Log.e(TAG, "backupConversationSync failed for ${conversation.id}", e)
        }
    }

    private fun updateIndex(root: File, updatedConv: Conversation) {
        scope.launch {
            indexMutex.withLock {
                try {
                    val indexFile = File(root, "index.json")
                    val existingList = if (indexFile.exists() && indexFile.length() > 0) {
                        runCatching {
                            json.decodeFromString<ChatHistoryIndex>(indexFile.readText()).conversations
                        }.getOrDefault(emptyList())
                    } else {
                        emptyList()
                    }

                    val map = existingList.associateBy { it.id }.toMutableMap()
                    map[updatedConv.id] = updatedConv
                    val updatedList = map.values.sortedByDescending { it.updatedAt }

                    val newIndex = ChatHistoryIndex(
                        version = 1,
                        lastUpdated = System.currentTimeMillis(),
                        conversations = updatedList
                    )
                    val tmpIndex = File(root, "index.json.tmp")
                    tmpIndex.writeText(json.encodeToString(newIndex))
                    if (!tmpIndex.renameTo(indexFile)) {
                        tmpIndex.copyTo(indexFile, overwrite = true)
                        tmpIndex.delete()
                    }
                    _savedCount.value = updatedList.size
                } catch (e: Exception) {
                    Log.e(TAG, "updateIndex failed", e)
                }
            }
        }
    }

    /**
     * Backs up all conversations and current in-memory messages immediately.
     */
    fun backupAllSync(
        context: Context,
        conversations: List<Conversation> = emptyList(),
        currentMessages: List<ChatMessage>? = null,
        currentConvId: String? = null
    ) {
        try {
            val root = getStorageDir(context)
            Log.i(TAG, "Starting full chat history backup to: ${root.absolutePath}")

            // Backup active conversation messages
            if (!currentConvId.isNullOrBlank() && currentMessages != null && currentMessages.isNotEmpty()) {
                val conv = conversations.find { it.id == currentConvId }
                    ?: Conversation(id = currentConvId, title = "Current Chat")
                backupConversationSync(context, conv, currentMessages)
            }

            // Backup all conversation entries
            for (c in conversations) {
                if (c.id == currentConvId && currentMessages != null && currentMessages.isNotEmpty()) {
                    continue
                }
                val existingMsgs = getRestoredMessages(context, c.id)
                backupConversationSync(context, c, existingMsgs)
            }

            // Mirror internal AGY summaries and brains
            val homeDir = LocalEnvironmentManager.getHomeDir(context)
            val geminiDir = File(homeDir, ".gemini/antigravity-cli")
            if (geminiDir.exists()) {
                val dbFile = File(geminiDir, "conversation_summaries.db")
                if (dbFile.exists()) {
                    val extSummaries = File(root, "summaries")
                    extSummaries.mkdirs()
                    copyFile(dbFile, File(extSummaries, "conversation_summaries.db"))
                }
                val brainDir = File(geminiDir, "brain")
                if (brainDir.exists() && brainDir.isDirectory) {
                    val extBrains = File(root, "brains")
                    extBrains.mkdirs()
                    copyDirectory(brainDir, extBrains)
                }
            }

            _lastBackupTime.value = System.currentTimeMillis()
            Log.i(TAG, "Full chat history backup completed successfully!")
        } catch (e: Exception) {
            Log.e(TAG, "backupAllSync failed", e)
        }
    }

    /**
     * Removes a deleted conversation from persistent storage.
     */
    fun deleteConversation(context: Context, conversationId: String) {
        if (conversationId.isBlank()) return
        scope.launch {
            try {
                val candidates = getAllCandidateDirs(context)
                for (dir in candidates) {
                    val f = File(dir, "conversations/$conversationId.json")
                    if (f.exists()) f.delete()
                    val b = File(dir, "brains/$conversationId")
                    if (b.exists()) b.deleteRecursively()

                    val indexFile = File(dir, "index.json")
                    if (indexFile.exists()) {
                        indexMutex.withLock {
                            val currentList = runCatching {
                                json.decodeFromString<ChatHistoryIndex>(indexFile.readText()).conversations
                            }.getOrDefault(emptyList())
                            val filtered = currentList.filter { it.id != conversationId }
                            val newIndex = ChatHistoryIndex(
                                version = 1,
                                lastUpdated = System.currentTimeMillis(),
                                conversations = filtered
                            )
                            indexFile.writeText(json.encodeToString(newIndex))
                            _savedCount.value = filtered.size
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "deleteConversation failed for $conversationId", e)
            }
        }
    }

    private fun copyFile(src: File, dst: File) {
        if (!src.exists()) return
        dst.parentFile?.mkdirs()
        FileInputStream(src).use { input ->
            FileOutputStream(dst).use { output ->
                input.copyTo(output)
            }
        }
    }

    private fun copyDirectory(src: File, dst: File) {
        if (!src.exists()) return
        if (src.isDirectory) {
            if (!dst.exists()) dst.mkdirs()
            src.listFiles()?.forEach { child ->
                copyDirectory(child, File(dst, child.name))
            }
        } else {
            copyFile(src, dst)
        }
    }
}
