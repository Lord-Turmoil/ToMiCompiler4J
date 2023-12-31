/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.ir.value;

import tomic.llvm.asm.IAsmWriter;
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

    @Override
    public IAsmWriter printAsm(IAsmWriter out) {
        return printUse(out);
    }

    @Override
    public IAsmWriter printUse(IAsmWriter out) {
        getType().printAsm(out).pushSpace();
        return printName(out);
    }

    @Override
    public IAsmWriter printName(IAsmWriter out) {
        return out.push('%').push(String.valueOf(getParent().slot(this)));
    }
}
