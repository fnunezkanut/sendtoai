package com.github.fnunezkanut.sendtoai

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.awt.datatransfer.StringSelection
import kotlin.coroutines.cancellation.CancellationException

class SendToAiPromptAction : DumbAwareAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        e.presentation.isEnabledAndVisible =
            e.project != null && editor != null && editor.selectionModel.hasSelection()
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val selected = editor.selectionModel.selectedText
        if (selected.isNullOrEmpty()) return

        val lang = languageTag(e.getData(CommonDataKeys.PSI_FILE), e.getData(CommonDataKeys.VIRTUAL_FILE))
        val block = codeFence(selected, lang)

        project.service<SendToAiScope>().cs.launch(Dispatchers.EDT) {
            try {
                ChatBridge.appendToChatInput(project, block)
            } catch (ce: CancellationException) {
                throw ce
            } catch (t: Throwable) {
                thisLogger().warn("Could not send selection to AI Assistant chat, falling back to clipboard", t)
                fallback(project, editor, block)
            }
        }
    }

    private fun fallback(project: Project, editor: Editor, block: String) {
        CopyPasteManager.getInstance().setContents(StringSelection(block))
        ActionManager.getInstance().getAction(SHOW_CHAT_ACTION_ID)?.let {
            ActionManager.getInstance().tryToExecute(it, null, editor.contentComponent, ActionPlaces.EDITOR_POPUP, true)
        }
        NotificationGroupManager.getInstance()
            .getNotificationGroup(NOTIFICATION_GROUP_ID)
            .createNotification(
                "Selection copied to clipboard. Paste it into the AI Assistant chat.",
                NotificationType.INFORMATION
            )
            .notify(project)
    }

    companion object {
        private const val SHOW_CHAT_ACTION_ID = "AIAssistant.ToolWindow.ShowOrFocus"
        private const val NOTIFICATION_GROUP_ID = "Send to AI Prompt"
        private val FENCE_TAG = Regex("[a-z0-9_+#.-]+")

        internal fun languageTag(psiFile: PsiFile?, file: VirtualFile?): String {
            val language = psiFile?.language
            if (language != null && language != PlainTextLanguage.INSTANCE) {
                val id = language.id.lowercase()
                if (FENCE_TAG.matches(id)) return id
            }
            val ext = file?.extension?.lowercase().orEmpty()
            return if (FENCE_TAG.matches(ext)) ext else ""
        }

        internal fun codeFence(text: String, lang: String): String {
            val longestRun = Regex("`{3,}").findAll(text).maxOfOrNull { it.value.length } ?: 0
            val fence = "`".repeat(maxOf(3, longestRun + 1))
            return "$fence$lang\n${text.trimEnd('\n')}\n$fence"
        }
    }
}

@Service(Service.Level.PROJECT)
internal class SendToAiScope(
    @Suppress("unused") private val project: Project,
    val cs: CoroutineScope
)