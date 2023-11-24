/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.ir.value.inst;

import tomic.llvm.asm.IAsmWriter;
import tomic.llvm.ir.value.Value;
import tomic.llvm.ir.value.ValueTypes;

public class BinaryOperator extends BinaryInstruction {
    public enum BinaryOpTypes {
        Add,
        Sub,
        Mul,
        Div,
        Mod
    }

    private final BinaryOpTypes opType;

    public BinaryOperator(Value lhs, Value rhs, BinaryOpTypes opType) {
        super(ValueTypes.BinaryOperatorTy, lhs.getType(), lhs, rhs);
        this.opType = opType;
    }

    public BinaryOpTypes getOpType() {
        return opType;
    }

    @Override
    public IAsmWriter printAsm(IAsmWriter out) {
        String op = switch (opType) {
            case Add -> "add nsw";
            case Sub -> "sub nsw";
            case Mul -> "mul nsw";
            case Div -> "sdiv";
            case Mod -> "srem";
        };

        printName(out).pushNext('=').pushNext(op).pushSpace();

        getType().printAsm(out).pushSpace();

        getLeftOperand().printName(out).push(", ");
        getRightOperand().printName(out);

        return out.pushNewLine();
    }
}
