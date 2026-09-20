package com.vanraj.assistant;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final int MIC_PERMISSION = 1001;

    private SpeechRecognizer recognizer;
    private Intent speechIntent;
    private TextToSpeech tts;

    private TextView status;
    private Button listenButton;

    private final Handler handler = new Handler();

    private boolean shouldListen = true;
    private boolean processing = false;
    private boolean voiceInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createUI();
        setupTTS();

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MIC_PERMISSION
            );

        } else {
            initializeVoice();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == MIC_PERMISSION) {

            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                initializeVoice();

            } else {

                status.setText(
                        "❌ Microphone permission required."
                );
            }
        }
    }

    private void createUI() {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 50, 40, 40);

        TextView title = new TextView(this);
        title.setText("🤖 VANRAJ VOICE ASSISTANT");
        title.setTextSize(26);

        status = new TextView(this);
        status.setText("\nAssistant starting...");
        status.setTextSize(18);

        listenButton = new Button(this);
        listenButton.setText("🎤 START LISTENING");

        Button accessibilityButton = new Button(this);
        accessibilityButton.setText("♿ ENABLE ACCESSIBILITY");

        listenButton.setOnClickListener(v -> {

            shouldListen = true;
            processing = false;
            startListening();
        });

        accessibilityButton.setOnClickListener(v -> {

            try {

                startActivity(
                        new Intent(
                                Settings.ACTION_ACCESSIBILITY_SETTINGS
                        )
                );

            } catch (Exception e) {

                speak("Accessibility settings open nahi hui bhai.");
            }
        });

        layout.addView(title);
        layout.addView(status);
        layout.addView(listenButton);
        layout.addView(accessibilityButton);

        setContentView(layout);
    }

    private void setupTTS() {

        tts = new TextToSpeech(this, result -> {

            if (result == TextToSpeech.SUCCESS) {

                int language = tts.setLanguage(
                        new Locale("hi", "IN")
                );

                if (language == TextToSpeech.LANG_MISSING_DATA ||
                        language == TextToSpeech.LANG_NOT_SUPPORTED) {

                    tts.setLanguage(Locale.ENGLISH);
                }
            }
        });
    }

    private void speak(String text) {

        if (status != null) {
            status.setText("🤖 " + text);
        }

        if (tts != null) {

            tts.speak(
                    text,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "vanraj_assistant"
            );
        }
    }

    private void initializeVoice() {

        if (voiceInitialized) {
            return;
        }

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {

            status.setText(
                    "❌ Speech recognition available nahi hai."
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
                "en-IN"
        );

        speechIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "hi-IN"
        );

        speechIntent.putExtra(
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                false
        );

        speechIntent.putExtra(
                RecognizerIntent.EXTRA_MAX_RESULTS,
                5
        );

        recognizer.setRecognitionListener(
                new RecognitionListener() {

                    @Override
                    public void onReadyForSpeech(
                            Bundle params) {

                        status.setText(
                                "🎤 Sun raha hoon... Bolo bhai!"
                        );

                        listenButton.setText(
                                "🎤 LISTENING..."
                        );
                    }

                    @Override
                    public void onBeginningOfSpeech() {

                        status.setText(
                                "🎤 Sun raha hoon..."
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

                        listenButton.setText(
                                "🎤 PROCESSING..."
                        );
                    }

                    @Override
                    public void onError(int error) {

                        processing = false;

                        listenButton.setText(
                                "🎤 START LISTENING"
                        );

                        if (shouldListen) {

                            handler.postDelayed(
                                    () -> startListening(),
                                    1000
                            );
                        }
                    }

                    @Override
                    public void onResults(
                            Bundle results) {

                        processing = false;

                        ArrayList<String> matches =
                                results.getStringArrayList(
                                        SpeechRecognizer.RESULTS_RECOGNITION
                                );

                        if (matches != null &&
                                !matches.isEmpty()) {

                            String command =
                                    matches.get(0);

                            status.setText(
                                    "👤 " + command
                            );

                            processCommand(command);
                        }

                        listenButton.setText(
                                "🎤 START LISTENING"
                        );

                        if (shouldListen) {

                            handler.postDelayed(
                                    () -> startListening(),
                                    1500
                            );
                        }
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

        voiceInitialized = true;

        speak(
                "Hello bhai. Main ready hoon."
        );

        handler.postDelayed(
                () -> startListening(),
                2000
        );
    }

    private void startListening() {

        if (!shouldListen || processing) {
            return;
        }

        if (recognizer == null) {
            initializeVoice();
            return;
        }

        try {

            processing = true;

            recognizer.cancel();
            recognizer.startListening(speechIntent);

        } catch (Exception e) {

            processing = false;

            status.setText(
                    "❌ Voice error: " + e.getMessage()
            );

            handler.postDelayed(
                    () -> startListening(),
                    1500
            );
        }
    }

    private void processCommand(String original) {

        if (original == null ||
                original.trim().isEmpty()) {

            return;
        }

        String command =
                original.toLowerCase(Locale.ROOT).trim();

        // STOP
        if (command.equals("stop") ||
                command.contains("stop assistant") ||
                command.contains("assistant stop") ||
                command.contains("band ho ja") ||
                command.contains("ruk ja")) {

            shouldListen = false;
            processing = false;

            if (recognizer != null) {
                recognizer.cancel();
            }

            speak(
                    "Theek hai bhai, main ruk gaya."
            );

            return;
        }

        // HOME
        if (command.equals("home") ||
                command.contains("home screen") ||
                command.contains("ghar screen")) {

            openHome();

            speak(
                    "Home screen khol raha hoon bhai."
            );

            return;
        }

        // BATTERY
        if (command.contains("battery") ||
                command.contains("bateri")) {

            Intent batteryIntent =
                    registerReceiver(
                            null,
                            new android.content.IntentFilter(
                                    Intent.ACTION_BATTERY_CHANGED
                            )
                    );

            if (batteryIntent != null) {

                int level =
                        batteryIntent.getIntExtra(
                                "level",
                                -1
                        );

                int scale =
                        batteryIntent.getIntExtra(
                                "scale",
                                -1
                        );

                if (level >= 0 && scale > 0) {

                    int percent =
                            (level * 100) / scale;

                    speak(
                            "Bhai battery " +
                            percent +
                            " percent hai."
                    );

                } else {

                    speak(
                            "Battery level nahi mil paya bhai."
                    );
                }

            } else {

                speak(
                        "Battery information nahi mili bhai."
                );
            }

            return;
        }

        // VIBRATE
        if (command.contains("vibrate") ||
                command.contains("vibration") ||
                command.contains("vibrate karo")) {

            Intent intent =
                    new Intent(
                            "com.vanraj.assistant.VIBRATE"
                    );

            sendBroadcast(intent);

            speak(
                    "Phone vibrate kar diya bhai."
            );

            return;
        }

        // TORCH ON
        if (command.contains("torch on") ||
                command.contains("flashlight on") ||
                command.contains("flash on") ||
                command.contains("torch chalu")) {

            Intent intent =
                    new Intent(
                            "com.vanraj.assistant.TORCH"
                    );

            intent.putExtra(
                    "state",
                    true
            );

            sendBroadcast(intent);

            speak(
                    "Torch on kar diya bhai."
            );

            return;
        }

        // TORCH OFF
        if (command.contains("torch off") ||
                command.contains("flashlight off") ||
                command.contains("flash off") ||
                command.contains("torch band")) {

            Intent intent =
                    new Intent(
                            "com.vanraj.assistant.TORCH"
                    );

            intent.putExtra(
                    "state",
                    false
            );

            sendBroadcast(intent);

            speak(
                    "Torch off kar diya bhai."
            );

            return;
        }

        // CHROME
        if (command.contains("chrome")) {

            String query =
                    extractSearch(command);

            if (!query.isEmpty()) {

                openGoogleSearch(query);

                speak(
                        "Chrome mein " +
                        query +
                        " search kar raha hoon bhai."
                );

            } else {

                openApp(
                        "com.android.chrome",
                        "Chrome"
                );
            }

            return;
        }

        // MAPS
        if (command.contains("maps") ||
                command.equals("map") ||
                command.contains(" map ")) {

            String query =
                    extractSearch(command);

            if (!query.isEmpty()) {

                openMapsSearch(query);

                speak(
                        "Maps mein location search kar raha hoon bhai."
                );

            } else {

                openApp(
                        "com.google.android.apps.maps",
                        "Maps"
                );
            }

            return;
        }

        // WHATSAPP
        if (command.contains("whatsapp")) {

            openApp(
                    "com.whatsapp",
                    "WhatsApp"
            );

            String text =
                    extractText(command);

            if (!text.isEmpty()) {

                typeAfterDelay(
                        text,
                        2000
                );
            }

            return;
        }

        // TELEGRAM
        if (command.contains("telegram")) {

            openApp(
                    "org.telegram.messenger",
                    "Telegram"
            );

            String text =
                    extractText(command);

            if (!text.isEmpty()) {

                typeAfterDelay(
                        text,
                        2000
                );
            }

            return;
        }

        // YOUTUBE
        if (command.contains("youtube")) {

            String query =
                    extractSearch(command);

            if (!query.isEmpty()) {

                openGoogleSearch(
                        "site:youtube.com " + query
                );

            } else {

                openApp(
                        "com.google.android.youtube",
                        "YouTube"
                );
            }

            return;
        }

        // CALCULATOR
        if (command.contains("calculator") ||
                command.contains("calculate") ||
                command.contains("calc")) {

            openApp(
                    "com.vivo.calculator",
                    "Calculator"
            );

            return;
        }

        // GMAIL
        if (command.contains("gmail") ||
                command.contains("email")) {

            openApp(
                    "com.google.android.gm",
                    "Gmail"
            );

            return;
        }

        // INSTAGRAM
        if (command.contains("instagram")) {

            openApp(
                    "com.instagram.android",
                    "Instagram"
            );

            return;
        }

        // FACEBOOK
        if (command.contains("facebook")) {

            openApp(
                    "com.facebook.katana",
                    "Facebook"
            );

            return;
        }

        // SPOTIFY
        if (command.contains("spotify")) {

            openApp(
                    "com.spotify.music",
                    "Spotify"
            );

            return;
        }

        // GENERIC TYPE
        if (command.startsWith("type ") ||
                command.startsWith("write ") ||
                command.startsWith("likho ") ||
                command.startsWith("likh ") ||
                command.contains("type this")) {

            String text =
                    extractText(command);

            if (!text.isEmpty()) {

                typeText(text);

            } else {

                speak(
                        "Bhai kya type karna hai?"
                );
            }

            return;
        }

        // HELLO
        if (command.equals("hello") ||
                command.equals("hi") ||
                command.equals("hey") ||
                command.contains("namaste")) {

            speak(
                    "Hello bhai. Bolo kya karna hai?"
            );

            return;
        }

        // UNKNOWN
        speak(
                "Bhai ye command abhi samajh nahi aayi."
        );
    }

    private void openApp(
            String packageName,
            String name) {

        try {

            Intent launch =
                    getPackageManager()
                            .getLaunchIntentForPackage(
                                    packageName
                            );

            if (launch == null) {

                speak(
                        name +
                        " phone mein installed nahi hai."
                );

                return;
            }

            launch.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
            );

            startActivity(launch);

            speak(
                    name +
                    " khol raha hoon bhai."
            );

        } catch (Exception e) {

            speak(
                    name +
                    " open nahi ho paya bhai."
            );
        }
    }

    private void openHome() {

        try {

            Intent intent =
                    new Intent(
                            Intent.ACTION_MAIN
                    );

            intent.addCategory(
                    Intent.CATEGORY_HOME
            );

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
            );

            startActivity(intent);

        } catch (Exception e) {

   
