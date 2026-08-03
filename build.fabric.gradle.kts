import org.gradle.kotlin.dsl.configureEach

plugins {
    id("build.common")
    id("dev.kikugie.loom-back-compat")
    id("com.gradleup.shadow") version "9.5.1"
}

// DO NOT set group = ...!
version = "${sc.current.version}-${property("mod.version")}"

if (property("dev.snapshot").toString().toBoolean()) {
    version = "$version-SNAPSHOT"
}

base.archivesName = "${property("mod.id")}-fabric"

sourceSets.main {
    resources.exclude("**/.cache")
}

val shadowGroup: String = "${property("mod.group")}.${property("mod.id")}.shadow"

configurations {
    named("implementation") {
        extendsFrom(configurations.shadow.get())
    }
}

val distributionJar: Provider<RegularFile> = when {
    sc.current.parsed < "26.1" -> tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar").flatMap { it.archiveFile }
    else -> tasks.shadowJar.flatMap { it.archiveFile }
}

val requiredJava: JavaVersion = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    sc.current.parsed >= "1.18" -> JavaVersion.VERSION_17
    sc.current.parsed >= "1.17" -> JavaVersion.VERSION_16
    else -> JavaVersion.VERSION_1_8
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava

    toolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}

loom {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json") // Useful for interface injection
    accessWidenerPath = sc.process(
        rootProject.file("src/main/resources/${property("mod.id")}.ct"),
        "build/${property("mod.id")}.ct"
    )

    decompilerOptions.named("vineflower") {
        options.put("mark-corresponding-synthetics", "1") // Adds names to lambdas - useful for mixins
    }

    runConfigs.all {
        preferGradleTask = true
        generateRunConfig = true
        runDirectory = rootProject.file("run") // Shares the run directory between versions
        jvmArguments.add("-Dmixin.debug.export=true") // Exports transformed classes for debugging
    }

    runConfigs["client"].apply {
        programArguments.add("--username=${project.property("dev.username")}")
        programArguments.add("--uuid=${project.property("dev.uuid")}")
    }
}

fabricApi {
    configureDataGeneration {
        client = true
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    loomx.applyMojangMappings()

    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")

    shadow("com.github.ben-manes.caffeine:caffeine:${property("deps.caffeine")}")

    testImplementation(platform("org.junit:junit-bom:${property("deps.junit")}"))
    testImplementation("net.fabricmc:fabric-loader-junit:${property("deps.fabric_loader")}")

    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("org.mockito:mockito-core:${property("deps.mockito")}")
    testImplementation("org.mockito:mockito-junit-jupiter:${property("deps.mockito")}")
}

tasks {
    register<Copy>("buildAndCollect") {
        group = "custom"
        description = "Builds mod jars and copies results to `build/libs/{mod version}/`"

        dependsOn(build)

        inputs.property("version", project.property("mod.version"))

        from(distributionJar)

        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
    }

    if (sc.current.parsed < "26.1") {
        named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
            dependsOn(shadowJar)
            inputFile.set(shadowJar.flatMap { it.archiveFile })
        }
    } else {
        assemble {
            dependsOn(shadowJar)
        }

        shadowJar {
            archiveClassifier.set("")
        }
    }

    shadowJar {
        dependsOn(jar)

        configurations = listOf(project.configurations.shadow.get())

        relocate("com.github.benmanes.caffeine", "${shadowGroup}.caffeine")
        relocate("com.google.errorprone", "${shadowGroup}.errorprone")
        relocate("org.jspecify", "${shadowGroup}.jspecify")

        mergeServiceFiles()
        addMultiReleaseAttribute.set(false)

        exclude("META-INF/LICENSE", "META-INF/maven/**", "META-INF/versions/**/OSGI-INF/**")
    }

    processResources {
        fun MutableMap<String, String>.register(key: String, property: String) {
            val value: String = sc.properties[property]
            inputs.property(key, value)
            set(key, value)
        }

        val props = buildMap {
            register("id", "mod.id")
            register("name", "mod.name")
            register("version", "mod.version")
            register("minecraft", "mod.mc_compat")
            register("description", "mod.description")
            register("author", "mod.author")
            register("contact_homepage", "mod.contact_homepage")
            register("contact_sources", "mod.contact_sources")
            register("contact_issues", "mod.contact_issues")
            register("license", "mod.license")
            register("fabric_loader", "deps.fabric_loader")
            register("fabric_api", "deps.fabric_api")
        }

        filesMatching("fabric.mod.json") { expand(props) }

        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }

        from(rootProject.file("LICENSE.md")) { into("") }

        exclude("META-INF/neoforge.mods.toml")
    }
}

publishMods {
    file.set(distributionJar)
    displayName.set("${property("mod.name")} Fabric ${property("mod.version")} for ${property("publish.start")}")
    modLoaders.add("fabric")

    modrinth {
        requires("fabric-api")
    }

    curseforge {
        javaVersions.add(requiredJava)
        requires("fabric-api")
    }
}
