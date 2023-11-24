/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.ir.value.inst;

import tomic.llvm.ir.type.Type;
import tomic.llvm.ir.value.Value;
import tomic.llvm.ir.value.ValueTypes;

public class CastInst extends UnaryInstruction {
    public CastInst(ValueTypes valueType, Type type, Value operand) {
        super(valueType, type, operand);
    }
}
