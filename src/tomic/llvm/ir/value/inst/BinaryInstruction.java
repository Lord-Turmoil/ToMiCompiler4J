/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.ir.value.inst;

import tomic.llvm.ir.type.Type;
import tomic.llvm.ir.value.Value;
import tomic.llvm.ir.value.ValueTypes;

public class BinaryInstruction extends Instruction {
    protected Value leftOperand;
    protected Value rightOperand;

    public BinaryInstruction(ValueTypes valueType, Type type, Value lhs, Value rhs) {
        super(valueType, type);
        leftOperand = lhs;
        rightOperand = rhs;
        addOperand(leftOperand);
        addOperand(rightOperand);
    }

    public Value getLeftOperand() {
        return leftOperand;
    }

    public Value getRightOperand() {
        return rightOperand;
    }

    @Override
    public boolean replaceOperand(Value oldOperand, Value newOperand) {
        if (super.replaceOperand(oldOperand, newOperand)) {
            if (leftOperand == oldOperand) {
                leftOperand = newOperand;
            }
            if (rightOperand == oldOperand) {
                rightOperand = newOperand;
            }
            return true;
        }
        return false;
    }
}
