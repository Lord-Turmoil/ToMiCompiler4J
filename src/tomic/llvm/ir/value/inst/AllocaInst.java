/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.ir.value.inst;

import tomic.llvm.asm.IAsmWriter;
import tomic.llvm.ir.type.PointerType;
import tomic.llvm.ir.type.Type;
import tomic.llvm.ir.value.ValueTypes;

public class AllocaInst extends Instruction {
    private final Type allocatedType;
    private final int alignment;

    public AllocaInst(Type allocatedType) {
        this(allocatedType, 0);
    }

    public AllocaInst(Type allocatedType, int alignment) {
        super(ValueTypes.AllocaInstTy, PointerType.get(allocatedType));
        this.allocatedType = allocatedType;
        this.alignment = alignment;
    }

    public Type getAllocatedType() {
        return allocatedType;
    }

    /**
     * %1 = alloca i32[, align 4]
     */
    @Override
    public IAsmWriter printAsm(IAsmWriter out) {
        printName(out).pushNext('=').pushNext("alloca").pushSpace();
        return getAllocatedType().printAsm(out).pushNewLine();
    }
}
