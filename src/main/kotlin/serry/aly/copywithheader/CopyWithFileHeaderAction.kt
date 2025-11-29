package serry.aly.copywithheader

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ide.CopyPasteManager
import java.awt.datatransfer.StringSelection
import com.intellij.openapi.actionSystem.ActionUpdateThread

private const val NOTIFICATION_GROUP_ID = "Custom Copy Notifications"
/**
 * An action that copies the entire file content wrapped with an XML/Markdown header
 * containing the relative file path and extension, replicating a custom VS Code multi-command.
 */
class CopyWithFileHeaderAction : AnAction() {


    override fun getActionUpdateThread(): ActionUpdateThread {
        // UI\-related enable/visibility checks should run on EDT
        return ActionUpdateThread.EDT
    }

    override fun actionPerformed(e: AnActionEvent) {
        // Essential checks to ensure we have a project, editor, and file
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        val document = editor.document

        // --- 1. Get Variables ---
        val projectPath = project.basePath ?: ""
        val filePath = virtualFile.path

        // Calculate the relative path, handling both forward and backward slashes (Windows/Unix compatibility)
        // We remove the project path prefix and any subsequent separator.
        val relativePath = filePath
            .removePrefix(projectPath)
            .removePrefix("/")
            .removePrefix("\\")

        // Get the file extension and the entire content
        val fileExt = virtualFile.extension
        val fileContent = document.text

        // --- 2. Generate the Output String (The Header + Content) ---
        // Uses Kotlin's multiline string literal for easy formatting.
        fun xmlEscape(s: String): String =
            s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")

        val safePath = xmlEscape(relativePath)

        val wrappedContent = buildString {
            append("<file path=\"")
            append(safePath)
            append("\">\n")
            append("```")
            append(fileExt)
            append("\n")
            append(fileContent)
            append("\n```") // closing fenced code block
            append("\n</file>")
        }

        // --- 3. Write Directly to Clipboard ---
        // This is fast and avoids any temporary editor changes or undo operations.
        val transferable = StringSelection(wrappedContent)
        CopyPasteManager.getInstance().setContents(transferable)

        // --- 4. Show a Success Notification ---
        // This provides user feedback that the operation succeeded.
        NotificationGroupManager.getInstance()
            .getNotificationGroup(NOTIFICATION_GROUP_ID) // Use a unique group ID
            .createNotification("File content and header copied!", NotificationType.INFORMATION)
            .notify(project)
    }

    /**
     * Updates the action's visibility. It should only be active when an editor is focused.
     */
    override fun update(e: AnActionEvent) {
        // Enable the action only if a project and an editor are active
        val project = e.project
        val editor = e.getData(CommonDataKeys.EDITOR)
        e.presentation.isEnabledAndVisible = project != null && editor != null
    }
}