package com.vanraj.assistant;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognitionService;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private TextView statusText;
    private SpeechRecognizer speechRecognizer;

    private static final int MIC_REQUEST = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        buildUI();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MIC_REQUEST
            );
        }

        runDiagnostic();
    }

    private void buildUI() {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);

        TextView title = new TextView(this);
        title.setText("VANRAJ AI - Speech Diagnostic");
        title.setTextSize(22);
        title.setPadding(0, 0, 0, 30);

        statusText = new TextView(this);
        statusText.setTextSize(16);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(statusText);

        Button testButton = new Button(this);
        testButton.setText("🎤 TEST SPEECH");

        testButton.setOnClickListener(v -> startSpeechTest());

        layout.addView(title);
        layout.addView(testButton);
        layout.addView(scroll);

        setContentView(layout);
    }

    private void runDiagnostic() {

        StringBuilder result = new StringBuilder();

        result.append("===== SPEECH DIAGNOSTIC =====\n\n");

        result.append("Android SDK: ")
                .append(Build.VERSION.SDK_INT)
                .append("\n");

        result.append("Device: ")
                .append(Build.MANUFACTURER)
                .append(" ")
                .append(Build.MODEL)
                .append("\n\n");

        boolean recognitionAvailable =
                SpeechRecognizer.isRecognitionAvailable(this);

        result.append("Speech Recognition Available: ")
                .append(recognitionAvailable)
                .append("\n");

        if (Build.VERSION.SDK_INT >= 31) {

            boolean onDeviceAvailable =
                    SpeechRecognizer.isOnDeviceRecognitionAvailable(this);

            result.append("On-Device Recognition Available: ")
                    .append(onDeviceAvailable)
                    .append("\n");
        }

        result.append("\n===== RECOGNITION SERVICES =====\n");

        Intent serviceIntent =
                new Intent(RecognitionService.SERVICE_INTERFACE);

        PackageManager pm = getPackageManager();

        List<ResolveInfo> services =
                pm.queryIntentServices(
                        serviceIntent,
                        PackageManager.GET_META_DATA
                );

        if (services == null || services.isEmpty()) {

            result.append(
                    "❌ NO RECOGNITION SERVICE FOUND\n"
            );

        } else {

            result.append(
                    "✅ Recognition services found: "
            ).append(services.size()).append("\n\n");

            for (ResolveInfo info : services) {

                ServiceInfo serviceInfo =
                        info.serviceInfo;

                if (serviceInfo == null) continue;

                result.append("PACKAGE: ")
                        .append(serviceInfo.packageName)
                        .append("\n");

                result.append("SERVICE: ")
                        .append(serviceInfo.name)
                        .append("\n");

                result.append("ENABLED: ")
                        .append(serviceInfo.enabled)
                        .append("\n");

                result.append("------------------------\n");
            }
        }

        result.append("\n===== RESULT =====\n");

        if (!recognitionAvailable) {

            result.append(
                    "❌ Android ko koi speech recognition provider nahi mil raha.\n"
            );

        } else {

            result.append(
                    "✅ Android ke paas speech recognition provider hai.\n"
            );

            result.append(
                    "Ab TEST SPEECH dabakar actual recognition test karo.\n"
            );
        }

        statusText.setText(result.toString());
    }

    private void startSpeechTest() {

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {

            statusText.append(
                    "\n\n❌ SpeechRecognizer available nahi hai."
            );

            return;
        }

        if (speechRecognizer != null) {

            speechRecognizer.destroy();
            speechRecognizer = null;
        }

        speechRecognizer =
                SpeechRecognizer.createSpeechRecognizer(this);

        speechRecognizer.setRecognitionListener(
                new RecognitionListener() {

                    @Override
                    public void onReadyForSpeech(Bundle params) {

                        statusText.append(
                                "\n\n🎤 READY — ab bolo..."
                        );
                    }

                    @Override
                    public void onBeginningOfSpeech() {

                        statusText.append(
                                "\n🎙️ Speech detected..."
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

                        statusText.append(
                                "\n⏹️ Speech ended..."
                        );
                    }

                    @Override
                    public void onError(int error) {

                        statusText.append(
                                "\n❌ ERROR CODE: "
                                        + error
                                        + "\n"
                                        + errorMessage(error)
                        );
                    }

                    @Override
                    public void onResults(Bundle results) {

                        ArrayList<String> matches =
                                results.getStringArrayList(
                                        SpeechRecognizer
                                                .RESULTS_RECOGNITION
                                );

                        statusText.append(
                                "\n\n✅ RECOGNITION RESULT:\n"
                        );

                        if (matches != null &&
                                !matches.isEmpty()) {

                            for (String text : matches) {

                                statusText.append(
                                        "→ "
                                                + text
                                                + "\n"
                                );
                            }

                        } else {

                            statusText.append(
                                    "❌ Result empty"
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

        Intent intent =
                new Intent(
                        RecognizerIntent.ACTION_RECOGNIZE_SPEECH
                );

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                "hi-IN"
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "hi-IN"
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                true
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_MAX_RESULTS,
                5
        );

        statusText.append(
                "\n\nStarting speech test..."
        );

        speechRecognizer.startListening(intent);
    }

    private String errorMessage(int error) {

        switch (error) {

            case SpeechRecognizer.ERROR_AUDIO:
                return "ERROR_AUDIO";

            case SpeechRecognizer.ERROR_CLIENT:
                return "ERROR_CLIENT";

            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                return "ERROR_INSUFFICIENT_PERMISSIONS";

            case SpeechRecognizer.ERROR_NETWORK:
                return "ERROR_NETWORK";

            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                return "ERROR_NETWORK_TIMEOUT";

            case SpeechRecognizer.ERROR_NO_MATCH:
                return "ERROR_NO_MATCH — speech recognize nahi hui";

            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                return "ERROR_RECOGNIZER_BUSY";

            case SpeechRecognizer.ERROR_SERVER:
                return "ERROR_SERVER";

            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                return "ERROR_SPEECH_TIMEOUT — speech nahi mili";

            case 10:
                return "ERROR_TOO_MANY_REQUESTS";

            case 11:
                return "ERROR_SERVER_DISCONNECTED";

            case 12:
                return "ERROR_LANGUAGE_NOT_SUPPORTED";

            case 13:
                return "ERROR_LANGUAGE_UNAVAILABLE";

            case 14:
                return "ERROR_CANNOT_CHECK_SUPPORT";

            default:
                return "UNKNOWN ERROR";
        }
    }

    @Override
    protected void onDestroy() {

        if (speechRecognizer != null) {

            speechRecognizer.destroy();
            speechRecognizer = null;
        }

        super.onDestroy();
    }
}
