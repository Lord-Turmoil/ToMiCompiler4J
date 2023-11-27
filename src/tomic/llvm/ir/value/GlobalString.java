/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.ir.value;

import tomic.llvm.asm.IAsmWriter;
import tomic.llvm.ir.LlvmContext;
import tomic.llvm.ir.type.PointerType;
import tomic.llvm.ir.type.Type;

public class GlobalString extends GlobalValue {
    private final String value;


    /**
     * F**k Java, I want my C++ constructor with friend class!
     */
    public GlobalString(Type type, String value, String name) {
        super(ValueTypes.GlobalStringTy, type, name);
        this.value = value;
    }

    public static GlobalString getInstance(LlvmContext context, String value) {
        return context.getGlobalString(value);
    }

    @Override
    public IAsmWriter printAsm(IAsmWriter out) {
        printName(out).pushNext('=').pushNext("private unnamed_addr constant").pushSpace();

        ((PointerType) getType()).getElementType().printAsm(out);

        out.pushNext('c').push('"');
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) == '\n') {
                out.push("\\0A");
            } else {
                out.push(value.charAt(i));
            }
        }
        out.push("\\00").push('"');

        return out.push(", align 1").pushNewLine();
    }

    public String getValue() {
        return value;
    }
}
