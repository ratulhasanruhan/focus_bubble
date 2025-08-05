package com.appibrium.focus_bubble

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class TextAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_VIEW_FOCUSED or 
                        AccessibilityEvent.TYPE_VIEW_CLICKED or
                        AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                        AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                   AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE
            notificationTimeout = 100
        }
        
        setServiceInfo(info)
        Log.d("WritingAssistant", "Accessibility Service Connected!")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let { 
            Log.d("WritingAssistant", "Event received: ${event.eventType}, Class: ${event.className}")
            
            when (it.eventType) {
                AccessibilityEvent.TYPE_VIEW_FOCUSED -> {
                    Log.d("WritingAssistant", "Focus event detected")
                    if (isTextField(event)) {
                        Log.d("WritingAssistant", "Text field focused - showing bubble")
                        // Show bubble overlay
                        val intent = Intent(this, OverlayService::class.java)
                        intent.putExtra("action", "show_bubble")
                        startService(intent)
                    }
                }
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                    Log.d("WritingAssistant", "Window state changed - hiding bubble")
                    // Hide bubble when window changes
                    val intent = Intent(this, OverlayService::class.java)
                    intent.putExtra("action", "hide_bubble")
                    startService(intent)
                }
                AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                    Log.d("WritingAssistant", "Click event detected")
                    // Hide bubble when clicking outside text fields
                    if (!isTextField(event)) {
                        Log.d("WritingAssistant", "Clicked outside text field - hiding bubble")
                        val intent = Intent(this, OverlayService::class.java)
                        intent.putExtra("action", "hide_bubble")
                        startService(intent)
                    }
                }
                AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                    Log.d("WritingAssistant", "Text change event detected")
                    if (isTextField(event)) {
                        Log.d("WritingAssistant", "Text changed in text field - showing bubble")
                        val intent = Intent(this, OverlayService::class.java)
                        intent.putExtra("action", "show_bubble")
                        startService(intent)
                    }
                }
            }
        }
    }

    override fun onInterrupt() {
        Log.d("WritingAssistant", "Accessibility Service Interrupted")
    }

    private fun isTextField(event: AccessibilityEvent): Boolean {
        val className = event.className?.toString() ?: ""
        val isEditable = event.source?.isEditable ?: false
        
        val isEditText = className.contains("EditText")
        val isTextView = className.contains("TextView") && isEditable
        val isInputField = className.contains("Input") || className.contains("Field")
        val isTextInput = className.contains("TextInput")
        val isWebView = className.contains("WebView") && isEditable
        val isAutoCompleteTextView = className.contains("AutoCompleteTextView")
        val isMultiAutoCompleteTextView = className.contains("MultiAutoCompleteTextView")
        
        val result = isEditText || isTextView || isInputField || isTextInput || isWebView || isAutoCompleteTextView || isMultiAutoCompleteTextView || isEditable
        
        Log.d("WritingAssistant", "isTextField check: $result, Class: $className, Editable: $isEditable")
        return result
    }
}
