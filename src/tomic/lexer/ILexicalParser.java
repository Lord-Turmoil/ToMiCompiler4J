/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.lexer;

import lib.twio.ITwioReader;
import tomic.lexer.token.Token;

public interface ILexicalParser {
    ILexicalParser setReader(ITwioReader reader);

    Token current();

    Token next();

    Token rewind();

    int setCheckPoint();

    void rollBack(int checkpoint);
}
