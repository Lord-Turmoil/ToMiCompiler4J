/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.lexer.impl.task;

import lib.twio.ITwioReader;
import tomic.lexer.token.ITokenMapper;
import tomic.lexer.token.Token;

public abstract class LexicalTask {
    protected final ITokenMapper mapper;

    protected LexicalTask(ITokenMapper mapper) {
        this.mapper = mapper;
    }

    public abstract boolean beginsWith(int begin);

    public abstract boolean endsWith(int end);

    public abstract Token analyze(ITwioReader reader);
}
