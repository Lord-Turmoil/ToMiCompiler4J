/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.pass.impl.pass;

import tomic.llvm.ir.value.BasicBlock;
import tomic.llvm.ir.value.Value;
import tomic.llvm.ir.value.inst.Instruction;
import tomic.llvm.ir.value.inst.UnaryOperator;

import java.util.ArrayList;

public class CombineUnaryOperatorPass extends BasicBlockPass {
    @Override
    protected void handleBasicBlock(BasicBlock basicBlock) {
        ArrayList<Instruction> instructionsToRemove = new ArrayList<>();
        var instructions = basicBlock.getInstructions();

        int i = 0;
        // The last instruction is always a branch instruction.
        while (i < instructions.size() - 1) {
            var inst = instructions.get(i);
            if (!(inst instanceof UnaryOperator current)) {
                i++;
                continue;
            }

            if (canRemove(current)) {
                instructionsToRemove.add(inst);
                i++;
                continue;
            }

            if (instructions.get(i + 1) instanceof UnaryOperator next) {
                if (canRemove(current, next)) {
                    removeInstructionPair(current, next);
                    instructionsToRemove.add(next);
                    instructionsToRemove.add(current);
                    i += 2;
                    continue;
                }
            }

            i++;
        }

        for (var inst : instructionsToRemove) {
            basicBlock.removeInstruction(inst);
        }
    }

    private void removeInstructionPair(UnaryOperator current, UnaryOperator next) {
        Value source = current.getOperand();
        next.getUsers().forEach(user -> user.replaceOperand(next, source));
    }

    private boolean canRemove(UnaryOperator operator) {
        return operator.getOpType() == UnaryOperator.UnaryOpTypes.Pos;
    }

    private boolean canRemove(UnaryOperator current, UnaryOperator next) {
        if (next.getOperand() != current) {
            return false;
        }
        if (current.getOpType() != next.getOpType()) {
            return false;
        }

        return current.getOpType() == UnaryOperator.UnaryOpTypes.Not
                || current.getOpType() == UnaryOperator.UnaryOpTypes.Neg;
    }
}
