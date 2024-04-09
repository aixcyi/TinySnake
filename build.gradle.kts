plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.21"
    id("org.jetbrains.intellij") version "1.13.3"
}

group = "cn.aixcyi.plugin"
version = "0.1.4"  // 大版本序号.发布序号.编译序号

repositories {
    mavenLocal()
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("PC-2022.2.5")
    plugins.set(listOf("PythonCore"))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("222")
        untilBuild.set("241.*")
        pluginDescription.set(file("DESCRIPTION.html").readText())
    }

    signPlugin {
        certificateChainFile.set(file("./.secret/chain.crt"))
        privateKeyFile.set(file("./.secret/private.pem"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}