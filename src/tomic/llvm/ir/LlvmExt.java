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
        return switch (entry.getDimension()) {
            case 0 -> IntegerType.get(context, 32);
            case 1 -> ArrayType.get(IntegerType.get(context, 32), entry.getSize(0));
            case 2 -> ArrayType.get(ArrayType.get(IntegerType.get(context, 32), entry.getSize(1)), entry.getSize(0));
            default -> throw new IllegalStateException("Unsupported dimension: " + entry.getDimension());
        };
    }

    public static Type getEntryType(LlvmContext context, ConstantEntry entry) {
        return switch (entry.getDimension()) {
            case 0 -> IntegerType.get(context, 32);
            case 1 -> ArrayType.get(IntegerType.get(context, 32), entry.getSize(0));
            case 2 -> ArrayType.get(ArrayType.get(IntegerType.get(context, 32), entry.getSize(1)), entry.getSize(0));
            default -> throw new IllegalStateException("Unsupported dimension: " + entry.getDimension());
        };
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
        return switch (entry.dimension) {
            case 0 -> IntegerType.get(context, 32);
            case 1 -> PointerType.get(IntegerType.get(context, 32));
            case 2 -> PointerType.get(ArrayType.get(IntegerType.get(context, 32), entry.sizes[1]));
            default -> throw new IllegalStateException("Unsupported dimension: " + entry.dimension);
        };
    }
}
