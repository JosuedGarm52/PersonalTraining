plugins {
    id("com.android.application") 
    id("org.jetbrains.kotlin.android")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.personaltraining"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.personaltraining"
        minSdk = 25
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    // Habilitar View Binding
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    val room_version = "2.6.1"

    implementation("androidx.room:room-runtime:$room_version")
    //optional - kotlin extensions and corutines support for room
    implementation("androidx.room:room-ktx:$room_version")

    val lifeCycle = "2.7.0"

    // Lifecycle components
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifeCycle")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifeCycle")
    implementation("androidx.lifecycle:lifecycle-common-java8:$lifeCycle")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifeCycle")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // To use Kotlin Symbol Processing (KSP)
    ksp("androidx.room:room-compiler:$room_version")

    implementation("com.github.chthai64:SwipeRevealLayout:1.4.0")

    val glideVersion = "4.12.0"
    val urlGLide = "com.github.bumptech.glide:glide:$glideVersion"

    implementation(urlGLide)
    ksp(urlGLide)

}