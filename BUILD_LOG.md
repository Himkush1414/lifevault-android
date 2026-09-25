# LifeVault Build Log

Decisions and deviations from `LifeVault_Build_Spec.md`, in build order. Section
references are to the spec.

## Toolchain (bootstrap, not part of the numbered build order)

The build environment had no JDK, Android SDK, or Gradle. Installed user-space
(no root available):
- Temurin JDK 17.0.13
- Android SDK cmdline-tools, platform-tools, `platforms;android-36`,
  `platforms;android-37.2`, `build-tools;36.0.0` / `37.0.0`
- Gradle via the wrapper (see below)

No Android emulator or physical device is available in this environment.
Emulator/device-dependent acceptance checks (ML Kit scanning, biometrics,
instrumented UI tests, `connectedCheck`) cannot be run here — they're called
out per step below and remain on the owner's manual test list (Section 11,
column B) until verified on a real device.

## Step 1 — Project initialisation

- **Kotlin/AGP/Gradle versions**: the spec says "latest stable at build time."
  As of 2026-09-25 that's AGP 9.4.1, which requires Gradle ≥9.6 and dropped
  the separate `org.jetbrains.kotlin.android` plugin in favor of AGP's
  built-in Kotlin support (JetBrains' Jan 2026 migration guide). Kotlin
  Gradle Plugin is pinned to 2.4.20 explicitly via a `buildscript` classpath
  override in the root `build.gradle.kts`, since AGP's bundled default
  wouldn't otherwise match the `org.jetbrains.kotlin.plugin.compose` /
  `.plugin.serialization` plugin versions declared in the version catalog.
- **KSP versioning**: KSP decoupled its version scheme from the Kotlin
  compiler version starting at 2.3.0 (no longer `<kotlin>-<ksp>` suffixed).
  Using plain `2.3.12`.
- **`oss-licenses-plugin`**: the Gradle plugin (`com.google.android.gms:
  oss-licenses-plugin`, distinct from the runtime `play-services-oss-licenses`
  library) has no published plugin-marker artifact, so it can't be applied
  via the version catalog's `plugins {}` DSL — applied the legacy way via a
  root `buildscript { classpath(...) }` block instead. Also needed bumping
  from 0.10.6 to 0.13.0: 0.10.6 calls the legacy `applicationVariants` API,
  which AGP 9's new variant API removed.
- **compileSdk/targetSdk**: spec suggested 36 ("verify in Play Console").
  Compose BOM 2026.09.00 requires compiling against API 37
  (`androidx.compose.foundation:foundation-android:1.12.1` and others).
  Using compileSdk 37 / targetSdk 37; Play's hard requirement as of
  2026-08-31 is targetSdk ≥36, so this is above the floor, not below it.
- **Launcher icon**: Section 4.4 describes the final shield/document-notch
  mark as design-system work (Step 2). Step 1 needs *some* icon for the
  manifest to compile, so added a placeholder adaptive icon (solid
  `#1F4FD8` background + a plain rounded-shield foreground/monochrome) with
  `TODO(step 2 - design system)` markers. Will be replaced, not iterated on,
  in Step 2.
- **Lint**: `warningsAsErrors = true` is applied at the module level (AGP
  doesn't cleanly support "errors only on release" in a single `lint {}`
  block), so it also gates debug builds — stricter than the spec's literal
  "release builds" wording, judged as the safer reading. Also added
  `tools:ignore="AppLinkUrlError"` on the `.lvault` VIEW intent-filter: that
  lint check assumes web/App Links and demands a host, which doesn't apply
  to local `content`/`file` scheme opens.
- **Dependency versions**: Android Lint's `GradleDependency` /
  `NewerVersionAvailable` checks enumerated the current-latest version of
  every catalog entry; used those exact versions rather than hand-picking,
  since they're authoritative for "latest stable at build time."

*Accept check status: `./gradlew :app:assembleDebug lint detekt test` all
green. No emulator run performed (none available) — deferred to the owner.*
