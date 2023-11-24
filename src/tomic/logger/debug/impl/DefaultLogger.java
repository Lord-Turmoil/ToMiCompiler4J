/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.logger.debug.impl;

import lib.twio.ITwioWriter;
import tomic.logger.debug.IDebugLogger;
import tomic.logger.debug.LogLevel;

import java.util.HashMap;
import java.util.Map;

public class DefaultLogger implements IDebugLogger {
    private ITwioWriter writer;
    private LogLevel level;
    private final Map<LogLevel, Integer> count = new HashMap<>();

    @Override
    public IDebugLogger setWriter(ITwioWriter writer) {
        this.writer = writer;
        return this;
    }

    @Override
    public IDebugLogger setLevel(LogLevel level) {
        this.level = level;
        return this;
    }

    @Override
    public void log(LogLevel level, String message) {
        if (level.ordinal() >= this.level.ordinal()) {
            writer.write('[' + level.toString() + "] ");
            writer.writeLine(message);
        }
        count.put(level, count.getOrDefault(level, 0) + 1);
    }

    @Override
    public int count(LogLevel level) {
        return count.getOrDefault(level, 0);
    }
}
