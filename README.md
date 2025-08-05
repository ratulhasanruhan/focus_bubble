# Writing Assistant - System-Wide Bubble Overlay

A Flutter application that provides system-wide writing assistance through overlay bubbles. When you focus on text fields in any app, a bubble appears with options for spell checking, grammar fixing, and other writing assistance features.

## Features

- **System-wide overlay**: Works across all Android apps
- **Text field detection**: Automatically detects when you focus on text fields
- **Writing assistance**: Provides spell check and grammar fix options
- **Permission management**: Handles overlay and accessibility permissions
- **Foreground service**: Runs reliably in the background

## How it works

1. **Grant permissions**: The app requests overlay and accessibility permissions
2. **Start service**: Activate the overlay service from the app
3. **Use anywhere**: Open any app with text fields
4. **Get assistance**: When you focus on a text field, a bubble appears with writing help options

## Technical Details

### Android Components

- **MainActivity**: Handles permission requests and service management
- **OverlayService**: Creates and manages the floating bubble overlay
- **TextAccessibilityService**: Detects text field focus events across all apps
- **Layout resources**: Custom bubble UI with action buttons

### Permissions Required

- `SYSTEM_ALERT_WINDOW`: Allows drawing overlays on top of other apps
- `FOREGROUND_SERVICE`: Enables the overlay service to run in background
- `BIND_ACCESSIBILITY_SERVICE`: Allows monitoring text field focus events

### Architecture

The app uses a combination of:
- **Accessibility Service**: Monitors system-wide text field focus events
- **Foreground Service**: Manages the overlay bubble lifecycle
- **Method Channel**: Communication between Flutter and native Android code
- **Window Manager**: Creates and positions the overlay bubble

## Setup Instructions

1. Install the app on an Android device
2. Grant overlay permission when prompted
3. Enable the accessibility service in Android settings
4. Start the overlay service from the app
5. Use any app with text fields to see the writing assistance bubble

## Limitations

- **Android only**: iOS doesn't allow system-wide overlays
- **Manual permissions**: Users must manually grant overlay and accessibility permissions
- **Android version restrictions**: Some Android versions may limit overlay functionality

## Development

This is a Flutter project with custom Android native code for overlay and accessibility services. The main components are:

- `lib/main.dart`: Flutter UI for permission management and service control
- `android/app/src/main/kotlin/`: Native Android services and activities
- `android/app/src/main/res/`: Layout and resource files for the overlay bubble

## Building

```bash
flutter pub get
flutter build apk
```

The app requires Android API level 21+ for overlay functionality.
