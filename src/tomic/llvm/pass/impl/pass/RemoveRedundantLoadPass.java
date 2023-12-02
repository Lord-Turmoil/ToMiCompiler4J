/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.pass.impl.pass;

import tomic.llvm.ir.value.BasicBlock;
import tomic.llvm.ir.value.GlobalVariable;
import tomic.llvm.ir.value.inst.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Remove duplicated load instructions.
 * This should be restricted in one basic block.
 */
public class RemoveRedundantLoadPass extends BasicBlockPass {
    private final Set<Integer> handled = new HashSet<>();

    @Override
    protected void handleBasicBlock(BasicBlock block) {
        handled.clear();

        int index = findLoadInst(block);
        while (index != -1) {
            handleLoadInst(block, index);
            handled.add(index);
            index = findLoadInst(block);
        }
    }

    private int findLoadInst(BasicBlock block) {
        int i = 0;
        for (var instruction : block.getInstructions()) {
            if (instruction instanceof LoadInst inst) {
                /*
                 * Be careful! Only handle LoadInst with AllocaInst,
                 * as array (GetElementPtr) may be modified in an
                 * "unexpected" way.
                 */
                var operand = inst.getOperand();
                if (operand instanceof AllocaInst || operand instanceof GlobalVariable) {
                    if (!handled.contains(i)) {
                        return i;
                    }
                }
            }
            i++;
        }

        return -1;
    }

    private void handleLoadInst(BasicBlock block, int index) {
        // Make a replica.
        ArrayList<Instruction> instructions = new ArrayList<>(block.getInstructions());
        int size = instructions.size();
        LoadInst source = (LoadInst) instructions.get(index);

        for (int i = index + 1; i < size; i++) {
            var instruction = instructions.get(i);
            if (instruction instanceof LoadInst inst) {
                if (inst.getOperand() == source.getOperand()) {
                    // Duplicated load!
                    PassExt.replaceOperand(inst, source);
                    block.removeInstruction(inst);
                }
            } else if (instruction instanceof StoreInst inst) {
                if (inst.getRightOperand() == source.getOperand()) {
                    // Sink found!
                    break;
                }
            } else if (instruction instanceof CallInst) {
                // Also sink.
                break;
            }
        }
    }
}
