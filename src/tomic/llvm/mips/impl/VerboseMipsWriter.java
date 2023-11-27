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
    public VerboseMipsWriter(ITwioWriter impl) {
        super(impl, '#');
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
        pushDollar().push(Registers.name(reg));
        return this;
    }
}
