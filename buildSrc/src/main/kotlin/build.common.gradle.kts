import com.vanniktech.maven.publish.Checksum

plugins {
    java
    idea
    id("com.vanniktech.maven.publish")
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

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

fun prop(property: String): String {
    return project.property(property) as String
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
