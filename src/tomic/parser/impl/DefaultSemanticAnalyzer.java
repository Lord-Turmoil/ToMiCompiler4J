/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.parser.impl;

import tomic.lexer.token.TokenTypes;
import tomic.logger.debug.IDebugLogger;
import tomic.logger.debug.LogLevel;
import tomic.logger.error.ErrorTypes;
import tomic.logger.error.IErrorLogger;
import tomic.parser.ISemanticAnalyzer;
import tomic.parser.ast.*;
import tomic.parser.table.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class DefaultSemanticAnalyzer implements ISemanticAnalyzer, IAstVisitor {
    private final IErrorLogger errorLogger;
    private final IDebugLogger debugLogger;

    private SymbolTable table;
    private SymbolTableBlock currentBlock;
    private final Stack<SyntaxNode> nodeStack;
    private SyntaxNode errorCandidate;

    public DefaultSemanticAnalyzer(IErrorLogger errorLogger, IDebugLogger debugLogger) {
        this.errorLogger = errorLogger;
        this.debugLogger = debugLogger;
        nodeStack = new Stack<>();
    }

    @Override
    public SymbolTable analyze(SyntaxTree tree) {
        table = new SymbolTable();
        nodeStack.clear();
        nodeStack.push(tree.getRoot());
        tree.accept(this);
        return table;
    }

    @Override
    public boolean visitEnter(SyntaxNode node) {
        nodeStack.push(node);

        return switch (node.getType()) {
            case COMP_UNIT -> enterCompUnit(node);
            case DECL -> enterDecl(node);
            case CONST_DECL -> enterConstDecl(node);
            case BLOCK -> enterBlock(node);
            case MAIN_FUNC_DEF -> enterMainFuncDef(node);
            case FOR_STMT -> enterForStmt(node);
            case CONST_EXP -> enterConstExp(node);
            default -> true;
        };
    }

    @Override
    public boolean visitExit(SyntaxNode node) {
        boolean ret = switch (node.getType()) {
            case COMP_UNIT -> exitCompUnit(node);
            case BTYPE -> exitBType(node);
            case CONST_DEF -> exitConstDef(node);
            case CONST_INIT_VAL -> exitConstInitVal(node);
            case VAR_DEF -> exitVarDef(node);
            case INIT_VAL -> exitInitVal(node);
            case FUNC_DEF -> exitFuncDef(node);
            case FUNC_DECL -> exitFuncDecl(node);
            case FUNC_TYPE -> exitFuncType(node);
            case FUNC_FPARAMS -> exitFuncFParams(node);
            case FUNC_FPARAM -> exitFuncFParam(node);
            case FUNC_APARAMS -> exitFuncAParams(node);
            case FUNC_APARAM -> exitFuncAParam(node);
            case BLOCK -> exitBlock(node);
            case MAIN_FUNC_DEF -> exitMainFuncDef(node);
            case ASSIGNMENT_STMT -> exitAssignmentStmt(node);
            case LVAL -> exitLVal(node);
            case COND -> exitCond(node);
            case FOR_INIT_STMT, FOR_STEP_STMT -> exitForInnerStmt(node);
            case EXP -> exitExp(node);
            case ADD_EXP, MUL_EXP, OR_EXP, AND_EXP, EQ_EXP, REL_EXP -> defaultExitExp(node);
            case CONST_EXP -> exitConstExp(node);
            case BREAK_STMT -> exitBreakStmt(node);
            case CONTINUE_STMT -> exitContinueStmt(node);
            case RETURN_STMT -> exitReturnStmt(node);
            case IN_STMT -> exitInStmt(node);
            case OUT_STMT -> exitOutStmt(node);
            case UNARY_EXP -> exitUnaryExp(node);
            case UNARY_OP -> exitUnaryOp(node);
            case PRIMARY_EXP -> exitPrimaryExp(node);
            case FUNC_CALL -> exitFuncCall(node);
            case NUMBER -> exitNumber(node);
            default -> true;
        };
        nodeStack.pop();

        return ret;
    }

    @Override
    public boolean visit(SyntaxNode node) {
        return IAstVisitor.super.visit(node);
    }

    /*
     * ===================== Utility Functions =============================
     */
    private SymbolTableBlock getOrCreateBlock(SyntaxNode node) {
        int blockId = node.getIntAttribute("tbl", -1);
        if (blockId != -1) {
            return table.getBlock(blockId);
        }

        SymbolTableBlock block;
        if (currentBlock != null) {
            block = currentBlock.newChild();
            node.setIntAttribute("tbl", block.getId());
        } else {
            block = table.newRoot();
            node.setIntAttribute("tbl", block.getId());
        }

        currentBlock = block;

        return block;
    }

    private boolean addToSymbolTable(SymbolTableEntry entry) {
        if (currentBlock.findLocalEntry(entry.getName()) != null) {
            log(LogLevel.ERROR, "Redefinition of " + entry.getName());
            logError(ErrorTypes.REDEFINED_SYMBOL, "Redefined symbol " + entry.getName());
            return false;
        }
        currentBlock.addEntry(entry);
        return true;
    }

    private int validateConstSubscription(SyntaxNode constExp) {
        if (!constExp.getBoolAttribute("det")) {
            log(LogLevel.ERROR, "Non-constant subscription");
            logError(ErrorTypes.UNKNOWN, "Non-constant subscription");
            return 0;
        }

        SymbolValueTypes type = SymbolValueTypes.values()[AstExt.getSynthesizedIntAttribute(constExp, "type")];
        if (type != SymbolValueTypes.INT) {
            log(LogLevel.ERROR, "Non-integer subscription");
            logError(ErrorTypes.UNKNOWN, "Non-integer subscription");
        }

        int size = constExp.getIntAttribute("value");
        if (size < 0) {
            log(LogLevel.ERROR, "Negative subscription");
            logError(ErrorTypes.UNKNOWN, "Negative subscription");
            size = 0;
        }

        return size;
    }

    private void validateSubscription(SyntaxNode node) {
        SymbolValueTypes type = SymbolValueTypes.values()[AstExt.getSynthesizedIntAttribute(node, "type")];
        if (type != SymbolValueTypes.INT) {
            log(LogLevel.ERROR, "Non-integer subscription");
            logError(ErrorTypes.UNKNOWN, "Non-integer subscription");
        }
    }

    /*
     * ===================== Logging =============================
     */
    private void log(LogLevel level, String message) {
        SyntaxNode node = errorCandidate != null ? errorCandidate : nodeStack.peek();
        var terminator = AstExt.getChildNode(node, SyntaxTypes.TERMINATOR);
        int line = terminator.getToken().lineNo;
        int column = terminator.getToken().charNo;

        debugLogger.log(LogLevel.ERROR, String.format("(%d:%d) %s", line, column, message));
    }

    private void logError(ErrorTypes type, String message) {
        SyntaxNode node = errorCandidate != null ? errorCandidate : nodeStack.peek();
        var terminator = AstExt.getChildNode(node, SyntaxTypes.TERMINATOR);
        int line = terminator.getToken().lineNo;
        int column = terminator.getToken().charNo;
        errorLogger.log(line, column, type, message);
    }

    /*
     * ===================== Parsing Handling =============================
     */
    private boolean enterCompUnit(SyntaxNode node) {
        getOrCreateBlock(node);
        return true;
    }

    private boolean exitCompUnit(SyntaxNode node) {
        currentBlock = currentBlock.getParent();
        return true;
    }

    private boolean enterDecl(SyntaxNode node) {
        var parent = node.getParent();

        if (parent.is(SyntaxTypes.COMP_UNIT)) {
            node.setBoolAttribute("global", true);
        }

        return true;
    }

    private boolean exitBType(SyntaxNode node) {
        SymbolValueTypes type = SymbolValueTypes.INT;
        node.setIntAttribute("type", type.ordinal());
        node.getParent().setIntAttribute("type", type.ordinal());
        return true;
    }

    private boolean enterConstDecl(SyntaxNode node) {
        node.setBoolAttribute("const", true);
        return true;
    }

    private boolean exitConstDef(SyntaxNode node) {
        int dim = AstExt.countDirectTerminalNode(node, TokenTypes.LEFT_BRACKET);
        SyntaxNode constInitVal = AstExt.getChildNode(node, SyntaxTypes.CONST_INIT_VAL);

        SyntaxNode ident = AstExt.getDirectChildNode(node, SyntaxTypes.TERMINATOR);
        var builder = ConstantEntry.builder(ident.getToken().lexeme);
        var type = SymbolValueTypes.values()[AstExt.getInheritedIntAttribute(node, "type")];
        if (dim == 0) {
            builder.setType(type);
        } else if (dim == 1) {
            int size = validateConstSubscription(AstExt.getDirectChildNode(node, SyntaxTypes.CONST_EXP));
            builder.setType(type).setSize(size);
        } else if (dim == 2) {
            int size1 = validateConstSubscription(AstExt.getDirectChildNode(node, SyntaxTypes.CONST_EXP));
            int size2 = validateConstSubscription(AstExt.getDirectChildNode(node, SyntaxTypes.CONST_EXP, 2));
            builder.setType(type).setSize(size1, size2);
        } else {
            log(LogLevel.ERROR, "Invalid dimension: " + dim);
            logError(ErrorTypes.UNKNOWN, "Invalid dimension: " + dim);
        }

        if (constInitVal.getBoolAttribute("det")) {
            if (dim == 0) {
                builder.setValue(constInitVal.getIntAttribute("value"));
            } else {
                builder.setValues(AstExt.deserializeArray(constInitVal.getAttribute("values")));
            }
        }

        if (dim != constInitVal.getIntAttribute("dim")) {
            log(LogLevel.ERROR, String.format("Dimension mismatch: %d != %d", dim, constInitVal.getIntAttribute("dim")));
            logError(ErrorTypes.UNKNOWN, String.format("Dimension mismatch: %d != %d", dim, constInitVal.getIntAttribute("dim")));
            return true;
        }

        addToSymbolTable(builder.build());

        return true;
    }

    private boolean exitConstInitVal(SyntaxNode node) {
        if (node.getFirstChild().is(SyntaxTypes.CONST_EXP)) {
            node.setIntAttribute("dim", 0);
            if (node.getFirstChild().getBoolAttribute("det")) {
                node.setBoolAttribute("det", true);
                node.setIntAttribute("value", node.getFirstChild().getIntAttribute("value"));
            }
            return true;
        }

        List<SyntaxNode> children = AstExt.getDirectChildNodes(node, SyntaxTypes.CONST_INIT_VAL);
        int size = children.size();
        int childDim = children.get(0).getIntAttribute("dim");
        int childSize = children.get(0).getIntAttribute("size");
        boolean det = true;

        for (var child : children) {
            if (child.getIntAttribute("dim") != childDim) {
                log(LogLevel.ERROR, "Dimension mismatch");
                logError(ErrorTypes.UNKNOWN, "Dimension mismatch");
            }
            if (child.getIntAttribute("size") != childSize) {
                log(LogLevel.ERROR, "Size mismatch");
                logError(ErrorTypes.UNKNOWN, "Size mismatch");
            }
            if (!child.getBoolAttribute("det")) {
                det = false;
            }
        }

        int dim = childDim + 1;
        node.setIntAttribute("dim", dim);
        node.setIntAttribute("size", size);
        ArrayList<ArrayList<Integer>> values = new ArrayList<>();
        if (det) {
            node.setBoolAttribute("det", true);
            if (dim == 1) {
                values.add(new ArrayList<>());
                for (var child : children) {
                    values.get(0).add(child.getIntAttribute("value"));
                }
            } else if (dim == 2) {
                for (var child : children) {
                    values.add(AstExt.deserializeArray(child.getAttribute("values")).get(0));
                }
            }
            node.setAttribute("values", AstExt.serializeArray(values));
        } else {
            node.setBoolAttribute("det", false);
            log(LogLevel.ERROR, "Non-deterministic constant initialization");
            logError(ErrorTypes.UNKNOWN, "Non-deterministic constant initialization");
        }

        return true;
    }

    private boolean exitVarDef(SyntaxNode node) {
        var initVal = AstExt.getChildNode(node, SyntaxTypes.INIT_VAL);
        boolean global = node.getBoolAttribute("global");

        if (global) {
            if (initVal != null && !initVal.getBoolAttribute("det")) {
                log(LogLevel.ERROR, "Non-deterministic global variable initialization");
                logError(ErrorTypes.UNKNOWN, "Non-deterministic global variable initialization");
            }
        }

        int dim = AstExt.countDirectTerminalNode(node, TokenTypes.LEFT_BRACKET);
        if (initVal != null) {
            if (dim != initVal.getIntAttribute("dim")) {
                log(LogLevel.ERROR, "Dimension mismatch");
                logError(ErrorTypes.UNKNOWN, "Dimension mismatch");
            }
        }

        SyntaxNode ident = AstExt.getDirectChildNode(node, SyntaxTypes.TERMINATOR);
        VariableEntry entry;
        var type = SymbolValueTypes.values()[AstExt.getInheritedIntAttribute(node, "type")];
        var builder = VariableEntry.builder(ident.getToken().lexeme);
        if (dim == 0) {
            entry = builder.setType(type).build();
        } else if (dim == 1) {
            int size = validateConstSubscription(AstExt.getDirectChildNode(node, SyntaxTypes.CONST_EXP));
            entry = builder.setType(type).setSizes(size).build();
        } else if (dim == 2) {
            int size1 = validateConstSubscription(AstExt.getDirectChildNode(node, SyntaxTypes.CONST_EXP));
            int size2 = validateConstSubscription(AstExt.getDirectChildNode(node, SyntaxTypes.CONST_EXP, 2));
            entry = builder.setType(type).setSizes(size1, size2).build();
        } else {
            log(LogLevel.ERROR, "Invalid dimension: " + dim);
            logError(ErrorTypes.UNKNOWN, "Invalid dimension: " + dim);
            return true;
        }

        node.setIntAttribute("dim", dim);
        addToSymbolTable(entry);

        return true;
    }

    private boolean exitInitVal(SyntaxNode node) {
        if (node.getFirstChild().is(SyntaxTypes.EXP)) {
            var exp = node.getFirstChild();
            if (exp.getIntAttribute("dim") != 0) {
                log(LogLevel.ERROR, "Dimension mismatch");
                logError(ErrorTypes.UNKNOWN, "Dimension mismatch");
            }
            node.setAttribute("dim", "0");
            if (node.getFirstChild().getBoolAttribute("det")) {
                node.setBoolAttribute("det", true);
                node.setIntAttribute("value", node.getFirstChild().getIntAttribute("value"));
            }
        } else {
            int size = AstExt.countDirectChildNode(node, SyntaxTypes.INIT_VAL);
            var child = AstExt.getDirectChildNode(node, SyntaxTypes.INIT_VAL);
            int dim = child.getIntAttribute("dim");
            int childSize = child.getIntAttribute("size");
            boolean det = true;
            for (child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
                if (!child.is(SyntaxTypes.CONST_INIT_VAL)) {
                    continue;
                }
                if (child.getIntAttribute("dim") != dim) {
                    log(LogLevel.ERROR, "Dimension mismatch");
                    logError(ErrorTypes.UNKNOWN, "Dimension mismatch");
                }
                if (child.getIntAttribute("size") != childSize) {
                    log(LogLevel.ERROR, "Size mismatch");
                    logError(ErrorTypes.UNKNOWN, "Size mismatch");
                }
                if (!child.getBoolAttribute("det")) {
                    det = false;
                }
            }
            node.setIntAttribute("dim", dim + 1);
            node.setIntAttribute("size", size);
            if (det) {
                node.setBoolAttribute("det", true);
            }
        }

        return true;
    }

    private boolean exitFuncDef(SyntaxNode node) {
        if (node.getBoolAttribute("bad")) {
            return true;
        }

        var type = SymbolValueTypes.values()[AstExt.getSynthesizedIntAttribute(node, "type")];
        if (type == SymbolValueTypes.INT) {
            // set error candidate to '}'
            errorCandidate = node.getLastChild().getLastChild();
            var lastStmt = AstExt.getChildNode(node, SyntaxTypes.BLOCK_ITEM, -1);
            if (lastStmt == null || AstExt.getChildNode(lastStmt, SyntaxTypes.RETURN_STMT) == null) {
                log(LogLevel.ERROR, "Missing return statement");
                logError(ErrorTypes.MISSING_RETURN_STATEMENT, "Missing return statement");
            }
            errorCandidate = null;
        }

        return true;
    }

    private boolean exitFuncDecl(SyntaxNode node) {
        var type = SymbolValueTypes.values()[AstExt.getSynthesizedIntAttribute(node, "type")];
        node.setIntAttribute("type", type.ordinal());
        node.getParent().setIntAttribute("type", type.ordinal());

        var ident = AstExt.getDirectChildNode(node, SyntaxTypes.TERMINATOR);
        var builder = FunctionEntry.builder(ident.getToken().lexeme).setType(type);
        var params = AstExt.getDirectChildNode(node, SyntaxTypes.FUNC_FPARAMS);
        if (params != null) {
            var paramList = AstExt.getDirectChildNodes(params, SyntaxTypes.FUNC_FPARAM);
            for (var param : paramList) {
                var paramType = SymbolValueTypes.values()[param.getIntAttribute("type")];
                int paramDim = param.getIntAttribute("dim");
                String paramName = param.getAttribute("name");
                int paramSize = param.getIntAttribute("size");
                builder.addParam(paramType, paramName, paramDim, paramSize);
            }
        }

        if (!addToSymbolTable(builder.build())) {
            node.getParent().setBoolAttribute("bad", true);
        }

        return true;
    }

    private boolean exitFuncType(SyntaxNode node) {
        var tokenType = node.getFirstChild().getToken().type;
        SymbolValueTypes type = switch (tokenType) {
            case INT -> SymbolValueTypes.INT;
            case VOID -> SymbolValueTypes.VOID;
            default -> SymbolValueTypes.ANY;
        };
        node.setIntAttribute("type", type.ordinal());
        node.getParent().setIntAttribute("type", type.ordinal());

        return true;
    }

    private boolean exitFuncFParams(SyntaxNode node) {
        int count = AstExt.countDirectChildNode(node, SyntaxTypes.FUNC_FPARAM);
        node.setIntAttribute("argc", count);
        return true;
    }

    private boolean exitFuncFParam(SyntaxNode node) {
        var ident = AstExt.getDirectChildNode(node, SyntaxTypes.TERMINATOR);
        String name = ident.getToken().lexeme;
        node.setAttribute("name", name);
        int dim = AstExt.countDirectTerminalNode(node, TokenTypes.LEFT_BRACKET);
        node.setIntAttribute("dim", dim);
        if (dim > 0) {
            node.setIntAttribute("type", SymbolValueTypes.ARRAY.ordinal());
        } else {
            node.setIntAttribute("type", SymbolValueTypes.INT.ordinal());
        }

        if (dim == 2) {
            var constExp = AstExt.getDirectChildNode(node, SyntaxTypes.CONST_EXP);
            if (constExp.getBoolAttribute("det")) {
                int size = constExp.getIntAttribute("value");
                if (size < 0) {
                    log(LogLevel.ERROR, "Negative array size");
                    logError(ErrorTypes.UNKNOWN, "Negative array size");
                } else {
                    node.setIntAttribute("size", size);
                }
            }
        }

        return true;
    }

    private boolean exitFuncAParams(SyntaxNode node) {
        int count = AstExt.countDirectChildNode(node, SyntaxTypes.FUNC_APARAM);
        node.setIntAttribute("argc", count);
        return true;
    }

    private boolean exitFuncAParam(SyntaxNode node) {
        var exp = AstExt.getDirectChildNode(node, SyntaxTypes.EXP);
        var type = SymbolValueTypes.values()[exp.getIntAttribute("type")];
        node.setIntAttribute("type", type.ordinal());

        if (type == SymbolValueTypes.ARRAY) {
            int dim = exp.getIntAttribute("dim");
            node.setIntAttribute("dim", dim);
            if (dim == 2) {
                node.setIntAttribute("size", exp.getIntAttribute("size"));
            }
        }

        return true;
    }

    private boolean enterBlock(SyntaxNode node) {
        currentBlock = getOrCreateBlock(node);

        if (node.getParent().is(SyntaxTypes.FUNC_DEF)) {
            if (node.getParent().getBoolAttribute("bad")) {
                return false;
            }

            var funcFParams = AstExt.getDirectChildNode(node.getPrevSibling(), SyntaxTypes.FUNC_FPARAMS);
            if (funcFParams != null) {
                var params = SymbolTableExt.buildParamVariableEntries(funcFParams);
                for (var param : params) {
                    errorCandidate = param.node();
                    addToSymbolTable(param.entry());
                    errorCandidate = null;
                }
            }
        }
        return true;
    }

    private boolean exitBlock(SyntaxNode node) {
        currentBlock = currentBlock.getParent();
        return true;
    }

    private boolean enterMainFuncDef(SyntaxNode node) {
        node.setIntAttribute("type", SymbolValueTypes.INT.ordinal());
        return true;
    }

    private boolean exitMainFuncDef(SyntaxNode node) {
        errorCandidate = node.getLastChild().getLastChild();
        var lastStmt = AstExt.getChildNode(node, SyntaxTypes.BLOCK_ITEM, -1);
        if (lastStmt == null || AstExt.getChildNode(lastStmt, SyntaxTypes.RETURN_STMT) == null) {
            log(LogLevel.ERROR, "Missing return statement");
            logError(ErrorTypes.MISSING_RETURN_STATEMENT, "Missing return statement");
        }
        errorCandidate = null;

        return true;
    }

    private boolean exitAssignmentStmt(SyntaxNode node) {
        var lVal = AstExt.getDirectChildNode(node, SyntaxTypes.LVAL);
        var type = SymbolValueTypes.values()[lVal.getIntAttribute("type")];
        if (type != SymbolValueTypes.INT) {
            log(LogLevel.ERROR, "Invalid assignment");
            logError(ErrorTypes.UNKNOWN, "Invalid assignment");
        }
        if (lVal.getBoolAttribute("const")) {
            log(LogLevel.ERROR, "Assignment to constant");
            logError(ErrorTypes.ASSIGN_TO_CONST, "Assignment to constant");
        }

        var exp = AstExt.getDirectChildNode(node, SyntaxTypes.EXP);
        if (type.ordinal() != AstExt.getSynthesizedIntAttribute(exp, "type")) {
            log(LogLevel.ERROR, "Type mismatch");
            logError(ErrorTypes.UNKNOWN, "Type mismatch");
        }

        return true;
    }

    private boolean exitLVal(SyntaxNode node) {
        SyntaxNode ident = node.getFirstChild();
        String name = ident.getToken().lexeme;
        SymbolTableEntry rawEntry = currentBlock.findEntry(name);

        node.setIntAttribute("type", SymbolValueTypes.INT.ordinal());

        if (rawEntry == null) {
            log(LogLevel.ERROR, "Undefined symbol: " + name);
            logError(ErrorTypes.UNDEFINED_SYMBOL, "Undefined symbol: " + name);
            return true;
        }

        int expectedDim = 0;
        int size = 0;
        if (rawEntry instanceof ConstantEntry entry) {
            node.setBoolAttribute("const", true);
            expectedDim = entry.getDimension();
            if (expectedDim == 2) {
                size = entry.getSize(1);
            }
        } else if (rawEntry instanceof VariableEntry entry) {
            expectedDim = entry.getDimension();
            if (expectedDim == 2) {
                size = entry.getSize(1);
            }
        } else {
            log(LogLevel.ERROR, "Invalid symbol: " + name);
            logError(ErrorTypes.UNDEFINED_SYMBOL, "Invalid symbol: " + name);
            node.setIntAttribute("type", SymbolValueTypes.ANY.ordinal());
            return true;
        }

        int actualDim = AstExt.countDirectChildNode(node, SyntaxTypes.EXP);
        if (actualDim > expectedDim) {
            actualDim = expectedDim;
            log(LogLevel.ERROR, "Dimension mismatch");
            logError(ErrorTypes.UNKNOWN, "Dimension mismatch");
        }

        int finalDim = expectedDim - actualDim;
        node.setIntAttribute("dim", finalDim);
        if (finalDim == 0) {
            node.setIntAttribute("type", SymbolValueTypes.INT.ordinal());
        } else {
            node.setIntAttribute("type", SymbolValueTypes.ARRAY.ordinal());
            if (finalDim == 2) {
                node.setIntAttribute("size", size);
            }
        }

        if (currentBlock.findLocalEntry(name) == null) {
            node.setIntAttribute("tbl", currentBlock.getParent().getId());
        } else {
            node.setIntAttribute("tbl", currentBlock.getId());
        }

        return true;
    }

    private boolean exitCond(SyntaxNode node) {
        var type = SymbolValueTypes.values()[AstExt.getSynthesizedIntAttribute(node, "type")];
        if (type != SymbolValueTypes.INT) {
            log(LogLevel.ERROR, "Non-integer condition");
            logError(ErrorTypes.UNKNOWN, "Non-integer condition");
        }

        return true;
    }

    private boolean enterForStmt(SyntaxNode node) {
        node.setBoolAttribute("loop", true);
        return true;
    }

    private boolean exitForInnerStmt(SyntaxNode node) {
        var lVal = AstExt.getDirectChildNode(node, SyntaxTypes.LVAL);
        var type = SymbolValueTypes.values()[lVal.getIntAttribute("type")];
        if (type != SymbolValueTypes.INT) {
            log(LogLevel.ERROR, "Invalid assignment");
            logError(ErrorTypes.UNKNOWN, "Invalid assignment");
            return true;
        }
        if (lVal.getBoolAttribute("const")) {
            log(LogLevel.ERROR, "Assignment to constant");
            logError(ErrorTypes.ASSIGN_TO_CONST, "Assignment to constant");
            return true;
        }

        var exp = AstExt.getDirectChildNode(node, SyntaxTypes.EXP);
        if (type.ordinal() != AstExt.getSynthesizedIntAttribute(exp, "type")) {
            log(LogLevel.ERROR, "Type mismatch");
            logError(ErrorTypes.UNKNOWN, "Type mismatch");
        }

        return true;
    }

    private boolean exitBreakStmt(SyntaxNode node) {
        if (!AstExt.getInheritedBoolAttribute(node, "loop")) {
            log(LogLevel.ERROR, "Break outside loop");
            logError(ErrorTypes.ILLEGAL_BREAK, "Break outside loop");
        }

        return true;
    }

    private boolean exitContinueStmt(SyntaxNode node) {
        if (!AstExt.getInheritedBoolAttribute(node, "loop")) {
            log(LogLevel.ERROR, "Continue outside loop");
            logError(ErrorTypes.ILLEGAL_CONTINUE, "Continue outside loop");
        }

        return true;
    }

    private boolean exitReturnStmt(SyntaxNode node) {
        var exp = AstExt.getDirectChildNode(node, SyntaxTypes.EXP);
        SymbolValueTypes type;
        if (exp != null) {
            type = SymbolValueTypes.values()[exp.getIntAttribute("type")];
        } else {
            type = SymbolValueTypes.VOID;
        }

        // Get
        var funcType = SymbolValueTypes.values()[AstExt.getInheritedIntAttribute(node, "type")];
        node.setIntAttribute("type", type.ordinal());

        if (funcType == SymbolValueTypes.VOID && type != SymbolValueTypes.VOID) {
            log(LogLevel.ERROR, "Invalid return type");
            logError(ErrorTypes.RETURN_TYPE_MISMATCH, "Invalid return type");
        } else if (funcType == SymbolValueTypes.INT && type != SymbolValueTypes.INT) {
            log(LogLevel.ERROR, "Invalid return type");
            logError(ErrorTypes.RETURN_TYPE_MISMATCH, "Invalid return type");
        }

        return true;
    }

    private boolean exitInStmt(SyntaxNode node) {
        var lVal = AstExt.getDirectChildNode(node, SyntaxTypes.LVAL);
        var type = SymbolValueTypes.values()[lVal.getIntAttribute("type")];
        if (type != SymbolValueTypes.INT) {
            log(LogLevel.ERROR, "Invalid assignment");
            logError(ErrorTypes.UNKNOWN, "Invalid assignment");
            return true;
        }
        if (lVal.getBoolAttribute("const")) {
            log(LogLevel.ERROR, "Assignment to constant");
            logError(ErrorTypes.ASSIGN_TO_CONST, "Assignment to constant");
            return true;
        }

        return true;
    }

    private boolean exitOutStmt(SyntaxNode node) {
        var formatStr = AstExt.getDirectChildNode(node, SyntaxTypes.TERMINATOR, 3);
        if (formatStr.getToken().type != TokenTypes.FORMAT) {
            log(LogLevel.ERROR, "Invalid format string");
            logError(ErrorTypes.UNKNOWN, "Invalid format string");
            return true;
        }
        String format = formatStr.getToken().lexeme;
        int formatArgc = AstExt.getFormatStringArgCount(format);
        var args = AstExt.getDirectChildNodes(node, SyntaxTypes.EXP);
        int argc = args.size();

        if (argc != formatArgc) {
            log(LogLevel.ERROR, "Argument count mismatch for printf");
            logError(ErrorTypes.PRINTF_EXTRA_ARGUMENTS, "Argument count mismatch for printf");
        }
        for (var arg : args) {
            var argType = SymbolValueTypes.values()[arg.getIntAttribute("type")];
            if (argType != SymbolValueTypes.INT) {
                log(LogLevel.ERROR, "Invalid argument type");
                logError(ErrorTypes.ARGUMENT_TYPE_MISMATCH, "Invalid argument type");
            }
        }

        return true;
    }

    private boolean defaultExitExp(SyntaxNode node) {
        node.setIntAttribute("type", SymbolValueTypes.INT.ordinal());

        if (node.hasManyChildren()) {
            var left = node.getFirstChild();
            var leftType = SymbolValueTypes.values()[left.getIntAttribute("type")];
            var right = node.getLastChild();
            var rightType = SymbolValueTypes.values()[right.getIntAttribute("type")];

            if (leftType != rightType) {
                log(LogLevel.ERROR, "Type mismatch");
                logError(ErrorTypes.UNKNOWN, "Type mismatch");
            } else {
                if (leftType == SymbolValueTypes.ARRAY) {
                    log(LogLevel.ERROR, "Invalid operation");
                    logError(ErrorTypes.UNKNOWN, "Invalid operation");
                } else {
                    node.setIntAttribute("type", leftType.ordinal());
                    if (left.getBoolAttribute("det") && right.getBoolAttribute("det")) {
                        node.setBoolAttribute("det", true);

                        int leftValue = left.getIntAttribute("value");
                        int rightValue = right.getIntAttribute("value");
                        var op = AstExt.getDirectChildNode(node, SyntaxTypes.TERMINATOR).getToken().lexeme;
                        int value = AstExt.evaluateBinary(op, leftValue, rightValue);

                        node.setIntAttribute("value", value);
                    }
                }
            }
        } else {
            var type = SymbolValueTypes.values()[node.getFirstChild().getIntAttribute("type")];
            node.setIntAttribute("type", type.ordinal());
            if (type == SymbolValueTypes.ARRAY) {
                int dim = node.getFirstChild().getIntAttribute("dim");
                node.setIntAttribute("dim", dim);
                if (dim == 2) {
                    node.setIntAttribute("size", node.getFirstChild().getIntAttribute("size"));
                }
            } else if (type == SymbolValueTypes.INT) {
                if (node.getFirstChild().getBoolAttribute("det")) {
                    node.setBoolAttribute("det", true);
                    node.setIntAttribute("value", node.getFirstChild().getIntAttribute("value"));
                }
            }
        }

        return true;
    }

    private boolean exitExp(SyntaxNode node) {
        var child = node.getFirstChild();
        var type = SymbolValueTypes.values()[child.getIntAttribute("type")];

        node.setIntAttribute("type", type.ordinal());
        if (child.getBoolAttribute("det")) {
            node.setBoolAttribute("det", true);
            node.setIntAttribute("value", child.getIntAttribute("value"));
        } else {
            if (type == SymbolValueTypes.ARRAY) {
                node.setIntAttribute("dim", child.getIntAttribute("dim"));
                if (child.getIntAttribute("dim") == 2) {
                    node.setIntAttribute("size", child.getIntAttribute("size"));
                }
            }
        }
        return true;
    }

    private boolean enterConstExp(SyntaxNode node) {
        node.setBoolAttribute("const", true);
        return true;
    }

    private boolean exitConstExp(SyntaxNode node) {
        exitExp(node);
        if (!node.getBoolAttribute("det")) {
            log(LogLevel.ERROR, "Non-deterministic constant expression");
            logError(ErrorTypes.UNKNOWN, "Non-deterministic constant expression");
        }

        return true;
    }

    private boolean exitUnaryExp(SyntaxNode node) {
        node.setIntAttribute("type", SymbolValueTypes.INT.ordinal());

        if (node.hasManyChildren()) {
            var exp = node.getLastChild();
            var type = SymbolValueTypes.values()[exp.getIntAttribute("type")];
            if (type != SymbolValueTypes.INT) {
                log(LogLevel.ERROR, "Invalid operation");
                logError(ErrorTypes.UNKNOWN, "Invalid operation");
            } else if (exp.getBoolAttribute("det")) {
                node.setBoolAttribute("det", true);
                int value = exp.getIntAttribute("value");
                var op = node.getFirstChild().getAttribute("op");
                node.setIntAttribute("value", AstExt.evaluateUnary(op, value));
            }
        } else {
            var child = node.getFirstChild();
            var type = SymbolValueTypes.values()[child.getIntAttribute("type")];
            node.setIntAttribute("type", type.ordinal());
            if (type == SymbolValueTypes.INT) {
                if (child.getBoolAttribute("det")) {
                    node.setBoolAttribute("det", true);
                    node.setIntAttribute("value", child.getIntAttribute("value"));
                }
            } else if (type == SymbolValueTypes.ARRAY) {
                node.setIntAttribute("dim", child.getIntAttribute("dim"));
                if (child.getIntAttribute("dim") == 2) {
                    node.setIntAttribute("size", child.getIntAttribute("size"));
                }
            }
        }

        return true;
    }

    private boolean exitUnaryOp(SyntaxNode node) {
        node.setAttribute("op", node.getFirstChild().getToken().lexeme);
        return true;
    }

    private boolean exitPrimaryExp(SyntaxNode node) {
        node.setIntAttribute("type", SymbolValueTypes.INT.ordinal());

        var child = node.getFirstChild();
        if (node.hasManyChildren()) {
            child = child.getNextSibling();
        }

        if (child.is(SyntaxTypes.LVAL)) {
            var type = SymbolValueTypes.values()[child.getIntAttribute("type")];
            if (type != SymbolValueTypes.INT) {
                node.setIntAttribute("type", type.ordinal());
                node.setIntAttribute("dim", child.getIntAttribute("dim"));
                if (child.getIntAttribute("dim") == 2) {
                    node.setIntAttribute("size", child.getIntAttribute("size"));
                }
            } else {
                int[] value = { 0 };
                if (AstExt.tryEvaluate(child, currentBlock, value)) {
                    node.setBoolAttribute("det", true);
                    node.setIntAttribute("value", value[0]);
                }
            }
        } else if (child.is(SyntaxTypes.NUMBER)) {
            node.setBoolAttribute("det", true);
            node.setIntAttribute("value", child.getIntAttribute("value"));
        } else {
            var type = SymbolValueTypes.values()[child.getIntAttribute("type")];
            if (type != SymbolValueTypes.INT) {
                node.setIntAttribute("type", type.ordinal());
                node.setIntAttribute("dim", child.getIntAttribute("dim"));
                if (child.getIntAttribute("dim") == 2) {
                    node.setIntAttribute("size", child.getIntAttribute("size"));
                }
            } else if (child.getBoolAttribute("det")) {
                node.setBoolAttribute("det", true);
                node.setIntAttribute("value", child.getIntAttribute("value"));
            }
        }

        return true;
    }

    private boolean exitFuncCall(SyntaxNode node) {
        String name = node.getFirstChild().getToken().lexeme;
        var rawEntry = currentBlock.findEntry(name);
        if (!(rawEntry instanceof FunctionEntry)) {
            log(LogLevel.ERROR, "Invalid function call");
            logError(ErrorTypes.UNDEFINED_SYMBOL, "Invalid function call");
            node.setIntAttribute("type", SymbolValueTypes.ANY.ordinal());
            return true;
        }
        var entry = (FunctionEntry) rawEntry;
        node.setIntAttribute("type", entry.getType().ordinal());

        int argc = AstExt.getSynthesizedIntAttribute(node, "argc");
        if (argc != entry.getParamCount()) {
            log(LogLevel.ERROR, String.format("Argument count mismatch, expect %d, got %d", entry.getParamCount(), argc));
            logError(ErrorTypes.ARGUMENT_COUNT_MISMATCH, "Argument count mismatch, expect " + entry.getParamCount() + ", got " + argc);
        }

        int upper = Math.min(argc, entry.getParamCount());
        if (upper == 0) {
            return true;
        }

        var params = AstExt.getDirectChildNode(node, SyntaxTypes.FUNC_APARAMS);
        var args = AstExt.getDirectChildNodes(params, SyntaxTypes.FUNC_APARAM);
        for (int i = 0; i < upper; i++) {
            var param = entry.getParam(i);
            var argType = SymbolValueTypes.values()[args.get(i).getIntAttribute("type")];
            if ((argType != param.type) && (argType != SymbolValueTypes.ANY)) {
                log(LogLevel.ERROR, "Argument type mismatch");
                logError(ErrorTypes.ARGUMENT_TYPE_MISMATCH, "Argument type mismatch");
                continue;
            }
            if (argType == SymbolValueTypes.ARRAY) {
                if (args.get(i).getIntAttribute("dim") != param.dimension) {
                    log(LogLevel.ERROR, "Argument dimension mismatch");
                    logError(ErrorTypes.ARGUMENT_TYPE_MISMATCH, "Argument dimension mismatch");
                    continue;
                }
                if (param.dimension == 2) {
                    if (args.get(i).getIntAttribute("size") != param.sizes[1]) {
                        log(LogLevel.ERROR, "Argument size mismatch");
                        logError(ErrorTypes.ARGUMENT_TYPE_MISMATCH, "Argument size mismatch");
                        continue;
                    }
                }
                if (AstExt.getSynthesizedBoolAttribute(args.get(i), "const")) {
                    log(LogLevel.ERROR, "Passing constant array");
                    logError(ErrorTypes.ARGUMENT_TYPE_MISMATCH, "Passing constant array");
                }
            }
        }

        return true;
    }

    private static boolean exitNumber(SyntaxNode node) {
        node.setIntAttribute("type", SymbolValueTypes.INT.ordinal());
        node.setBoolAttribute("det", true);

        node.setIntAttribute("value", Integer.parseInt(node.getFirstChild().getToken().lexeme));

        return true;
    }
}