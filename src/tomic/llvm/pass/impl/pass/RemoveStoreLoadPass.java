/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.pass.impl.pass;

import tomic.llvm.ir.value.Argument;
import tomic.llvm.ir.value.BasicBlock;
import tomic.llvm.ir.value.inst.Instruction;
import tomic.llvm.ir.value.inst.LoadInst;
import tomic.llvm.ir.value.inst.StoreInst;

import java.util.ArrayList;

/**
 * Remove load immediately preceded by store to the same address.
 */
public class RemoveStoreLoadPass extends BasicBlockPass {
    private final ArrayList<Instruction> loadToRemove = new ArrayList<>();

    @Override
    protected void handleBasicBlock(BasicBlock basicBlock) {
        loadToRemove.clear();
        ArrayList<Instruction> instructions = new ArrayList<>(basicBlock.getInstructions());

        int index = findStoreLoad(instructions, 0);
        while (index != -1) {
            handleStoreLoad((StoreInst) instructions.get(index), (LoadInst) instructions.get(index + 1));
            index = findStoreLoad(instructions, index + 2);
        }

        for (var inst : loadToRemove) {
            basicBlock.removeInstruction(inst);
        }
    }

    private int findStoreLoad(ArrayList<Instruction> instructions, int index) {
        for (int i = index; i < instructions.size() - 1; i++) {
            if (instructions.get(i) instanceof StoreInst storeInst) {
                if (storeInst.getLeftOperand() instanceof Argument argument) {
                    if (argument.getArgNo() < 4) {
                        continue;
                    }
                }
                if (instructions.get(i + 1) instanceof LoadInst loadInst) {
                    if (storeInst.getRightOperand() == loadInst.getOperand()) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    private void handleStoreLoad(StoreInst storeInst, LoadInst loadInst) {
        var value = storeInst.getLeftOperand();
        PassExt.replaceOperand(loadInst, value);
        loadToRemove.add(loadInst);
    }
}
