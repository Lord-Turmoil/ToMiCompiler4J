package tomic.llvm.ir.value.inst;

import tomic.llvm.ir.type.PointerType;
import tomic.llvm.ir.value.Value;
import tomic.llvm.ir.value.ValueTypes;

public class LoadInst extends UnaryInstruction{
    private final Value address;

    public LoadInst(Value address) {
        super(ValueTypes.LoadInstTy, ((PointerType) address.getType()).getElementType(), address);
        this.address = address;
    }

    public Value getAddress() {
        return address;
    }
}
