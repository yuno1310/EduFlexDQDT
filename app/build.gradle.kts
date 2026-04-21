plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
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

val geminiApiKey = dotenvValue("GEMINI_API_KEY")
    ?: providers.gradleProperty("GEMINI_API_KEY").orNull
    ?: providers.environmentVariable("GEMINI_API_KEY").orNull
    ?: ""

val openRouterApiKey = dotenvValue("OPENROUTER_API_KEY")
    ?: providers.gradleProperty("OPENROUTER_API_KEY").orNull
    ?: providers.environmentVariable("OPENROUTER_API_KEY").orNull
    ?: ""

val anthropicApiKey = dotenvValue("ANTHROPIC_API_KEY")
    ?: providers.gradleProperty("ANTHROPIC_API_KEY").orNull
    ?: providers.environmentVariable("ANTHROPIC_API_KEY").orNull
    ?: ""

val groqApiKey = dotenvValue("GROQ_API_KEY")
    ?: providers.gradleProperty("GROQ_API_KEY").orNull
    ?: providers.environmentVariable("GROQ_API_KEY").orNull
    ?: ""

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
        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
        buildConfigField("String", "OPENROUTER_API_KEY", "\"$openRouterApiKey\"")
        buildConfigField("String", "ANTHROPIC_API_KEY", "\"$anthropicApiKey\"")
        buildConfigField("String", "GROQ_API_KEY", "\"$groqApiKey\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
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
}
