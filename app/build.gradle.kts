plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id ("kotlin-kapt")
    id ("kotlin-parcelize")
    id("com.google.devtools.ksp")
    id ("androidx.navigation.safeargs")
}

android {
    namespace = "com.example.pbd_jwr"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.pbd_jwr"
        minSdk = 29
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures{
        dataBinding {
            enable = true
        }

        viewBinding {
            enable = true
        }
    }

}

dependencies {
    implementation("androidx.appcompat:appcompat:$rootProject.appCompatVersion")
    implementation("androidx.activity:activity-ktx:$rootProject.activityVersion")
    implementation("androidx.camera:camera-core:1.3.2")
    implementation("androidx.camera:camera-lifecycle:1.3.2")
    implementation("androidx.camera:camera-view:1.3.2")
    implementation("androidx.compose.ui:ui-android:1.6.4")
    implementation("androidx.compose.foundation:foundation-android:1.6.4")
    implementation("androidx.compose.material3:material3-android:1.2.1")

    kapt("com.android.databinding:compiler:3.1.4")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")



    // ROOM
    val roomVersion = "2.6.1"

    // Room components
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    androidTestImplementation("androidx.room:room-testing:$roomVersion")

    val lifecycleVersion = "2.7.0"
    // Lifecycle components
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-common-java8:$lifecycleVersion")

    // Kotlin components
    val coroutine = "1.7.1"
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.9.20")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutine")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutine")

    // UI
    implementation("androidx.constraintlayout:constraintlayout:$rootProject.constraintLayoutVersion")
    implementation("com.google.android.material:material:$rootProject.materialVersion")

    // Navigation
    val navVersion = "2.7.7"
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")

    // Testing
    testImplementation("junit:junit:$rootProject.junitVersion")
//    androidTestImplementation("androidx.arch.core:core-testing:$rootProject.coreTestingVersion")
//    androidTestImplementation("androidx.test.espresso:espresso-core:$rootProject.espressoVersion")
//    androidTestImplementation("androidx.test.ext:junit:$rootProject.androidxJunitVersion")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // For unit tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:3.12.4")

    // Location
    implementation("com.google.android.gms:play-services-location:18.0.0")



    //okHttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("com.squareup.moshi:moshi:1.12.0")
    implementation ("com.squareup.moshi:moshi-kotlin:1.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    //Encrypted SharedPref
    implementation("androidx.security:security-crypto:1.1.0-alpha03")


    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation(platform("androidx.compose:compose-bom:2023.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material:material-icons-extended:1.5.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")


    val cameraxVersion = "1.3.0-rc01"

    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-video:$cameraxVersion")

    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("androidx.camera:camera-extensions:$cameraxVersion")

    implementation("org.apache.poi:poi:5.2.5")
    implementation("org.apache.poi:poi-ooxml:5.2.5")

    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("com.androidplot:androidplot-core:1.5.10")

}

kapt {
    generateStubs = true
}

