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

// Just BranchInst without condition.
public class JumpInst extends Instruction {
    private BasicBlock target;
    private final boolean isReturn;

    public JumpInst(BasicBlock target) {
        this(target, false);
        addOperand(target);
    }

    public JumpInst(BasicBlock target, boolean isReturn) {
        super(ValueTypes.JumpInstTy, target.getContext().getVoidTy());
        this.target = target;
        this.isReturn = isReturn;
        addOperand(target);
    }

    public BasicBlock getTarget() {
        return target;
    }

    public void setTarget(BasicBlock target) {
        removeOperand(this.target);
        this.target = target;
        addOperand(target);
    }


    public boolean isReturn() {
        return isReturn;
    }

    @Override
    public void replaceOperand(Value oldOperand, Value newOperand) {
        super.replaceOperand(oldOperand, newOperand);
        if (target == oldOperand) {
            target = (BasicBlock) newOperand;
        }
    }

    /**
     * br label %25
     */
    @Override
    public IAsmWriter printAsm(IAsmWriter out) {
        out.push("br").pushSpace();
        return target.printUse(out).pushNewLine();
    }
}
