// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}

subprojects {
    configurations.configureEach {
        resolutionStrategy {
            force(
                "androidx.core:core-ktx:1.15.0",
                "androidx.core:core:1.15.0",
                "androidx.lifecycle:lifecycle-runtime-ktx:2.8.7",
                "androidx.lifecycle:lifecycle-runtime-compose-android:2.8.7",
                "androidx.lifecycle:lifecycle-viewmodel-compose-android:2.8.7",
                "androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7"
            )
        }
    }
}