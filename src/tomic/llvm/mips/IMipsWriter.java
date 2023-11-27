/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.mips;

import tomic.llvm.asm.IAsmWriter;

public interface IMipsWriter extends IAsmWriter {
    IMipsWriter pushLabel(String label);

    IMipsWriter pushComma();

    IMipsWriter pushDollar();

    IMipsWriter pushRegister(int reg);
}
