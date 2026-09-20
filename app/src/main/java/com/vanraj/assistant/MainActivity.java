package com.vanraj.assistant;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    private SpeechRecognizer recognizer;
    private Intent speechIntent;
    private TextToSpeech tts;
    private TextView status;

    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        status = new TextView(this);
        status.setTextSize(20);
        status.setPadding(40, 60, 40, 40);
        status.setText("🤖 VANRAJ ASSISTANT\n\nStarting...");
        setContentView(status);

        tts = new TextToSpeech(this, result -> {
            if (result == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
            }
        });

        if (android.os.Build.VERSION.SDK_INT >= 23 &&
                checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    101
            );

        } else {
            startAssistant();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] results) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                results
        );

        if (requestCode == 101) {

            if (results.length > 0 &&
                    results[0] == PackageManager.PERMISSION_GRANTED) {

                startAssistant();

            } else {

                status.setText(
                        "❌ Microphone permission nahi mila.\n\n" +
                        "Settings → Apps → Voice Assistant → Permissions → Microphone ON karo."
                );
            }
        }
    }

    private void startAssistant() {

        status.setText(
                "🤖 VANRAJ ASSISTANT\n\n" +
                "Starting microphone..."
        );

        handler.postDelayed(() -> {

            setupSpeech();

            handler.postDelayed(() -> {
                listen();
            }, 1000);

        }, 1000);
    }

    private void setupSpeech() {

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {

            status.setText(
                    "❌ Speech Recognition available nahi hai.\n\n" +
                    "Google app / Speech Services by Google install ya enable karo."
            );

            return;
        }

        recognizer =
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
                "hi-IN"
        );

        speechIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "hi-IN"
        );

        speechIntent.putExtra(
                RecognizerIntent.EXTRA_MAX_RESULTS,
                3
        );

        speechIntent.putExtra(
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                false
        );

        recognizer.setRecognitionListener(
                new RecognitionListener() {

                    @Override
                    public void onReadyForSpeech(Bundle params) {

                        status.setText(
                                "🎤 SUN RAHA HOON BHAI...\n\n" +
                                "Bolo 👂"
                        );
                    }

                    @Override
                    public void onBeginningOfSpeech() {

                        status.setText(
                                "👂 Bolo bhai..."
                        );
                    }

                    @Override
                    public void onRmsChanged(float rmsdB) {
                    }

                    @Override
                    public void onBufferReceived(byte[] buffer) {
                    }

                    @Override
                    public void onEndOfSpeech() {

                        status.setText(
                                "⚙️ Command samajh raha hoon..."
                        );
                    }

                    @Override
                    public void onError(int error) {

                        status.setText(
                                "⚠️ Speech error " +
                                error +
                                "\n\nDobara sun raha hoon..."
                        );

                        restartListening();
                    }

                    @Override
                    public void onResults(Bundle results) {

                        ArrayList<String> list =
                                results.getStringArrayList(
                                        SpeechRecognizer.RESULTS_RECOGNITION
                                );

                        if (list == null ||
                                list.isEmpty()) {

                            restartListening();
                            return;
                        }

                        String command =
                                list.get(0);

                        status.setText(
                                "👤 You:\n" +
                                command +
                                "\n\n⚙️ Processing..."
                        );

                        executeCommand(command);

                        handler.postDelayed(() -> {
                            listen();
                        }, 1500);
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

    private void listen() {

        if (recognizer == null) {
            setupSpeech();
        }

        if (recognizer == null) {
            return;
        }

        try {

            recognizer.cancel();

            status.setText(
                    "🎤 SUN RAHA HOON BHAI...\n\n" +
                    "Bolo..."
            );

            recognizer.startListening(
                    speechIntent
            );

        } catch (Exception e) {

            status.setText(
                    "⚠️ Mic start nahi hua.\n\n" +
                    "Retry kar raha hoon..."
            );

            restartListening();
        }
    }

    private void restartListening() {

        handler.postDelayed(() -> {

            if (!isFinishing()) {
                listen();
            }

        }, 2000);
    }

    private void executeCommand(String command) {

        if (command == null ||
                command.trim().isEmpty()) {

            return;
        }

        String lower =
                command.toLowerCase(Locale.ROOT).trim();

        /*
         * HOME
         */

        if (lower.equals("home") ||
                lower.contains("home jao") ||
                lower.contains("home kholo")) {

            VoiceAccessibilityService service =
                    VoiceAccessibilityService.getInstance();

            if (service != null) {

                service.goHome();

                speak("Home khol diya bhai");

            } else {

                speak(
                        "Accessibility service ON nahi hai"
                );
            }

            return;
        }

        /*
         * BACK
         */

        if (lower.equals("back") ||
                lower.contains("back karo") ||
                lower.contains("peeche jao")) {

            VoiceAccessibilityService service =
                    VoiceAccessibilityService.getInstance();

            if (service != null) {

                service.goBack();

                speak("Back kar diya bhai");

            } else {

                speak(
                        "Accessibility service ON nahi hai"
                );
            }

            return;
        }

        /*
         * RECENTS
         */

        if (lower.contains("recent")) {

            VoiceAccessibilityService service =
                    VoiceAccessibilityService.getInstance();

            if (service != null) {

                service.openRecents();

                speak(
                        "Recent apps khol diya bhai"
                );
            }

            return;
        }

        /*
         * SCROLL
         */

        if (lower.contains("scroll down") ||
                lower.contains("neeche scroll")) {

            VoiceAccessibilityService service =
                    VoiceAccessibilityService.getInstance();

            if (service != null) {

                service.scrollDown();

                speak(
                        "Neeche scroll kar diya"
                );
            }

            return;
        }

        if (lower.contains("scroll up") ||
                lower.contains("upar scroll")) {

            VoiceAccessibilityService service =
                    VoiceAccessibilityService.getInstance();

            if (service != null) {

                service.scrollUp();

                speak(
                        "Upar scroll kar diya"
                );
            }

            return;
        }

        /*
         * TYPE
         */

        if (lower.startsWith("type ") ||
                lower.startsWith("likho ") ||
                lower.contains("message likho")) {

            String text =
                    extractText(command);

            VoiceAccessibilityService service =
                    VoiceAccessibilityService.getInstance();

            if (service != null) {

                boolean ok =
                        service.typeText(text);

                if (ok) {

                    speak(
                            "Text type kar diya bhai"
                    );

                } else {

                    speak(
                            "Text field nahi mili bhai"
                    );
                }

            } else {

                speak(
                        "Accessibility service ON nahi hai"
                );
            }

            return;
        }

        /*
         * SEARCH
         */

        if (lower.startsWith("search ")) {

            String query =
                    command.substring(7).trim();

            searchWeb(query);

            return;
        }

        /*
         * OPEN APP
         */

        String app =
                extractAppName(command);

        if (!app.isEmpty()) {

            openApp(app);

            return;
        }

        speak(
                "Command samajh nahi aayi bhai"
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
                    index +
                    "message likho".length()
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
            packageName = "com.google.android.apps.maps";

        else if (appName.equals("play store"))
            packageName = "com.android.vending";

        else if (appName.equals("outlook"))
            packageName = "com.microsoft.office.outlook";

        else if (appName.equals("snapchat"))
            packageName = "com.snapchat.android";

        if (packageName == null) {

            speak(
                    "App ka naam samajh nahi aaya"
            );

            return;
        }

        try {

            Intent intent =
                    getPackageManager()
                            .getLaunchIntentForPackage(
                                    packageName
                            );

            if (intent == null) {

                speak(
                        appName +
                        " phone mein nahi mila"
                );

                return;
            }

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
            );

            startActivity(intent);

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

            speak(
                    "Search khol diya bhai"
            );

        } catch (Exception e) {

            speak(
                    "Search open nahi hua"
            );
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

        handler.removeCallbacksAndMessages(null);

        if (recognizer != null) {

            recognizer.destroy();
            recognizer = null;
        }

        if (tts != null) {

            tts.stop();
            tts.shutdown();
            tts = null;
        }

        super.onDestroy();
    }
}
