package com.github.fnunezkanut.sendtoai

import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("unit")
class SendToAiPromptActionTest {
    @Test
    fun `codeFence - wraps text with language tag`() {
        SendToAiPromptAction.codeFence("fun x() {}", "kotlin") shouldBe
            "```kotlin\nfun x() {}\n```"
    }

    @Test
    fun `codeFence - empty language tag`() {
        SendToAiPromptAction.codeFence("text", "") shouldBe "```\ntext\n```"
    }

    @Test
    fun `codeFence - trims trailing newline on inner text`() {
        SendToAiPromptAction.codeFence("line\n", "kotlin") shouldBe "```kotlin\nline\n```"
    }

    @Test
    fun `codeFence - uses longer fence when selection contains backticks`() {
        SendToAiPromptAction.codeFence("```", "kotlin") shouldBe "````kotlin\n```\n````"
    }

    @Test
    fun `languageTag - uses psi file language id`() {
        val language = mockk<Language>()
        every { language.id } returns "Kotlin"
        val psiFile = mockk<PsiFile>()
        every { psiFile.language } returns language

        SendToAiPromptAction.languageTag(psiFile, null) shouldBe "kotlin"
    }

    @Test
    fun `languageTag - plain text falls back to file extension`() {
        val psiFile = mockk<PsiFile>()
        every { psiFile.language } returns PlainTextLanguage.INSTANCE
        val file = mockk<VirtualFile>()
        every { file.extension } returns "md"

        SendToAiPromptAction.languageTag(psiFile, file) shouldBe "md"
    }

    @Test
    fun `languageTag - invalid extension returns empty string`() {
        val psiFile = mockk<PsiFile>()
        every { psiFile.language } returns PlainTextLanguage.INSTANCE
        val file = mockk<VirtualFile>()
        every { file.extension } returns "weird ext"

        SendToAiPromptAction.languageTag(psiFile, file) shouldBe ""
    }
}