/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.pass.impl.pass;

import tomic.llvm.ir.Module;
import tomic.llvm.ir.value.BasicBlock;
import tomic.llvm.ir.value.Function;
import tomic.llvm.pass.ILlvmPass;

public abstract class BasicBlockPass implements ILlvmPass {
    @Override
    public void run(Module module) {
        module.getAllFunctions().forEach(this::handleFunction);
    }

    private void handleFunction(Function function) {
        function.getBasicBlocks().forEach(this::handleBasicBlock);
    }

    protected abstract void handleBasicBlock(BasicBlock basicBlock);
}
