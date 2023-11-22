package tomic.llvm.ir.value;

import tomic.llvm.ir.type.Type;

public class GlobalVariable extends GlobalValue {
    private final boolean isConstant;
    private final ConstantData initializer;

    public GlobalVariable(Type type, boolean isConstant, String name) {
        this(type, isConstant, name, null);
    }

    public GlobalVariable(Type type, boolean isConstant, String name, ConstantData initializer) {
        super(ValueTypes.GlobalVariableTy, type, name);
        this.isConstant = isConstant;
        this.initializer = initializer;
    }
}
