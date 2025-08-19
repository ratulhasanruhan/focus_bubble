🔹 Why You Need MethodChannel

Flutter (with flutter_windows embedding) can only access its own widgets by default. To monitor system-wide inputs (like Notepad, Chrome, Word, etc.), you’ll need native Windows APIs (Win32) via a MethodChannel.

So the flow is:

Flutter App ↔ MethodChannel ↔ Windows Native (C++/C#/Rust) ↔ System Text Input

🔹 Steps to Implement
1. Setup MethodChannel

In your Flutter app, create a channel:

import 'package:flutter/services.dart';

class NativeBridge {
static const platform = MethodChannel('text_tracker');

static Future<void> startTracking() async {
await platform.invokeMethod('startTracking');
}

static Future<void> stopTracking() async {
await platform.invokeMethod('stopTracking');
}
}

2. Windows Side (C++ / C#)

Inside the windows/ folder of Flutter, modify runner.cpp or add a plugin:

Use Windows Hooks (SetWindowsHookEx) with WH_KEYBOARD_LL and WH_CBT to track:

Keyboard input

Focus changes (detects which control / textfield is active)

Example C++ pseudocode:

HHOOK keyboardHook;
HHOOK cbtHook;

LRESULT CALLBACK KeyboardProc(int code, WPARAM wParam, LPARAM lParam) {
if (code >= 0) {
// Send key info to Flutter
std::string keyData = "Key pressed!";
// Use FlutterMethodChannel to send back
}
return CallNextHookEx(keyboardHook, code, wParam, lParam);
}

LRESULT CALLBACK CbtProc(int code, WPARAM wParam, LPARAM lParam) {
if (code == HCBT_SETFOCUS) {
// Detect active textfield window handle
HWND hwnd = (HWND)wParam;
// Send hwnd info back to Flutter
}
return CallNextHookEx(cbtHook, code, wParam, lParam);
}

3. Sending Data Back to Flutter

On Windows native side, you call back to Flutter using:

flutter::EncodableMap response;
response[flutter::EncodableValue("event")] = flutter::EncodableValue("focus");
response[flutter::EncodableValue("hwnd")] = flutter::EncodableValue((int64_t)hwnd);

channel->InvokeMethod("onFocusChanged", std::make_unique<flutter::EncodableValue>(response));


On Dart side:

NativeBridge.platform.setMethodCallHandler((call) async {
if (call.method == "onFocusChanged") {
var hwnd = call.arguments["hwnd"];
// Show your bubble overlay on top of the textfield
}
});

4. Drawing Bubble Overlay

In Flutter, use a transparent always-on-top window (like bitsdojo_window + Win32 overlay APIs) OR a popup widget aligned to the detected hwnd position.

To get position of target textfield:

Use GetWindowRect(hwnd, &rect) in C++.

Send rect to Flutter.

Flutter positions bubble accordingly.

✅ Verdict:
Yes, you can absolutely build this system with Flutter + Windows MethodChannel.
But:

You must write native C++/C# code to hook into Windows text inputs.

Flutter handles only UI + bubble overlay.