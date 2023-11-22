package tomic.llvm.ir.value;

import tomic.llvm.ir.type.Type;

public class Argument extends Value {
    private Function parent;
    private final int argNo;

    public Argument(Type type, String name, int argNo) {
        super(ValueTypes.ArgumentTy, type);
        this.setName(name);
        this.parent = null;
        this.argNo = argNo;
    }

    public Function getParent() {
        return parent;
    }

    public void setParent(Function parent) {
        this.parent = parent;
    }

    public int getArgNo() {
        return argNo;
    }
}
