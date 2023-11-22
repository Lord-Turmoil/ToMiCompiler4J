package tomic.llvm.ir.value;

import tomic.llvm.asm.IAsmWriter;
import tomic.llvm.ir.type.PointerType;
import tomic.llvm.ir.type.Type;

public class GlobalVariable extends GlobalValue {
    private final boolean isConstant;
    private final ConstantData initializer;

    public GlobalVariable(Type type, boolean isConstant, String name) {
        this(type, isConstant, name, null);
    }

    public GlobalVariable(Type type, boolean isConstant, String name, ConstantData initializer) {
        super(ValueTypes.GlobalVariableTy, PointerType.get(type), name);
        this.isConstant = isConstant;
        this.initializer = initializer;
    }

    @Override
    public IAsmWriter printAsm(IAsmWriter out) {
        printName(out).pushNext('=').pushNext("dso_local");

        out.pushNext(isConstant ? "constant" : "global").pushSpace();

        if (initializer != null) {
            initializer.printAsm(out);
        } else {
            var type = ((PointerType) getType()).getElementType();
            type.printAsm(out);
            if (type.isArrayTy()) {
                out.pushNext("zeroinitializer");
            } else {
                out.pushNext("0");
            }
        }

        return out.pushNewLine();
    }
}
