package com.example.data.workspace

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

sealed class WorkspaceItem {
    abstract val name: String
    abstract val relativePath: String
    abstract val isDirectory: Boolean

    data class FileItem(
        override val name: String,
        override val relativePath: String,
        val size: Long
    ) : WorkspaceItem() {
        override val isDirectory: Boolean = false
    }

    data class DirectoryItem(
        override val name: String,
        override val relativePath: String
    ) : WorkspaceItem() {
        override val isDirectory: Boolean = true
    }
}

object WorkspaceManager {
    fun getWorkspaceRoot(context: Context): File {
        val prefs = context.getSharedPreferences("workspace_prefs", Context.MODE_PRIVATE)
        val customPath = prefs.getString("custom_root_path", null)
        val dir = if (!customPath.isNullOrEmpty()) {
            File(customPath)
        } else {
            File(context.filesDir, "agent_workspace")
        }
        if (!dir.exists()) {
            dir.mkdirs()
            try {
                if (customPath.isNullOrEmpty() && !isUsingSaf(context)) {
                    // Seed introductory welcome files
                    File(dir, "README.md").writeText(
                        "# Welcome to your Agent Workspace!\n\n" +
                        "This workspace is a fully functional workspace inside your app's persistent sandbox.\n\n" +
                        "### What you can do here:\n" +
                        "1. **Chat & Delegate**: Ask Gemini in the chat to create, write, edit, or delete folder hierarchies and code files. Watching the tool calls trigger in real time is supported!\n" +
                        "2. **Open & Edit**: Click on files in this workspace to open them in an interactive text editor window, edit, and click save.\n" +
                        "3. **Organize**: Create folders or delete unnecessary items directly from the files list.\n" +
                        "4. **Share**: Upload text files or export the entire workspace as a downloadable ZIP package!\n\n" +
                        "### Example Workspace Structure:\n" +
                        "- `greet_agent.py` - Check out the python script we spawned for you."
                    )
                    File(dir, "greet_agent.py").writeText(
                        "def greet(name):\n" +
                        "    print(f\"Hello {name}! Ready to build some amazing offline code?\")\n\n" +
                        "greet(\"Developer Android\")"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return dir
    }

    fun isUsingSaf(context: Context): Boolean {
        val prefs = context.getSharedPreferences("workspace_prefs", Context.MODE_PRIVATE)
        return !prefs.getString("custom_root_uri", null).isNullOrEmpty()
    }

    fun getWorkspaceRootLabel(context: Context): String {
        val prefs = context.getSharedPreferences("workspace_prefs", Context.MODE_PRIVATE)
        val customUri = prefs.getString("custom_root_uri", null)
        if (!customUri.isNullOrEmpty()) {
            return try {
                val doc = DocumentFile.fromTreeUri(context, Uri.parse(customUri))
                doc?.name ?: "External Folder"
            } catch (e: Exception) {
                "External Directory"
            }
        }
        val customPath = prefs.getString("custom_root_path", null)
        if (!customPath.isNullOrEmpty()) {
            return customPath
        }
        return "App Sandbox"
    }

    fun setCustomWorkspaceRoot(context: Context, path: String) {
        val prefs = context.getSharedPreferences("workspace_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("custom_root_path", path)
            .remove("custom_root_uri")
            .apply()
        val dir = File(path)
        if (!dir.exists()) {
            dir.mkdirs()
        }
    }

    fun setCustomWorkspaceUri(context: Context, uri: String) {
        val prefs = context.getSharedPreferences("workspace_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("custom_root_uri", uri)
            .remove("custom_root_path")
            .apply()
    }

    fun resetWorkspaceRoot(context: Context) {
        val prefs = context.getSharedPreferences("workspace_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .remove("custom_root_path")
            .remove("custom_root_uri")
            .apply()
    }

    fun getSafRoot(context: Context): DocumentFile? {
        val prefs = context.getSharedPreferences("workspace_prefs", Context.MODE_PRIVATE)
        val uriString = prefs.getString("custom_root_uri", null) ?: return null
        return try {
            val uri = Uri.parse(uriString)
            DocumentFile.fromTreeUri(context, uri)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun findDocumentByPath(root: DocumentFile, relativePath: String): DocumentFile? {
        if (relativePath.isEmpty()) return root
        var current = root
        val segments = relativePath.split("/").filter { it.isNotEmpty() }
        for (segment in segments) {
            current = current.findFile(segment) ?: return null
        }
        return current
    }

    fun findOrCreateDocumentByPath(root: DocumentFile, relativePath: String, createAsDir: Boolean = false): DocumentFile? {
        val segments = relativePath.split("/").filter { it.isNotEmpty() }
        if (segments.isEmpty()) return root
        var current = root
        for (i in 0 until segments.size - 1) {
            val segment = segments[i]
            var next = current.findFile(segment)
            if (next == null || !next.isDirectory) {
                next = current.createDirectory(segment) ?: return null
            }
            current = next
        }
        val lastName = segments.last()
        var lastFile = current.findFile(lastName)
        if (lastFile == null) {
            lastFile = if (createAsDir) {
                current.createDirectory(lastName)
            } else {
                val mimeType = getMimeType(lastName)
                current.createFile(mimeType, lastName)
            }
        }
        return lastFile
    }

    private fun getMimeType(fileName: String): String {
        val ext = fileName.substringAfterLast('.', "")
        return when (ext.lowercase()) {
            "html", "htm" -> "text/html"
            "css" -> "text/css"
            "js" -> "application/javascript"
            "json" -> "application/json"
            "md" -> "text/markdown"
            "txt" -> "text/plain"
            "py" -> "text/x-python"
            "kt", "kts" -> "text/plain"
            else -> "application/octet-stream"
        }
    }

    fun readDocumentText(context: Context, doc: DocumentFile): String {
        return try {
            context.contentResolver.openInputStream(doc.uri)?.use { inputStream ->
                inputStream.bufferedReader().use { it.readText() }
            } ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun writeDocumentText(context: Context, doc: DocumentFile, content: String) {
        try {
            context.contentResolver.openOutputStream(doc.uri, "rwt")?.use { outputStream ->
                outputStream.bufferedWriter().use { it.write(content) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun listFilesAndFolders(context: Context, relativePath: String = ""): List<WorkspaceItem> {
        if (isUsingSaf(context)) {
            val root = getSafRoot(context) ?: return emptyList()
            val targetDir = if (relativePath.isEmpty()) root else findDocumentByPath(root, relativePath)
            if (targetDir == null || !targetDir.isDirectory) return emptyList()

            return (targetDir.listFiles()).map { file ->
                val fName = file.name ?: ""
                val relPath = if (relativePath.isEmpty()) fName else "$relativePath/$fName"
                if (file.isDirectory) {
                    WorkspaceItem.DirectoryItem(fName, relPath)
                } else {
                    WorkspaceItem.FileItem(fName, relPath, file.length())
                }
            }.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
        } else {
            val root = getWorkspaceRoot(context)
            val targetDir = if (relativePath.isEmpty()) root else File(root, relativePath)
            if (!targetDir.exists() || !targetDir.isDirectory) return emptyList()

            return (targetDir.listFiles() ?: emptyArray()).map { file ->
                val relPath = file.relativeTo(root).path
                if (file.isDirectory) {
                    WorkspaceItem.DirectoryItem(file.name, relPath)
                } else {
                    WorkspaceItem.FileItem(file.name, relPath, file.length())
                }
            }.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
        }
    }

    fun getWorkspaceTreeSummary(context: Context): String {
        if (isUsingSaf(context)) {
            val root = getSafRoot(context) ?: return "Empty workspace"
            val sb = StringBuilder()
            walkDocumentTree(root, "", sb)
            return if (sb.isEmpty()) "Empty workspace" else sb.toString()
        } else {
            val root = getWorkspaceRoot(context)
            val sb = StringBuilder()
            root.walkTopDown().forEach { file ->
                if (file == root) return@forEach
                val relPath = file.relativeTo(root).path
                val indent = "  ".repeat(relPath.count { it == '/' })
                if (file.isDirectory) {
                    sb.append("$indent- Directory: $relPath/\n")
                } else {
                    sb.append("$indent- File: $relPath (${file.length()} bytes)\n")
                }
            }
            return if (sb.isEmpty()) "Empty workspace" else sb.toString()
        }
    }

    private fun walkDocumentTree(doc: DocumentFile, currentRelativePath: String, sb: StringBuilder) {
        for (file in doc.listFiles()) {
            val name = file.name ?: continue
            val relPath = if (currentRelativePath.isEmpty()) name else "$currentRelativePath/$name"
            val indent = "  ".repeat(relPath.count { it == '/' })
            if (file.isDirectory) {
                sb.append("$indent- Directory: $relPath/\n")
                walkDocumentTree(file, relPath, sb)
            } else {
                sb.append("$indent- File: $relPath (${file.length()} bytes)\n")
            }
        }
    }

    fun readFile(context: Context, relativePath: String): String {
        if (isUsingSaf(context)) {
            val root = getSafRoot(context) ?: return ""
            val doc = findDocumentByPath(root, relativePath)
            if (doc != null && doc.isFile) {
                return readDocumentText(context, doc)
            }
            return ""
        } else {
            val file = File(getWorkspaceRoot(context), relativePath)
            return if (file.exists() && file.isFile) {
                try {
                    file.readText()
                } catch (e: Exception) {
                    "Error reading file: ${e.message}"
                }
            } else {
                ""
            }
        }
    }

    fun writeFile(context: Context, relativePath: String, content: String) {
        if (isUsingSaf(context)) {
            val root = getSafRoot(context) ?: return
            val doc = findOrCreateDocumentByPath(root, relativePath, createAsDir = false)
            if (doc != null) {
                writeDocumentText(context, doc, content)
            }
        } else {
            val root = getWorkspaceRoot(context)
            val file = File(root, relativePath)
            try {
                file.parentFile?.mkdirs()
                file.writeText(content)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun replaceCode(context: Context, relativePath: String, target: String, replacement: String): Boolean {
        if (isUsingSaf(context)) {
            val root = getSafRoot(context) ?: return false
            val doc = findDocumentByPath(root, relativePath)
            if (doc == null || !doc.isFile) return false
            return try {
                val content = readDocumentText(context, doc)
                if (content.contains(target)) {
                    val updated = content.replace(target, replacement)
                    writeDocumentText(context, doc, updated)
                    true
                } else {
                    val targetNormal = target.trim().replace("\r\n", "\n")
                    val contentNormal = content.replace("\r\n", "\n")
                    if (contentNormal.contains(targetNormal)) {
                        val updatedNormal = contentNormal.replace(targetNormal, replacement.replace("\r\n", "\n"))
                        writeDocumentText(context, doc, updatedNormal)
                        true
                    } else {
                        false
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        } else {
            val root = getWorkspaceRoot(context)
            val file = File(root, relativePath)
            if (!file.exists() || !file.isFile) return false
            return try {
                val content = file.readText()
                if (content.contains(target)) {
                    val updated = content.replace(target, replacement)
                    file.writeText(updated)
                    true
                } else {
                    val targetNormal = target.trim().replace("\r\n", "\n")
                    val contentNormal = content.replace("\r\n", "\n")
                    if (contentNormal.contains(targetNormal)) {
                        val updatedNormal = contentNormal.replace(targetNormal, replacement.replace("\r\n", "\n"))
                        file.writeText(updatedNormal)
                        true
                    } else {
                        false
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    fun createDirectory(context: Context, relativePath: String) {
        if (isUsingSaf(context)) {
            val root = getSafRoot(context) ?: return
            findOrCreateDocumentByPath(root, relativePath, createAsDir = true)
        } else {
            val root = getWorkspaceRoot(context)
            val dir = File(root, relativePath)
            try {
                dir.mkdirs()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deletePath(context: Context, relativePath: String): Boolean {
        if (isUsingSaf(context)) {
            val root = getSafRoot(context) ?: return false
            val doc = findDocumentByPath(root, relativePath)
            if (doc != null && doc.exists()) {
                return try {
                    doc.delete()
                } catch (e: Exception) {
                    false
                }
            }
            return false
        } else {
            val root = getWorkspaceRoot(context)
            val file = File(root, relativePath)
            if (file.exists()) {
                return try {
                    file.deleteRecursively()
                } catch (e: Exception) {
                    false
                }
            }
            return false
        }
    }

    fun movePath(context: Context, source: String, destination: String): Boolean {
        if (isUsingSaf(context)) {
            val root = getSafRoot(context) ?: return false
            val srcDoc = findDocumentByPath(root, source) ?: return false
            if (!srcDoc.exists()) return false

            return try {
                if (srcDoc.isDirectory) {
                    val success = copyDirectorySaf(context, root, srcDoc, destination)
                    if (success) {
                        srcDoc.delete()
                        true
                    } else {
                        false
                    }
                } else {
                    val destDoc = findOrCreateDocumentByPath(root, destination, createAsDir = false) ?: return false
                    val content = readDocumentText(context, srcDoc)
                    writeDocumentText(context, destDoc, content)
                    srcDoc.delete()
                    true
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        } else {
            val root = getWorkspaceRoot(context)
            val srcFile = File(root, source)
            val destFile = File(root, destination)
            if (srcFile.exists()) {
                destFile.parentFile?.mkdirs()
                return srcFile.renameTo(destFile)
            }
            return false
        }
    }

    private fun copyDirectorySaf(context: Context, root: DocumentFile, srcDir: DocumentFile, destRelativePath: String): Boolean {
        val destDir = findOrCreateDocumentByPath(root, destRelativePath, createAsDir = true) ?: return false
        for (item in srcDir.listFiles()) {
            val itemName = item.name ?: continue
            val itemDestPath = "$destRelativePath/$itemName"
            if (item.isDirectory) {
                if (!copyDirectorySaf(context, root, item, itemDestPath)) return false
            } else {
                val targetDoc = findOrCreateDocumentByPath(root, itemDestPath, createAsDir = false) ?: return false
                val content = readDocumentText(context, item)
                writeDocumentText(context, targetDoc, content)
            }
        }
        return true
    }

    fun exportToZip(context: Context): File? {
        val zipFile = File(context.cacheDir, "workspace_export.zip")
        if (zipFile.exists()) {
            zipFile.delete()
        }

        try {
            ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                if (isUsingSaf(context)) {
                    val root = getSafRoot(context) ?: return null
                    zipDocumentTree(context, root, "", zos)
                } else {
                    val root = getWorkspaceRoot(context)
                    root.walkTopDown().forEach { file ->
                        if (file == root) return@forEach
                        val relativePath = file.relativeTo(root).path
                        val entryName = relativePath + if (file.isDirectory) "/" else ""
                        val zipEntry = ZipEntry(entryName)
                        zos.putNextEntry(zipEntry)
                        if (file.isFile) {
                            FileInputStream(file).use { fis ->
                                fis.copyTo(zos)
                            }
                        }
                        zos.closeEntry()
                    }
                }
            }
            return zipFile
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun zipDocumentTree(context: Context, doc: DocumentFile, currentRelativePath: String, zos: ZipOutputStream) {
        for (file in doc.listFiles()) {
            val name = file.name ?: continue
            val relPath = if (currentRelativePath.isEmpty()) name else "$currentRelativePath/$name"
            val entryName = relPath + if (file.isDirectory) "/" else ""
            val zipEntry = ZipEntry(entryName)
            zos.putNextEntry(zipEntry)
            if (file.isFile) {
                try {
                    context.contentResolver.openInputStream(file.uri)?.use { inputStream ->
                        inputStream.copyTo(zos)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            zos.closeEntry()
            if (file.isDirectory) {
                zipDocumentTree(context, file, relPath, zos)
            }
        }
    }
}
