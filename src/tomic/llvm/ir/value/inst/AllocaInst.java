package tomic.llvm.ir.value.inst;

import tomic.llvm.ir.type.PointerType;
import tomic.llvm.ir.type.Type;
import tomic.llvm.ir.value.ValueTypes;

public class AllocaInst extends Instruction {
    private final Type allocatedType;
    private final int alignment;

    public AllocaInst(Type allocatedType) {
        this(allocatedType, 0);
    }

    public AllocaInst(Type allocatedType, int alignment) {
        super(ValueTypes.AllocaInstTy, PointerType.get(allocatedType));
        this.allocatedType = allocatedType;
        this.alignment = alignment;
    }

    public Type getAllocatedType() {
        return allocatedType;
    }
}
