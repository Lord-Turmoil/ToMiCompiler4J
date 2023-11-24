/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.logger.debug;

import lib.twio.ITwioWriter;

public interface IDebugLogger {
    IDebugLogger setWriter(ITwioWriter writer);

    IDebugLogger setLevel(LogLevel level);

    default void debug(String message) {log(LogLevel.DEBUG, message);}

    default void info(String message) {log(LogLevel.INFO, message);}

    default void warning(String message) {log(LogLevel.WARNING, message);}

    default void error(String message) {log(LogLevel.ERROR, message);}

    default void fatal(String message) {log(LogLevel.FATAL, message);}

    void log(LogLevel level, String message);

    int count(LogLevel level);
}
