## Project Overview

- **Send to AI Prompt** is an IntelliJ IDEA plugin (`com.github.fnunezkanut.sendtoai`).
- It adds a right-click editor action that appends the selected text to the JetBrains AI Assistant chat input, wrapped in a Markdown code fence tagged with the file language.
- JetBrains AI Assistant has no public chat-input API. All calls into `com.intellij.ml.llm` live in [`ChatBridge.kt`](src/main/kotlin/com/github/fnunezkanut/sendtoai/ChatBridge.kt) so a future AI Assistant release only requires updating that file.
- Target IDE: IntelliJ IDEA **2026.2.3+** (`sinceBuild` `262.10968.63`). The plugin depends on `com.intellij.ml.llm`.

## Coding Standards

### Kotlin Conventions
- Keep to the code style specified in `.editorconfig` and follow the [official Kotlin style guide](https://kotlinlang.org/docs/coding-conventions.html)
- Use idiomatic Kotlin 2.4 and above
- Prefer `val` over `var` when possible
- Use data classes for immutable data structures
- Avoid single expression functions
- Use named parameters in function calls having 2 or more arguments
- Where possible use American English
- When refactoring never ever delete comments as they might be relevant

### Naming Conventions
- Package names: `com.github.fnunezkanut.sendtoai`
- Class names: PascalCase
- Function names: camelCase
- Constants: UPPER_SNAKE_CASE
- File names: PascalCase for classes, PascalCase for enum entries, camelCase for functions

### Code Organization
- Keep functions small and focused
- Do not use star imports

### Logging
- Use IntelliJ `com.intellij.openapi.diagnostic.Logger` / `thisLogger()` for plugin code
- Prefer `warn` with a throwable when catching unexpected failures (see fallback in `SendToAiPromptAction`)

## Build and Development

### Version Management
- Single-module project built with Gradle Kotlin DSL
- Update `version` in root [`build.gradle.kts`](build.gradle.kts) when releasing
- Read [`.sdkmanrc`](.sdkmanrc) for JDK **25** and Gradle **9.7.1** (`sdk env`)

### Common Tasks
- `./gradlew build`: Compile, lint (`check`), and unit tests; on local machines runs `formatKotlin` first
- `./gradlew buildPlugin`: Produce installable ZIP under `build/distributions/`
- `./gradlew verifyPlugin`: JetBrains Plugin Verifier (downloads IDE builds on first run)
- `./gradlew runIde`: Sandbox IDE with this plugin and AI Assistant
- `./gradlew test`: Unit tests only
- `./gradlew lint` / `./gradlew format`: Kotlinter (ktlint **1.8.0**)
- `./gradlew clean build`: Clean build

### Dependency Management
- Standard Gradle plus IntelliJ Platform Gradle Plugin **2.19.0**
- `intellijIdea("2026.2.3")` and AI Assistant plugin `262.10968.97` for compile and sandbox runs
- Resolve version conflicts explicitly when adding dependencies

### Dependency Source and Inspection
- When inspecting a dependency (`com.example:library:1.0.0`):
  1. Attach sources in the IDE if missing
  2. Confirm `library-1.0.0-sources.jar` under `~/.gradle/caches` (or `~/.m2/repository`)
  3. Prefer reading sources over decompiled bytecode
- If sources are missing, run `./gradlew dependencies`
- When examining a JAR, use a temp path under `build/`, not the project root

## Testing Guidelines

### Test Structure
- Test files: `{ClassName}Test.kt`
- Test package: `com.github.fnunezkanut.sendtoai`
- Use JUnit Jupiter **6.1.3** with Kotest assertions **6.2.5** (same versions as lambda4k)
- Mock external dependencies with MockK
- Use descriptive test names
- Unit tests usually follow Given-When-Then
- Unit tests use JUnit `@Tag("unit")`
- Group many assertions on the same object into Kotest `assertSoftly` blocks
- Avoid AssertJ; use Kotest assertions
- Avoid mocking data classes when a real instance is simple enough
- Group related tests with JUnit `@Nested` where it helps readability

### Test Example
```kotlin
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("unit")
class ExampleTest {
    @Test
    fun `helper - should behave`() {
        // GIVEN
        val input = "test"

        // WHEN
        val result = helper(input)

        // THEN
        result shouldBe expectedValue
    }
}
```

Pure helper logic (`codeFence`, `languageTag`, `appendBlock`) belongs in unit tests. Action/coroutine flows and `ChatBridge.appendToChatInput` need platform fixtures or heavy mocking; keep those for later integration tests tagged `@Tag("integration")` if added.

## Error Handling

- User-visible issues: balloon notifications (group **Send to AI Prompt** in [`plugin.xml`](src/main/resources/META-INF/plugin.xml))
- When AI Assistant chat integration fails: log a warning and run the clipboard + open-chat fallback in [`SendToAiPromptAction.kt`](src/main/kotlin/com/github/fnunezkanut/sendtoai/SendToAiPromptAction.kt)

## Troubleshooting

- **Lint failures:** run `./gradlew format`, then `./gradlew lint`
- **Test failures:** check MockK stubs and IntelliJ API types on the test compile classpath
- **Sandbox / runIde:** logs under `.intellijPlatform/sandbox/sendtoai/IU-2026.2.3/log_runIde/idea.log`
- **Plugin Verifier:** reports in `build/reports/pluginVerifier`

## Git Workflow

- Use feature branches and descriptive commit messages
- Do not commit secrets, sandbox caches (`.intellijPlatform/`), or local IDE config you did not intend to share

