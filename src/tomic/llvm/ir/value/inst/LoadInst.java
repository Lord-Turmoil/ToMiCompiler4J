/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.ir.value.inst;

import tomic.llvm.asm.IAsmWriter;
import tomic.llvm.ir.type.PointerType;
import tomic.llvm.ir.value.Value;
import tomic.llvm.ir.value.ValueTypes;

public class LoadInst extends UnaryInstruction{
    private final Value address;

    public LoadInst(Value address) {
        super(ValueTypes.LoadInstTy, ((PointerType) address.getType()).getElementType(), address);
        this.address = address;
    }

    public Value getAddress() {
        return address;
    }

    /**
     * %3 = load i32, i32* %1, align 4
     */
    @Override
    public IAsmWriter printAsm(IAsmWriter out) {
        printName(out).pushNext('=').pushNext("load").pushSpace();
        getType().printAsm(out).push(',').pushSpace();
        return getAddress().printUse(out).pushNewLine();
    }
}
