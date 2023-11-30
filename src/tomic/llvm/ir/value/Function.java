/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.ir.value;

import tomic.llvm.asm.IAsmWriter;
import tomic.llvm.ir.SlotTracker;
import tomic.llvm.ir.type.FunctionType;
import tomic.llvm.ir.type.Type;
import tomic.llvm.ir.value.inst.BranchInst;
import tomic.llvm.ir.value.inst.JumpInst;
import tomic.llvm.ir.value.inst.ReturnInst;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Function extends GlobalValue {
    private final ArrayList<Argument> arguments;
    private final LinkedList<BasicBlock> basicBlocks;
    private BasicBlock returnBlock;
    private final SlotTracker slotTracker = new SlotTracker();

    public static Function newInstance(Type returnType, String name, List<Argument> arguments) {
        ArrayList<Type> argTypes = new ArrayList<>();
        for (var arg : arguments) {
            argTypes.add(arg.getType());
        }

        return new Function(FunctionType.get(returnType, argTypes), name, arguments);
    }

    public static Function newInstance(Type returnType, String name) {
        return new Function(FunctionType.get(returnType), name);
    }

    private Function(Type type, String name, List<Argument> arguments) {
        super(ValueTypes.FunctionTy, type, name);
        this.arguments = new ArrayList<>(arguments);
        this.basicBlocks = new LinkedList<>();
        this.arguments.forEach(arg -> arg.setParent(this));
        returnBlock = new BasicBlock(this);

        if (getReturnType().isVoidTy()) {
            returnBlock.insertInstruction(new ReturnInst(getContext()));
        }
    }

    private Function(Type type, String name) {
        this(type, name, new ArrayList<>());
    }

    public void trace() {
        slotTracker.trace(this);
    }

    public BasicBlock newBasicBlock() {
        var block = new BasicBlock(this);
        insertBasicBlock(block);
        return block;
    }

    public void insertBasicBlock(BasicBlock basicBlock) {
        if (basicBlock.getParent() != null) {
            basicBlock.getParent().removeBasicBlock(basicBlock);
        }
        basicBlock.setParent(this);
        basicBlocks.add(basicBlock);
    }

    public void removeBasicBlock(BasicBlock basicBlock) {
        basicBlocks.remove(basicBlock);
    }

    public LinkedList<BasicBlock> getBasicBlocks() {
        return basicBlocks;
    }

    public int getArgumentCount() {
        return arguments.size();
    }

    public Argument getArgument(int index) {
        return arguments.get(index);
    }

    public void addArgument(Argument argument) {
        arguments.add(argument);
    }

    public ArrayList<Argument> getArguments() {
        return arguments;
    }

    public Type getReturnType() {
        return ((FunctionType) getType()).getReturnType();
    }

    // A utility method to get the slot of a value in this function.
    public int slot(Value value) {
        return slotTracker.slot(value);
    }

    @Override
    public IAsmWriter printAsm(IAsmWriter out) {
        var type = (FunctionType) getType();

        if (type.getReturnType().isVoidTy()) {
            var block = getBasicBlocks().getLast();
            if (block.getInstructions().isEmpty()) {
                block.insertInstruction(new ReturnInst(getContext()));
            } else {
                if (!(block.getInstructions().getLast() instanceof ReturnInst)) {
                    block.insertInstruction(new ReturnInst(getContext()));
                }
            }
        }

        out.pushNewLine();

        out.push("define").pushNext("dso_local").pushSpace();
        type.getReturnType().printAsm(out).pushSpace();

        printName(out).push('(');
        boolean first = true;
        for (var arg : getArguments()) {
            if (!first) {
                out.push(", ");
            }
            arg.printAsm(out);
            first = false;
        }
        out.push(')');

        out.pushNext('{').pushNewLine();

        getBasicBlocks().forEach(block -> block.printAsm(out));

        return out.push('}').pushNewLine();
    }

    private boolean refactored = false;

    @Override
    public void refactor() {
        // Prevent from refactoring twice.
        if (refactored) {
            return;
        }
        refactored = true;

        // Refactor basic blocks
        basicBlocks.forEach(BasicBlock::refactor);

        // Add return block
        insertBasicBlock(returnBlock);

        // Connect neighboring basic blocks
        for (int i = 0; i < basicBlocks.size() - 1; i++) {
            var block = basicBlocks.get(i);
            var next = basicBlocks.get(i + 1);
            if (needConnect(block)) {
                block.insertInstruction(new JumpInst(next, next == returnBlock));
            }
        }

        boolean merged = false;
        var preds = returnBlock.getPredecessors();
        if (preds.size() == 1) {
            var pred = preds.get(0);
            var prevBlock = basicBlocks.get(basicBlocks.size() - 2);
            if (pred == prevBlock) {
                if (pred.getInstructions().getLast() instanceof JumpInst jmp && jmp.isReturn()) {
                    pred.removeInstruction(jmp);
                    returnBlock.getInstructions().forEach(pred::insertInstruction);
                    merged = true;
                }
            }
        } else if (preds.isEmpty()) {
            merged = true;
        }

        if (merged) {
            removeBasicBlock(returnBlock);
        }
    }

    private boolean needConnect(BasicBlock block) {
        if (block.isEmpty()) {
            return true;
        }

        var inst = block.getInstructions().getLast();
        if (inst instanceof JumpInst || inst instanceof BranchInst) {
            return false;
        }

        return !(inst instanceof ReturnInst);
    }
}
