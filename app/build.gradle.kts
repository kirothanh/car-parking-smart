plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.javamapbox"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.javamapbox"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    //    Mapbox dependencies
    implementation("com.mapbox.maps:android:10.15.0")
    implementation("com.mapbox.navigation:android:2.15.2")
    implementation("com.mapbox.search:mapbox-search-android-ui:1.0.0-rc.6")
}