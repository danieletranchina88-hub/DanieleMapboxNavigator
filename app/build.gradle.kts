plugins {
    id("com.android.application")
}

val mapboxPublicToken = providers.gradleProperty("MAPBOX_PUBLIC_TOKEN")
    .orElse(providers.environmentVariable("MAPBOX_PUBLIC_TOKEN"))
    .getOrElse("pk.token-not-configured")

android {
    namespace = "it.daniele.mapboxnavigator"
    compileSdk = 37

    defaultConfig {
        applicationId = "it.daniele.mapboxnavigator"
        minSdk = 23
        targetSdk = 36
        versionCode = 2
        versionName = "0.2.0"

        resValue("string", "mapbox_access_token", mapboxPublicToken)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        resValues = true
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources.excludes += setOf(
            "META-INF/DEPENDENCIES",
            "META-INF/LICENSE*",
            "META-INF/NOTICE*"
        )
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = true
    }
}

dependencies {
    val mapboxNavigationVersion = "3.30.0"

    implementation("com.mapbox.navigationcore:android-ndk27:$mapboxNavigationVersion")
    implementation("com.mapbox.navigationcore:ui-maps-ndk27:$mapboxNavigationVersion")
    implementation("com.mapbox.navigationcore:ui-components-ndk27:$mapboxNavigationVersion")
    implementation("com.mapbox.navigationcore:tripdata-ndk27:$mapboxNavigationVersion")
    implementation("com.mapbox.navigationcore:voice-ndk27:$mapboxNavigationVersion")
    implementation("com.mapbox.search:mapbox-search-android-ui-ndk27:2.30.0")

    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("com.google.android.material:material:1.13.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
