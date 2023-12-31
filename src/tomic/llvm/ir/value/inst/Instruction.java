/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.ir.value.inst;

import tomic.llvm.asm.IAsmWriter;
import tomic.llvm.ir.Module;
import tomic.llvm.ir.type.Type;
import tomic.llvm.ir.value.BasicBlock;
import tomic.llvm.ir.value.Function;
import tomic.llvm.ir.value.User;
import tomic.llvm.ir.value.ValueTypes;

public abstract class Instruction extends User {
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

    public int getIndex() {
        return getParent().getInstructions().indexOf(this);
    }

    @Override
    public IAsmWriter printName(IAsmWriter out) {
        return out.push('%').push(String.valueOf(getParentFunction().slot(this)));
    }

    @Override
    public IAsmWriter printUse(IAsmWriter out) {
        getType().printAsm(out).pushSpace();
        return printName(out);
    }
}
