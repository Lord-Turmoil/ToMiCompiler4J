/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.ir.type;

import tomic.llvm.asm.IAsmWriter;
import tomic.llvm.ir.LlvmContext;

public class Type {
    private final TypeID typeId;
    private final LlvmContext context;

    public Type(LlvmContext context, TypeID typeId) {
        this.context = context;
        this.typeId = typeId;
    }


    public static Type getVoidTy(LlvmContext context) {
        return context.getVoidTy();
    }

    public static Type getLabelTy(LlvmContext context) {
        return context.getLabelTy();
    }

    public boolean isVoidTy() {
        return typeId == TypeID.VoidTyID;
    }

    public boolean isLabelTy() {
        return typeId == TypeID.LabelTyID;
    }

    public boolean isIntegerTy() {
        return typeId == TypeID.IntegerTyID;
    }

    public boolean isFunctionTy() {
        return typeId == TypeID.FunctionTyID;
    }

    public boolean isArrayTy() {
        return typeId == TypeID.ArrayTyID;
    }

    public boolean isPointerTy() {
        return typeId == TypeID.PointerTyID;
    }

    public LlvmContext getContext() {
        return context;
    }

    public enum TypeID {
        // Primitive types
        VoidTyID,
        LabelTyID,

        // Derived types
        IntegerTyID,
        FunctionTyID,
        ArrayTyID,
        PointerTyID
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Type other) {
            return typeId == other.typeId;
        }
        return false;
    }

    public IAsmWriter printAsm(IAsmWriter out) {
        switch (typeId) {
            case VoidTyID -> out.push("void");
            case LabelTyID -> out.push("label");
            default -> throw new IllegalStateException("Should not reach here");
        }
        return out;
    }

    public int getBytes() {
        switch (typeId) {
            case VoidTyID -> throw new UnsupportedOperationException("Void type has no size");
            case LabelTyID -> throw new UnsupportedOperationException("Label type has no size");
            default -> throw new IllegalStateException("Should not reach here");
        }
    }
}
