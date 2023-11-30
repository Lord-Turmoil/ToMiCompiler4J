/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.pass.impl;

import tomic.llvm.ir.Module;
import tomic.llvm.ir.value.BasicBlock;
import tomic.llvm.ir.value.inst.*;
import tomic.llvm.pass.ILlvmPass;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Remove duplicated load instructions.
 * This should be restricted in one basic block.
 */
public class RemoveDuplicatedLoadPass implements ILlvmPass {
    @Override
    public void run(Module module) {
        for (var function : module.getAllFunctions()) {
            function.getBasicBlocks().forEach(this::handleBasicBlock);
        }
    }

    private static final Set<Integer> handled = new HashSet<>();

    private void handleBasicBlock(BasicBlock block) {
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
                if (inst.getOperand() instanceof AllocaInst) {
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
        int count = 0;
        int threashold = 16;

        for (int i = index + 1; i < size; i++) {
            var instruction = instructions.get(i);
            if (instruction instanceof LoadInst inst) {
                if (inst.getOperand() == source.getOperand()) {
                    // Duplicated load!
                    for (var user : inst.getUsers()) {
                        user.replaceOperand(inst, source);
                    }
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

            // Don't go too far, as saving value to stack is worse.
            if (!instruction.getType().isVoidTy()) {
                count++;
                if (count > threashold) {
                    break;
                }
            }
        }
    }
}
