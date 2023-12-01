/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.ir.type;

import tomic.llvm.asm.IAsmWriter;
import tomic.llvm.ir.LlvmContext;

public class IntegerType extends Type {
    private final int bitWidth;

    public IntegerType(LlvmContext context, int bitWidth) {
        super(context, TypeID.IntegerTyID);
        this.bitWidth = bitWidth;
    }

    public static IntegerType get(LlvmContext context, int bitWidth) {
        return switch (bitWidth) {
            case 1 -> context.getInt1Ty();
            case 8 -> context.getInt8Ty();
            case 16 -> context.getInt16Ty();
            case 32 -> context.getInt32Ty();
            case 64 -> context.getInt64Ty();
            default -> throw new IllegalArgumentException("Invalid bit width");
        };
    }

    public int getBitWidth() {
        return bitWidth;
    }

    public boolean isBoolean() {
        return bitWidth == 1;
    }

    public boolean isInteger() {
        return bitWidth > 1;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IntegerType other) {
            return bitWidth == other.bitWidth;
        }
        return false;
    }

    @Override
    public IAsmWriter printAsm(IAsmWriter out) {
        return out.push('i').push(String.valueOf(bitWidth));
    }

    @Override
    public int getBytes() {
        /*
         * 2023/12/1 TS: FIX
         * Warning! It cannot be 0!
         */
        return Math.max(bitWidth / 8, 1);
    }
}
