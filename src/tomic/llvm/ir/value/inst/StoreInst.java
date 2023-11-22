package tomic.llvm.ir.value.inst;

import tomic.llvm.asm.IAsmWriter;
import tomic.llvm.ir.value.Value;
import tomic.llvm.ir.value.ValueTypes;

public class StoreInst extends BinaryInstruction{
    public StoreInst(Value value, Value address) {
        super(ValueTypes.StoreInstTy, value.getContext().getVoidTy(), value, address);
    }

    /**
     * store i32 1, i32* %3[, align 4]
     */
    @Override
    public IAsmWriter printAsm(IAsmWriter out) {
        out.push("store").pushSpace();
        getLeftOperand().printUse(out);
        out.push(',').pushSpace();
        getRightOperand().printUse(out);
        return out.pushNewLine();
    }
}
