package tomic.llvm.ir.type;

import tomic.llvm.asm.IAsmWriter;

public class PointerType extends Type {
    private final Type elementType;

    public PointerType(Type elementType) {
        super(elementType.getContext(), TypeID.PointerTyID);
        this.elementType = elementType;
    }

    public static PointerType get(Type elementType) {
        return null;
    }

    public Type getElementType() {
        return elementType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PointerType other) {
            return elementType.equals(other.elementType);
        }
        return false;
    }

    public boolean match(Type elementType) {
        return this.elementType.equals(elementType);
    }

    @Override
    public void printAsm(IAsmWriter out) {
        elementType.printAsm(out);
        out.push('*');
    }
}
