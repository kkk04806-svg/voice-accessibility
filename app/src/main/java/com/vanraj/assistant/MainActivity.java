cd ~/assistant/accessibility

cat > app/src/main/java/com/vanraj/assistant/MainActivity.java <<'JAVA'
package com.vanraj.assistant;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
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
            startListening();
        });

        accessibilityButton.setOnClickListener(v -> {
            startActivity(
                    new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            );
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

        status.setText("🤖 " + text);

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

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {

            status.setText(
                    "❌ Phone mein speech recognition available nahi hai."
            );

            return;
        }

        recognizer = SpeechRecognizer.createSpeechRecognizer(this);

        speechIntent = new Intent(
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
                    public void onReadyForSpeech(Bundle params) {

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
                    public void onRmsChanged(float rmsdB) {
                    }

                    @Override
                    public void onBufferReceived(byte[] buffer) {
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
                                    800
                            );
                        }
                    }

                    @Override
                    public void onResults(Bundle results) {

                        processing = false;

                        ArrayList<String> matches =
                                results.getStringArrayList(
                                        SpeechRecognizer.RESULTS_RECOGNITION
                                );

                        if (matches != null &&
                                !matches.isEmpty()) {

                            String command = matches.get(0);

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
                                    1200
                            );
                        }
                    }

                    @Override
                    public void onPartialResults(Bundle partialResults) {
                    }

                    @Override
                    public void onEvent(
                            int eventType,
                            Bundle params) {
                    }
                }
        );

        speak("Hello bhai. Main ready hoon.");

        handler.postDelayed(
                () -> startListening(),
                1800
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
        }
    }

    private void processCommand(String original) {

        if (original == null) {
            return;
        }

        String command =
                original.toLowerCase(Locale.ROOT).trim();

        // STOP
        if (command.equals("stop") ||
                command.contains("stop assistant") ||
                command.contains("assistant stop") ||
                command.contains("band ho ja")) {

            shouldListen = false;

            if (recognizer != null) {
                recognizer.cancel();
            }

            speak(
                    "Theek hai bhai, main ruk gaya."
            );

            return;
        }

        // HOME
        if (command.contains("home screen") ||
                command.equals("home")) {

            openHome();
            speak("Home screen khol raha hoon bhai.");
            return;
        }

        // BATTERY
        if (command.contains("battery")) {

            speak(
                    "Battery command abhi system integration mein hai."
            );

            return;
        }

        // VIBRATE
        if (command.contains("vibrate") ||
                command.contains("vibration")) {

            Intent intent = new Intent(
                    "com.vanraj.assistant.VIBRATE"
            );

            sendBroadcast(intent);

            speak("Phone vibrate kar diya bhai.");
            return;
        }

        // TORCH
        if (command.contains("torch on") ||
                command.contains("flashlight on")) {

            Intent intent = new Intent(
                    "com.vanraj.assistant.TORCH"
            );

            intent.putExtra("state", true);
            sendBroadcast(intent);

            speak("Torch on kar diya bhai.");
            return;
        }

        if (command.contains("torch off") ||
                command.contains("flashlight off")) {

            Intent intent = new Intent(
                    "com.vanraj.assistant.TORCH"
            );

            intent.putExtra("state", false);
            sendBroadcast(intent);

            speak("Torch off kar diya bhai.");
            return;
        }

        // CHROME SEARCH
        if (command.contains("chrome")) {

            String query = extractSearch(command);

            if (!query.isEmpty()) {

                openGoogleSearch(query);

                speak(
                        "Chrome mein " +
                        query +
                        " search kar raha hoon."
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
                command.contains("map")) {

            String query = extractSearch(command);

            if (!query.isEmpty()) {

                openMapsSearch(query);

                speak(
                        "Maps mein location search kar raha hoon."
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

            String text = extractText(command);

            if (!text.isEmpty()) {

                typeAfterDelay(text, 1800);
            }

            return;
        }

        // TELEGRAM
        if (command.contains("telegram")) {

            openApp(
                    "org.telegram.messenger",
                    "Telegram"
            );

            String text = extractText(command);

            if (!text.isEmpty()) {

                typeAfterDelay(text, 1800);
            }

            return;
        }

        // YOUTUBE
        if (command.contains("youtube")) {

            String query = extractSearch(command);

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
                command.contains("calculate")) {

            openApp(
                    "com.vivo.calculator",
                    "Calculator"
            );

            return;
        }

        // GMAIL
        if (command.contains("gmail")) {

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
                command.contains("type this")) {

            String text = extractText(command);

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
                command.contains("namaste")) {

            speak(
                    "Hello bhai. Bolo kya karna hai?"
            );

            return;
        }

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
                    " open nahi ho paya."
            );
        }
    }

    private void openHome() {

        try {

            Intent intent = new Intent(
                    Intent.ACTION_MAIN
            );

            intent.addCategory(
                    Intent.CATEGORY_HOME
            );

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
            );

            startActivity(intent);

        } catch (Exception ignored) {
        }
    }

    private void openGoogleSearch(
            String query) {

        try {

            String url =
                    "https://www.google.com/search?q="
                    + java.net.URLEncoder.encode(
                            query,
                            "UTF-8"
                    );

            Intent intent =
                    new Intent(
                            Intent.ACTION_VIEW,
                            android.net.Uri.parse(url)
                    );

            startActivity(intent);

        } catch (Exception e) {

            speak("Search open nahi ho payi bhai.");
        }
    }

    private void openMapsSearch(
            String query) {

        try {

            String url =
                    "https://www.google.com/maps/search/?api=1&query="
                    + java.net.URLEncoder.encode(
                            query,
                            "UTF-8"
                    );

            Intent intent =
                    new Intent(
                            Intent.ACTION_VIEW,
                            android.net.Uri.parse(url)
                    );

            startActivity(intent);

        } catch (Exception e) {

            speak("Maps search nahi ho payi bhai.");
        }
    }

    private String extractSearch(
            String command) {

        String[] markers = {
                "search for",
                "search",
                "google",
                "find",
                "mein search",
                "me search"
        };

        for (String marker : markers) {

            int index =
                    command.indexOf(marker);

            if (index >= 0) {

                String result =
                        command.substring(
                                index + marker.length()
                        ).trim();

                if (!result.isEmpty()) {
                    return result;
                }
            }
        }

        return "";
    }

    private String extractText(
            String command) {

        String[] markers = {
                "message",
                "msg",
                "text",
                "type",
                "write",
                "likho",
                "likh",
                "mein likh",
                "me likh"
        };

        for (String marker : markers) {

            int index =
                    command.indexOf(marker);

            if (index >= 0) {

                String result =
                        command.substring(
                                index + marker.length()
                        ).trim();

                String[] remove = {
                        "send",
                        "kar",
                        "karo",
                        "please",
                        "hai"
                };

                for (String word : remove) {

                    if (result.startsWith(word + " ")) {

                        result =
                                result.substring(
                                        word.length()
                                ).trim();
                    }
                }

                if (!result.isEmpty()) {
                    return result;
                }
            }
        }

        return "";
    }

    private void typeAfterDelay(
            String text,
            long delay) {

        handler.postDelayed(
                () -> typeText(text),
                delay
        );
    }

    private void typeText(
            String text) {

        VoiceAccessibilityService service =
                VoiceAccessibilityService.getInstance();

        if (service == null) {

            speak(
                    "Bhai Accessibility Service ON nahi hai."
            );

            return;
        }

        boolean result =
                service.typeText(text);

        if (result) {

            speak(
                    "Text type kar diya bhai."
            );

        } else {

          
