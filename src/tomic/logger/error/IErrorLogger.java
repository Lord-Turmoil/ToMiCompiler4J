/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.logger.error;

import lib.twio.ITwioWriter;

public interface IErrorLogger {
    void log(int line, int column, ErrorTypes type, String message);

    void dumps(ITwioWriter writer);

    int count();
}
