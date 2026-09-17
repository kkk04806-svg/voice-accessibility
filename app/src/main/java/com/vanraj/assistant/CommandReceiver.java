package com.vanraj.assistant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class CommandReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (!"com.vanraj.assistant.TYPE_TEXT".equals(intent.getAction())) {
            return;
        }

        String text = intent.getStringExtra("text");
        if (text == null) {
            text = "";
        }

        VoiceAccessibilityService service =
                VoiceAccessibilityService.getInstance();

        boolean ok = service != null && service.typeText(text);

        Log.d("VoiceAssistant", "Broadcast typing: " + ok);
    }
}
