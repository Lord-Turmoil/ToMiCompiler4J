/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.ir.value.inst;

import tomic.llvm.ir.type.Type;
import tomic.llvm.ir.value.Value;
import tomic.llvm.ir.value.ValueTypes;

public class UnaryInstruction extends Instruction {
    protected Value operand;

    protected UnaryInstruction(ValueTypes valueType, Type type, Value operand) {
        super(valueType, type);
        this.operand = operand;
        addOperand(operand);
    }

    public Value getOperand() {
        return operand;
    }

    @Override
    public boolean replaceOperand(Value oldOperand, Value newOperand) {
        if (super.replaceOperand(oldOperand, newOperand)) {
        if (operand == oldOperand) {
            operand = newOperand;
        }
            return true;
        }
        return false;
    }
}
