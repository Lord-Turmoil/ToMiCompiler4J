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

        /*
         * 2023/11/29 TS: FIX
         * Remember to add operands to the instruction...
         */
        addOperands(parameters);
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

    public List<Value> getParams() {
        return parameters;
    }

    public Function getFunction() {
        return function;
    }

    @Override
    public boolean replaceOperand(Value oldOperand, Value newOperand) {
        if (!super.replaceOperand(oldOperand, newOperand)) {
            return false;
        }

        if (function == oldOperand) {
            function = (Function) newOperand;
        } else {
            // WARNING! Must preserve the order! And find all the occurrences!
            int index = parameters.indexOf(oldOperand);
            while (index != -1) {
                parameters.remove(index);
                parameters.add(index, newOperand);
                index = parameters.indexOf(oldOperand);
            }
        }

        return true;
    }

    @Override
    public IAsmWriter printAsm(IAsmWriter out) {
        if (!getType().isVoidTy()) {
            printName(out).pushNext('=').pushSpace();
        }

        out.push("call").pushSpace();
        getFunction().getReturnType().printAsm(out).pushSpace();

        getFunction().printName(out).push('(');
        boolean first = true;
        for (var param : parameters) {
            if (!first) {
                out.push(", ");
            }
            param.printUse(out);
            first = false;
        }
        return out.push(')').pushNewLine();
    }
}
