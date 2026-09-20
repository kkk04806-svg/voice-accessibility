package com.vanraj.assistant;

import android.util.Log;

import java.util.Locale;

public final class TradingSkill {

    private static final String TAG = "TradingSkill";

    private TradingSkill() {
        // Utility class
    }

    /**
     * Handles trading-related voice commands.
     *
     * This version does NOT place real trades.
     * It only identifies the trading request and returns a safe response.
     */
    public static String handle(String command) {

        if (command == null) {
            return "Trading command empty hai.";
        }

        String original = command.trim();

        if (original.isEmpty()) {
            return "Trading command empty hai.";
        }

        String lower = original.toLowerCase(Locale.ROOT);

        Log.d(TAG, "Trading command: " + original);

        // Analysis
        if (containsAny(
                lower,
                "analyse",
                "analysis",
                "analyze",
                "market analysis",
                "market analyse",
                "market dekho",
                "market check"
        )) {
            return "Trading analysis mode ready hai. Coin ya market batao.";
        }

        // BTC
        if (containsAny(
                lower,
                "btc",
                "bitcoin",
                "बिटकॉइन"
        )) {
            return "Bitcoin request samajh gaya. Analysis ke liye BTC timeframe batao.";
        }

        // ETH
        if (containsAny(
                lower,
                "eth",
                "ethereum",
                "एथेरियम"
        )) {
            return "Ethereum request samajh gaya. Analysis ke liye ETH timeframe batao.";
        }

        // Signal
        if (containsAny(
                lower,
                "signal",
                "buy signal",
                "sell signal",
                "entry signal",
                "trade signal"
        )) {
            return "Trading signal mode ready hai. Coin aur timeframe batao.";
        }

        // Price
        if (containsAny(
                lower,
                "price",
                "rate",
                "current price",
                "live price"
        )) {
            return "Price request samajh gaya. Coin ka naam batao.";
        }

        // Risk
        if (containsAny(
                lower,
                "risk",
                "stop loss",
                "stoploss",
                "take profit",
                "target"
        )) {
            return "Risk management mode ready hai. Coin, entry aur capital batao.";
        }

        // Leverage
        if (containsAny(
                lower,
                "leverage",
                "5x",
                "10x",
                "20x"
        )) {
            return "Leverage request samajh gaya. Main calculation aur risk explain kar sakta hoon.";
        }

        // Real trading request
        if (containsAny(
                lower,
                "trade lagao",
                "trade karo",
                "buy karo",
                "sell karo",
                "order lagao",
                "order place"
        )) {
            return "Real trade place nahi kar raha. Pehle exact order details aur confirmation chahiye.";
        }

        // Default
        return "Trading request samajh gaya: " + original;
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
