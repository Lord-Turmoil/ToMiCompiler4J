/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.pass;

import tomic.llvm.ir.Module;

/**
 * Represents a pass in LLVM.
 */
public interface ILlvmPass {
    /**
     * Run the pass.
     */
    void run(Module module);
}
