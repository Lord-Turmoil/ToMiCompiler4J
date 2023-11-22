package tomic.llvm.ir.value;

import tomic.llvm.ir.type.Type;

public class Constant extends User {
    protected Constant(ValueTypes valueType, Type type) {
        super(valueType, type);
    }
}
