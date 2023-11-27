/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.mips.impl;

import lib.twio.ITwioWriter;
import tomic.llvm.asm.impl.BaseAsmWriter;
import tomic.llvm.mips.IMipsWriter;
import tomic.llvm.mips.memory.Registers;

public class VerboseMipsWriter extends BaseAsmWriter implements IMipsWriter {
    private int currentIndent = 0;
    private boolean isNewLine = true;

    public VerboseMipsWriter(ITwioWriter impl) {
        super(impl, '#', 4);
    }

    @Override
    public IMipsWriter push(char ch) {
        if (isNewLine) {
            isNewLine = false;  // prevent re-entry
            for (var i = 0; i < currentIndent; ++i) {
                pushIndent();
            }
        }
        super.push(ch);
        return this;
    }

    @Override
    public IMipsWriter push(String str) {
        if (isNewLine) {
            isNewLine = false;  // prevent re-entry
            for (var i = 0; i < currentIndent; ++i) {
                pushIndent();
            }
        }
        super.push(str);
        return this;
    }

    @Override
    public IMipsWriter pushLabel(String label) {
        push(label).push(':').pushNewLine();
        return this;
    }

    @Override
    public IMipsWriter pushComma() {
        push(',');
        return this;
    }

    @Override
    public IMipsWriter pushDollar() {
        push('$');
        return this;
    }

    @Override
    public IMipsWriter pushRegister(int reg) {
        if (!Registers.isPseudo(reg)) {
            pushDollar();
        }
        push(Registers.name(reg));
        return this;
    }

    @Override
    public IMipsWriter pushNewLine() {
        super.pushNewLine();
        isNewLine = true;
        return this;
    }

    @Override
    public IMipsWriter setIndent(int indent) {
        currentIndent = indent;
        return this;
    }
}
