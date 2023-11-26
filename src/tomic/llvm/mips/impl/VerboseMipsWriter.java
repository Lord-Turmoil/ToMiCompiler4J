/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.mips.impl;

import lib.twio.ITwioWriter;
import tomic.llvm.asm.impl.BaseAsmWriter;

public class VerboseMipsWriter extends BaseAsmWriter {
    public VerboseMipsWriter(ITwioWriter impl) {
        super(impl, '#');
    }
}
