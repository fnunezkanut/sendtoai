package com.github.fnunezkanut.sendtoai

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("unit")
class ChatBridgeTest {
    @Test
    fun `appendBlock - blank existing returns block only`() {
        ChatBridge.appendBlock("", "```kotlin\nx\n```") shouldBe "```kotlin\nx\n```"
    }

    @Test
    fun `appendBlock - non-blank existing appends with separator and trims trailing whitespace`() {
        ChatBridge.appendBlock("hello  \n", "block") shouldBe "hello\n\nblock"
    }
}