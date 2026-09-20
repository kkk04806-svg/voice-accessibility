package com.vanraj.assistant;

import android.content.Context;

public class CommandEngine {

    private final SkillEngine skillEngine;

    public CommandEngine(Context context) {
        skillEngine = new SkillEngine(context);
    }

    public String execute(String command) {
        if (command == null ||
                command.trim().isEmpty()) {

            return "Command nahi mili bhai.";
        }

        return skillEngine.execute(command);
    }
}
