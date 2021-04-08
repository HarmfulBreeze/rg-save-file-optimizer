plugins {
    application
    id("org.beryx.runtime") version "1.12.2"
}

group = "com.piorrro33"
version = "0.1dev"

application {
    mainClass.set("com.piorrro33.rgsavefileoptimizer.Main")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(16))
    }
}

runtime {
    addOptions("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages")
    modules.addAll("java.base")
    jpackage {
        appVersion = "0.2"
        outputDir = "jpackage/${project.name}-${project.version}"
        imageOptions = listOf("--win-console")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("info.picocli:picocli:4.6.1")
    annotationProcessor("info.picocli:picocli-codegen:4.6.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Aproject=${project.group}/${project.name}")
    options.release.set(16)
    // Hack for Java 16 support
    options.isIncremental = false
//    options.forkOptions.jvmArgs?.addAll(listOf("--add-opens", "jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}