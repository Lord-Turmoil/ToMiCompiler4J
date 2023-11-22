package tomic.llvm.ir.value.inst;

import tomic.llvm.ir.value.Value;
import tomic.llvm.ir.value.ValueTypes;

public class OutputInst extends UnaryInstruction {
    public OutputInst(Value operand) {
        super(ValueTypes.OutputInstTy, operand.getContext().getVoidTy(), operand);
    }

    public boolean isInteger() {
        return operand.getType().isIntegerTy();
    }
}
