package tomic.logger.debug.impl;

import lib.twio.ITwioWriter;
import tomic.logger.debug.IDebugLogger;
import tomic.logger.debug.LogLevel;

import java.util.HashMap;
import java.util.Map;

public class DumbLogger implements IDebugLogger {
    private final Map<LogLevel, Integer> count = new HashMap<>();

    @Override
    public DumbLogger setWriter(ITwioWriter writer) {
        return this;
    }

    @Override
    public DumbLogger setLevel(LogLevel level) {
        return this;
    }

    @Override
    public void log(LogLevel level, String message) {
        count.put(level, count.getOrDefault(level, 0) + 1);
    }

    @Override
    public int count(LogLevel level) {
        return count.getOrDefault(level, 0);
    }
}
