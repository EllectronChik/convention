# Convention Gradle Plugins

A small set of Gradle convention plugins for multi-module Kotlin/Android builds.

This project provides:
- central version propagation across subprojects
- centralized publishing defaults on the root project
- per-module publishing with optional overrides

## Plugin IDs

| Plugin ID | Apply on | Purpose |
| --- | --- | --- |
| `dev.ellectronchik.versioning` | root project | Propagates version settings to Kotlin JVM and Android modules. |
| `dev.ellectronchik.publishing.config` | root project | Declares shared publishing defaults in `corePublishing { ... }`. |
| `dev.ellectronchik.publishing` | module projects | Configures `maven-publish` for Kotlin JVM and Android library modules. |

## Compatibility

- Gradle wrapper: `8.14`
- Java toolchain for plugin build/tests: `17`
- Compiled against:
  - AGP `8.13.0`
  - Kotlin Gradle Plugin `2.3.0`

## Quick Start (Composite Build)

### 1) Wire this plugin build in `settings.gradle.kts`

```kotlin
pluginManagement {
    includeBuild("../convention") // path to this repository
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
```

### 2) Configure root plugins in root `build.gradle.kts`

```kotlin
plugins {
    id("dev.ellectronchik.versioning")
    id("dev.ellectronchik.publishing.config")
}

versioning {
    currentVersionName = "1.2.3"
    currentAndroidVersionCode = 120300
    overrideModuleVersion = false
}

corePublishing {
    repositories {
        mavenLocal()
    }

    groupId = "com.example.libs"
    withSourceJar = true
    withJavadocJar = true
    useDokka = true
}
```

### 3) Apply module publishing plugin in publishable modules

Kotlin/JVM module:

```kotlin
plugins {
    kotlin("jvm")
    id("dev.ellectronchik.publishing")
}

modulePublishing {
    artifactId = "my-jvm-lib"
    overrideDefaults {
        withJavadocJar = true
        useDokka = false
    }
}
```

Android library module:

```kotlin
import dev.ellectronchik.convention.publishing.models.PublicationType

plugins {
    id("com.android.library")
    kotlin("android")
    id("dev.ellectronchik.publishing")
}

android {
    namespace = "com.example.mylib"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
}

modulePublishing {
    overrideDefaults {
        publishVariant = PublicationType.RELEASE
    }
}
```

## DSL Reference

### `versioning { ... }` (root)

| Property | Type | Default | Required | Description |
| --- | --- | --- | --- | --- |
| `overrideModuleVersion` | `Boolean` | `false` | No | If `true`, forces module version to `currentVersionName`. |
| `currentVersionName` | `String` | none | Yes (when version is resolved) | Version name propagated to modules. |
| `currentAndroidVersionCode` | `Int` | none | Yes (for Android app modules) | Version code for Android application modules. |

Behavior:
- Android application modules:
  - if `overrideModuleVersion = true`, force `versionName` and `versionCode`
  - if `overrideModuleVersion = false`, only fill missing values
- Kotlin JVM and Android library modules resolve `project.version` from this extension when needed.

### `corePublishing { ... }` (root)

| Property | Type | Effective default |
| --- | --- | --- |
| `repositories { ... }` | `RepositoryHandler.() -> Unit` | none |
| `publishVariant` | `PublicationType` | `RELEASE` |
| `groupId` | `String` | unresolved until fallback chain |
| `withSourceJar` | `Boolean` | `true` |
| `withJavadocJar` | `Boolean` | `false` |
| `useDokka` | `Boolean` | `true` |

### `modulePublishing { ... }` (module)

| Property | Type | Default |
| --- | --- | --- |
| `artifactId` | `String` | module path with `:` replaced by `-` |
| `overrideDefaults.publishVariant` | `PublicationType` | inherited from `corePublishing.publishVariant` |
| `overrideDefaults.groupId` | `String` | inherited/fallback |
| `overrideDefaults.withSourceJar` | `Boolean` | inherited/fallback (`true`) |
| `overrideDefaults.withJavadocJar` | `Boolean` | inherited/fallback (`false`) |
| `overrideDefaults.useDokka` | `Boolean` | inherited/fallback (`true`) |

## Resolution Rules

Publishing values are resolved with this precedence:

- `groupId`:
  1. `modulePublishing.overrideDefaults.groupId`
  2. `corePublishing.groupId`
  3. module `project.group`
  4. root `project.group`
  5. fail if still empty or `unspecified`

- `version`:
  1. module `project.version`
  2. root `project.version`
  3. fail if still empty or `unspecified`

## Publication Behavior

- `dev.ellectronchik.publishing` auto-applies `maven-publish`.
- Kotlin/JVM modules publish a `java` Maven publication.
- Android library modules publish variant-based Maven publications:
  - `DEBUG` or `RELEASE`: single variant
  - `ALL`: all variants
- If `withJavadocJar = true` and `useDokka = true`, Dokka is auto-applied and a Dokka Javadoc JAR is attached.

Example task names you will see:
- `publishJavaPublicationToMavenLocal`
- `publishReleasePublicationToMavenLocal`

## Constraints and Failure Cases

- `dev.ellectronchik.versioning` must be applied on the root project.
- `dev.ellectronchik.publishing.config` must be applied on the root project.
- `dev.ellectronchik.publishing` requires `dev.ellectronchik.publishing.config` on the root project.
- Missing required versioning properties throw `MissingPropertyException`.

## Development

Run checks locally:

```bash
./gradlew test
```

Run compatibility tests for a specific toolchain tuple:

```bash
./gradlew test -PtestGradleVersion=8.14.2 -PtestAgpVersion=8.13.2 -PtestKgpVersion=2.3.0
```

Tested compatibility matrix (`.github/workflows/test.yml`) that is expected to work:

| Gradle   | AGP | KGP |
|----------| --- | --- |
| `8.2.1`  | `8.2.2` | `1.9.22` |
| `8.4`    | `8.3.0` | `1.9.24` |
| `8.6`    | `8.4.0` | `1.9.24` |
| `8.7`    | `8.5.0` | `2.0.0` |
| `8.7`    | `8.6.0` | `2.1.0` |
| `8.9`    | `8.7.0` | `2.1.0` |
| `8.10.2` | `8.8.0` | `2.1.0` |
| `8.11.1` | `8.9.0` | `2.1.0` |
| `8.11.1` | `8.10.0` | `2.2.0` |
| `8.13`   | `8.11.0` | `2.2.0` |
| `8.13`   | `8.12.0` | `2.2.0` |
| `8.14.2` | `8.13.2` | `2.3.0` |
| `9.1.0`  | `9.0.0` | `2.3.0` |

Build plugin artifacts:

```bash
./gradlew build
```
