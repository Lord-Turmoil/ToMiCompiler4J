/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.ir.value.inst;

import tomic.llvm.asm.IAsmWriter;
import tomic.llvm.ir.value.Value;
import tomic.llvm.ir.value.ValueTypes;

public class UnaryOperator extends UnaryInstruction {
    public enum UnaryOpTypes {
        Not,
        Neg,
        Pos
    }

    private final UnaryOpTypes opType;

    public UnaryOperator(Value operand, UnaryOpTypes opType) {
        super(ValueTypes.UnaryOperatorTy, operand.getType(), operand);
        this.opType = opType;
    }

    public UnaryOpTypes getOpType() {
        return opType;
    }


    @Override
    public IAsmWriter printAsm(IAsmWriter out) {
        if (opType == UnaryOpTypes.Not) {
            return printNot(out);
        }

        String op = switch (opType) {
            case Neg -> "sub nsw";
            case Pos -> "add nsw";
            default -> throw new IllegalStateException("Unexpected value: " + opType);
        };

        printName(out).pushNext('=').pushNext(op).pushSpace();

        getType().printAsm(out);
        out.pushNext('0').push(',').pushSpace();
        getOperand().printName(out);

        return out.pushNewLine();
    }

    /**
     * %5 = xor i1 %4, 1
     */
    private IAsmWriter printNot(IAsmWriter out) {
        if (!getOperand().getIntegerType().isBoolean()) {
            throw new IllegalStateException("Not operator can only be applied to boolean types");
        }

        printName(out).pushNext('=').pushNext("xor").pushSpace();
        getOperand().getType().printAsm(out).pushSpace();
        getOperand().printName(out).push(',').pushSpace();
        return out.push('1').pushNewLine();
    }
}
