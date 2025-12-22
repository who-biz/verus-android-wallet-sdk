buildscript {

    repositories {
        google()
        gradlePluginPortal()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", version = libs.versions.kotlin.get()))
        classpath(libs.gradle.plugin.rust)
        classpath(libs.gradle.plugin.navigation)
    }
}

plugins {
    id("com.github.ben-manes.versions")
    id("com.osacky.fulladle")
    id("zcash-sdk.detekt-conventions")
    id("zcash-sdk.ktlint-conventions")
    id("zcash-sdk.rosetta-conventions")
    `maven-publish`
}

tasks {
    withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
        gradleReleaseChannel = "current"

        resolutionStrategy {
            componentSelection {
                all {
                    if (isNonStable(candidate.version) && !isNonStable(currentVersion)) {
                        reject("Unstable")
                    }
                }
            }
        }
    }

    register("checkProperties") {
        doLast {
            // Ensure that developers do not change default values of certain properties directly in the repo, but
            // instead set them in their local ~/.gradle/gradle.properties file (or use command line arguments)
            val expectedPropertyValues = mapOf(
                "ZCASH_MAVEN_PUBLISH_USERNAME" to "",
                "ZCASH_MAVEN_PUBLISH_PASSWORD" to "",
                "ZCASH_ASCII_GPG_KEY" to "",

                "IS_SNAPSHOT" to "true",

                "ZCASH_IS_TREAT_WARNINGS_AS_ERRORS" to "true",

                "IS_USE_TEST_ORCHESTRATOR" to "false",

                "ZCASH_FIREBASE_TEST_LAB_API_KEY_PATH" to "",
                "ZCASH_FIREBASE_TEST_LAB_PROJECT" to "",

                "ZCASH_EMULATOR_WTF_API_KEY" to "",

                "IS_MINIFY_SDK_ENABLED" to "false",
                "IS_MINIFY_APP_ENABLED" to "true",

                "ZCASH_DEBUG_KEYSTORE_PATH" to "",
                "ZCASH_RELEASE_KEYSTORE_PATH" to "",
                "ZCASH_RELEASE_KEYSTORE_PASSWORD" to "",
                "ZCASH_RELEASE_KEY_ALIAS" to "",
                "ZCASH_RELEASE_KEY_ALIAS_PASSWORD" to "",

                "IS_SIGN_RELEASE_BUILD_WITH_DEBUG_KEY" to "false",
                
                "IS_DEBUGGABLE_WHILE_BENCHMARKING" to "false"
            )

            val warnings = expectedPropertyValues.filter { (key, value) ->
                project.properties[key].toString() != value
            }.map { "Property ${it.key} does not have expected value \"${it.value}\"" }

            if (warnings.isNotEmpty()) {
                throw GradleException(warnings.joinToString(separator = "\n"))
            }
        }
    }
}

val unstableKeywords = listOf("alpha", "beta", "rc", "m", "ea", "build")

fun isNonStable(version: String): Boolean {
    val versionLowerCase = version.lowercase()

    return unstableKeywords.any { versionLowerCase.contains(it) }
}

//afterEvaluate {
//    publishing {
//        publications {
//            release(MavenPublication) {
//                from components.release
//                groupId = "com.github.VerusCoin"
//                artifactId = "verus-android-wallet-sdk"
//                version = '0.0.1'
//            }
//        }
//    }
//}
