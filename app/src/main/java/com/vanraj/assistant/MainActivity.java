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
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final String TAG = "VoiceAssistant";

    private SpeechRecognizer speechRecognizer;
    private Intent speechIntent;
    private final Handler handler = new Handler();

    private TextView statusText;

    private boolean listening = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        statusText = new TextView(this);
        statusText.setText("Voice Assistant\n\nStarting...");
        statusText.setTextSize(20);
        statusText.setPadding(40, 80, 40, 40);

        setContentView(statusText);

        if (android.os.Build.VERSION.SDK_INT >= 23 &&
                checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    1001
            );
        } else {
            initializeAssistant();
        }
    }

    private void initializeAssistant() {

        statusText.setText(
                "Voice Assistant\n\n" +
                "Ready\n" +
                "Bolo bhai..."
        );

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            statusText.setText(
                    "Speech recognition available nahi hai.\n\n" +
                    "Phone ka speech service check karo."
            );
            return;
        }

        speechRecognizer =
                SpeechRecognizer.createSpeechRecognizer(this);

        speechIntent =
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

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
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                true
        );

        speechIntent.putExtra(
                RecognizerIntent.EXTRA_MAX_RESULTS,
                5
        );

        speechRecognizer.setRecognitionListener(
                new RecognitionListener() {

                    @Override
                    public void onReadyForSpeech(Bundle params) {

                        listening = true;

                        statusText.setText(
                                "🎤 Sun raha hoon...\n\n" +
                                "Bolo bhai!"
                        );
                    }

                    @Override
                    public void onBeginningOfSpeech() {

                        statusText.setText(
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

                        listening = false;

                        statusText.setText(
                                "Processing..."
                        );
                    }

                    @Override
                    public void onError(int error) {

                        listening = false;

                        Log.e(
                                TAG,
                                "Speech error = " + error
                        );

                        statusText.setText(
                                "Speech error: " +
                                error +
                                "\nRetrying..."
                        );

                        retryListening();
                    }

                    @Override
                    public void onResults(Bundle results) {

                        listening = false;

                        ArrayList<String> matches =
                                results.getStringArrayList(
                                        SpeechRecognizer.RESULTS_RECOGNITION
                                );

                        if (matches == null ||
                                matches.isEmpty()) {

                            retryListening();
                            return;
                        }

                        String command =
                                matches.get(0);

                        if (command == null ||
                                command.trim().isEmpty()) {

                            retryListening();
                            return;
                        }

                        command = command.trim();

                        Log.d(
                                TAG,
                                "Recognized: " + command
                        );

                        statusText.setText(
                                "You said:\n" +
                                command +
                                "\n\nProcessing..."
                        );

                        processCommand(command);
                    }

                    @Override
                    public void onPartialResults(Bundle partialResults) {

                        ArrayList<String> partial =
                                partialResults.getStringArrayList(
                                        SpeechRecognizer.RESULTS_RECOGNITION
                                );

                        if (partial != null &&
                                !partial.isEmpty()) {

                            statusText.setText(
                                    "🎤 " + partial.get(0)
                            );
                        }
                    }

                    @Override
                    public void onEvent(
                            int eventType,
                            Bundle params) {
                    }
                }
        );

        startListening();
    }

    private void startListening() {

        if (speechRecognizer == null) {
            return;
        }

        if (listening) {
            return;
        }

        try {

            speechRecognizer.cancel();

            handler.postDelayed(
                    () -> {

                        try {

                            statusText.setText(
                                    "🎤 Sun raha hoon...\n\n" +
                                    "Bolo bhai!"
                            );

                            speechRecognizer.startListening(
                                    speechIntent
                            );

                        } catch (Exception e) {

                            Log.e(
                                    TAG,
                                    "startListening failed",
                                    e
                            );

                            retryListening();
                        }

                    },
                    300
            );

        } catch (Exception e) {

            Log.e(
                    TAG,
                    "Speech start error",
                    e
            );

            retryListening();
        }
    }

    private void retryListening() {

        handler.removeCallbacksAndMessages(null);

        handler.postDelayed(
                this::startListening,
                1200
        );
    }

    private void processCommand(String command) {

        if (command == null) {
            retryListening();
            return;
        }

        String original =
                command.trim();

        String lower =
                original.toLowerCase(
                        Locale.ROOT
                );

        /*
         * HOME
         */
        if (containsAny(
                lower,
                "home",
                "होम",
                "घर"
        )) {

            VoiceAccessibilityService service =
                    VoiceAccessibilityService.getInstance();

            if (service != null) {

                service.goHome();

                statusText.setText(
                        "Home opened"
                );
            }

            retryListening();
            return;
        }

        /*
         * BACK
         */
        if (containsAny(
                lower,
                "back",
                "वापस",
                "पीछे"
        )) {

            VoiceAccessibilityService service =
                    VoiceAccessibilityService.getInstance();

            if (service != null) {

                service.goBack();

                statusText.setText(
                        "Back"
                );
            }

            retryListening();
            return;
        }

        /*
         * RECENTS
         */
        if (containsAny(
                lower,
                "recent",
                "recents",
                "रीसेंट"
        )) {

            VoiceAccessibilityService service =
                    VoiceAccessibilityService.getInstance();

            if (service != null) {

                service.openRecents();

                statusText.setText(
                        "Recent apps opened"
                );
            }

            retryListening();
            return;
        }

        /*
         * SCROLL DOWN
         */
        if (containsAny(
                lower,
                "scroll down",
                "नीचे स्क्रोल",
                "नीचे स्क्रॉल"
        )) {

            VoiceAccessibilityService service =
                    VoiceAccessibilityService.getInstance();

            if (service != null) {
                service.scrollDown();
            }

            retryListening();
            return;
        }

        /*
         * SCROLL UP
         */
        if (containsAny(
                lower,
                "scroll up",
                "ऊपर स्क्रोल",
                "ऊपर स्क्रॉल"
        )) {

            VoiceAccessibilityService service =
                    VoiceAccessibilityService.getInstance();

            if (service != null) {
                service.scrollUp();
            }

            retryListening();
            return;
        }

        /*
         * TYPE COMMAND
         */
        if (lower.startsWith("type ")) {

            String text =
                    original.substring(5).trim();

            typeText(text);

            return;
        }

        if (lower.startsWith("write ")) {

            String text =
                    original.substring(6).trim();

            typeText(text);

            return;
        }

        if (lower.startsWith("लिखो ")) {

            String text =
                    original.substring(6).trim();

            typeText(text);

            return;
        }

        /*
         * CLICK COMMAND
         */
        if (lower.startsWith("click ")) {

            String text =
                    original.substring(6).trim();

            clickText(text);

            return;
        }

        if (lower.startsWith("क्लिक ")) {

            String text =
                    original.substring(6).trim();

            clickText(text);

            return;
        }

        /*
         * CODING
         */
        if (containsAny(
                lower,
                "code",
                "coding",
                "program",
                "script",
                "कोड",
                "कोडिंग"
        )) {

            String result =
                    CodingEngine.handle(original);

            statusText.setText(
                    result
            );

            retryListening();
            return;
        }

        /*
         * TRADING
         */
        if (containsAny(
                lower,
                "trading",
                "trade",
                "btc",
                "bitcoin",
                "crypto",
                "trading",
                "ट्रेडिंग",
                "बिटकॉइन"
        )) {

            String result =
                    TradingSkill.handle(original);

            statusText.setText(
                    result
            );

            retryListening();
            return;
        }

        /*
         * OPEN APP
         */
        if (containsAny(
                lower,
                "open",
                "launch",
                "start",
                "khol",
                "kholo",
                "kholna",
                "ओपन",
                "खोल",
                "खोलो",
                "चलाओ"
        )) {

            String appName =
                    extractAppName(original);

            if (!appName.isEmpty()) {

                boolean opened =
                        openApp(appName);

                if (opened) {

                    statusText.setText(
                            "Opening " +
                            appName
                    );

                } else {

                    statusText.setText(
                            "App nahi mila: " +
                            appName
                    );
                }

                retryListening();
                return;
            }
        }

        /*
         * DIRECT APP NAME
         */
        if (openApp(original)) {

            retryListening();
            return;
        }

        statusText.setText(
                "Command samajh nahi aayi:\n\n" +
                original
        );

        retryListening();
    }

    private void typeText(String text) {

        VoiceAccessibilityService service =
                VoiceAccessibilityService.getInstance();

        if (service == null) {

            statusText.setText(
                    "Accessibility Service OFF hai."
            );

            retryListening();
            return;
        }

        boolean result =
                service.typeText(text);

        if (result) {

            statusText.setText(
                    "Text type ho gaya."
            );

        } else {

            statusText.setText(
                    "Editable field nahi mila."
            );
        }

        retryListening();
    }

    private void clickText(String text) {

        VoiceAccessibilityService service =
                VoiceAccessibilityService.getInstance();

        if (service == null) {

            statusText.setText(
                    "Accessibility Service OFF hai."
            );

            retryListening();
            return;
        }

        boolean result =
                service.clickText(text);

        if (result) {

            statusText.setText(
                    "Clicked: " +
                    text
            );

        } else {

            statusText.setText(
                    "Button/text nahi mila: " +
                    text
            );
        }

        retryListening();
    }

    private boolean openApp(String wanted) {

        if (wanted == null ||
                wanted.trim().isEmpty()) {

            return false;
        }

        String search =
                normalize(wanted);

        PackageManager pm =
                getPackageManager();

        Intent launcherIntent =
                new Intent(
                        Intent.ACTION_MAIN,
                        null
                );

        launcherIntent.addCategory(
                Intent.CATEGORY_LAUNCHER
        );

        List<android.content.pm.ResolveInfo> apps =
                pm.queryIntentActivities(
                        launcherIntent,
                        PackageManager.MATCH_ALL
                );

        android.content.pm.ResolveInfo best =
                null;

        int bestScore = 0;

        for (android.content.pm.ResolveInfo info : apps) {

            if (info == null ||
                    info.activityInfo == null) {
                continue;
            }

            CharSequence label =
                    info.loadLabel(pm);

            String appLabel =
                    label == null
                            ? ""
                            : label.toString();

            String normalizedLabel =
                    normalize(appLabel);

            int score =
                    getAppScore(
                            search,
                            normalizedLabel
                    );

            if (score > bestScore) {

                bestScore = score;
                best = info;
            }
        }

        /*
         * Common Hindi aliases
         */
        if (best == null ||
                bestScore < 50) {

            for (android.content.pm.ResolveInfo info : apps) {

                if (info == null ||
                        info.activityInfo == null) {
                    continue;
                }

                String packageName =
                        info.activityInfo.packageName;

                int score =
                        getPackageScore(
                                search,
                                packageName
                        );

                if (score > bestScore) {

                    bestScore = score;
                    best = info;
                }
            }
        }

        if (best == null ||
                best.activityInfo == null) {

            return false;
        }

        try {

            Intent launchIntent =
                    new Intent(
                            Intent.ACTION_MAIN
                    );

            launchIntent.addCategory(
                    Intent.CATEGORY_LAUNCHER
            );

            launchIntent.setClassName(
                    best.activityInfo.packageName,
                    best.activityInfo.name
            );

            launchIntent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            );

            startActivity(
                    launchIntent
            );

            return true;

        } catch (Exception e) {

            Log.e(
                    TAG,
                    "Could not launch app",
                    e
            );

            return false;
        }
    }

    private int getAppScore(
            String wanted,
            String label) {

        if (wanted.equals(label)) {
            return 100;
        }

        if (wanted.contains(label) &&
                label.length() > 2) {
            return 90;
        }

        if (label.contains(wanted) &&
                wanted.length() > 2) {
            return 80;
        }

        if (hindiAlias(wanted, label)) {
            return 85;
        }

        String[] words =
                wanted.split("\\s+");

        int score = 0;

        for (String word : words) {

            if (word.length() < 3) {
                continue;
            }

            if (label.contains(word)) {
                score += 20;
            }
        }

        return score;
    }

    private int getPackageScore(
            String wanted,
            String packageName) {

        String pkg =
                packageName.toLowerCase(
                        Locale.ROOT
                );

        if (wanted.contains("whatsapp") &&
                pkg.contains("whatsapp")) {
            return 90;
        }

        if ((wanted.contains("youtube") ||
             wanted.contains("यूट्यूब") ||
             wanted.contains("युटुब")) &&
                pkg.contains("youtube")) {
            return 90;
        }

        if (wanted.contains("chrome") &&
                pkg.contains("chrome")) {
            return 90;
        }

        if ((wanted.contains("telegram") ||
             wanted.contains("टेलीग्राम")) &&
                pkg.contains("telegram")) {
            return 90;
        }

        if (wanted.contains("instagram") &&
                pkg.contains("instagram")) {
            return 90;
        }

        if (wanted.contains("facebook") &&
                pkg.contains("facebook")) {
            return 90;
        }

        if (wanted.contains("calculator") &&
                pkg.contains("calculator")) {
            return 90;
        }

        if (wanted.contains("maps") &&
                pkg.contains("maps")) {
            return 90;
        }

        return 0;
    }

    private boolean hindiAlias(
            String wanted,
            String label) {

        if ((wanted.contains("व्हाट्सएप") ||
             wanted.contains("वाट्सएप")) &&
                label.contains("whatsapp")) {
            return true;
        }

        if ((wanted.contains("यूट्यूब") ||
             wanted.contains("युटुब") ||
             wanted.contains("यूटुब")) &&
                label.contains("youtube")) {
            return true;
        }

        if ((wanted.contains("इंस्टाग्राम")) &&
                label.contains("instagram")) {
            return true;
        }

        if ((wanted.contains("टेलीग्राम")) &&
                label.contains("telegram")) {
            return true;
        }

        if ((wanted.contains("फेसबुक")) &&
                label.contains("facebook")) {
            return true;
        }

        if ((wanted.contains("क्रोम")) &&
                label.contains("chrome")) {
            return true;
        }

        if ((wanted.contains("कैलकुलेटर")) &&
                label.contains("calculator")) {
            return true;
        }

        if ((wanted.contains("मैप") ||
             wanted.contains("मैप्स")) &&
                label.contains("maps")) {
            return true;
        }

        return false;
    }

    private String extractAppName(
            String command) {

        String result =
                command.trim();

        String[] prefixes = {

                "open ",
                "launch ",
                "start ",
                "khol ",
                "kholo ",
                "kholna ",

                "ओपन ",
                "खोल ",
                "खोलो ",
                "चलाओ "
        };

        for (String prefix : prefixes) {

            if (result
                    .toLowerCase(
                            Locale.ROOT
                    )
                    .startsWith(
                            prefix
                    )) {

                result =
                        result.substring(
                                prefix.length()
                        ).trim();

                break;
            }
        }

        /*
         * Remove common trailing Hindi/English words
         */
        String[] endings = {

                " kar",
                " karo",
                " please",

                " करना",
                " करो",
                " कर",
                " खोल",
                " खोलो"
        };

        String lower =
                result.toLowerCase(
                        Locale.ROOT
                );

        for (String ending : endings) {

            if (lower.endsWith(ending)) {

                result =
                        result.substring(
                                0,
                                result.length()
                                        - ending.length()
                        ).trim();

                break;
            }
        }

        return result;
    }

    private String normalize(
            String text) {

        return text
                .toLowerCase(
                        Locale.ROOT
                )
                .trim()
                .replaceAll(
                        "\\s+",
                        " "
                );
    }

    private boolean containsAny(
            String text,
            String... values) {

        for (String value : values) {

            if (text.contains(value)) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void onDestroy() {

        handler.removeCallbacksAndMessages(
                null
        );

        if (speechRecognizer != null) {

            try {
                speechRecognizer.destroy();
            } catch (Exception ignored) {
            }

            speechRecognizer = null;
        }

        super.onDestroy();
    }
}
