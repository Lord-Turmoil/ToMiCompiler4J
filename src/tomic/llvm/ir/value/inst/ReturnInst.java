package tomic.llvm.ir.value.inst;

import tomic.llvm.ir.LlvmContext;
import tomic.llvm.ir.value.Value;
import tomic.llvm.ir.value.ValueTypes;

public class ReturnInst extends Instruction {
    private Value value;

    public ReturnInst(Value value) {
        super(ValueTypes.ReturnInstTy, value.getContext().getVoidTy());
        this.value = value;
    }

    public ReturnInst(LlvmContext context) {
        super(ValueTypes.ReturnInstTy, context.getVoidTy());
        this.value = null;
    }

    public boolean hasValue() {
        return value != null;
    }
}
