package it.unige.dibris.batchrmperm.service;


import it.saonzo.rmperm.IOutput;

import java.util.ArrayList;
import java.util.List;

public class RmPermConsole implements IOutput {
    private final List<String> consoleOutput;


    public RmPermConsole() {
        consoleOutput = new ArrayList<>();
    }

    public List<String> getConsoleOutput() {
        return consoleOutput;
    }

    public String getConsoleOutputToString() {
        return String.join(" ", consoleOutput);
    }

    @Override
    public void printf(Level level, String s, Object... objects) {
        String formatted = String.format(s, objects);
        if (formatted.startsWith("Couldn't find") || formatted.startsWith("Method Landroid/view/"))
            return; // useless info: Couldn't find * inside the manifest OR the Accessibility permission
        if (level == Level.ERROR)
            consoleOutput.add(formatted);
        else if (level.priority >= Level.NORMAL.priority)
            consoleOutput.add(formatted);
    }
}
