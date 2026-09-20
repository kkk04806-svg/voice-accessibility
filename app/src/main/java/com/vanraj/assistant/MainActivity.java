package com.vanraj.assistant;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    private SpeechRecognizer speechRecognizer;
    private Intent speechIntent;
    private TextToSpeech tts;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 30, 30, 30);

        TextView title = new TextView(this);
        title.setText("VANRAJ VOICE ASSISTANT");
        title.setTextSize(22);

        statusText = new TextView(this);
        statusText.setText(
                "Ready bhai.\n\n" +
                "Accessibility Service ON rakho.\n" +
                "Neeche Listen dabao aur command bolo."
        );
        statusText.setTextSize(17);
        statusText.setPadding(0, 30, 0, 30);

        Button listenButton = new Button(this);
        listenButton.setText("🎤 LISTEN");

        layout.addView(title);
        layout.addView(statusText);
        layout.addView(listenButton);

        setContentView(layout);

        // Text to Speech
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.ENGLISH);
            }
        });

        // Microphone permission
        if (android.os.Build.VERSION.SDK_INT >= 23) {

            if (checkSelfPermission(
                    Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{
                                Manifest.permission.RECORD_AUDIO
                        },
                        100
                );
            }
        }

        setupSpeech();

        listenButton.setOnClickListener(v -> startListening());
    }

    private void setupSpeech() {

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {

            statusText.setText(
                    "Speech recognition available nahi hai."
            );

            return;
        }

        speechRecognizer =
                SpeechRecognizer.createSpeechRecognizer(this);

        speechIntent =
                new Intent(
                        RecognizerIntent.ACTION_RECOGNIZE_SPEECH
                );

        speechIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );

        speechIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                "en-IN"
        );

        speechIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "hi-IN"
        );

        speechIntent.putExtra(
                RecognizerIntent.EXTRA_MAX_RESULTS,
                3
        );

        speechRecognizer.setRecognitionListener(
                new RecognitionListener() {

                    @Override
                    public void onReadyForSpeech(
                            Bundle params) {

                        statusText.setText(
                                "🎤 Sun raha hoon bhai..."
                        );
                    }

                    @Override
                    public void onBeginningOfSpeech() {

                        statusText.setText(
                                "👂 Bolo bhai..."
                        );
                    }

                    @Override
                    public void onRmsChanged(
                            float rmsdB) {
                    }

                    @Override
                    public void onBufferReceived(
                            byte[] buffer) {
                    }

                    @Override
                    public void onEndOfSpeech() {

                        statusText.setText(
                                "Processing..."
                        );
                    }

                    @Override
                    public void onError(
                            int error) {

                        statusText.setText(
                                "Speech error: " + error
                        );
                    }

                    @Override
                    public void onResults(
                            Bundle results) {

                        ArrayList<String> list =
                                results.getStringArrayList(
                                        SpeechRecognizer
                                                .RESULTS_RECOGNITION
                                );

                        if (list == null ||
                                list.isEmpty()) {

                            statusText.setText(
                                    "Kuch suna nahi bhai."
                            );

                            return;
                        }

                        String command = list.get(0);

                        statusText.setText(
                                "You: " + command
                        );

                        executeCommand(command);
                    }

                    @Override
                    public void onPartialResults(
                            Bundle partialResults) {
                    }

                    @Override
                    public void onEvent(
                            int eventType,
                            Bundle params) {
                    }
                }
        );
    }

    private void startListening() {

        if (speechRecognizer == null) {

            setupSpeech();

            if (speechRecognizer == null) {
                return;
            }
        }

        try {

            speechRecognizer.startListening(
                    speechIntent
            );

        } catch (Exception e) {

            statusText.setText(
                    "Listening error: " +
                    e.getMessage()
            );
        }
    }

    private void executeCommand(String command) {

        if (command == null) {
            return;
        }

        String lower =
                command.toLowerCase(Locale.ROOT).trim();

        // HOME
        if (lower.equals("home") ||
                lower.contains("home jao") ||
                lower.contains("home kholo")) {

            VoiceAccessibilityService service =
                    VoiceAccessibilityService.getInstance();

            if (service != null) {
                service.goHome();
                speak("Home khol diya bhai");
            } else {
                speak("Accessibility service ON nahi hai");
            }

            return;
        }

        // BACK
        if (lower.equals("back") ||
                lower.contains("back karo") ||
                lower.contains("peeche jao")) {

            VoiceAccessibilityService service =
                    VoiceAccessibilityService.getInstance();

            if (service != null) {
                service.goBack();
                speak("Back kar diya bhai");
            } else {
                speak("Accessibility service ON nahi hai");
            }

            return;
        }

        // RECENTS
        if (lower.contains("recent apps") ||
                lower.contains("recent kholo")) {

            VoiceAccessibilityService service =
                    VoiceAccessibilityService.getInstance();

            if (service != null) {
                service.openRecents();
                speak("Recent apps khol diya bhai");
            }

            return;
        }

        // SCROLL DOWN
        if (lower.contains("scroll down") ||
                lower.contains("neeche scroll")) {

            VoiceAccessibilityService service =
                    VoiceAccessibilityService.getInstance();

            if (service != null) {
                service.scrollDown();
                speak("Neeche scroll kar diya");
            }

            return;
        }

        // SCROLL UP
        if (lower.contains("scroll up") ||
                lower.contains("upar scroll")) {

            VoiceAccessibilityService service =
                    VoiceAccessibilityService.getInstance();

            if (service != null) {
                service.scrollUp();
                speak("Upar scroll kar diya");
            }

            return;
        }

        // TYPE
        if (lower.startsWith("type ") ||
                lower.startsWith("likho ") ||
                lower.contains("message likho")) {

            String text = extractText(command);

            VoiceAccessibilityService service =
                    VoiceAccessibilityService.getInstance();

            if (service != null) {

                service.typeText(text);

                speak("Text type kar diya bhai");

            } else {

                speak(
                        "Accessibility service ON nahi hai"
                );
            }

            return;
        }

        // OPEN APP
        String appName =
                extractAppName(command);

        if (!appName.isEmpty()) {

            openApp(appName);

            return;
        }

        // SEARCH
        if (lower.startsWith("search ")) {

            String query =
                    command.substring(7).trim();

            searchWeb(query);

            return;
        }

        speak(
                "Command abhi samajh nahi aayi bhai"
        );
    }

    private String extractText(String command) {

        String lower =
                command.toLowerCase(Locale.ROOT);

        if (lower.startsWith("type ")) {

            return command.substring(5).trim();
        }

        if (lower.startsWith("likho ")) {

            return command.substring(6).trim();
        }

        int index =
                lower.indexOf("message likho");

        if (index >= 0) {

            return command.substring(
                    index + "message likho".length()
            ).trim();
        }

        return command;
    }

    private String extractAppName(String command) {

        String lower =
                command.toLowerCase(Locale.ROOT);

        String[] apps = {
                "whatsapp",
                "telegram",
                "chrome",
                "youtube",
                "calculator",
                "gmail",
                "instagram",
                "facebook",
                "spotify",
                "maps",
                "play store",
                "outlook",
                "snapchat"
        };

        for (String app : apps) {

            if (lower.equals(app) ||
                    lower.equals(app + " kholo") ||
                    lower.equals("open " + app) ||
                    lower.equals("open " + app + " app")) {

                return app;
            }
        }

        return "";
    }

    private void openApp(String appName) {

        String packageName = null;

        if (appName.equals("whatsapp"))
            packageName = "com.whatsapp";

        else if (appName.equals("telegram"))
            packageName = "org.telegram.messenger";

        else if (appName.equals("chrome"))
            packageName = "com.android.chrome";

        else if (appName.equals("youtube"))
            packageName = "com.google.android.youtube";

        else if (appName.equals("calculator"))
            packageName = "com.vivo.calculator";

        else if (appName.equals("gmail"))
            packageName = "com.google.android.gm";

        else if (appName.equals("instagram"))
            packageName = "com.instagram.android";

        else if (appName.equals("facebook"))
            packageName = "com.facebook.katana";

        else if (appName.equals("spotify"))
            packageName = "com.spotify.music";

        else if (appName.equals("maps"))
            packageName =
                    "com.google.android.apps.maps";

        else if (appName.equals("play store"))
            packageName = "com.android.vending";

        else if (appName.equals("outlook"))
            packageName =
                    "com.microsoft.office.outlook";

        else if (appName.equals("snapchat"))
            packageName =
                    "com.snapchat.android";

        if (packageName == null) {

            speak("App ka naam samajh nahi aaya");

            return;
        }

        try {

            Intent launchIntent =
                    getPackageManager()
                            .getLaunchIntentForPackage(
                                    packageName
                            );

            if (launchIntent == null) {

                speak(
                        appName +
                        " phone mein nahi mila"
                );

                return;
            }

            launchIntent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
            );

            startActivity(launchIntent);

            speak(
                    appName +
                    " khol diya bhai"
            );

        } catch (Exception e) {

            speak(
                    appName +
                    " open nahi hua"
            );
        }
    }

    private void searchWeb(String query) {

        try {

            String url =
                    "https://www.google.com/search?q=" +
                    android.net.Uri.encode(query);

            Intent intent =
                    new Intent(
                            Intent.ACTION_VIEW,
                            android.net.Uri.parse(url)
                    );

            startActivity(intent);

            speak("Search khol diya bhai");

        } catch (Exception e) {

            speak("Search open nahi hua");
        }
    }

    private void speak(String text) {

        if (tts == null) {
            return;
        }

        tts.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "assistant_reply"
        );
    }

    @Override
    protected void onDestroy() {

        if (speechRecognizer != null) {

            speechRecognizer.destroy();
            speechRecognizer = null;
        }

        if (tts != null) {

            tts.stop();
            tts.shutdown();
            tts = null;
        }

        super.onDestroy();
    }
}
