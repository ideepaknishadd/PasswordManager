# Password Manager App

## Overview
A secure password manager application

## Features
- Add, view, edit, and delete passwords using bottom sheet dialogs.
- Secure encryption using AES with Android Keystore.
- Room database for local storage.
- Biometric authentication.
- Password strength meter.
- Password generation.
- Toggle password visibility in add/edit and details screens.

## Requirements
- Android SDK
- Kotlin
- Jetpack Compose
- Room Database

## Installation
1. Clone the repository: `git clone https://github.com/ideepaknishadd/PasswordManager.git`
2. Open in Android Studio.
3. Build and run on an emulator or physical device.

## Usage
1. Launch the app. If biometric authentication is enabled, authenticate using your biometric.
2. View the password list on the Home screen.
3. Click a password to view details in a bottom sheet dialog or use the FAB to add a new password via a bottom sheet.
4. Edit or delete passwords via the bottom sheet dialogs.
5. Toggle password visibility in add/edit and details screens.