/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.mips;

public interface IMipsPrinter {
    void printLabel(IMipsWriter out, String label);

    void printMove(IMipsWriter out, int dst, int src);

    void printLoadWord(IMipsWriter out, int dst, int offset, int base);

    void printStoreWord(IMipsWriter out, int src, int offset, int base);

    void printBinaryOperator(IMipsWriter out, String op, int dst, int lhs, int rhs);
    void printBinaryOperator(IMipsWriter out, String op, int dst, int lhs, String rhs);

    void printUnaryOperator(IMipsWriter out, String op, int dst, int operand);
    void printUnaryOperator(IMipsWriter out, String op, int dst, String operand);

    void printReturn(IMipsWriter out);

    void printStackGrow(IMipsWriter out, int register);
    void printStackShrink(IMipsWriter out, int register);


    void printSaveStack(IMipsWriter out, int src, int offset);

    void printLoadStack(IMipsWriter out, int dst, int offset);

    void printCall(IMipsWriter out, String name);

    void printJump(IMipsWriter out, String label);

    void printBranch(IMipsWriter out, int flag, String trueLabel, String falseLabel);
    void printInverseBranch(IMipsWriter out, int flag, String trueLabel, String falseLabel);
}
