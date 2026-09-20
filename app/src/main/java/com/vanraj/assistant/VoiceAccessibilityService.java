package com.vanraj.assistant;

import android.accessibilityservice.AccessibilityService;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class VoiceAccessibilityService extends AccessibilityService {

    private static final String TAG = "VoiceAssistant";

    private static VoiceAccessibilityService instance;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        instance = this;

        Log.d(TAG, "Accessibility Service Connected");
    }

    public static VoiceAccessibilityService getInstance() {
        return instance;
    }

    public static boolean isRunning() {
        return instance != null;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Accessibility events yahan receive hote hain.
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Accessibility Service Interrupted");
    }

    @Override
    public void onDestroy() {
        instance = null;
        Log.d(TAG, "Accessibility Service Destroyed");
        super.onDestroy();
    }

    public boolean typeText(String text) {

        if (text == null) {
            text = "";
        }

        AccessibilityNodeInfo root = getRootInActiveWindow();

        if (root == null) {
            Log.e(TAG, "Active window nahi mila");
            return false;
        }

        AccessibilityNodeInfo input = root.findFocus(
                AccessibilityNodeInfo.FOCUS_INPUT
        );

        if (input == null || !input.isEditable()) {

            if (input != null) {
                input.recycle();
            }

            input = findEditableField(root);
        }

        if (input == null) {
            Log.e(TAG, "Editable text field nahi mila");
            root.recycle();
            return false;
        }

        Bundle arguments = new Bundle();

        arguments.putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                text
        );

        boolean result = input.performAction(
                AccessibilityNodeInfo.ACTION_SET_TEXT,
                arguments
        );

        Log.d(TAG, "Typing result: " + result);

        input.recycle();
        root.recycle();

        return result;
    }

    private AccessibilityNodeInfo findEditableField(
            AccessibilityNodeInfo node) {

        if (node == null) {
            return null;
        }

        if (node.isEditable() && node.isFocused()) {
            return AccessibilityNodeInfo.obtain(node);
        }

        for (int i = 0; i < node.getChildCount(); i++) {

            AccessibilityNodeInfo child = node.getChild(i);

            if (child == null) {
                continue;
            }

            AccessibilityNodeInfo result = findEditableField(child);

            child.recycle();

            if (result != null) {
                return result;
            }
        }

        return null;
    }
}
