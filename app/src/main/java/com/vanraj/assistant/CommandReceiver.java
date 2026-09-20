package com.vanraj.assistant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class CommandReceiver
        extends BroadcastReceiver {

    public static final String TYPE_TEXT =
            "com.vanraj.assistant.TYPE_TEXT";

    private static final String TAG =
            "VoiceAssistant";

    @Override
    public void onReceive(
            Context context,
            Intent intent) {

        if (intent == null) {
            return;
        }

        String action =
                intent.getAction();

        if (!TYPE_TEXT.equals(action)) {
            return;
        }

        String text =
                intent.getStringExtra(
                        "text"
                );

        if (text == null) {
            text = "";
        }

        VoiceAccessibilityService service =
                VoiceAccessibilityService
                        .getInstance();

        if (service == null) {

            Log.e(
                    TAG,
                    "Accessibility Service OFF"
            );

            return;
        }

        boolean result =
                service.typeText(text);

        Log.d(
                TAG,
                "Typing result = " + result
        );
    }
}
