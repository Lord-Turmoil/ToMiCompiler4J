/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.ir.value.inst;

import tomic.llvm.asm.IAsmWriter;
import tomic.llvm.ir.value.BasicBlock;
import tomic.llvm.ir.value.ValueTypes;

// Just BranchInst without condition.
public class JumpInst extends Instruction {
    private BasicBlock target;
    private final boolean isReturn;

    public JumpInst(BasicBlock target) {
        this(target, false);
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


    public boolean isReturn() {
        return isReturn;
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
