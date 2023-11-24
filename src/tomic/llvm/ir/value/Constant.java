/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.ir.value;

import tomic.llvm.ir.type.Type;

public class Constant extends User {
    protected Constant(ValueTypes valueType, Type type) {
        super(valueType, type);
    }
}
