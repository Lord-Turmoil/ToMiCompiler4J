/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.asm.impl;

import lib.twio.ITwioWriter;
import tomic.llvm.asm.IAsmPrinter;
import tomic.llvm.asm.IAsmWriter;
import tomic.llvm.ir.Module;

public class VerboseAsmPrinter implements IAsmPrinter {
    @Override
    public void print(Module module, ITwioWriter writer) {
        var out = new VerboseAsmWriter(writer);

        printHeader(out);
        printModule(out, module);
        printFooter(out);
    }

    private void printHeader(IAsmWriter out) {
        out.pushComment("This file is generated by ToMiC4J");
        out.pushComment("LLVM IR Version: 1.3.2").pushNewLine();
    }

    private void printFooter(IAsmWriter out) {
        out.pushNewLine();
        out.pushComment("End of LLVM IR");
    }

    private void printModule(IAsmWriter out, Module module) {
        out.commentBegin();
        out.push("Module ID = ").push('\'');
        out.push(module.getName()).push('\'');
        out.commentEnd().pushNewLine();

        printDeclaration(out);

        module.getGlobalVariables().forEach(it -> it.printAsm(out));
        if (!module.getGlobalStrings().isEmpty()) {
            out.pushNewLine();
            module.getGlobalStrings().forEach(it -> it.printAsm(out));
        }

        module.getFunctions().forEach(it -> it.printAsm(out));

        module.getMainFunction().printAsm(out);
    }

    private void printDeclaration(IAsmWriter out) {
        out.push("declare i32 @getint()");
        out.pushNewLine();
        out.push("declare void @putint(i32)");
        out.pushNewLine();
        out.push("declare void @putstr(i8*)");
        out.pushNewLine();
        out.pushNewLine();
    }
}
