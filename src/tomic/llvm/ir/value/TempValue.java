/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.ir.value;

import tomic.llvm.ir.type.Type;

/**
 * Just a temporary value, which is not a real value in LLVM IR.
 */
public class TempValue extends Value {
    public TempValue(Type type) {
        super(ValueTypes.TempTy, type);
    }
}
