/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.ir.value.inst;

import tomic.llvm.asm.IAsmWriter;
import tomic.llvm.ir.LlvmContext;
import tomic.llvm.ir.value.Value;
import tomic.llvm.ir.value.ValueTypes;

public class ReturnInst extends Instruction {
    private Value value;

    public ReturnInst(Value value) {
        super(ValueTypes.ReturnInstTy, value.getContext().getVoidTy());
        this.value = value;

        /*
         * 2023/11/29 TS: FIX
         * Remember to add operand.
         */
        addOperand(value);
    }

    public ReturnInst(LlvmContext context) {
        super(ValueTypes.ReturnInstTy, context.getVoidTy());
        this.value = null;
    }

    public boolean hasValue() {
        return value != null;
    }

    public Value getValue() {
        return value;
    }

    @Override
    public boolean replaceOperand(Value oldOperand, Value newOperand) {
        if (super.replaceOperand(oldOperand, newOperand)) {
            if (value == oldOperand) {
                value = newOperand;
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public IAsmWriter printAsm(IAsmWriter out) {
        out.push("ret");
        if (value != null && !value.getType().isVoidTy()) {
            out.pushSpace();
            value.printUse(out);
        } else {
            out.pushNext("void");
        }
        return out.pushNewLine();
    }
}
