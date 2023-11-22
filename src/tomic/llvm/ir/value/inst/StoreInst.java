package tomic.llvm.ir.value.inst;

import tomic.llvm.ir.value.Value;
import tomic.llvm.ir.value.ValueTypes;

public class StoreInst extends BinaryInstruction{
    public StoreInst(Value value, Value address) {
        super(ValueTypes.StoreInstTy, value.getContext().getVoidTy(), value, address);
    }
}
