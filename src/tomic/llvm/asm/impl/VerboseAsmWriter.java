/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.asm.impl;

import lib.twio.ITwioWriter;
import tomic.llvm.asm.IAsmWriter;

public class VerboseAsmWriter implements IAsmWriter {
    private final ITwioWriter impl;

    public VerboseAsmWriter(ITwioWriter impl) {
        this.impl = impl;
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
    public IAsmWriter pushComment(String comment) {
        return push("; ").push(comment).pushNewLine();
    }

    @Override
    public IAsmWriter commentBegin() {
        return push("; ");
    }

    @Override
    public IAsmWriter commentEnd() {
        return pushNewLine();
    }
}
