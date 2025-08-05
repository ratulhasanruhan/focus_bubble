// main.dart
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'System Overlay App',
      theme: ThemeData(primarySwatch: Colors.blue),
      home: OverlayControlPage(),
    );
  }
}

class OverlayControlPage extends StatefulWidget {
  @override
  _OverlayControlPageState createState() => _OverlayControlPageState();
}

class _OverlayControlPageState extends State<OverlayControlPage> {
  static const platform = MethodChannel('com.example.overlay/system_overlay');
  bool _isOverlayActive = false;
  bool _hasPermission = false;

  @override
  void initState() {
    super.initState();
    _checkPermission();
    _setupMethodCallHandler();
  }

  void _setupMethodCallHandler() {
    platform.setMethodCallHandler((call) async {
      switch (call.method) {
        case 'onTextFieldFocused':
          print('Text field focused in external app');
          break;
        case 'onTextFieldUnfocused':
          print('Text field unfocused in external app');
          break;
      }
    });
  }

  Future<void> _checkPermission() async {
    try {
      final bool hasPermission = await platform.invokeMethod(
        'checkOverlayPermission',
      );
      setState(() {
        _hasPermission = hasPermission;
      });
    } catch (e) {
      print('Error checking permission: $e');
    }
  }

  Future<void> _requestPermission() async {
    try {
      await platform.invokeMethod('requestOverlayPermission');
      await _checkPermission();
    } catch (e) {
      print('Error requesting permission: $e');
    }
  }

  Future<void> _startOverlayService() async {
    try {
      await platform.invokeMethod('startOverlayService');
      setState(() {
        _isOverlayActive = true;
      });
    } catch (e) {
      print('Error starting overlay: $e');
    }
  }

  Future<void> _stopOverlayService() async {
    try {
      await platform.invokeMethod('stopOverlayService');
      setState(() {
        _isOverlayActive = false;
      });
    } catch (e) {
      print('Error stopping overlay: $e');
    }
  }

  Future<void> _testOverlay() async {
    try {
      await platform.invokeMethod('testOverlay');
      print('Test overlay triggered');

      // Show a snackbar to confirm the test
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            'Test overlay triggered! Bubble should appear for 3 seconds.',
          ),
          duration: Duration(seconds: 2),
        ),
      );
    } catch (e) {
      print('Error testing overlay: $e');
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Error testing overlay: $e'),
          backgroundColor: Colors.red,
        ),
      );
    }
  }

  Future<void> _checkAccessibilityService() async {
    try {
      final bool isEnabled = await platform.invokeMethod(
        'checkAccessibilityService',
      );
      print('Accessibility service enabled: $isEnabled');

      // Show dialog with status
      showDialog(
        context: context,
        builder: (BuildContext context) {
          return AlertDialog(
            title: Text('Accessibility Service Status'),
            content: Text(
              isEnabled
                  ? '✅ Accessibility service is enabled and working!'
                  : '❌ Accessibility service is not enabled.\n\nPlease enable it in Android Settings to detect text fields in other apps.',
            ),
            actions: [
              TextButton(
                onPressed: () => Navigator.of(context).pop(),
                child: Text('OK'),
              ),
              if (!isEnabled)
                TextButton(
                  onPressed: () async {
                    Navigator.of(context).pop();
                    await platform.invokeMethod('openAccessibilitySettings');
                  },
                  child: Text('Open Settings'),
                ),
            ],
          );
        },
      );
    } catch (e) {
      print('Error checking accessibility service: $e');
      // Show error dialog
      showDialog(
        context: context,
        builder: (BuildContext context) {
          return AlertDialog(
            title: Text('Error'),
            content: Text('Failed to check accessibility service status: $e'),
            actions: [
              TextButton(
                onPressed: () => Navigator.of(context).pop(),
                child: Text('OK'),
              ),
            ],
          );
        },
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('System-Wide Bubble Overlay'),
        backgroundColor: Colors.blue,
      ),
      body: SingleChildScrollView(
        child: Padding(
          padding: EdgeInsets.all(20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Card(
                child: Padding(
                  padding: EdgeInsets.all(16),
                  child: Column(
                    children: [
                      Icon(
                        _hasPermission ? Icons.check_circle : Icons.warning,
                        size: 48,
                        color: _hasPermission ? Colors.green : Colors.orange,
                      ),
                      SizedBox(height: 16),
                      Text(
                        _hasPermission
                            ? 'Overlay Permission Granted'
                            : 'Overlay Permission Required',
                        style: TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      SizedBox(height: 8),
                      Text(
                        _hasPermission
                            ? 'Your app can now show overlays on top of other apps.'
                            : 'To show bubbles over other apps, you need to grant overlay permission.',
                        textAlign: TextAlign.center,
                        style: TextStyle(color: Colors.grey[600]),
                      ),
                    ],
                  ),
                ),
              ),
              SizedBox(height: 20),
              if (!_hasPermission)
                ElevatedButton(
                  onPressed: _requestPermission,
                  child: Text('Request Overlay Permission'),
                  style: ElevatedButton.styleFrom(
                    padding: EdgeInsets.symmetric(vertical: 16),
                  ),
                ),
              if (_hasPermission) ...[
                Card(
                  child: Padding(
                    padding: EdgeInsets.all(16),
                    child: Column(
                      children: [
                        Icon(
                          _isOverlayActive
                              ? Icons.visibility
                              : Icons.visibility_off,
                          size: 48,
                          color: _isOverlayActive ? Colors.green : Colors.grey,
                        ),
                        SizedBox(height: 16),
                        Text(
                          _isOverlayActive
                              ? 'Overlay Active'
                              : 'Overlay Inactive',
                          style: TextStyle(
                            fontSize: 18,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        SizedBox(height: 8),
                        Text(
                          _isOverlayActive
                              ? 'The bubble will appear when you type in other apps.'
                              : 'Start the overlay service to show bubbles system-wide.',
                          textAlign: TextAlign.center,
                          style: TextStyle(color: Colors.grey[600]),
                        ),
                      ],
                    ),
                  ),
                ),
                SizedBox(height: 16),
                ElevatedButton(
                  onPressed: _isOverlayActive
                      ? _stopOverlayService
                      : _startOverlayService,
                  child: Text(
                    _isOverlayActive ? 'Stop Overlay' : 'Start Overlay',
                  ),
                  style: ElevatedButton.styleFrom(
                    padding: EdgeInsets.symmetric(vertical: 16),
                    backgroundColor: _isOverlayActive
                        ? Colors.red
                        : Colors.green,
                  ),
                ),
                SizedBox(height: 16),
                ElevatedButton(
                  onPressed: _testOverlay,
                  child: Text('Test Overlay'),
                  style: ElevatedButton.styleFrom(
                    padding: EdgeInsets.symmetric(vertical: 16),
                    backgroundColor: Colors.orange,
                  ),
                ),
                SizedBox(height: 16),
                ElevatedButton(
                  onPressed: _checkAccessibilityService,
                  child: Text('Check Accessibility Service'),
                  style: ElevatedButton.styleFrom(
                    padding: EdgeInsets.symmetric(vertical: 16),
                    backgroundColor: Colors.purple,
                  ),
                ),
              ],
              SizedBox(height: 30),
              Container(
                padding: EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: Colors.blue[50],
                  borderRadius: BorderRadius.circular(8),
                  border: Border.all(color: Colors.blue[200]!),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'How it works:',
                      style: TextStyle(
                        fontWeight: FontWeight.bold,
                        color: Colors.blue[800],
                      ),
                    ),
                    SizedBox(height: 8),
                    Text(
                      '1. Grant overlay permission\n'
                      '2. Start the overlay service\n'
                      '3. Open any app with text fields\n'
                      '4. The bubble will appear when you focus on text fields\n'
                      '5. Tap the bubble for writing assistance',
                      style: TextStyle(color: Colors.blue[700]),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
