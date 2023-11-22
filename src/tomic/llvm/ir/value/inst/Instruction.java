package tomic.llvm.ir.value.inst;

import tomic.llvm.ir.Module;
import tomic.llvm.ir.type.Type;
import tomic.llvm.ir.value.BasicBlock;
import tomic.llvm.ir.value.Function;
import tomic.llvm.ir.value.User;
import tomic.llvm.ir.value.ValueTypes;

public class Instruction extends User {
    private BasicBlock parent;

    protected Instruction(ValueTypes valueType, Type type) {
        super(valueType, type);
    }

    public BasicBlock getParent() {
        return parent;
    }

    public void setParent(BasicBlock parent) {
        this.parent = parent;
    }

    public Function getParentFunction() {
        return parent.getParent();
    }

    public Module getParentModule() {
        return getParentFunction().getParent();
    }
}
