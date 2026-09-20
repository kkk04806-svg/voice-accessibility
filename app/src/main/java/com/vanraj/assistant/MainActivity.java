package com.vanraj.assistant;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);

        TextView title = new TextView(this);
        title.setText("Voice Assistant\n\nText Typing Engine");
        title.setTextSize(24);

        TextView info = new TextView(this);
        info.setText(
                "\nAccessibility Service ko ON karo.\n\n" +
                "Isse assistant active app ke text field mein text type kar sakega.\n\n" +
                "Service ON karne ke baad WhatsApp ya kisi bhi text box mein cursor rakho."
        );
        info.setTextSize(17);

        Button settingsButton = new Button(this);
        settingsButton.setText("OPEN ACCESSIBILITY SETTINGS");

        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(
                    Settings.ACTION_ACCESSIBILITY_SETTINGS
            );
            startActivity(intent);
        });

        layout.addView(title);
        layout.addView(info);
        layout.addView(settingsButton);

        setContentView(layout);
    }
}
