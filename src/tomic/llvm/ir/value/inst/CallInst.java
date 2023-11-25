/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.ir.value.inst;

import tomic.llvm.asm.IAsmWriter;
import tomic.llvm.ir.value.Function;
import tomic.llvm.ir.value.Value;
import tomic.llvm.ir.value.ValueTypes;

import java.util.ArrayList;
import java.util.List;

public class CallInst extends Instruction {
    private Function function;
    private ArrayList<Value> parameters;

    public CallInst(Function function) {
        this(function, new ArrayList<>());
    }

    public CallInst(Function function, List<Value> parameters) {
        super(ValueTypes.CallInstTy, function.getReturnType());
        this.function = function;
        this.parameters = new ArrayList<>(parameters);
    }

    public ArrayList<Value> getParameters() {
        return parameters;
    }

    public int getParamCount() {
        return parameters.size();
    }

    public Value getParam(int index) {
        return parameters.get(index);
    }

    public Function getFunction() {
        return function;
    }

    @Override
    public IAsmWriter printAsm(IAsmWriter out) {
        if (!getType().isVoidTy()) {
            printName(out).pushNext('=').pushSpace();
        }

        out.push("call").pushSpace();
        getFunction().getReturnType().printAsm(out).pushSpace();

        getFunction().printName(out).push('(');
        for (var param : parameters) {
            if (param != parameters.get(0)) {
                out.push(", ");
            }
            param.printUse(out);
        }
        return out.push(')').pushNewLine();
    }
}
