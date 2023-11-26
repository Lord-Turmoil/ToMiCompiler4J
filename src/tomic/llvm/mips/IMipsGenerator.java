/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.mips;

import lib.twio.ITwioWriter;
import tomic.llvm.ir.Module;

public interface IMipsGenerator {
    void generate(Module module, ITwioWriter output);
}
