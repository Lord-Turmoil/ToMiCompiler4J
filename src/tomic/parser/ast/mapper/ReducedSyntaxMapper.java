package tomic.parser.ast.mapper;

import tomic.parser.ast.SyntaxTypes;

import java.util.HashMap;
import java.util.Map;

public class ReducedSyntaxMapper implements ISyntaxMapper {
    private Map<SyntaxTypes, String> descriptions;

    public ReducedSyntaxMapper() {
        init();
    }

    @Override
    public String description(SyntaxTypes type) {
        return descriptions.getOrDefault(type, null);
    }

    private void init() {
        descriptions = new HashMap<>();

//        descriptions.put(SyntaxTypes.UNKNOWN, "Unknown");
//        descriptions.put(SyntaxTypes.EPSILON, "Epsilon");
        descriptions.put(SyntaxTypes.TERMINATOR, "Terminator");

        descriptions.put(SyntaxTypes.COMP_UNIT, "CompUnit");

//        descriptions.put(SyntaxTypes.DECL, "Decl");
//        descriptions.put(SyntaxTypes.BTYPE, "BType");
        descriptions.put(SyntaxTypes.CONST_DECL, "ConstDecl");
        descriptions.put(SyntaxTypes.CONST_DEF, "ConstDef");
        descriptions.put(SyntaxTypes.CONST_INIT_VAL, "ConstInitVal");
        descriptions.put(SyntaxTypes.VAR_DECL, "VarDecl");
        descriptions.put(SyntaxTypes.VAR_DEF, "VarDef");
        descriptions.put(SyntaxTypes.INIT_VAL, "InitVal");

        descriptions.put(SyntaxTypes.FUNC_DEF, "FuncDef");
//        descriptions.put(SyntaxTypes.FUNC_DECL, "FuncDecl");
        descriptions.put(SyntaxTypes.FUNC_TYPE, "FuncType");
        descriptions.put(SyntaxTypes.FUNC_FPARAMS, "FuncFParams");
        descriptions.put(SyntaxTypes.FUNC_FPARAM, "FuncFParam");
        descriptions.put(SyntaxTypes.FUNC_APARAMS, "FuncRParams");
//        descriptions.put(SyntaxTypes.FUNC_APARAM, "FuncAParam");
        descriptions.put(SyntaxTypes.BLOCK, "Block");
//        descriptions.put(SyntaxTypes.BLOCK_ITEM, "BlockItem");

        descriptions.put(SyntaxTypes.MAIN_FUNC_DEF, "MainFuncDef");

        descriptions.put(SyntaxTypes.STMT, "Stmt");
//        descriptions.put(SyntaxTypes.ASSIGNMENT_STMT, "AssignmentStmt");
        descriptions.put(SyntaxTypes.LVAL, "LVal");
        descriptions.put(SyntaxTypes.COND, "Cond");
//        descriptions.put(SyntaxTypes.IF_STMT, "IfStmt");
//        descriptions.put(SyntaxTypes.FOR_STMT, "ForStmt");
        descriptions.put(SyntaxTypes.FOR_INIT_STMT, "ForStmt");
        descriptions.put(SyntaxTypes.FOR_STEP_STMT, "ForStmt");
//        descriptions.put(SyntaxTypes.EXP_STMT, "ExpStmt");
//        descriptions.put(SyntaxTypes.BREAK_STMT, "BreakStmt");
//        descriptions.put(SyntaxTypes.CONTINUE_STMT, "ContinueStmt");
//        descriptions.put(SyntaxTypes.RETURN_STMT, "ReturnStmt");
//        descriptions.put(SyntaxTypes.IN_STMT, "InStmt");
//        descriptions.put(SyntaxTypes.OUT_STMT, "OutStmt");

        descriptions.put(SyntaxTypes.EXP, "Exp");
        descriptions.put(SyntaxTypes.CONST_EXP, "ConstExp");
        descriptions.put(SyntaxTypes.ADD_EXP, "AddExp");
        descriptions.put(SyntaxTypes.MUL_EXP, "MulExp");
        descriptions.put(SyntaxTypes.UNARY_EXP, "UnaryExp");
        descriptions.put(SyntaxTypes.UNARY_OP, "UnaryOp");
        descriptions.put(SyntaxTypes.PRIMARY_EXP, "PrimaryExp");
//        descriptions.put(SyntaxTypes.FUNC_CALL, "FunctionCall");
        descriptions.put(SyntaxTypes.NUMBER, "Number");
        descriptions.put(SyntaxTypes.OR_EXP, "LOrExp");
        descriptions.put(SyntaxTypes.AND_EXP, "LAndExp");
        descriptions.put(SyntaxTypes.EQ_EXP, "EqExp");
        descriptions.put(SyntaxTypes.REL_EXP, "RelExp");
    }
}
