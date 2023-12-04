/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.pass.impl.pass;

import tomic.llvm.ir.value.BasicBlock;
import tomic.llvm.ir.value.inst.CallInst;
import tomic.llvm.ir.value.inst.Instruction;

import java.util.ArrayList;
import java.util.Collections;

public class RemoveUnusedInstPass extends BasicBlockPass {
    @Override
    protected void handleBasicBlock(BasicBlock basicBlock) {
        ArrayList<Instruction> instructions = new ArrayList<>(basicBlock.getInstructions());
        Collections.reverse(instructions);
        for (var inst : instructions) {
            /*
             * void instructions like store, br won't have user,
             * but should not be removed.
             */
            if (inst.getType().isVoidTy()) {
                continue;
            }
            if (inst instanceof CallInst) {
                // CallInst is a special case, it may have side effects.
                continue;
            }

            if (inst.getUsers().isEmpty()) {
                // Remove operands to cause chain reaction.
                basicBlock.removeInstruction(inst);
            }
        }
    }
}
