package com.vanraj.assistant;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private SpeechRecognizer recognizer;
    private TextToSpeech tts;
    private TextView status;

    private final Handler handler = new Handler();

    private boolean listening = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(35, 60, 35, 40);

        TextView title = new TextView(this);
        title.setText("🤖 VANRAJ AI ASSISTANT");
        title.setTextSize(24);

        status = new TextView(this);
        status.setTextSize(18);
        status.setPadding(0, 50, 0, 20);
        status.setText("Starting...");

        layout.addView(title);
        layout.addView(status);

        setContentView(layout);

        tts = new TextToSpeech(this, result -> {
            if (result == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
            }
        });

        if (checkSelfPermission(
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.RECORD_AUDIO
                    },
                    100
            );

        } else {
            startAssistant();
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

        if (requestCode == 100) {

            if (grantResults.length > 0 &&
                    grantResults[0]
                            == PackageManager.PERMISSION_GRANTED) {

                startAssistant();

            } else {

                status.setText(
                        "❌ Microphone permission OFF hai.\n\n" +
                        "Settings → Apps → Voice Assistant → " +
                        "Permissions → Microphone → Allow"
                );
            }
        }
    }

    private void startAssistant() {

        status.setText(
                "🤖 VANRAJ AI\n\n" +
                "Speech engine start ho raha hai..."
        );

        handler.postDelayed(
                this::setupSpeech,
                500
        );
    }

    private void setupSpeech() {

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {

            status.setText(
                    "❌ Speech Recognition service nahi mili.\n\n" +
                    "Google Speech Services / Google app check karo."
            );

            speak(
                    "Speech recognition service nahi mili"
            );

            return;
        }

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
                    public void onReadyForSpeech(
                            Bundle params) {

                        listening = true;

                        status.setText(
                                "🎤 SUN RAHA HOON BHAI...\n\n" +
                                "Hindi / English / Hinglish bolo."
                        );
                    }

                    @Override
                    public void onBeginningOfSpeech() {

                        status.setText(
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

                        listening = false;

                        status.setText(
                                "🧠 Command samajh raha hoon..."
                        );
                    }

                    @Override
                    public void onError(int error) {

                        listening = false;

                        status.setText(
                                "⚠️ Speech error: " +
                                error +
                                "\n\nRetry..."
                        );

                        restartListening();
                    }

                    @Override
                    public void onResults(
                            Bundle results) {

                        listening = false;

                        ArrayList<String> matches =
                                results.getStringArrayList(
                                        SpeechRecognizer
                                                .RESULTS_RECOGNITION
                                );

                        if (matches == null ||
                                matches.isEmpty()) {

                            restartListening();
                            return;
                        }

                        String command =
                                matches.get(0);

                        status.setText(
                                "👤 You:\n" +
                                command +
                                "\n\n⚙️ Processing..."
                        );

                        executeCommand(command);

                        handler.postDelayed(
                                () -> listen(),
                                1200
                        );
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

        listen();
    }

    private void listen() {

        if (recognizer == null) {
            setupSpeech();
            return;
        }

        if (listening) {
            return;
        }

        try {

            Intent intent =
                    new Intent(
                            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
                    );

            intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent
                            .LANGUAGE_MODEL_FREE_FORM
            );

            /*
             * en-IN gives good Hindi/English mixed
             * recognition on many Indian phones.
             */
            intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE,
                    "en-IN"
            );

            intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                    "hi-IN"
            );

            intent.putExtra(
                    RecognizerIntent.EXTRA_MAX_RESULTS,
                    5
            );

            intent.putExtra(
                    RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                    false
            );

            intent.putExtra(
                    RecognizerIntent.EXTRA_PROMPT,
                    "Bolo bhai"
            );

            recognizer.cancel();

            recognizer.startListening(intent);

        } catch (Exception e) {

            status.setText(
                    "⚠️ Listening start failed\n" +
                    e.getMessage()
            );

            restartListening();
        }
    }

    private void restartListening() {

        handler.postDelayed(
                () -> {

                    if (!isFinishing()) {
                        listen();
                    }

                },
                1800
        );
    }

    /*
     * UNIVERSAL COMMAND PARSER
     */

    private void executeCommand(String original) {

        if (original == null) {
            return;
        }

        String command =
                original.trim();

        String lower =
                command.toLowerCase(
                        Locale.ROOT
                );

        /*
         * HOME
         */

        if (lower.equals("home") ||
                lower.contains("home jao") ||
                lower.contains("home kholo") ||
                lower.contains("home open")) {

            VoiceAccessibilityService service =
                    VoiceAccessibilityService.getInstance();

            if (service != null) {
                service.goHome();
                speak("Home kar diya bhai");
            } else {
                accessibilityOff();
            }

            return;
        }

        /*
         * BACK
         */

        if (lower.equals("back") ||
                lower.contains("back karo") ||
                lower.contains("back jao") ||
                lower.contains("peeche jao")) {

            VoiceAccessibilityService service =
                    VoiceAccessibilityService.getInstance();

            if (service != null) {
                service.goBack();
                speak("Back kar diya bhai");
            } else {
                accessibilityOff();
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
                speak("Recent apps khol diya");
            } else {
                accessibilityOff();
            }

            return;
        }

        /*
         * SCROLL
         */

        if (lower.contains("scroll down") ||
                lower.contains("neeche scroll") ||
                lower.contains("neeche karo")) {

            VoiceAccessibilityService service =
                    VoiceAccessibilityService.getInstance();

            if (service != null) {
                service.scrollDown();
                speak("Neeche scroll kar diya");
            } else {
                accessibilityOff();
            }

            return;
        }

        if (lower.contains("scroll up") ||
                lower.contains("upar scroll") ||
                lower.contains("upar karo")) {

            VoiceAccessibilityService service =
                    VoiceAccessibilityService.getInstance();

            if (service != null) {
                service.scrollUp();
                speak("Upar scroll kar diya");
            } else {
                accessibilityOff();
            }

            return;
        }

        /*
         * TYPE / LIKHO
         */

        if (lower.startsWith("type ") ||
                lower.startsWith("likho ") ||
                lower.startsWith("write ") ||
                lower.contains("message likho") ||
                lower.contains("text likho")) {

            String text =
                    extractText(command);

            typeText(text);

            return;
        }

        /*
         * SEARCH
         */

        if (lower.startsWith("search ") ||
                lower.startsWith("google ")) {

            String query;

            if (lower.startsWith("search ")) {
                query = command.substring(7).trim();
            } else {
                query = command.substring(7).trim();
            }

            searchWeb(query);

            return;
        }

        /*
         * OPEN APP
         */

        String appName =
                extractRequestedApp(command);

        if (!appName.isEmpty()) {

            openInstalledApp(appName);

            return;
        }

        /*
         * If nothing matched,
         * try treating the complete command
         * as an installed app name.
         */

        if (tryOpenByWords(command)) {
            return;
        }

        speak(
                "Command samajh nahi aayi bhai. " +
                "Dobara bolo."
        );
    }

    /*
     * TEXT EXTRACTION
     */

    private String extractText(
            String command) {

        String lower =
                command.toLowerCase(
                        Locale.ROOT
                );

        String[] prefixes = {
                "type ",
                "likho ",
                "write ",
                "message likho ",
                "text likho "
        };

        for (String prefix : prefixes) {

            if (lower.startsWith(prefix)) {

                return command
                        .substring(prefix.length())
                        .trim();
            }
        }

        int index =
                lower.indexOf(
                        "message likho"
                );

        if (index >= 0) {

            return command.substring(
                    index +
                    "message likho".length()
            ).trim();
        }

        return command;
    }

    private void typeText(String text) {

        VoiceAccessibilityService service =
                VoiceAccessibilityService.getInstance();

        if (service == null) {
            accessibilityOff();
            return;
        }

        if (text.isEmpty()) {

            speak(
                    "Kya type karna hai bhai?"
            );

            return;
        }

        boolean result =
                service.typeText(text);

        if (result) {

            speak(
                    "Text type kar diya bhai"
            );

        } else {

            speak(
                    "Active text field nahi mili bhai"
            );
        }
    }

    /*
     * INSTALLED APP DISCOVERY
     */

    private String extractRequestedApp(
            String command) {

        String cleaned =
                command.toLowerCase(
                        Locale.ROOT
                ).trim();

        String[] prefixes = {
                "open ",
                "open app ",
                "launch ",
                "start ",
                "khol ",
                "khol do ",
                "kholo ",
                "app kholo ",
                "app open ",
                "chalao ",
                "chala do "
        };

        for (String prefix : prefixes) {

            if (cleaned.startsWith(prefix)) {

                String name =
                        cleaned.substring(
                                prefix.length()
                        ).trim();

                name =
                        removeEndWords(name);

                if (!name.isEmpty()) {
                    return name;
                }
            }
        }

        if (cleaned.endsWith(" kholo")) {

            return removeEndWords(
                    cleaned.substring(
                            0,
                            cleaned.length() - 7
                    )
            );
        }

        if (cleaned.endsWith(" open")) {

            return removeEndWords(
                    cleaned.substring(
                            0,
                            cleaned.length() - 5
                    )
            );
        }

        return "";
    }

    private String removeEndWords(
            String value) {

        String result =
                value.trim();

        String[] endings = {
                " app",
                " application",
                " ko",
                " please",
                " kar do"
        };

        for (String ending : endings) {

            if (result.endsWith(ending)) {

                result =
                        result.substring(
                                0,
                                result.length()
                                        - ending.length()
                        ).trim();
            }
        }

        return result;
    }

    private boolean tryOpenByWords(
            String command) {

        String target =
                command.toLowerCase(
                        Locale.ROOT
                ).trim();

        if (target.contains(" ")) {
            return false;
        }

        return openInstalledApp(target);
    }

    /*
     * FIND INSTALLED APP
     */

    private boolean openInstalledApp(
            String requestedName) {

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

        if (apps == null ||
                apps.isEmpty()) {

            speak(
                    "Installed apps nahi mil rahe bhai"
            );

            return false;
        }

        String wanted =
                requestedName
                        .toLowerCase(
                                Locale.ROOT
                        )
                        .trim();

        ResolveInfo bestMatch = null;

        int bestScore = 0;

        for (ResolveInfo info : apps) {

            if (info.activityInfo == null) {
                continue;
            }

            CharSequence label =
                    info.loadLabel(pm);

            String appLabel =
                    label == null
                            ? ""
                            : label.toString();

            String labelLower =
                    appLabel.toLowerCase(
                            Locale.ROOT
                    );

            String packageName =
                    info.activityInfo.packageName
                            .toLowerCase(
                                    Locale.ROOT
                            );

            int score = 0;

            if (labelLower.equals(wanted)) {
                score = 100;
            } else if (labelLower.startsWith(wanted)) {
                score = 80;
            } else if (labelLower.contains(wanted)) {
                score = 60;
            } else if (packageName.contains(wanted)) {
                score = 40;
            }

            if (score > bestScore) {

                bestScore = score;
                bestMatch = info;
            }
        }

        if (bestMatch == null) {

            speak(
                    requestedName +
                    " phone mein nahi mila bhai"
            );

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
                    bestMatch.activityInfo.packageName,
                    bestMatch.activityInfo.name
            );

            launch.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
            );

            startActivity(launch);

            String appName =
                    bestMatch
                            .loadLabel(pm)
                            .toString();

            status.setText(
                    "📱 Opening: " +
                    appName
            );

            speak(
                    appName +
                    " khol diya bhai"
            );

            return true;

        } catch (Exception e) {

            speak(
                    "App open nahi hua bhai"
            );

            return false;
        }
    }

    /*
     * WEB SEARCH
     */

    private void searchWeb(
            String query) {

        if (query == null ||
                query.trim().isEmpty()) {

            speak(
                    "Kya search karna hai bhai?"
            );

            return;
        }

        try {

            String url =
                    "https://www.google.com/search?q=" +
                    Uri.encode(query);

            Intent intent =
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(url)
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

    private void accessibilityOff() {

        speak(
                "Accessibility Service ON nahi hai bhai"
        );
    }

    private void speak(String message) {

        if (tts == null) {
            return;
        }

        tts.speak(
                message,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "vanraj_reply"
        );
    }

    @Override
    protected void onDestroy() {

        handler.removeCallbacksAndMessages(null);

        if (recognizer != null) {

            try {
                recognizer.cancel();
                recognizer.destroy();
            } catch (Exception ignored) {
            }

            recognizer = null;
        }

        if (tts != null) {

            try {
                tts.stop();
                tts.shutdown();
            } catch (Exception ignored) {
            }

            tts = null;
        }

        super.onDestroy();
    }
}
