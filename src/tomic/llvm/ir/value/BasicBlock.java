/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.ir.value;

import tomic.llvm.asm.IAsmWriter;
import tomic.llvm.ir.LlvmContext;
import tomic.llvm.ir.value.inst.BranchInst;
import tomic.llvm.ir.value.inst.Instruction;
import tomic.llvm.ir.value.inst.JumpInst;
import tomic.llvm.ir.value.inst.ReturnInst;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class BasicBlock extends Value {
    private Function parent;
    private final LinkedList<Instruction> instructions;

    public BasicBlock(LlvmContext context) {
        super(ValueTypes.BasicBlockTy, context.getLabelTy());
        this.parent = null;
        instructions = new LinkedList<>();
    }

    public BasicBlock(Function parent) {
        super(ValueTypes.BasicBlockTy, parent.getContext().getLabelTy());
        this.parent = parent;
        instructions = new LinkedList<>();
    }

    public void setParent(Function function) {
        this.parent = function;
    }

    public Function getParent() {
        return parent;
    }

    public void insertInstruction(Instruction instruction) {
        instruction.setParent(this);
        instructions.add(instruction);
    }

    public void insertInstructionFirst(Instruction instruction) {
        instruction.setParent(this);
        instructions.addFirst(instruction);
    }

    public void insertInstructionBefore(Instruction instruction, Instruction before) {
        instruction.setParent(this);
        instructions.add(instructions.indexOf(before), instruction);
    }

    public void insertInstructionAfter(Instruction instruction, Instruction after) {
        instruction.setParent(this);
        instructions.add(instructions.indexOf(after) + 1, instruction);
    }

    public void removeInstruction(Instruction instruction) {
        instruction.getOperands().forEach(operand -> operand.removeUser(instruction));
        instructions.remove(instruction);
    }

    public LinkedList<Instruction> getInstructions() {
        return instructions;
    }

    public List<BasicBlock> getPredecessors() {
        ArrayList<BasicBlock> preds = new ArrayList<>();
        for (var inst : getUsers()) {
            if (inst instanceof Instruction user && user.getParent() != this) {
                preds.add(user.getParent());
            }
        }
        return preds;
    }

    public boolean isEmpty() {
        return instructions.isEmpty();
    }

    @Override
    public IAsmWriter printAsm(IAsmWriter out) {
        var func = getParent();

        if (this != func.getBasicBlocks().getFirst()) {
            String slot = String.valueOf(getParent().slot(this));
            out.pushNewLine().push(slot).push(':');

            if (!getUsers().isEmpty()) {
                out.pushSpaces(50 - slot.length() - 1);
                out.commentBegin();
                out.push("preds = ");
                boolean first = true;
                ArrayList<BasicBlock> preds = getUsers().stream()
                        .filter(user -> user instanceof Instruction inst && inst.getParent() != this)
                        .collect(ArrayList::new, (list, user) -> list.add(((Instruction) user).getParent()), ArrayList::addAll);
                Collections.reverse(preds);
                for (var pred : preds) {
                    if (!first) {
                        out.push(", ");
                    }
                    pred.printName(out);
                    first = false;
                }
            }
            out.commentEnd();
        }

        getInstructions().forEach(inst -> inst.printAsm(out.pushSpaces(4)));

        return out;
    }

    public int getIndex() {
        return getParent().getBasicBlocks().indexOf(this);
    }

    @Override
    public IAsmWriter printName(IAsmWriter out) {
        return out.push('%').push(String.valueOf(getParent().slot(this)));
    }

    @Override
    public IAsmWriter printUse(IAsmWriter out) {
        getType().printAsm(out).pushSpace();
        return printName(out);
    }

    @Override
    public void refactor() {
        Instruction inst = null;
        for (var i : getInstructions()) {
            if (i instanceof JumpInst jumpInst) {
                inst = jumpInst;
                break;
            } else if (i instanceof BranchInst branchInst) {
                inst = branchInst;
                break;
            } else if (i instanceof ReturnInst returnInst) {
                inst = returnInst;
                break;
            }
        }

        /*
         * F**k you, IntelliJ IDEA.
         */
        if (inst != null) {
            var unreachable = new LinkedList<>(instructions.subList(instructions.indexOf(inst) + 1, instructions.size()));
            unreachable.forEach(this::removeInstruction);
        }
    }
}
