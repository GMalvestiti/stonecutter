import com.vanniktech.maven.publish.Checksum
import com.vanniktech.maven.publish.JavadocJar

plugins {
    java
    idea
    id("com.vanniktech.maven.publish")
    id("me.modmuss50.mod-publish-plugin")
}

repositories {
    /**
     * Restricts dependency search of the given [groups] to the [maven URL][url], improving the setup speed.
     */
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }
    strictMaven("https://www.cursemaven.com", "CurseForge", "curse.maven")
    strictMaven("https://api.modrinth.com/maven", "Modrinth", "maven.modrinth")
}

fun prop(property: String): String {
    return project.property(property) as String
}

fun getChangelog(): String {
    val changelogFile = rootProject.file("CHANGELOG.md")

    val lines = changelogFile.readLines()
    val builder = StringBuilder()

    var insideTargetSection = false
    val targetHeader = "# ${prop("mod.version")}"

    for (line in lines) {
        if (insideTargetSection && line.startsWith("# ")) {
            break
        }

        if (line.trim().contains(targetHeader, true)) {
            insideTargetSection = true
            continue
        }

        if (insideTargetSection) {
            builder.append(line).append("\n")
        }
    }

    return builder.toString().trim()
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    register<Copy>("installGitHooks") {
        group = "help"
        description = "Installs git hooks for the project"

        from(File(rootProject.rootDir, "scripts/pre-commit"))
        into(File(rootProject.rootDir, ".git/hooks"))

        filePermissions {
            unix("rwxr-xr-x")
        }
    }
}

afterEvaluate {
    mavenPublishing {
        publishToMavenCentral(automaticRelease = true)
        signAllPublications()

        coordinates(
            prop("mod.group"),
            project.base.archivesName.get(),
            project.version as String
        )

        if (!prop("dev.javadoc").toBoolean()) {
            configureBasedOnAppliedPlugins(
                javadocJar = JavadocJar.Empty()
            )
        }

        pom {
            name.set(prop("mod.name"))
            description.set(prop("mod.description"))
            inceptionYear.set("2026")
            url.set(prop("mod.contact_sources"))

            licenses {
                license {
                    name.set(prop("mod.license"))
                    url.set("${prop("mod.contact_sources")}/blob/main/LICENSE")
                    distribution.set("${prop("mod.contact_sources")}/blob/main/LICENSE")
                }
            }

            developers {
                developer {
                    id.set(prop("mod.author"))
                    name.set(prop("mod.author"))
                    url.set("https://github.com/${prop("mod.author")}")
                }
            }

            scm {
                url.set(prop("mod.contact_sources"))
                connection.set("scm:git:git://github.com/${prop("mod.author")}/${prop("mod.id")}.git")
                developerConnection.set("scm:git:ssh://git@github.com/${prop("mod.author")}/${prop("mod.id")}.git")
            }
        }

        checksums(Checksum.MD5, Checksum.SHA1, Checksum.SHA256, Checksum.SHA512)
        excludeSignatureChecksums(false)
    }
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}
