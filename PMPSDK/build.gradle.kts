plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    `maven-publish`
}

android {
    namespace = "com.example.pmpsdk"
    compileSdk = 35

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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


}

dependencies {

   api ("androidx.lifecycle:lifecycle-runtime-ktx:2.9.2")
    api ("androidx.viewpager2:viewpager2:1.1.0")
    api(libs.extension.okhttp)

    implementation(libs.androidx.appcompat)
    implementation(libs.material)
   api(libs.androidx.recyclerview)
    testImplementation(libs.junit)
    api(libs.runner)
    androidTestImplementation(libs.espresso.core)
    //异常上报使用的依赖
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    api("com.squareup.retrofit2:retrofit:2.9.0")
    api("com.squareup.retrofit2:converter-gson:2.9.0")
    api("com.google.code.gson:gson:2.10.1")
    api("com.squareup.okhttp3:okhttp:4.11.0")
    api("com.squareup.okhttp3:logging-interceptor:4.11.0")
}
publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.boolean314"
            artifactId = "PMPSDK"
            version = "1.4.0"
            afterEvaluate {
                from(components["release"])
            }
        }
    }
}