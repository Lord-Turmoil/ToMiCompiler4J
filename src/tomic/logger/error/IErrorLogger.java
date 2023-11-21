package tomic.logger.error;

import lib.twio.ITwioWriter;

public interface IErrorLogger {
    void log(int line, int column, ErrorTypes type, String message);

    void dumps(ITwioWriter writer);

    int count();
}
