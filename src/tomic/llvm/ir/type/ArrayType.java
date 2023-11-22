package tomic.llvm.ir.type;

import tomic.llvm.asm.IAsmWriter;

public class ArrayType extends Type {
    private final Type elementType;
    private final int elementCount;

    public ArrayType(Type elementType, int elementCount) {
        super(elementType.getContext(), TypeID.ArrayTyID);
        this.elementType = elementType;
        this.elementCount = elementCount;
    }

    public static ArrayType get(Type elementType, int elementCount) {
        return elementType.getContext().getArrayType(elementType, elementCount);
    }

    public Type getElementType() {
        return elementType;
    }

    public int getElementCount() {
        return elementCount;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ArrayType other) {
            return elementType.equals(other.elementType) && elementCount == other.elementCount;
        }
        return false;
    }

    public boolean match(Type elementType, int elementCount) {
        return this.elementType.equals(elementType) && this.elementCount == elementCount;
    }

    @Override
    public void printAsm(IAsmWriter out) {
        out.push('[');
        out.push(String.valueOf(elementCount));
        out.pushNext(" x ");
        elementType.printAsm(out);
        out.push(']');
    }
}
