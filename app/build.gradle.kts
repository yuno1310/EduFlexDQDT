plugins {
    id("com.android.application")
    id("com.google.gms.google-services") apply false
}

// Push notifications are optional. Clean checkouts must build without Firebase credentials.
if (file("google-services.json").isFile) {
    apply(plugin = "com.google.gms.google-services")
}

val dotenvFile = rootProject.file(".env")
fun dotenvValue(key: String): String? = if (dotenvFile.exists()) {
    dotenvFile.readLines()
        .asSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") && it.contains("=") }
        .map { line ->
            val k = line.substringBefore("=").trim()
            val v = line.substringAfter("=").trim().removeSurrounding("\"").removeSurrounding("'")
            k to v
        }
        .firstOrNull { (k, _) -> k == key }
        ?.second
} else null

val apiBaseUrl = dotenvValue("API_BASE_URL")
    ?: providers.gradleProperty("API_BASE_URL").orNull
    ?: providers.environmentVariable("API_BASE_URL").orNull
    ?: "http://10.0.2.2:8080/"

android {
    namespace = "com.eduflex.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.eduflex.android"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("boolean", "HTTP_LOGGING_ENABLED", "true")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            buildConfigField("boolean", "HTTP_LOGGING_ENABLED", "false")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")
    implementation(platform("com.google.firebase:firebase-bom:34.10.0"))
    implementation("com.google.firebase:firebase-messaging")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.navigation:navigation-fragment:2.9.7")
    implementation("androidx.navigation:navigation-ui:2.9.7")

    // Retrofit + Gson
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // WorkManager for daily study reminders
    implementation("androidx.work:work-runtime:2.10.1")

    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    testImplementation("junit:junit:4.13.2")
}
