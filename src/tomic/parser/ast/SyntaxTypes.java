/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.parser.ast;

public enum SyntaxTypes {
    UNKNOWN,

    EPSILON,
    TERMINATOR,

    COMP_UNIT,

    // Decl
    DECL,
    BTYPE,
    CONST_DECL,
    CONST_DEF,
    CONST_INIT_VAL,
    VAR_DECL,
    VAR_DEF,
    INIT_VAL,

    // FuncDef
    FUNC_DEF,
    FUNC_DECL,
    FUNC_TYPE,
    FUNC_FPARAMS,
    FUNC_FPARAM,
    FUNC_APARAMS,
    FUNC_APARAM,
    BLOCK,
    BLOCK_ITEM,

    // MainFuncDef
    MAIN_FUNC_DEF,

    // Stmt
    STMT,
    ASSIGNMENT_STMT,
    LVAL,
    COND,
    IF_STMT,
    FOR_STMT,
    FOR_INIT_STMT,
    FOR_STEP_STMT,
    EXP_STMT,
    BREAK_STMT,
    CONTINUE_STMT,
    RETURN_STMT,
    IN_STMT,
    OUT_STMT,

    // Exp
    EXP,
    CONST_EXP,
    ADD_EXP,
    MUL_EXP,
    UNARY_EXP,
    UNARY_OP,
    PRIMARY_EXP,
    FUNC_CALL,
    NUMBER,
    OR_EXP,
    AND_EXP,
    EQ_EXP,
    REL_EXP,
}
