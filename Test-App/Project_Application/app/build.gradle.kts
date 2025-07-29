plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    id("com.google.gms.google-services")
    id("com.google.protobuf") version "0.9.5" apply false
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.example.project_application"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.project_application"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.0"
    }

    androidResources {
        noCompress += listOf("task") // Prevent compression of LLM task files
    }

    packaging {
        resources {
            excludes.add("META-INF/INDEX.LIST")
            excludes.add("META-INF/DEPENDENCIES")
            // pickFirst instead:
            // pickFirsts.add("META-INF/DEPENDENCIES")
        }
    }
}

dependencies {
    // Compose + UI
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.compose.foundation:foundation:1.5.0")
    implementation("androidx.compose.material:material:1.5.0")
    implementation("com.google.accompanist:accompanist-pager:0.28.0")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.28.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.15.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    // MediaPipe LLM (Text and Image)
    //implementation("com.google.mediapipe:framework:0.10.1")
    implementation("com.google.mediapipe:tasks-genai:0.10.25")
    //implementation("com.google.mediapipe:tasks-core:0.10.24")
    implementation("com.google.mediapipe:tasks-vision-image-generator:0.10.21")

    // Protobuf runtime
    implementation("com.google.protobuf:protobuf-javalite:4.26.1")

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //implementation("com.google.api-client:google-api-client-android:1.35.0")
    //implementation("com.google.oauth-client:google-oauth-client-jetty:1.35.0")
    //implementation("com.google.apis:google-api-services-gmail:v1-rev110-1.25.0")
    //implementation("com.google.http-client:google-http-client-gson:1.35.0")
    implementation("com.google.android.gms:play-services-auth:21.4.0") // Google sign-in

// Google Java client (Gmail API)
    implementation("com.google.api-client:google-api-client-android:2.8.0")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.36.0")
    implementation("com.google.api-client:google-api-client-gson:2.8.0")
    implementation("com.google.http-client:google-http-client:1.47.1")

// Gmail API itself
    implementation("com.google.apis:google-api-services-gmail:v1-rev110-1.25.0")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation("com.sun.mail:jakarta.mail:2.0.1")

}
