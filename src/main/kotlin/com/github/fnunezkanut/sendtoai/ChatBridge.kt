package com.github.fnunezkanut.sendtoai

import com.intellij.ml.llm.core.chat.session.ChatCreationContext
import com.intellij.ml.llm.core.chat.session.ChatOrigin
import com.intellij.ml.llm.core.chat.session.ChatSession
import com.intellij.ml.llm.core.chat.session.ChatSessionHost
import com.intellij.ml.llm.core.chat.session.ChatSourceAction
import com.intellij.ml.llm.core.chat.session.FocusedChatSessionHost
import com.intellij.ml.llm.core.chat.ui.AIAssistantChatUtil
import com.intellij.openapi.project.Project

/**
 * The only place that touches AI Assistant (`com.intellij.ml.llm`) internals.
 * These are not public API and may change between AI Assistant releases.
 */
internal object ChatBridge {
    suspend fun appendToChatInput(project: Project, text: String) {
        val session = FocusedChatSessionHost.getInstance(project).getFocusedChatSession()
            ?: createSession(project)
        val existing = session.getInputText()
        session.setInputText(appendBlock(existing, text))
        AIAssistantChatUtil.openChat(project, session)
    }

    private suspend fun createSession(project: Project): ChatSession =
        ChatSessionHost.getInstance(project).createChatSession(
            ChatCreationContext(ChatOrigin.AIAssistantTool, ChatSourceAction.NEW_CHAT_FROM_EDITOR)
        )

    fun appendBlock(existing: String, block: String): String = if (existing.isBlank()) block else existing.trimEnd() + "\n\n" + block
}