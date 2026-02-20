# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SecureQR is an Android app for creating and scanning encrypted QR codes. Users can encode text into QR codes with optional AES-256-GCM encryption (password-based), and scan QR codes with automatic detection of encrypted content.

## Build Commands

```bash
./gradlew assembleDebug        # Build debug APK
./gradlew assembleRelease      # Build release APK (requires signing config in gradle.properties)
./gradlew test                 # Run unit tests
./gradlew connectedAndroidTest # Run instrumented tests (requires device/emulator)
./gradlew clean                # Clean build artifacts
```

Release signing requires these properties in `gradle.properties`: `KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.

## Architecture

Single-activity app (`MainActivity`) using Jetpack Navigation with bottom navigation between two fragments:

- **ScannerFragment** — CameraX preview with ML Kit barcode scanning. Detects QR codes within a defined scan area overlay, auto-detects encrypted content (by attempting `CipherText.decode`), and navigates to EditorFragment with scanned data via shared `EditorViewModel`.
- **EditorFragment** — QR code creation/viewing. Supports plain and encrypted modes. Generates QR codes using ZXing. Allows sharing generated QR images.
- **EditorViewModel** — Shared ViewModel (scoped to activity) bridging scanner and editor. Holds `secureQrMode`, `data` (plaintext), and `encodedData` (displayed/QR-encoded).

## Crypto Format

`biz/Crypto.java` handles AES-256-GCM encryption with PBKDF2 key derivation (65536 iterations). `biz/CipherText.java` serializes ciphertext as: `SecureQR.<base64-salt>.<base64-iv>.<base64-ciphertext>` (dot-separated, `SecureQR` prefix used for format detection during scanning).

## Key Details

- Language: Java (no Kotlin source files)
- Min SDK: 26, Target SDK: 35, Gradle: 8.9, AGP: 8.7.3
- View binding enabled (no data binding)
- R8/ProGuard enabled for release builds
- Google AdMob integrated (banner ad in main activity)
- Camera permission handled via TedPermission library
