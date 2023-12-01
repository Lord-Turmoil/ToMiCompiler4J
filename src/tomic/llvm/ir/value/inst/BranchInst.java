/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.ir.value.inst;

import tomic.llvm.asm.IAsmWriter;
import tomic.llvm.ir.value.BasicBlock;
import tomic.llvm.ir.value.Value;
import tomic.llvm.ir.value.ValueTypes;

public class BranchInst extends Instruction {
    private Value condition;
    private BasicBlock trueBlock;
    private BasicBlock falseBlock;

    public BranchInst(Value condition, BasicBlock trueBlock, BasicBlock falseBlock) {
        super(ValueTypes.BranchInstTy, condition.getContext().getVoidTy());
        this.condition = condition;
        this.trueBlock = trueBlock;
        this.falseBlock = falseBlock;

        addOperand(condition);
        addOperand(trueBlock);
        addOperand(falseBlock);
    }

    public Value getCondition() {
        return condition;
    }

    public BasicBlock getTrueBlock() {
        return trueBlock;
    }

    public void setTrueBlock(BasicBlock trueBlock) {
        removeOperand(this.trueBlock);
        this.trueBlock = trueBlock;
        addOperand(trueBlock);
    }

    public BasicBlock getFalseBlock() {
        return falseBlock;
    }

    public void setFalseBlock(BasicBlock falseBlock) {
        removeOperand(this.falseBlock);
        this.falseBlock = falseBlock;
        addOperand(falseBlock);
    }

    @Override
    public boolean replaceOperand(Value oldOperand, Value newOperand) {
        if (super.replaceOperand(oldOperand, newOperand)) {
            if (condition == oldOperand) {
                condition = newOperand;
            }
            if (trueBlock == oldOperand) {
                trueBlock = (BasicBlock) newOperand;
            }
            if (falseBlock == oldOperand) {
                falseBlock = (BasicBlock) newOperand;
            }
            return true;
        }
        return false;
    }

    /**
     * br i1 %8, label %19, label %9
     */
    @Override
    public IAsmWriter printAsm(IAsmWriter out) {
        out.push("br").pushSpace();
        condition.printUse(out).push(',').pushSpace();
        trueBlock.printUse(out).push(',').pushSpace();
        return falseBlock.printUse(out).pushNewLine();
    }
}
