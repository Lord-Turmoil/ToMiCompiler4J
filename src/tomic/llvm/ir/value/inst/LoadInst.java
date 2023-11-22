package tomic.llvm.ir.value.inst;

import tomic.llvm.ir.type.Type;
import tomic.llvm.ir.value.Value;
import tomic.llvm.ir.value.ValueTypes;

public class LoadInst extends UnaryInstruction{
    private final Value address;

    public LoadInst(Type type, Value address) {
        super(ValueTypes.LoadInstTy, type, address);
        this.address = address;
    }

    public Value getAddress() {
        return address;
    }
}
