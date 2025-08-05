package com.appibrium.focus_bubble

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class TextAccessibilityService : AccessibilityService() {
    private var currentFocusedTextField: String? = null
    private var hideHandler: android.os.Handler? = null
    private var hideRunnable: Runnable? = null
    private val HIDE_DELAY = 2000L // 2 seconds delay before hiding

    override fun onServiceConnected() {
        super.onServiceConnected()
        
        hideHandler = android.os.Handler(android.os.Looper.getMainLooper())
        
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED or
                        AccessibilityEvent.TYPE_VIEW_FOCUSED or
                        AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            notificationTimeout = 50
        }
        
        setServiceInfo(info)
        Log.d("WritingAssistant", "Accessibility Service Connected!")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let { 
            Log.d("WritingAssistant", "Event received: ${event.eventType}, Class: ${event.className}")
            
            when (it.eventType) {
                AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                    Log.d("WritingAssistant", "Text change event detected")
                    if (isTextField(event)) {
                        Log.d("WritingAssistant", "User is typing - showing bubble")
                        showBubble()
                        // Start timer to hide bubble after typing stops
                        startHideTimer()
                    }
                }
                AccessibilityEvent.TYPE_VIEW_FOCUSED -> {
                    Log.d("WritingAssistant", "Focus event detected")
                    if (isTextField(event)) {
                        Log.d("WritingAssistant", "Text field focused - showing bubble")
                        showBubble()
                        startHideTimer()
                    }
                }
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                    Log.d("WritingAssistant", "Window state changed - hiding bubble")
                    hideBubble()
                }
            }
        }
    }

    private fun showBubble() {
        val intent = Intent(this, OverlayService::class.java)
        intent.putExtra("action", "show_bubble")
        startService(intent)
    }

    private fun hideBubble() {
        val intent = Intent(this, OverlayService::class.java)
        intent.putExtra("action", "hide_bubble")
        startService(intent)
    }

    private fun startHideTimer() {
        cancelHideTimer() // Cancel any existing timer
        hideRunnable = Runnable {
            Log.d("WritingAssistant", "Hide timer expired - hiding bubble")
            currentFocusedTextField = null
            hideBubble()
        }
        hideHandler?.postDelayed(hideRunnable!!, HIDE_DELAY)
        Log.d("WritingAssistant", "Hide timer started with ${HIDE_DELAY}ms delay")
    }

    private fun cancelHideTimer() {
        hideRunnable?.let {
            hideHandler?.removeCallbacks(it)
            hideRunnable = null
            Log.d("WritingAssistant", "Hide timer cancelled")
        }
    }

    override fun onInterrupt() {
        Log.d("WritingAssistant", "Accessibility Service Interrupted")
        cancelHideTimer()
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelHideTimer()
    }

    private fun getFieldIdentifier(event: AccessibilityEvent): String {
        val source = event.source
        return if (source != null) {
            "${source.className}_${source.viewIdResourceName}_${source.windowId}"
        } else {
            "${event.className}_${event.windowId}"
        }
    }

    private fun isTextField(event: AccessibilityEvent): Boolean {
        val className = event.className?.toString() ?: ""
        val isEditable = event.source?.isEditable ?: false
        val contentDescription = event.contentDescription?.toString() ?: ""
        val text = event.text?.toString() ?: ""
        
        val isEditText = className.contains("EditText")
        val isTextView = className.contains("TextView") && isEditable
        val isInputField = className.contains("Input") || className.contains("Field")
        val isTextInput = className.contains("TextInput")
        val isWebView = className.contains("WebView") && isEditable
        val isAutoCompleteTextView = className.contains("AutoCompleteTextView")
        val isMultiAutoCompleteTextView = className.contains("MultiAutoCompleteTextView")
        val isSearchView = className.contains("SearchView")
        val isSearchAutoComplete = className.contains("SearchAutoComplete")
        
        // Additional checks for common input patterns
        val hasInputHint = contentDescription.contains("input") || contentDescription.contains("text")
        val hasTextContent = text.isNotEmpty() && text.length < 1000 // Reasonable text length
        
        val result = isEditText || isTextView || isInputField || isTextInput || isWebView || 
                    isAutoCompleteTextView || isMultiAutoCompleteTextView || isSearchView || 
                    isSearchAutoComplete || isEditable || hasInputHint || hasTextContent
        
        Log.d("WritingAssistant", "isTextField check: $result, Class: $className, Editable: $isEditable, ContentDesc: $contentDescription")
        return result
    }
}
