import com.palantir.gradle.graal.NativeImageTask

plugins {
    application
    id("com.palantir.graal") version "0.7.2"
}

group = "com.piorrro33"
version = "1.0"

application {
    mainClass.set("com.piorrro33.rgsavefileoptimizer.Main")
}

tasks.withType<NativeImageTask>().configureEach {
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(11))
        }
    }
}

val vsVarsPath: String? by project
graal {
    graalVersion("21.0.0.2")
    javaVersion("11")
    outputName(project.name)
    mainClass(application.mainClass.get())
    if (vsVarsPath != null) windowsVsVarsPath(vsVarsPath)
    option("--static")
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
    options.release.set(11)
    // Hack for JDK 16 support
    options.isIncremental = false
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}