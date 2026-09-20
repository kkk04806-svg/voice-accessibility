package com.vanraj.assistant;

import android.accessibilityservice.AccessibilityService;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class VoiceAccessibilityService
        extends AccessibilityService {

    private static final String TAG =
            "VoiceAssistant";

    private static VoiceAccessibilityService instance;

    @Override
    protected void onServiceConnected() {

        super.onServiceConnected();

        instance = this;

        Log.d(
                TAG,
                "Accessibility Service Connected"
        );
    }

    public static VoiceAccessibilityService getInstance() {

        return instance;
    }

    public static boolean isRunning() {

        return instance != null;
    }

    @Override
    public void onAccessibilityEvent(
            AccessibilityEvent event) {

        // Screen events can be handled here.
    }

    @Override
    public void onInterrupt() {

        Log.d(
                TAG,
                "Accessibility interrupted"
        );
    }

    @Override
    public void onDestroy() {

        instance = null;

        Log.d(
                TAG,
                "Accessibility destroyed"
        );

        super.onDestroy();
    }

    public boolean typeText(
            String text) {

        if (text == null) {
            text = "";
        }

        AccessibilityNodeInfo root =
                getRootInActiveWindow();

        if (root == null) {

            Log.e(
                    TAG,
                    "Active window nahi mila"
            );

            return false;
        }

        AccessibilityNodeInfo input =
                root.findFocus(
                        AccessibilityNodeInfo.FOCUS_INPUT
                );

        if (input == null ||
                !input.isEditable()) {

            if (input != null) {
                input.recycle();
            }

            input =
                    findEditableField(root);
        }

        if (input == null) {

            root.recycle();

            Log.e(
                    TAG,
                    "Editable field nahi mila"
            );

            return false;
        }

        Bundle arguments =
                new Bundle();

        arguments.putCharSequence(
                AccessibilityNodeInfo
                        .ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                text
        );

        boolean result =
                input.performAction(
                        AccessibilityNodeInfo.ACTION_SET_TEXT,
                        arguments
                );

        input.recycle();
        root.recycle();

        Log.d(
                TAG,
                "Type result = " + result
        );

        return result;
    }

    public boolean clickText(
            String text) {

        if (text == null ||
                text.trim().isEmpty()) {

            return false;
        }

        AccessibilityNodeInfo root =
                getRootInActiveWindow();

        if (root == null) {
            return false;
        }

        boolean result =
                clickRecursive(
                        root,
                        text.toLowerCase()
                );

        root.recycle();

        return result;
    }

    private boolean clickRecursive(
            AccessibilityNodeInfo node,
            String wanted) {

        if (node == null) {
            return false;
        }

        CharSequence text =
                node.getText();

        CharSequence description =
                node.getContentDescription();

        String nodeText =
                text == null
                        ? ""
                        : text.toString()
                                .toLowerCase();

        String nodeDescription =
                description == null
                        ? ""
                        : description.toString()
                                .toLowerCase();

        if (node.isClickable() &&
                (
                        nodeText.contains(wanted) ||
                        nodeDescription.contains(wanted)
                )) {

            return node.performAction(
                    AccessibilityNodeInfo.ACTION_CLICK
            );
        }

        for (int i = 0;
             i < node.getChildCount();
             i++) {

            AccessibilityNodeInfo child =
                    node.getChild(i);

            if (child == null) {
                continue;
            }

            boolean result =
                    clickRecursive(
                            child,
                            wanted
                    );

            child.recycle();

            if (result) {
                return true;
            }
        }

        return false;
    }

    private AccessibilityNodeInfo findEditableField(
            AccessibilityNodeInfo node) {

        if (node == null) {
            return null;
        }

        if (node.isEditable() &&
                node.isFocused()) {

            return AccessibilityNodeInfo.obtain(
                    node
            );
        }

        for (int i = 0;
             i < node.getChildCount();
             i++) {

            AccessibilityNodeInfo child =
                    node.getChild(i);

            if (child == null) {
                continue;
            }

            AccessibilityNodeInfo result =
                    findEditableField(child);

            child.recycle();

            if (result != null) {
                return result;
            }
        }

        return null;
    }

    public boolean goBack() {

        return performGlobalAction(
                GLOBAL_ACTION_BACK
        );
    }

    public boolean goHome() {

        return performGlobalAction(
                GLOBAL_ACTION_HOME
        );
    }

    public boolean openRecents() {

        return performGlobalAction(
                GLOBAL_ACTION_RECENTS
        );
    }

    public boolean scrollDown() {

        AccessibilityNodeInfo root =
                getRootInActiveWindow();

        if (root == null) {
            return false;
        }

        boolean result =
                root.performAction(
                        AccessibilityNodeInfo
                                .ACTION_SCROLL_FORWARD
                );

        root.recycle();

        return result;
    }

    public boolean scrollUp() {

        AccessibilityNodeInfo root =
                getRootInActiveWindow();

        if (root == null) {
            return false;
        }

        boolean result =
                root.performAction(
                        AccessibilityNodeInfo
                                .ACTION_SCROLL_BACKWARD
                );

        root.recycle();

        return result;
    }
}
