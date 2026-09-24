# Send to AI Prompt

An IntelliJ IDEA plugin that adds a **Send to AI Prompt** item to the editor's right-click menu. The item only appears when text is selected.

Choosing it appends the selected text to the JetBrains AI Assistant chat input, wrapped in a Markdown code fence tagged with the file's language:

````markdown
```kotlin
fun hello() = println("Hello")
```
````

- If a chat is open, the selection is added after any text already in the input.
- If no chat is open, a new chat is created.
- The AI Assistant chat window is then opened and focused.

AI Assistant has no public API, so the plugin uses its internal chat classes. All of those calls live in [`ChatBridge.kt`](src/main/kotlin/com/github/fnunezkanut/sendtoai/ChatBridge.kt). If a future AI Assistant release changes them, the plugin falls back to copying the fenced selection to the clipboard, opening the chat window, and showing a notification asking you to paste it.

## Requirements

- **JDK 25.** The build uses `jvmToolchain(25)`, matching the JetBrains Runtime bundled with IntelliJ IDEA 2026.2. [`.sdkmanrc`](.sdkmanrc) pins `java=25.0.4-amzn`; run `sdk env` to switch to it.
- **Gradle 9.7.1.** Provided by the Gradle wrapper (`./gradlew`), so no separate install is needed. It is also pinned in `.sdkmanrc`.
- **IntelliJ IDEA 2026.2.3 (build `262.10968.63`) or newer**, with the JetBrains AI Assistant plugin installed and signed in.

## Build

```bash
./gradlew buildPlugin
./gradlew test
```

Unit tests use **JUnit Jupiter 6.1.3** and Kotest assertions (same versions as the lambda4k project), with the `@Tag("unit")` annotation—not JUnit 4. `./gradlew build` runs them through Gradle’s `check` lifecycle (after the formatter on local machines).

This produces the installable plugin at `build/distributions/sendtoai-<version>.zip`. The first build downloads IntelliJ IDEA 2026.2.3 and AI Assistant `262.10968.97` into the Gradle cache, so it takes a while.

To check binary compatibility with 2026.2.3 and newer IDE releases (including EAPs), run the JetBrains Plugin Verifier:

```bash
./gradlew verifyPlugin
```

The first run downloads several IDE builds. Reports are written to `build/reports/pluginVerifier`.

### Linting

Kotlin style is enforced with [ktlint](https://pinterest.github.io/ktlint/latest/) via the [Kotlinter](https://github.com/jeremymailen/kotlinter-gradle) Gradle plugin. Rule overrides live in [`.editorconfig`](.editorconfig) (same ktlint settings as the lambda4k project).

```bash
./gradlew lint        # check only (alias for lintKotlin)
./gradlew format      # auto-format (alias for formatKotlin)
```

On a local machine, `./gradlew build` runs the formatter first, then compile and tests. In CI (`CI` or `JENKINS_URL` set), lint violations fail the build. The `build` task also runs `lintKotlin` through Gradle’s `check` lifecycle.

## Test in IntelliJ

### Sandbox IDE

```bash
./gradlew runIde
```

This starts a separate IntelliJ IDEA with this plugin and AI Assistant installed. Its settings and caches are kept under `.intellijPlatform/sandbox`, so your normal IDE is untouched.

1. Open any project and sign in to AI Assistant.
2. Select some code in an editor, right-click, and choose **Send to AI Prompt**.
3. Check that the AI Assistant chat opens with the selection in a fenced code block.
4. Try both cases: a chat that already has text in its input (the selection should be appended after it), and no chat open (a new chat should be created).

### Your own IDE

1. Build the plugin with `./gradlew buildPlugin`.
2. In IntelliJ IDEA, open **Settings > Plugins**, click the gear icon, and choose **Install Plugin from Disk...**.
3. Select the zip from `build/distributions` and restart the IDE if prompted.

### Troubleshooting

The sandbox IDE's log is at `.intellijPlatform/sandbox/sendtoai/IU-2026.2.3/log_runIde/idea.log` (in your own IDE, use **Help > Show Log in Finder**). If the clipboard fallback was used, the log contains a warning starting with `Could not send selection to AI Assistant chat`, followed by the underlying error.
