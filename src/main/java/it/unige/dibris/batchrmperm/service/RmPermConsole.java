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

    @Override
    public void printf(Level level, String s, Object... objects) {
        if (level == Level.ERROR)
            consoleOutput.add(String.format(s, objects));
        else if (level.priority >= Level.NORMAL.priority)
            consoleOutput.add(String.format(s, objects));
    }
}
