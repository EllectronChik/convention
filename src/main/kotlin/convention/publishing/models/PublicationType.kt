package dev.ellectronchik.convention.publishing.models

/** Publication scope for Android library variants. */
enum class PublicationType(
    val id: String,
) {
    /** Publish only the `debug` variant. */
    DEBUG("debug"),

    /** Publish only the `release` variant. */
    RELEASE("release"),

    /** Publish all variants through the AGP `default` multi-variant group. */
    ALL("default"),
}
