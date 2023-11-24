/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.ir.value.inst;

import tomic.llvm.asm.IAsmWriter;
import tomic.llvm.ir.type.ArrayType;
import tomic.llvm.ir.type.PointerType;
import tomic.llvm.ir.type.Type;
import tomic.llvm.ir.value.Value;
import tomic.llvm.ir.value.ValueTypes;

import java.util.ArrayList;
import java.util.List;

public class GetElementPtrInst extends Instruction {
    private Value address;
    private final ArrayList<Value> subscripts;

    private GetElementPtrInst(Type type, Value address, List<Value> subscripts) {
        super(ValueTypes.GetElementPtrInstTy, type);
        this.address = address;
        this.subscripts = new ArrayList<>(subscripts);
        addOperand(address);
        addOperands(subscripts);
    }

    /**
     * %3 = getelementptr [5 x [7 x i32]], [5 x [7 x i32]]* @a, i32 0, i32 3, i32 4
     * [5 x [7 x i32]]* 0, 3, 4
     */
    public static GetElementPtrInst create(Value address, List<Value> subscripts) {
        Type type = address.getPointerType().getElementType();
        for (int i = 1; i < subscripts.size(); i++) {
            if (!(type instanceof ArrayType)) {
                throw new IllegalArgumentException("GetElementPtrInst: type is not array type");
            }
            type = ((ArrayType) type).getElementType();
        }
        return new GetElementPtrInst(PointerType.get(type), address, subscripts);
    }

    public Value getAddress() {
        return address;
    }

    public ArrayList<Value> getSubscripts() {
        return subscripts;
    }

    /**
     * %3 = getelementptr [5 x [7 x i32]], [5 x [7 x i32]]* @a, i32 0, i32 3, i32 4
     */
    @Override
    public IAsmWriter printAsm(IAsmWriter out) {
        printName(out).pushNext('=').pushNext("getelementptr inbounds").pushSpace();
        address.getPointerType().getElementType().printAsm(out).push(',').pushSpace();
        address.printUse(out);
        for (Value subscript : subscripts) {
            out.push(',').pushSpace();
            subscript.printUse(out);
        }
        return out.pushNewLine();
    }
}
