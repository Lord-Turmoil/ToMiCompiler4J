/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.ir;

import tomic.llvm.ir.type.*;
import tomic.llvm.ir.value.GlobalString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LlvmContext {
    private final Type voidTy = new Type(this, Type.TypeID.VoidTyID);
    private final Type labelTy = new Type(this, Type.TypeID.LabelTyID);
    private final IntegerType int1Ty = new IntegerType(this, 1);
    private final IntegerType int8Ty = new IntegerType(this, 8);
    private final IntegerType int16Ty = new IntegerType(this, 16);
    private final IntegerType int32Ty = new IntegerType(this, 32);
    private final IntegerType int64Ty = new IntegerType(this, 64);

    private final ArrayList<ArrayType> arrayTypes = new ArrayList<>();
    private final ArrayList<FunctionType> functionTypes = new ArrayList<>();
    private final ArrayList<PointerType> pointerTypes = new ArrayList<>();
    private final Map<String, GlobalString> globalStrings = new HashMap<>();

    public Type getVoidTy() {
        return voidTy;
    }

    public Type getLabelTy() {
        return labelTy;
    }

    public IntegerType getInt1Ty() {
        return int1Ty;
    }

    public IntegerType getInt8Ty() {
        return int8Ty;
    }

    public IntegerType getInt16Ty() {
        return int16Ty;
    }

    public IntegerType getInt32Ty() {
        return int32Ty;
    }

    public IntegerType getInt64Ty() {
        return int64Ty;
    }


    public ArrayType getArrayType(Type elementType, int elementCount) {
        for (ArrayType type : arrayTypes) {
            if (type.match(elementType, elementCount)) {
                return type;
            }
        }
        var type = new ArrayType(elementType, elementCount);
        arrayTypes.add(type);
        return type;
    }

    public PointerType getPointerType(Type elementType) {
        for (PointerType type : pointerTypes) {
            if (type.match(elementType)) {
                return type;
            }
        }
        var type = new PointerType(elementType);
        pointerTypes.add(type);
        return type;
    }

    public FunctionType getFunctionType(Type returnType, List<Type> paramTypes) {
        for (FunctionType type : functionTypes) {
            if (type.match(returnType, paramTypes)) {
                return type;
            }
        }
        var type = new FunctionType(returnType, paramTypes);
        functionTypes.add(type);
        return type;
    }

    public FunctionType getFunctionType(Type returnType) {
        return getFunctionType(returnType, new ArrayList<>());
    }

    private static int idx = -1;
    public GlobalString getGlobalString(String value) {
        if (globalStrings.containsKey(value)) {
            return globalStrings.get(value);
        }

        String name;
        if (++idx > 0) {
            name = ".str." + idx;
        } else {
            name = ".str";
        }

        int size = value.length() + 1;
        var type = PointerType.get(ArrayType.get(IntegerType.get(this, 8), size));

        var globalString = new GlobalString(type, value, name);
        globalStrings.put(value, globalString);

        return globalString;
    }
}
