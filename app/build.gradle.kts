plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id ("com.google.gms.google-services")
}

android {
    buildFeatures {
        viewBinding = true
    }
    namespace = "com.example.jobify"
    compileSdk = 35
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
    defaultConfig {
        applicationId = "com.example.jobify"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled =true


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

}

dependencies {
// Firebase dependencies
    implementation ("androidx.core:core-ktx:1.10.1")
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    implementation("org.chromium.net:cronet-embedded:119.6045.31")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.1")
    // Firebase Firestore dependency
    implementation ("com.google.firebase:firebase-firestore:25.1.1") // Use the latest version
    implementation ("androidx.multidex:multidex:2.0.1")
    // Firebase Authentication dependency (if not already added)
    implementation ("com.google.firebase:firebase-auth:23.1.0")// Use the latest version
// BCrypt library for password hashing
    implementation ("at.favre.lib:bcrypt:0.10.2") // Use the latest version
    // Other Firebase dependencies (optional)
    implementation ("com.google.firebase:firebase-core:21.1.1")
    // Add the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-auth")
    implementation ("com.google.firebase:firebase-auth-ktx:23.1.0") // For Firebase Authentication
    implementation ("com.google.firebase:firebase-firestore-ktx:25.1.1") // Optional: For Firestore
    implementation("androidx.core:core-ktx:1.15.0")
    implementation ("androidx.navigation:navigation-fragment-ktx:2.8.5") // Use the latest version
    implementation ("androidx.navigation:navigation-ui-ktx:2.8.5") // Use the latest version
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.annotation:annotation:1.9.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.5")
    implementation("androidx.activity:activity:1.9.3")
    implementation("com.google.firebase:firebase-storage-ktx:21.0.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    implementation ("androidx.core:core-splashscreen:1.0.0")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("androidx.recyclerview:recyclerview:1.3.1")
    implementation ("com.github.bumptech.glide:glide:4.12.0")

}