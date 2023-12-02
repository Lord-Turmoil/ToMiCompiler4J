/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.pass.impl.pass;

import tomic.llvm.ir.Module;
import tomic.llvm.ir.value.BasicBlock;
import tomic.llvm.ir.value.GlobalVariable;
import tomic.llvm.ir.value.inst.*;
import tomic.llvm.pass.ILlvmPass;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class RemoveRedundantStorePass implements ILlvmPass {
    @Override
    public void run(Module module) {
        for (var function : module.getAllFunctions()) {
            function.getBasicBlocks().forEach(this::handleBasicBlock);
        }
    }

    private final Set<Integer> handled = new HashSet<>();
    private final ArrayList<Instruction> storeToRemove = new ArrayList<>();

    private void handleBasicBlock(BasicBlock block) {
        handled.clear();
        storeToRemove.clear();

        int index = findStoreInst(block);
        while (index != -1) {
            handleStoreInst(block, index);
            handled.add(index);
            index = findStoreInst(block);
        }

        for (var inst : storeToRemove) {
            block.removeInstruction(inst);
        }
    }

    private int findStoreInst(BasicBlock block) {
        ArrayList<Instruction> instructions = new ArrayList<>(block.getInstructions());
        int i = instructions.size() - 1;
        while (i >= 0) {
            if (instructions.get(i) instanceof StoreInst inst) {
                /*
                 * Be careful! Only handle StoreInst with AllocaInst,
                 * as array (GetElementPtr) may be modified in an
                 * "unexpected" way.
                 */
                var operand = inst.getRightOperand();
                if (operand instanceof AllocaInst || operand instanceof GlobalVariable) {
                    if (!handled.contains(i)) {
                        return i;
                    }
                }
            }
            i--;
        }

        return -1;
    }

    private void handleStoreInst(BasicBlock block, int index) {
        ArrayList<Instruction> instructions = new ArrayList<>(block.getInstructions());
        int size = instructions.size();
        StoreInst source = (StoreInst) instructions.get(index);

        for (int i = index - 1; i >= 0; i--) {
            var instruction = instructions.get(i);
            if (instruction instanceof StoreInst inst) {
                if (inst.getRightOperand() == source.getRightOperand()) {
                    /*
                     * Duplicated store!
                     * But don't remove it now, as it may disrupt the
                     * order of instructions.
                     */
                    storeToRemove.add(inst);
                    handled.add(i);
                }
            } else if (instruction instanceof LoadInst inst) {
                if (inst.getOperand() == source.getRightOperand()) {
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
