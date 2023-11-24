/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.lexer;

import lib.twio.ITwioReader;
import lib.twio.ITwioWriter;

public interface IPreprocessor {
    IPreprocessor setReader(ITwioReader reader);
    IPreprocessor setWriter(ITwioWriter writer);

    ITwioReader getReader();
    ITwioWriter getWriter();

    void process();
}
