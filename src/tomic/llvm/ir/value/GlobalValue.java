package tomic.llvm.ir.value;

import tomic.llvm.ir.Module;
import tomic.llvm.ir.type.Type;

public class GlobalValue extends Constant {
    private Module parent;

    protected GlobalValue(ValueTypes valueType, Type type, String name) {
        super(valueType, type);
        setName(name);
    }

    public Module getParent() {
        return parent;
    }

    public void setParent(Module parent) {
        this.parent = parent;
    }
}
