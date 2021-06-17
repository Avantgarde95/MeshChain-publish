import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    java
    kotlin("jvm") version "1.3.61"
    id("org.web3j") version "4.5.0"
    id("com.moowork.node") version "1.3.1"
}

group = "com.github.avantgarde95.bc3d"

repositories {
    jcenter()
    mavenCentral()
    maven { setUrl("https://jitpack.io") }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.web3j:core:4.5.12")
    implementation("com.beust:klaxon:5.2")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("org.apache.xmlrpc:xmlrpc-common:3.1.3")
    implementation("org.apache.xmlrpc:xmlrpc-client:3.1.3")
    implementation("org.apache.xmlrpc:xmlrpc-server:3.1.3")
    implementation("com.github.kittinunf.fuel:fuel:2.3.0")
    implementation("com.github.kittinunf.fuel:fuel-coroutines:2.3.0")
    implementation("com.github.kotlin-graphics.glm:glm:v0.9.9.0-build-13")
    implementation("com.github.danny02:JOpenCTM:1.5.2")
    implementation("com.formdev:flatlaf:0.41")
    implementation("com.formdev:flatlaf-intellij-themes:0.41")

    arrayOf(
            "",
            ":natives-windows-amd64",
            ":natives-linux-amd64"
    ).forEach { platform ->
        implementation("org.jogamp.gluegen:gluegen-rt:2.3.2$platform")
        implementation("org.jogamp.jogl:jogl-all:2.3.2$platform")
    }

    arrayOf(
            "",
            "-glfw",
            "-jemalloc",
            "-openal",
            "-opengl",
            "-stb"
    ).forEach { type ->
        arrayOf("", ":natives-windows", ":natives-linux").forEach { platform ->
            implementation("org.lwjgl:lwjgl$type:3.2.3$platform")
        }
    }
}

sourceSets["main"].withConvention(KotlinSourceSet::class) {
    kotlin.srcDir("${web3j.generatedFilesBaseDir}/main/java")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClassName = "$group.MainKt"
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Main-Class"] = application.mainClassName
    }

    from(configurations.runtimeClasspath.get().map {
        if (it.isDirectory) it else zipTree(it)
    }) {
        exclude("META-INF/*.SF")
        exclude("META-INF/*.DSA")
        exclude("META-INF/*.RSA")
    }
}

val run by tasks.getting(JavaExec::class) {
    standardInput = System.`in`
}

solidity {
    val os = OperatingSystem.current()

    val path = when {
        os.isWindows -> "win"
        os.isMacOsX -> "mac"
        else -> "linux"
    }

    executable = "./solc/$path/solc/solc"
}

tasks.register("bcRun") {
    dependsOn("run")
}

tasks.register("bcPack") {
    dependsOn("jar")
}

tasks.register("bcInstall") {
    dependsOn("npm_install")
}

tasks.register("bcCompile") {
    dependsOn("generateContractWrappers")
}

tasks.register("bcMigrateGanache") {
    dependsOn("npm_run_migrateGanache")
}

tasks.register("bcMigrateGeth") {
    dependsOn("npm_run_migrateGeth")
}
