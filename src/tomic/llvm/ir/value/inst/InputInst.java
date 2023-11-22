package tomic.llvm.ir.value.inst;

import tomic.llvm.ir.LlvmContext;
import tomic.llvm.ir.value.ValueTypes;

public class InputInst extends Instruction {
    public InputInst(LlvmContext context) {
        super(ValueTypes.InputInstTy, context.getVoidTy());
    }
}
