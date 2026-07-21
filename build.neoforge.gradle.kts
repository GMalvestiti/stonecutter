plugins {
    id("build.common")
    id("neoforge.mutex")
    id("net.neoforged.moddev") version "2.0.141"
}

version = "${property("mod.version")}+${sc.current.version}"
base.archivesName = "${property("mod.id") as String}-neoforge"

sourceSets.main {
    resources.srcDir("src/main/resources")
    resources.srcDir("src/main/generated")
    resources.exclude("**/.cache")
}

val requiredJava = when {
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

neoForge {
    version = property("deps.neoforge_loader") as String

    mods {
        register("${property("mod.id")}") {
            sourceSet(sourceSets.main.get())
        }
    }

    runs {
        configureEach {
            gameDirectory = file("../../run/")
            systemProperty("forge.logging.markers", "REGISTRIES")
            logLevel = org.slf4j.event.Level.DEBUG
        }

        register("client") {
            client()
            programArgument("--username=Riser876")
            programArgument("--uuid=13957e2e-2731-4479-8a6d-d42f89f8d756")
            systemProperty("neoforge.enabledGameTestNamespaces", property("mod.id") as String)
        }

        register("server") {
            server()
            programArgument("--nogui")
            systemProperty("neoforge.enabledGameTestNamespaces", property("mod.id") as String)
        }

        register("gameTestServer") {
            type = "gameTestServer"
            systemProperty("neoforge.enabledGameTestNamespaces", property("mod.id") as String)
        }

        register("data") {
            if (sc.current.parsed < "1.21.4") {
                data()
            } else {
                clientData()
            }

            programArguments.addAll(
                "--mod", property("mod.id") as String,
                "--all",
                "--output", file("src/main/generated").absolutePath,
                "--existing", file("src/main/resources").absolutePath
            )
        }
    }
}

dependencies {
    jarJar(implementation("com.github.ben-manes.caffeine:caffeine") {
        version {
            prefer("${property("deps.caffeine")}")
        }
    })
    if (sc.current.parsed < "1.21.9") {
        "additionalRuntimeClasspath"("com.github.ben-manes.caffeine:caffeine:${property("deps.caffeine")}")
    }
}

tasks {
    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds mod jars and copies results to `build/libs/{mod version}/`"

        dependsOn(build)

        inputs.property("version", project.property("mod.version"))

        from(
            jar.flatMap { it.archiveFile }
        )

        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
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
            register("neoforge_loader", "deps.neoforge_loader")
        }

        filesMatching("META-INF/neoforge.mods.toml") { expand(props) }

        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }

        from(rootProject.file("LICENSE")) { into("") }

        exclude("fabric.mod.json", "*.ct", "*.classtweaker")
    }

    named("createMinecraftArtifacts") {
        dependsOn("stonecutterGenerate")
    }
}
