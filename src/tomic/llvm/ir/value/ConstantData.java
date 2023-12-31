/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.ir.value;

import tomic.llvm.asm.IAsmWriter;
import tomic.llvm.ir.LlvmContext;
import tomic.llvm.ir.type.ArrayType;
import tomic.llvm.ir.type.Type;

import java.util.ArrayList;
import java.util.List;

public class ConstantData extends Constant {
    private boolean allZero;
    private int value;
    private final ArrayList<ConstantData> values;

    public ConstantData(Type type, int value) {
        super(ValueTypes.ConstantDataTy, type);
        this.value = value;
        this.allZero = value == 0;
        values = null;
    }

    public ConstantData(LlvmContext context, boolean value) {
        this(context.getInt1Ty(), value ? 1 : 0);
    }

    public ConstantData(List<ConstantData> values) {
        super(ValueTypes.ConstantDataTy, ArrayType.get(values.get(0).getType(), values.size()));
        this.values = new ArrayList<>(values);
        this.allZero = true;
        for (var value : values) {
            if (!value.allZero) {
                allZero = false;
                break;
            }
        }
    }

    public boolean isArray() {
        return values != null;
    }

    public int getValue() {
        return value;
    }

    public boolean isAllZero() {
        return allZero;
    }

    public List<ConstantData> getValues() {
        return values;
    }

    @Override
    public IAsmWriter printAsm(IAsmWriter out) {
        getType().printAsm(out);
        if (isArray()) {
            if (allZero) {
                out.pushNext("zeroinitializer");
            } else {
                out.pushNext('[');
                boolean first = true;
                for (var value : values) {
                    if (!first) {
                        out.push(", ");
                    }
                    value.printAsm(out);
                    first = false;
                }
                out.push(']');
            }
        } else {
            out.pushNext(String.valueOf(value));
        }
        return out;
    }

    @Override
    public IAsmWriter printName(IAsmWriter out) {
        if (isArray()) {
            out.push('[');
            boolean first = true;
            for (var value : values) {
                if (!first) {
                    out.push(", ");
                }
                value.printAsm(out);
                first = false;
            }
            out.push(']');
        } else {
            out.push(String.valueOf(value));
        }
        return out;
    }

    @Override
    public IAsmWriter printUse(IAsmWriter out) {
        getType().printAsm(out).pushSpace();
        return printName(out);
    }
}
