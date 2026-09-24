plugins {
    kotlin("jvm") version "2.4.20"
    id("org.jetbrains.intellij.platform") version "2.19.0"
    id("org.jmailen.kotlinter") version "5.7.0"
}

group = "com.github.fnunezkanut"
version = "0.1.0"

val isLocalBuild = providers.environmentVariable("CI").orNull.isNullOrEmpty()
    && providers.environmentVariable("JENKINS_URL").orNull.isNullOrEmpty()

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdea("2026.2.3")
        plugin("com.intellij.ml.llm", "262.10968.97")
    }
    testImplementation("io.kotest:kotest-assertions-core-jvm:6.2.5")
    testImplementation("io.mockk:mockk:1.14.11")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:6.1.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.1.3")
}

kotlin {
    jvmToolchain(25)
}

intellijPlatform {
    pluginConfiguration {
        id = "com.github.fnunezkanut.sendtoai"
        name = "Send to AI Prompt"
        ideaVersion {
            sinceBuild = "262.10968.63"
            untilBuild = provider { null }
        }
    }
    pluginVerification {
        ides {
            recommended()
        }
    }
}

tasks.test {
    jvmArgs("-Xshare:off", "-XX:+EnableDynamicAgentLoading")
    useJUnitPlatform {
        includeTags("unit")
        excludeTags("integration")
    }
    maxParallelForks = (Runtime.getRuntime().availableProcessors() - 1).coerceAtLeast(1)
    systemProperties = mapOf(
        "junit.jupiter.execution.parallel.enabled" to "true",
        "junit.jupiter.execution.parallel.mode.default" to "concurrent",
    )
}

kotlinter {
    ktlintVersion = "1.8.0"
    ignoreLintFailures = isLocalBuild
    ignoreFormatFailures = false
}
if (isLocalBuild) {
    tasks.named("build") {
        dependsOn(tasks.named("formatKotlin"))
    }
}

// TODO cleanup once 2.4.20+ is supported in kotlinter plugin https://github.com/jeremymailen/kotlinter-gradle/issues/562
configurations.named("ktlint") {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin" && requested.name == "kotlin-compiler-embeddable") {
            useVersion("2.4.10")
        }
    }
}

tasks.register("lint") { dependsOn("lintKotlin") }
tasks.register("format") { dependsOn("formatKotlin") }
