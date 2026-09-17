package com.vanraj.assistant;

import android.accessibilityservice.AccessibilityService;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityEvent;

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

    public boolean typeText(String text) {

        AccessibilityNodeInfo root = getRootInActiveWindow();

        if (root == null) {
            Log.e(TAG, "Active window not available");
            return false;
        }

        AccessibilityNodeInfo input = root.findFocus(
                AccessibilityNodeInfo.FOCUS_INPUT
        );

        if (input == null || !input.isEditable()) {
            if (input != null) {
                input.recycle();
            }

            input = findEditable(root);
        }

        if (input == null) {
            Log.e(TAG, "No editable input field found");
            root.recycle();
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

        Log.d(TAG, "Typing result: " + result);

        input.recycle();
        root.recycle();

        return result;
    }

    private AccessibilityNodeInfo findEditable(
            AccessibilityNodeInfo node) {

        if (node == null) {
            return null;
        }

        if (node.isEditable() && node.isFocused()) {
            return AccessibilityNodeInfo.obtain(node);
        }

        for (int i = 0; i < node.getChildCount(); i++) {

            AccessibilityNodeInfo child = node.getChild(i);

            AccessibilityNodeInfo result = findEditable(child);

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
