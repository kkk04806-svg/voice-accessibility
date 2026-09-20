package com.vanraj.assistant;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends Activity {

    private static final int MIC_PERMISSION = 1001;

    private SpeechRecognizer recognizer;
    private Intent speechIntent;

    private TextView status;
    private boolean listening = false;
    private boolean restarting = false;

    private final Handler handler = new Handler();

    private final Map<String, String> appAliases =
            new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        buildUI();
        setupSpeech();

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MIC_PERMISSION
            );
        } else {
            startListeningDelayed(700);
        }
    }

    private void buildUI() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(35, 40, 35, 35);

        TextView title = new TextView(this);
        title.setText("VANRAJ AI");
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 20);

        status = new TextView(this);
        status.setTextSize(16);
        status.setTextColor(Color.DKGRAY);
        status.setPadding(0, 20, 0, 20);

        Button listen = new Button(this);
        listen.setText("🎤 LISTEN");

        listen.setOnClickListener(v -> {
            startListening();
        });

        Button accessibility = new Button(this);
        accessibility.setText("♿ Accessibility Settings");

        accessibility.setOnClickListener(v -> {
            try {
                startActivity(
                        new Intent(
                                Settings.ACTION_ACCESSIBILITY_SETTINGS
                        )
                );
            } catch (Exception ignored) {
            }
        });

        root.addView(title);
        root.addView(status);
        root.addView(listen);
        root.addView(accessibility);

        setContentView(root);
    }

    private void setupSpeech() {

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {

            setStatus(
                    "❌ Speech recognition unavailable"
            );

            return;
        }

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
                5
        );

        speechIntent.putExtra(
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                false
        );

        createRecognizer();
    }

    private void createRecognizer() {

        if (recognizer != null) {
            try {
                recognizer.destroy();
            } catch (Exception ignored) {
            }
        }

        recognizer =
                SpeechRecognizer.createSpeechRecognizer(this);

        recognizer.setRecognitionListener(
                new RecognitionListener() {

                    @Override
                    public void onReadyForSpeech(Bundle params) {
                        listening = true;
                        setStatus("🎤 Sun raha hoon...");
                    }

                    @Override
                    public void onBeginningOfSpeech() {
                        setStatus("🎙️ Bolo bhai...");
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
                        setStatus("⏳ Samajh raha hoon...");
                    }

                    @Override
                    public void onError(int error) {

                        listening = false;

                        String msg = speechError(error);

                        setStatus("⚠️ " + msg);

                        /*
                         * Error 7 / 11 par recognizer ko recreate
                         * karke retry karte hain.
                         */
                        if (error ==
                                SpeechRecognizer.ERROR_NO_MATCH ||
                            error == 11 ||
                            error ==
                                SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {

                            restartListening();
                        }
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

                            restartListening();
                            return;
                        }

                        String command = matches.get(0);

                        setStatus(
                                "🗣️ " + command
                        );

                        handleCommand(command);
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

    private void startListeningDelayed(long delay) {

        handler.postDelayed(
                this::startListening,
                delay
        );
    }

    private void startListening() {

        if (speechIntent == null) {
            setupSpeech();
        }

        if (recognizer == null) {
            createRecognizer();
        }

        if (listening) {
            return;
        }

        try {

            listening = true;

            recognizer.startListening(
                    speechIntent
            );

        } catch (Exception e) {

            listening = false;

            createRecognizer();

            handler.postDelayed(
                    this::startListening,
                    500
            );
        }
    }

    private void restartListening() {

        if (restarting) return;

        restarting = true;

        handler.postDelayed(() -> {

            restarting = false;

            createRecognizer();

            startListening();

        }, 900);
    }

    /*
     * ============================
     * UNIVERSAL COMMAND ENGINE
     * ============================
     */

    private void handleCommand(String raw) {

        if (raw == null) {
            restartListening();
            return;
        }

        String command =
                normalize(raw);

        if (command.isEmpty()) {
            restartListening();
            return;
        }

        /*
         * Stop
         */
        if (containsAny(
                command,
                "stop assistant",
                "assistant stop",
                "band ho ja",
                "बंद हो जा",
                "बंद करो"
        )) {

            setStatus("🛑 Assistant stopped");
            return;
        }

        /*
         * HOME
         */
        if (containsAny(
                command,
                "go home",
                "home",
                "होम",
                "होम जाओ"
        )) {

            executeHome();
            continueListening();
            return;
        }

        /*
         * BACK
         */
        if (containsAny(
                command,
                "go back",
                "back",
                "पीछे जाओ",
                "वापस जाओ"
        )) {

            executeBack();
            continueListening();
            return;
        }

        /*
         * RECENTS
         */
        if (containsAny(
                command,
                "recent apps",
                "recents",
                "recent",
                "रीसेंट ऐप"
        )) {

            executeRecents();
            continueListening();
            return;
        }

        /*
         * SCROLL
         */
        if (containsAny(
                command,
                "scroll down",
                "नीचे स्क्रोल",
                "नीचे करो"
        )) {

            executeScrollDown();
            continueListening();
            return;
        }

        if (containsAny(
                command,
                "scroll up",
                "ऊपर स्क्रोल",
                "ऊपर करो"
        )) {

            executeScrollUp();
            continueListening();
            return;
        }

        /*
         * COMPOUND COMMAND
         */
        if (isCompound(command)) {

            executeCompound(command);
            return;
        }

        /*
         * SEARCH
         */
        if (command.startsWith("search ") ||
            command.startsWith("सर्च ") ||
            command.contains("search karo") ||
            command.contains("सर्च करो")) {

            String query =
                    extractSearch(command);

            if (!query.isEmpty()) {
                webSearch(query);
            }

            continueListening();
            return;
        }

        /*
         * TYPE
         */
        if (command.startsWith("type ") ||
            command.startsWith("टाइप ") ||
            command.contains("likho ") ||
            command.contains("लिखो ")) {

            String text =
                    extractType(command);

            if (!text.isEmpty()) {
                typeText(text);
            }

            continueListening();
            return;
        }

        /*
         * CLICK
         */
        if (command.startsWith("click ") ||
            command.startsWith("क्लिक ") ||
            command.startsWith("press ")) {

            String target =
                    extractAfterCommand(
                            command,
                            "click",
                            "क्लिक",
                            "press"
                    );

            clickText(target);

            continueListening();
            return;
        }

        /*
         * FILE / CODING
         */
        if (command.contains("file banao") ||
            command.contains("फाइल बनाओ") ||
            command.contains("code file") ||
            command.contains("कोड फाइल")) {

            CodingEngine.handle(
                    this,
                    command
            );

            continueListening();
            return;
        }

        /*
         * TRADING
         */
        if (command.contains("btc") ||
            command.contains("bitcoin") ||
            command.contains("trading") ||
            command.contains("trade") ||
            command.contains("ट्रेडिंग")) {

            TradingSkill.handle(
                    this,
                    command
            );

            continueListening();
            return;
        }

        /*
         * RESEARCH / WEB
         */
        if (command.contains("research") ||
            command.contains("research karo") ||
            command.contains("जानकारी") ||
            command.contains("information") ||
            command.contains("web search")) {

            String query =
                    extractResearch(command);

            if (!query.isEmpty()) {
                webSearch(query);
            }

            continueListening();
            return;
        }

        /*
         * APP OPEN
         */
        if (looksLikeOpenCommand(command)) {

            String appName =
                    extractAppName(command);

            if (!appName.isEmpty()) {

                boolean opened =
                        openInstalledApp(appName);

                if (!opened) {

                    setStatus(
                            "❌ App nahi mila: "
                                    + appName
                    );
                }
            }

            continueListening();
            return;
        }

        /*
         * Direct app name
         */
        if (openInstalledApp(command)) {

            continueListening();
            return;
        }

        setStatus(
                "❓ Command samajh nahi aayi: "
                        + raw
        );

        continueListening();
    }

    /*
     * ============================
     * COMPOUND COMMANDS
     * ============================
     */

    private boolean isCompound(String command) {

        return command.contains(" aur ") ||
               command.contains(" and ") ||
               command.contains(" फिर ") ||
               command.contains(" then ");
    }

    private void executeCompound(String command) {

        String[] parts =
                command.split(
                        "\\s+(aur|and|फिर|then)\\s+"
                );

        for (String part : parts) {

            part = part.trim();

            if (part.isEmpty()) continue;

            executeSinglePart(part);

            try {
                Thread.sleep(1200);
            } catch (InterruptedException ignored) {
            }
        }

        continueListening();
    }

    private void executeSinglePart(String command) {

        if (looksLikeOpenCommand(command)) {

            String app =
                    extractAppName(command);

            openInstalledApp(app);
            return;
        }

        if (command.startsWith("search ")) {

            webSearch(
                    command.substring(7).trim()
            );
            return;
        }

        if (command.startsWith("type ")) {

            typeText(
                    command.substring(5).trim()
            );
            return;
        }

        if (command.startsWith("click ")) {

            clickText(
                    command.substring(6).trim()
            );
            return;
        }

        handleCommand(command);
    }

    /*
     * ============================
     * INSTALLED APP DISCOVERY
     * ============================
     */

    private boolean openInstalledApp(String requested) {

        if (requested == null) return false;

        String wanted =
                normalize(requested);

        if (wanted.isEmpty()) return false;

        PackageManager pm =
                getPackageManager();

        Intent launcherIntent =
                new Intent(
                        Intent.ACTION_MAIN
                );

        launcherIntent.addCategory(
                Intent.CATEGORY_LAUNCHER
        );

        List<ResolveInfo> apps =
                pm.queryIntentActivities(
                        launcherIntent,
                        PackageManager.MATCH_ALL
                );

        /*
         * First aliases
         */
        String aliasPackage =
                findKnownAlias(wanted);

        if (aliasPackage != null) {

            Intent launch =
                    pm.getLaunchIntentForPackage(
                            aliasPackage
                    );

            if (launch != null) {

                launch.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                );

                startActivity(launch);

                setStatus(
                        "🚀 Opening "
                                + requested
                );

                return true;
            }
        }

        /*
         * Dynamic installed app discovery
         */
        ResolveInfo best = null;

        int bestScore = 0;

        for (ResolveInfo info : apps) {

            if (info.activityInfo == null)
                continue;

            CharSequence label =
                    info.loadLabel(pm);

            String appLabel =
                    label == null
                            ? ""
                            : label.toString();

            String normalizedLabel =
                    normalize(appLabel);

            String packageName =
                    info.activityInfo.packageName;

            String normalizedPackage =
                    normalize(packageName);

            int score = 0;

            if (normalizedLabel.equals(wanted)) {
                score = 100;
            } else if (
                    normalizedLabel.contains(wanted)) {
                score = 80;
            } else if (
                    wanted.contains(normalizedLabel) &&
                    normalizedLabel.length() > 2) {
                score = 70;
            } else if (
                    normalizedPackage.contains(wanted)) {
                score = 50;
            }

            if (score > bestScore) {

                bestScore = score;
                best = info;
            }
        }

        if (best == null) {
            return false;
        }

        try {

            Intent launch =
                    new Intent(
                            Intent.ACTION_MAIN
                    );

            launch.addCategory(
                    Intent.CATEGORY_LAUNCHER
            );

            launch.setClassName(
                    best.activityInfo.packageName,
                    best.activityInfo.name
            );

            launch.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
            );

            startActivity(launch);

            setStatus(
                    "🚀 Opening "
                            + best.loadLabel(pm)
            );

            return true;

        } catch (Exception e) {

            return false;
        }
    }

    private String findKnownAlias(String command) {

        String c = command;

        if (c.contains("whatsapp") ||
            c.contains("व्हाट्सएप") ||
            c.contains("व्हाट्सऐप")) {

            return "com.whatsapp";
        }

        if (c.contains("youtube") ||
            c.contains("यूट्यूब") ||
            c.contains("युटुब")) {

            return "com.google.android.youtube";
        }

        if (c.contains("chrome") ||
            c.contains("क्रोम")) {

            return "com.android.chrome";
        }

        if (c.contains("telegram") ||
            c.contains("टेलीग्राम")) {

            return "org.telegram.messenger";
        }

        if (c.contains("instagram") ||
            c.contains("इंस्टाग्राम")) {

            return "com.instagram.android";
        }

        if (c.contains("facebook") ||
            c.contains("फेसबुक")) {

            return "com.facebook.katana";
        }

        if (c.contains("gmail") ||
            c.contains("जीमेल")) {

            return "com.google.android.gm";
        }

        if (c.contains("maps") ||
            c.contains("google maps") ||
            c.contains("मैप") ||
            c.contains("मैप्स")) {

            return "com.google.android.apps.maps";
        }

        if (c.contains("calculator") ||
            c.contains("calc") ||
            c.contains("कैलकुलेटर")) {

            return "com.vivo.calculator";
        }

        if (c.contains("play store") ||
            c.contains("playstore") ||
            c.contains("प्ले स्टोर")) {

            return "com.android.vending";
        }

        return null;
    }

    /*
     * ============================
     * ACCESSIBILITY ACTIONS
     * ============================
     */

    private void typeText(String text) {

        VoiceAccessibilityService service =
                VoiceAccessibilityService.getInstance();

        if (service == null) {

            setStatus(
                    "⚠️ Accessibility Service ON karo"
            );

            return;
        }

        boolean result =
                service.typeText(text);

        setStatus(
                result
                        ? "⌨️ Text typed"
                        : "❌ Text field nahi mila"
        );
    }

    private void clickText(String text) {

        VoiceAccessibilityService service =
                VoiceAccessibilityService.getInstance();

        if (service == null) {

            setStatus(
                    "⚠️ Accessibility Service ON karo"
            );

            return;
        }

        boolean result =
                service.clickText(text);

        setStatus(
                result
                        ? "👆 Clicked: " + text
                        : "❌ Button/text nahi mila"
        );
    }

    private void executeBack() {

        VoiceAccessibilityService service =
                VoiceAccessibilityService.getInstance();

        if (service != null) {
            service.goBack();
        }
    }

    private void executeHome() {

        VoiceAccessibilityService service =
                VoiceAccessibilityService.getInstance();

        if (service != null) {
            service.goHome();
        }
    }

    private void executeRecents() {

        VoiceAccessibilityService service =
                VoiceAccessibilityService.getInstance();

        if (service != null) {
            service.openRecents();
        }
    }

    private void executeScrollDown() {

        VoiceAccessibilityService service =
                VoiceAccessibilityService.getInstance();

        if (service != null) {
            service.scrollDown();
        }
    }

    private void executeScrollUp() {

        VoiceAccessibilityService service =
                VoiceAccessibilityService.getInstance();

        if (service != null) {
            service.scrollUp();
        }
    }

    /*
     * ============================
     * WEB
     * ============================
     */

    private void webSearch(String query) {

        if (query == null ||
                query.trim().isEmpty()) {
            return;
        }

        try {

            String url =
                    "https://www.google.com/search?q="
                            + Uri.encode(query);

            Intent intent =
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(url)
                    );

            startActivity(intent);

            setStatus(
                    "🌐 Searching: "
                            + query
            );

        } catch (Exception e) {

            setStatus(
                    "❌ Browser open nahi hua"
            );
        }
    }

    /*
     * ============================
     * COMMAND HELPERS
     * ============================
     */

    private boolean looksLikeOpenCommand(
            String command) {

        return command.startsWith("open ") ||
               command.startsWith("launch ") ||
               command.startsWith("start ") ||
               command.startsWith("khol ") ||
               command.startsWith("kholo ") ||
               command.startsWith("chalao ") ||
               command.startsWith("खोल ") ||
               command.startsWith("खोलो ") ||
               command.startsWith("ओपन ");
    }

    private String extractAppName(
            String command) {

        String result = command;

        String[] prefixes = {
                "open ",
                "launch ",
                "start ",
                "khol ",
                "kholo ",
                "chalao ",
                "खोल ",
                "खोलो ",
                "ओपन "
        };

        for (String prefix : prefixes) {

            if (result.startsWith(prefix)) {

                result =
                        result.substring(
                                prefix.length()
                        ).trim();

                break;
            }
        }

        return result;
    }

    private String extractAfterCommand(
            String command,
            String... prefixes) {

        for (String prefix : prefixes) {

            if (command.startsWith(prefix)) {

                return command.substring(
                        prefix.length()
                ).trim();
            }
        }

        return command;
    }

    private String extractType(String command) {

        String[] prefixes = {
                "type ",
                "टाइप ",
                "likho ",
                "लिखो "
        };

        return extractAfterCommand(
                command,
                prefixes
        );
    }

    private String extractSearch(String command) {

        String[] prefixes = {
                "search ",
                "सर्च ",
                "search karo ",
                "सर्च करो "
        };

        return extractAfterCommand(
                command,
                prefixes
        );
    }

    private String extractResearch(
            String command) {

        String[] prefixes = {
                "research ",
                "research karo ",
                "web search ",
                "जानकारी "
        };

        return extractAfterCommand(
                command,
                prefixes
        );
    }

    private boolean containsAny(
            String command,
            String... words) {

        for (String word : words) {

            if (command.contains(word)) {
                return true;
            }
        }

        return false;
    }

    private String normalize(String input) {

        return input
                .toLowerCase(Locale.ROOT)
                .trim()
                .replaceAll("\\s+", " ");
    }

    private void continueListening() {

        handler.postDelayed(
                this::startListening,
                800
        );
    }

    private void setStatus(String text) {

        if (status != null) {
            status.setText(text);
        }
    }

    private String speechError(int error) {

        switch (error) {

            case SpeechRecognizer.ERROR_AUDIO:
                return "Audio error";

            case SpeechRecognizer.ERROR_CLIENT:
                return "Client error";

            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                return "Microphone permission missing";

            case SpeechRecognizer.ERROR_NETWORK:
                return "Network error";

            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                return "Network timeout";

            case SpeechRecognizer.ERROR_NO_MATCH:
                return "Speech match nahi mila";

            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                return "Recognizer busy";

            case SpeechRecognizer.ERROR_SERVER:
                return "Speech server error";

            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                return "Speech timeout";

            case 11:
                return "Speech service disconnected";

            case 12:
                return "Language unsupported";

            case 13:
                return "Language unavailable";

            default:
                return "Speech error: " + error;
        }
    }

    @Override
    protected void onDestroy() {

        handler.removeCallbacksAndMessages(null);

        if (recognizer != null) {

            try {
                recognizer.destroy();
            } catch (Exception ignored) {
            }

            recognizer = null;
        }

        super.onDestroy();
    }
}
