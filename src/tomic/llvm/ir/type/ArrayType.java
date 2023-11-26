/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.ir.type;

import tomic.llvm.asm.IAsmWriter;

public class ArrayType extends Type {
    private final Type elementType;
    private final int elementCount;

    public ArrayType(Type elementType, int elementCount) {
        super(elementType.getContext(), TypeID.ArrayTyID);
        this.elementType = elementType;
        this.elementCount = elementCount;
    }

    public static ArrayType get(Type elementType, int elementCount) {
        return elementType.getContext().getArrayType(elementType, elementCount);
    }

    public Type getElementType() {
        return elementType;
    }

    public int getElementCount() {
        return elementCount;
    }

    public int getSize() {
        if (elementType instanceof ArrayType array) {
            return array.getSize() * elementCount;
        } else {
            return elementCount;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ArrayType other) {
            return elementType.equals(other.elementType) && elementCount == other.elementCount;
        }
        return false;
    }

    public boolean match(Type elementType, int elementCount) {
        return this.elementType.equals(elementType) && this.elementCount == elementCount;
    }

    @Override
    public IAsmWriter printAsm(IAsmWriter out) {
        out.push('[').push(String.valueOf(elementCount)).push(" x ");
        return elementType.printAsm(out).push(']');
    }

    @Override
    public int getBytes() {
        return elementType.getBytes() * elementCount;
    }
}
