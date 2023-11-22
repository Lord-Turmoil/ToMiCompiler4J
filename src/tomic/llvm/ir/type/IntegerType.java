package tomic.llvm.ir.type;

import tomic.llvm.ir.LlvmContext;

public class IntegerType extends Type {
    private final int bitWidth;

    public IntegerType(LlvmContext context, int bitWidth) {
        super(context, TypeID.IntegerTyID);
        this.bitWidth = bitWidth;
    }

    public static IntegerType get(LlvmContext context, int bitWidth) {
        return switch (bitWidth) {
            case 8 -> context.getInt8Ty();
            case 16 -> context.getInt16Ty();
            case 32 -> context.getInt32Ty();
            case 64 -> context.getInt64Ty();
            default -> throw new IllegalArgumentException("Invalid bit width");
        };
    }

    public int getBitWidth() {
        return bitWidth;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IntegerType other) {
            return bitWidth == other.bitWidth;
        }
        return false;
    }
}
