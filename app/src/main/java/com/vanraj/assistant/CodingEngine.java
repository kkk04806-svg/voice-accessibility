package com.vanraj.assistant;

import android.util.Log;

import java.util.Locale;

public final class CodingEngine {

    private static final String TAG = "CodingEngine";

    private CodingEngine() {
        // Utility class
    }

    /**
     * Handles coding-related voice commands.
     *
     * This first version is intentionally local and dependency-free.
     * It provides a clear response that MainActivity can speak/display.
     */
    public static String handle(String command) {

        if (command == null) {
            return "Coding command empty hai.";
        }

        String original = command.trim();

        if (original.isEmpty()) {
            return "Coding command empty hai.";
        }

        String lower = original.toLowerCase(Locale.ROOT);

        Log.d(TAG, "Coding command: " + original);

        // Basic coding requests
        if (containsAny(
                lower,
                "code banao",
                "code bana",
                "coding karo",
                "coding kar",
                "program banao",
                "program bana",
                "script banao",
                "script bana"
        )) {
            return "Coding mode ready hai. Batao kya code banana hai.";
        }

        // Android requests
        if (containsAny(
                lower,
                "android app",
                "android application",
                "apk banao",
                "apk bana"
        )) {
            return "Android coding mode ready hai. App ka feature batao.";
        }

        // Python
        if (containsAny(
                lower,
                "python code",
                "python script",
                "python program"
        )) {
            return "Python coding mode ready hai. Python mein kya banana hai?";
        }

        // Java
        if (containsAny(
                lower,
                "java code",
                "java program",
                "java class"
        )) {
            return "Java coding mode ready hai. Java mein kya banana hai?";
        }

        // HTML / website
        if (containsAny(
                lower,
                "html",
                "website banao",
                "website bana",
                "web app banao",
                "web app bana"
        )) {
            return "Web coding mode ready hai. Website mein kya banana hai?";
        }

        // Debug / fix
        if (containsAny(
                lower,
                "code fix",
                "code thik",
                "bug fix",
                "error fix",
                "debug karo",
                "debug kar"
        )) {
            return "Debug mode ready hai. Error ya code bhejo.";
        }

        // Default
        return "Coding request samajh gaya: " + original;
    }

    private static boolean containsAny(
            String text,
            String... values) {

        for (String value : values) {
            if (text.contains(value)) {
                return true;
            }
        }

        return false;
    }
}
