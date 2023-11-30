/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.pass.impl;

import tomic.llvm.ir.Module;
import tomic.llvm.pass.ILlvmPass;

/**
 * Clean up LLVM IR. This should be the last pass.
 */
public class CleanUpPass implements ILlvmPass {
    @Override
    public void run(Module module) {

    }
}
