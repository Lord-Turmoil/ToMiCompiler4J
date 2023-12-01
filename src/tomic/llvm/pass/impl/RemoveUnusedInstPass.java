/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.pass.impl;

import tomic.llvm.ir.Module;
import tomic.llvm.ir.value.Function;
import tomic.llvm.pass.ILlvmPass;

public class RemoveUnusedInstPass implements ILlvmPass {
    @Override
    public void run(Module module) {

    }

    private void handleFunction(Function function) {
    }
}
