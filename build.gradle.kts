// Top-level build file — configuration for all sub-projects.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.protobuf) apply false
    // Uncomment after adding google-services.json:
    // alias(libs.plugins.google.services) apply false
    // alias(libs.plugins.firebase.crashlytics) apply false
}
