package tomic.llvm.ir.value.inst;

import tomic.llvm.ir.type.Type;
import tomic.llvm.ir.value.Value;
import tomic.llvm.ir.value.ValueTypes;

public class UnaryInstruction extends Instruction {
    protected Value operand;

    protected UnaryInstruction(ValueTypes valueType, Type type, Value operand) {
        super(valueType, type);
        this.operand = operand;
        this.addOperand(operand);
    }

    public Value getOperand() {
        return operand;
    }
}
