package tomic.llvm.ir.value.inst;

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
}
