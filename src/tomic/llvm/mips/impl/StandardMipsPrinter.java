/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.mips.impl;

import tomic.llvm.mips.IMipsPrinter;
import tomic.llvm.mips.IMipsWriter;
import tomic.llvm.mips.memory.Registers;

public class StandardMipsPrinter implements IMipsPrinter {
    @Override
    public void printLabel(IMipsWriter out, String label) {
        out.push(label).push(':').pushNewLine();
    }

    @Override
    public void printMove(IMipsWriter out, int dst, int src) {
        out.push("move").pushSpace();
        out.pushRegister(dst).pushComma().pushSpace();
        out.pushRegister(src).pushNewLine();
    }

    @Override
    public void printLoadWord(IMipsWriter out, int dst, int offset, int base) {
        out.push("lw").pushSpace();
        out.pushRegister(dst).pushComma().pushSpace();
        out.push(String.valueOf(offset)).push('(');
        out.pushRegister(base).push(')').pushNewLine();
    }

    @Override
    public void printStoreWord(IMipsWriter out, int src, int offset, int base) {
        out.push("sw").pushSpace();
        out.pushRegister(src).pushComma().pushSpace();
        out.push(String.valueOf(offset)).push('(');
        out.pushRegister(base).push(')').pushNewLine();
    }

    @Override
    public void printBinaryOperator(IMipsWriter out, String op, int dst, int lhs, int rhs) {
        out.push(op).pushSpace();
        out.pushRegister(dst).pushComma().pushSpace();
        out.pushRegister(lhs).pushComma().pushSpace();
        out.pushRegister(rhs).pushNewLine();
    }

    @Override
    public void printReturn(IMipsWriter out) {
        out.push("jr").pushSpace();
        out.pushRegister(Registers.RA).pushNewLine();
    }
}
