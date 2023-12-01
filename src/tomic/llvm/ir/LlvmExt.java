/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.ir;

import tomic.llvm.ir.type.*;
import tomic.parser.table.ConstantEntry;
import tomic.parser.table.FunctionEntry;
import tomic.parser.table.VariableEntry;

import java.util.ArrayList;

public class LlvmExt {
    private LlvmExt() {}

    public static Type getEntryType(LlvmContext context, VariableEntry entry) {
        Type type = IntegerType.get(context, 32);
        for (int i = entry.getDimension() - 1; i >= 0; i--) {
            type = ArrayType.get(type, entry.getSize(i));
        }
        return type;
    }

    public static Type getEntryType(LlvmContext context, ConstantEntry entry) {
        Type type = IntegerType.get(context, 32);
        for (int i = entry.getDimension() - 1; i >= 0; i--) {
            type = ArrayType.get(type, entry.getSize(i));
        }
        return type;
    }

    public static Type getEntryType(LlvmContext context, FunctionEntry entry) {
        Type returnType = switch (entry.getType()) {
            case INT -> IntegerType.get(context, 32);
            case VOID -> Type.getVoidTy(context);
            default -> throw new IllegalStateException("Unsupported type: " + entry.getType());
        };

        ArrayList<Type> paramTypes = new ArrayList<>();
        for (var arg : entry.getParams()) {
            paramTypes.add(getEntryType(context, arg));
        }

        return FunctionType.get(returnType, paramTypes);
    }

    public static Type getEntryType(LlvmContext context, FunctionEntry.ParamEntry entry) {
        if (entry.getDimension() == 0) {
            return IntegerType.get(context, 32);
        }

        var sizes = entry.getSizes();
        Type type = IntegerType.get(context, 32);
        for (int i = sizes.size() - 1; i >= 1; i--) {
            type = ArrayType.get(type, sizes.get(i));
        }

        return PointerType.get(type);
    }
}
