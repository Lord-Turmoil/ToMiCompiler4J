/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.lexer;

import lib.twio.ITwioReader;
import tomic.lexer.token.Token;

public interface ILexicalAnalyzer {
    void setReader(ITwioReader reader);

    Token next();
}
