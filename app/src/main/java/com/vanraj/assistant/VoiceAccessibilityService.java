package com.vanraj.assistant;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityNodeInfo;
import android.os.Bundle;
import android.util.Log;

public class VoiceAccessibilityService extends AccessibilityService {

    private static final String TAG = "VoiceAssistant";

    private static VoiceAccessibilityService instance;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        Log.d(TAG, "Accessibility Service Connected");
    }

    @Override
    public void onAccessibilityEvent(
            android.view.accessibility.AccessibilityEvent event) {
        // UI events are received here.
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Accessibility Service Interrupted");
    }

    @Override
    public void onDestroy() {
        instance = null;
        super.onDestroy();
    }

    public static boolean isRunning() {
        return instance != null;
    }

    public boolean typeText(String text) {

        AccessibilityNodeInfo root = getRootInActiveWindow();

        if (root == null) {
            Log.e(TAG, "Active window not available");
            return false;
        }

        AccessibilityNodeInfo input =
                findFocusedInput(root);

        if (input == null) {
            Log.e(TAG, "No focused input field found");
            return false;
        }

        Bundle args = new Bundle();
        args.putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                text
        );

        boolean result = input.performAction(
                AccessibilityNodeInfo.ACTION_SET_TEXT,
                args
        );

        input.recycle();

        Log.d(TAG, "Typing result: " + result);

        return result;
    }

    private AccessibilityNodeInfo findFocusedInput(
            AccessibilityNodeInfo node) {

        if (node == null) {
            return null;
        }

        if (node.isFocused() && node.isEditable()) {
            return AccessibilityNodeInfo.obtain(node);
        }

        for (int i = 0; i < node.getChildCount(); i++) {

            AccessibilityNodeInfo child = node.getChild(i);

            AccessibilityNodeInfo result =
                    findFocusedInput(child);

            if (child != null) {
                child.recycle();
            }

            if (result != null) {
                return result;
            }
        }

        return null;
    }
}
