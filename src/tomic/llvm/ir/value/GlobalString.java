package tomic.llvm.ir.value;

import tomic.llvm.ir.LlvmContext;
import tomic.llvm.ir.type.ArrayType;
import tomic.llvm.ir.type.IntegerType;
import tomic.llvm.ir.type.PointerType;
import tomic.llvm.ir.type.Type;

public class GlobalString extends GlobalValue {
    private final String value;

    private GlobalString(Type type, String value, String name) {
        super(ValueTypes.GlobalStringTy, type, name);
        this.value = value;
    }

    private static int idx = -1;

    public static GlobalString getInstance(LlvmContext context, String value) {
        String name;
        if (++idx > 0) {
            name = ".str." + idx;
        } else {
            name = ".str";
        }

        int size = value.length() + 1;
        var type = PointerType.get(ArrayType.get(IntegerType.get(context, 8), size));

        return new GlobalString(type, value, name);
    }
}
