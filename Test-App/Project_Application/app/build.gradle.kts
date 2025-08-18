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

        buildConfigField("String", "TELEGRAM_BOT_TOKEN", "\"${project.properties["TELEGRAM_BOT_TOKEN"]}\"")
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
        buildConfig = true
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
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE-notice.md"
            excludes += "META-INF/NOTICE.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.md"
        }
    }
}

dependencies {
    // Compose + UI
    implementation("androidx.compose.material:material-icons-extended:1.4.3")
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
    implementation("com.google.android.gms:play-services-auth:21.4.0") // keep latest

    // MediaPipe LLM (Text and Image)
    //implementation("com.google.mediapipe:framework:0.10.1") // not used
    implementation("com.google.mediapipe:tasks-genai:0.10.25")
    //implementation("com.google.mediapipe:tasks-core:0.10.24") // not used
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

    // Google Sign-in + Gmail API
    implementation("com.google.api-client:google-api-client-android:2.8.0")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.36.0")
    implementation("com.google.api-client:google-api-client-gson:2.8.0")
    implementation("com.google.http-client:google-http-client:1.47.1")
    implementation("com.google.apis:google-api-services-gmail:v1-rev110-1.25.0")

    implementation("com.squareup.okhttp3:okhttp:5.1.0") // keep latest

    // Mail
    implementation("com.sun.mail:android-mail:1.6.7")
    // implementation("com.sun.mail:android-activation:1.6.7") // optional

    implementation("androidx.security:security-crypto:1.0.0")

    configurations.all {
        resolutionStrategy {
            force("com.google.api-client:google-api-client:1.35.2")
            force("com.google.oauth-client:google-oauth-client:1.35.0")
            force("com.google.http-client:google-http-client-android:1.40.1")
        }
    }

    // Maps + Location
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("androidx.navigation:navigation-compose:2.9.3")
    implementation("com.google.maps.android:maps-compose:6.7.1")
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.10.3") // keep latest
    //implementation("androidx.work:work-runtime-ktx:2.9.0") // duplicate

    // ML Kit (OCR + Vision)
    implementation("com.google.mlkit:vision-common:17.3.0") // keep latest
    implementation("com.google.mlkit:text-recognition:16.0.1") // keep latest
    implementation("com.google.mlkit:image-labeling:17.0.9") // keep latest
    //implementation("com.google.mlkit:vision-common:16.3.0") // duplicate
    //implementation("com.google.mlkit:text-recognition:16.0.0") // duplicate
    //implementation("com.google.mlkit:image-labeling:17.0.7") // duplicate

    // Telegram (via OkHttp already added)
    //implementation("com.squareup.okhttp3:okhttp:4.12.0") // duplicate

    // Gmail API older versions (commented out)
    //implementation("com.google.android.gms:play-services-auth:21.2.0") // duplicate
    //implementation("com.google.api-client:google-api-client-android:2.6.0") // older
    //implementation("com.google.apis:google-api-services-gmail:v1-rev20220404-2.0.0") // older

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    implementation("androidx.camera:camera-core:1.4.2")
    implementation("androidx.camera:camera-camera2:1.4.2")
    implementation("androidx.camera:camera-lifecycle:1.4.2")
    implementation("androidx.camera:camera-view:1.4.2")

    implementation("com.google.accompanist:accompanist-pager:0.36.0")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.36.0")
    implementation("com.google.accompanist:accompanist-permissions:0.37.3")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
}
