/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.ir.value.inst;

import tomic.llvm.asm.IAsmWriter;
import tomic.llvm.ir.type.IntegerType;
import tomic.llvm.ir.value.ConstantData;
import tomic.llvm.ir.value.Value;
import tomic.llvm.ir.value.ValueTypes;

public class CompInst extends BinaryInstruction {
    public enum CompOpTypes {
        Eq,     // ==
        Ne,     // !=
        Slt,    // <
        Sle,    // <=
        Sgt,    // >
        Sge,    // >=
    }

    private final CompOpTypes opType;

    public CompInst(Value operand1, Value operand2, CompOpTypes opType) {
        super(ValueTypes.CompareInstTy, IntegerType.get(operand1.getContext(), 1), operand1, operand2);
        this.opType = opType;
        if (operand1.getType() != operand2.getType()) {
            throw new IllegalArgumentException("Operands must have the same type");
        }
    }

    // Compare with zero.
    public CompInst(Value operand, CompOpTypes opType) {
        this(operand, new ConstantData(operand.getType(), 0), opType);
    }

    public CompOpTypes getOpType() {
        return opType;
    }

    // %5 = icmp eq i1 %3, 0
    @Override
    public IAsmWriter printAsm(IAsmWriter out) {
        printName(out).pushNext('=').pushNext("icmp").pushSpace();
        out.push(getOpType().toString().toLowerCase()).pushSpace();
        leftOperand.getType().printAsm(out).pushSpace();
        leftOperand.printName(out).push(',').pushSpace();
        return rightOperand.printName(out).pushNewLine();
    }
}
