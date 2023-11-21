package tomic.logger.error.impl;

import lib.twio.ITwioWriter;
import tomic.logger.error.ErrorTypes;
import tomic.logger.error.IErrorLogger;

public class DumbErrorLogger implements IErrorLogger {
    private int errorCount;

    @Override
    public void log(int line, int column, ErrorTypes type, String message) {
        errorCount++;
    }

    @Override
    public void dumps(ITwioWriter writer) {
        return;
    }

    @Override
    public int count() {
        return errorCount;
    }
}
