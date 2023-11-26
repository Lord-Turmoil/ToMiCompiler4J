/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.asm.impl;

import lib.twio.ITwioWriter;
import tomic.llvm.asm.IAsmWriter;

public class BaseAsmWriter implements IAsmWriter {
    private final ITwioWriter impl;
    private final char commentCharacter;

    public BaseAsmWriter(ITwioWriter impl, char commentCharacter) {
        this.impl = impl;
        this.commentCharacter = commentCharacter;
    }

    @Override
    public IAsmWriter push(char ch) {
        impl.write(ch);
        return this;
    }

    @Override
    public IAsmWriter push(String str) {
        impl.write(str);
        return this;
    }

    @Override
    public IAsmWriter commentBegin() {
        return push(commentCharacter).pushSpace();
    }

    @Override
    public IAsmWriter commentEnd() {
        return pushNewLine();
    }
}
