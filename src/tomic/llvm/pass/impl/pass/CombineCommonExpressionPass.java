/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.pass.impl.pass;

import tomic.llvm.ir.value.BasicBlock;
import tomic.llvm.ir.value.inst.BinaryOperator;
import tomic.llvm.ir.value.inst.Instruction;
import tomic.llvm.ir.value.inst.UnaryOperator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Combine common expression. This should be run after {@link RemoveRedundantLoadPass}
 * and {@link RemoveRedundantStorePass}. As load and store are properly
 * handled, we can safely combine common expression without worrying about
 * the side effect of load and store. As when a load occurs, the operand
 * of operator will not be the same.
 */
public class CombineCommonExpressionPass extends BasicBlockPass {
    private final Set<Integer> handled = new HashSet<>();
    private final ArrayList<Instruction> instructionsToRemove = new ArrayList<>();

    @Override
    protected void handleBasicBlock(BasicBlock basicBlock) {
        handled.clear();
        instructionsToRemove.clear();
        ArrayList<Instruction> instructions = new ArrayList<>(basicBlock.getInstructions());

        int index = findNextCandidate(instructions, 0);
        while (index != -1) {
            handleInstruction(instructions.get(index), instructions, index);
            index = findNextCandidate(instructions, index + 1);
        }

        for (var inst : instructionsToRemove) {
            basicBlock.removeInstruction(inst);
        }
    }

    private int findNextCandidate(List<Instruction> instructions, int start) {
        for (int i = start; i < instructions.size(); i++) {
            if (handled.contains(i)) {
                continue;
            }

            var inst = instructions.get(i);
            if (inst instanceof BinaryOperator || inst instanceof UnaryOperator) {
                return i;
            }
        }

        return -1;
    }

    private void handleInstruction(Instruction instruction, List<Instruction> instructions, int index) {
        if (instruction instanceof BinaryOperator inst) {
            handleBinaryOperator(inst, instructions, index);
        } else if (instruction instanceof UnaryOperator inst) {
            handleUnaryOperator(inst, instructions, index);
        }
    }

    private void handleBinaryOperator(BinaryOperator source, List<Instruction> instructions, int index) {
        handled.add(index);

        for (int i = index + 1; i < instructions.size(); i++) {
            if (instructions.get(i) instanceof BinaryOperator inst) {
                if (match(source, inst)) {
                    inst.getUsers().forEach(user -> user.replaceOperand(inst, source));
                    instructionsToRemove.add(inst);
                    handled.add(i);
                }
            }
        }
    }

    private void handleUnaryOperator(UnaryOperator source, List<Instruction> instructions, int index) {
        handled.add(index);

        for (int i = index + 1; i < instructions.size(); i++) {
            if (instructions.get(i) instanceof UnaryOperator inst) {
                if (match(source, inst)) {
                    PassExt.replaceOperand(inst, source);
                    instructionsToRemove.add(inst);
                    handled.add(i);
                }
            }
        }
    }

    private boolean match(BinaryOperator inst1, BinaryOperator inst2) {
        if (inst1.getOpType() != inst2.getOpType()) {
            return false;
        }
        if (inst1.getLeftOperand() == inst2.getLeftOperand() && inst1.getRightOperand() == inst2.getRightOperand()) {
            return true;
        }
        if (inst1.getLeftOperand() == inst2.getRightOperand() && inst1.getRightOperand() == inst2.getLeftOperand()) {
            return true;
        }

        return false;
    }

    private boolean match(UnaryOperator inst1, UnaryOperator inst2) {
        return inst1.getOpType() == inst2.getOpType() &&
                inst1.getOperand() == inst2.getOperand();
    }
}
