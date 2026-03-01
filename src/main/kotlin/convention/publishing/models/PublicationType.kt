package dev.ellectronchik.convention.publishing.models

enum class PublicationType(
    val id: String,
) {
    DEBUG("debug"),
    RELEASE("release"),
    ALL("all"),
}
