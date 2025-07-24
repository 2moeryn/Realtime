// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Menggunakan alias dari version catalog untuk semua plugin
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.google.gms.google.services) apply false // Menggunakan alias juga untuk google-services
}
