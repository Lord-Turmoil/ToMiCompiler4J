/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.asm.impl;

import lib.twio.ITwioWriter;

public class VerboseAsmWriter extends BaseAsmWriter {
    public VerboseAsmWriter(ITwioWriter impl) {
        super(impl, ';', 4);
    }
}
