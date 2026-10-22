import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	alias(libs.plugins.kotlin.jvm)
}

// AGP-free JVM module: the daemon runs on 21, so Kotlin would default to
// target 21 while the java block below says 17, and Gradle refuses the
// mismatch. Pin Kotlin to the same 17 the Java tasks already use.
kotlin {
	compilerOptions {
		jvmTarget = JvmTarget.JVM_17
	}
}

java {
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
	// KSP symbol API only. The processor must not see the module it processes,
	// so nothing from :app is (or can be) on this classpath.
	implementation(libs.symbol.processing.api)
}
