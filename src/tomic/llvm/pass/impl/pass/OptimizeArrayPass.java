/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.pass.impl.pass;

import tomic.llvm.ir.value.BasicBlock;
import tomic.llvm.ir.value.ConstantData;
import tomic.llvm.ir.value.Value;
import tomic.llvm.ir.value.inst.GetElementPtrInst;
import tomic.llvm.ir.value.inst.Instruction;

import java.util.ArrayList;

/**
 * Run after remove redundant load/store, before remove
 * unused instructions.
 */
public class OptimizeArrayPass extends BasicBlockPass {
    @Override
    protected void handleBasicBlock(BasicBlock basicBlock) {
        ArrayList< Instruction> instructions = new ArrayList<>(basicBlock.getInstructions());
        for (Instruction instruction : instructions) {
            if (instruction instanceof GetElementPtrInst inst) {
                handleGetElementPtrInst(inst);
            }
        }
    }

    private void handleGetElementPtrInst(GetElementPtrInst inst) {
        if (!(inst.getAddress() instanceof GetElementPtrInst address)) {
            return;
        }

        if (address.hasFixedDimension()) {
            mergeGetElementPtrInst(inst, address);
        } else if (address.getUsers().size() == 1) {
            mergeGetElementPtrInst(inst, address);
        }
    }

    private void mergeGetElementPtrInst(GetElementPtrInst dst, GetElementPtrInst src) {
        ArrayList<Value> subscripts = new ArrayList<>(src.getSubscripts());

        for (int i = 0; i < dst.getSubscripts().size(); i++) {
            if (i == 0) {
                if (dst.getSubscripts().get(i) instanceof ConstantData constantData) {
                    if (constantData.getValue() == 0) {
                        continue;
                    }
                }
            }
            subscripts.add(dst.getSubscripts().get(i));
        }

        var inst = GetElementPtrInst.create(src.getAddress(), subscripts);
        dst.getParent().insertInstructionAfter(inst, dst);

        PassExt.replaceOperand(dst, inst);
    }
}
