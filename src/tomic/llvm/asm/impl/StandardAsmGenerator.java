/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.asm.impl;

import tomic.lexer.token.TokenTypes;
import tomic.llvm.asm.IAsmGenerator;
import tomic.llvm.ir.LlvmExt;
import tomic.llvm.ir.Module;
import tomic.llvm.ir.type.ArrayType;
import tomic.llvm.ir.type.IntegerType;
import tomic.llvm.ir.type.PointerType;
import tomic.llvm.ir.type.Type;
import tomic.llvm.ir.value.*;
import tomic.llvm.ir.value.inst.*;
import tomic.parser.ast.*;
import tomic.parser.table.*;

import java.util.*;

public class StandardAsmGenerator implements IAsmGenerator, IAstVisitor {
    private SyntaxTree syntaxTree;
    private SymbolTable symbolTable;
    private Module module;

    private Function currentFunction;
    private BasicBlock currentBlock;

    private final Stack<ForContext> forCtxStack = new Stack<>();

    private final Map<SymbolTableEntry, Value> valueMap = new HashMap<>();

    @Override
    public Module generate(SyntaxTree syntaxTree, SymbolTable symbolTable, String name) {
        this.syntaxTree = syntaxTree;
        this.symbolTable = symbolTable;

        this.module = new Module(name);

        if (!parseCompilationUnit()) {
            return null;
        }

        module.refactor();

        return module;
    }

    @Override
    public boolean visitEnter(SyntaxNode node) {
        if (node.is(SyntaxTypes.BLOCK_ITEM)) {
            return parseBlockItem(node);
        } else if (node.is(SyntaxTypes.STMT)) {
            var statement = node.getFirstChild();
            if (statement.is(SyntaxTypes.BLOCK)) {
                return true;
            }
            parseStatement(statement);
            return false;
        }
        return true;
    }

    @Override
    public boolean visitExit(SyntaxNode node) {
        return IAstVisitor.super.visitExit(node);
    }

    @Override
    public boolean visit(SyntaxNode node) {
        return IAstVisitor.super.visit(node);
    }

    /*
     * ===================== Overall Parsing =====================
     */
    private boolean parseCompilationUnit() {
        var root = syntaxTree.getRoot();

        for (var it = root.getFirstChild(); it != null; it = it.getNextSibling()) {
            if (it.is(SyntaxTypes.DECL)) {
                parseGlobalDecl(it);
            } else if (it.is(SyntaxTypes.FUNC_DEF)) {
                parseFunction(it);
            } else if (it.is(SyntaxTypes.MAIN_FUNC_DEF)) {
                parseMainFunction(it);
            } else {
                throw new IllegalStateException("Unexpected node type: " + it.getType());
            }
        }

        return true;
    }

    private Function parseMainFunction(SyntaxNode node) {
        var context = module.getContext();

        Function function = Function.newInstance(IntegerType.get(context, 32), "main");
        setCurrentFunction(function);
        setCurrentBasicBlock(function.newBasicBlock());

        node.accept(this);

        module.addFunction(function);

        return function;
    }

    private Function parseFunction(SyntaxNode node) {
        var decl = node.getFirstChild();
        Type returnType = getNodeType(decl);

        var block = getSymbolTableBlock(node.getLastChild());
        var params = AstExt.getChildNode(decl, SyntaxTypes.FUNC_FPARAMS);
        ArrayList<Argument> args = new ArrayList<>();
        if (params != null && params.hasChildren()) {
            int argNo = 0;
            for (var param = params.getFirstChild(); param != null; param = param.getNextSibling()) {
                if (param.is(SyntaxTypes.FUNC_FPARAM)) {
                    args.add(parseArgument(param, argNo++, block));
                }
            }
        }

        String name = decl.childAt(1).getToken().lexeme;
        var entry = getSymbolTableBlock(node).findEntry(name);
        Function function = Function.newInstance(returnType, name, args);
        var body = initFunctionParams(function, block);

        setCurrentFunction(function);
        setCurrentBasicBlock(body);

        addValue(entry, function);
        node.accept(this);

        module.addFunction(function);
        return function;
    }

    private Argument parseArgument(SyntaxNode node, int argNo, SymbolTableBlock block) {
        String name = node.getAttribute("name");
        var entry = block.findEntry(name);
        Type type = getEntryType(entry);

        if (type.isIntegerTy()) {
            return new Argument(type, name, argNo);
        }
        return new Argument(PointerType.get(((ArrayType) type).getElementType()), name, argNo);
    }

    private BasicBlock initFunctionParams(Function function, SymbolTableBlock block) {
        var body = new BasicBlock(function);
        ArrayList<Value> allocas = new ArrayList<>();

        for (var it : function.getArguments()) {
            var alloca = new AllocaInst(it.getType());
            var entry = block.findEntry(it.getName());
            body.insertInstruction(alloca);
            allocas.add(alloca);
            addValue(entry, alloca);
        }
        for (var it : function.getArguments()) {
            body.insertInstruction(new StoreInst(it, allocas.get(it.getArgNo())));
        }

        function.insertBasicBlock(body);

        return body;
    }

    private boolean parseBlockItem(SyntaxNode node) {
        var child = node.getFirstChild();

        if (child.is(SyntaxTypes.VAR_DECL)) {
            parseVariableDecl(child);
        } else if (child.is(SyntaxTypes.CONST_DECL)) {
            parseVariableDecl(child);
        } else if (child.is(SyntaxTypes.STMT)) {
            var statement = child.getFirstChild();
            if (statement.is(SyntaxTypes.BLOCK)) {
                return true;
            }
            parseStatement(statement);
        } else {
            throw new IllegalStateException("Unexpected node type: " + child.getType());
        }

        return false;
    }

    private void parseStatement(SyntaxNode node) {
        switch (node.getType()) {
            case RETURN_STMT -> parseReturnStmt(node);
            case ASSIGNMENT_STMT -> parseAssignStmt(node);
            case EXP_STMT -> parseExpression(node.getFirstChild());
            case IN_STMT -> parseInputStmt(node);
            case OUT_STMT -> parseOutputStmt(node);
            case IF_STMT -> parseIfStmt(node);
            case FOR_STMT -> parseForStmt(node);
            case BREAK_STMT -> parseBreakStmt(node);
            case CONTINUE_STMT -> parseContinueStmt(node);
            default -> throw new IllegalStateException("Unexpected node type: " + node.getType());
        }
    }

    /*
     * ==================== Utility functions ====================
     */
    private SymbolTableBlock getSymbolTableBlock(SyntaxNode node) {
        int tbl = AstExt.getInheritedIntAttribute(node, "tbl", -1);
        var block = symbolTable.getBlock(tbl);
        if (block == null) {
            throw new IllegalStateException("Symbol table block not found: " + tbl);
        }
        return block;
    }

    private void addValue(SymbolTableEntry entry, Value value) {
        valueMap.put(entry, value);
    }

    private Value getValue(SymbolTableEntry entry) {
        var value = valueMap.getOrDefault(entry, null);
        if (value == null) {
            throw new IllegalStateException("Value not found for entry: " + entry);
        }
        return value;
    }

    private SymbolTableEntry getLValEntry(SyntaxNode node) {
        var block = getSymbolTableBlock(node);
        return block.findEntry(node.getFirstChild().getToken().lexeme);
    }

    // node is a LVal
    private Value getLValValue(SyntaxNode node) {
        var block = getSymbolTableBlock(node);
        var entry = block.findEntry(node.getFirstChild().getToken().lexeme);
        return getValue(entry);
    }

    private Function getFunction(SyntaxNode node) {
        var block = getSymbolTableBlock(node);
        var entry = block.findEntry(node.getFirstChild().getToken().lexeme);
        return (Function) getValue(entry);
    }

    private Type getEntryType(SymbolTableEntry entry) {
        var context = module.getContext();
        if (entry instanceof VariableEntry e) {
            return LlvmExt.getEntryType(context, e);
        } else if (entry instanceof ConstantEntry e) {
            return LlvmExt.getEntryType(context, e);
        } else if (entry instanceof FunctionEntry e) {
            return LlvmExt.getEntryType(context, e);
        } else {
            throw new IllegalStateException("Unexpected entry type: " + entry.getClass());
        }
    }

    private Type getNodeType(SyntaxNode node) {
        var type = SymbolValueTypes.values()[node.getIntAttribute("type")];
        return switch (type) {
            case INT -> IntegerType.get(module.getContext(), 32);
            case VOID -> Type.getVoidTy(module.getContext());
            default -> throw new IllegalStateException("Unsupported type: " + type);
        };
    }

    private Function setCurrentFunction(Function function) {
        var old = currentFunction;
        currentFunction = function;
        return old;
    }

    private BasicBlock setCurrentBasicBlock(BasicBlock block) {
        var old = currentBlock;
        currentBlock = block;

        if (block.getParent() != currentFunction) {
            if (currentBlock.getParent() != null) {
                currentBlock.getParent().removeBasicBlock(currentBlock);
            }
            currentFunction.insertBasicBlock(block);
        }

        return old;
    }

    private Instruction insertInstruction(Instruction instruction) {
        currentBlock.insertInstruction(instruction);
        return instruction;
    }

    private BasicBlock newBasicBlock() {
        return new BasicBlock(module.getContext());
    }

    /*
     * ==================== Parsing functions ====================
     */
    private void parseGlobalDecl(SyntaxNode node) {
        var child = node.getFirstChild();
        if (child.is(SyntaxTypes.VAR_DECL)) {
            for (var it = child.getFirstChild(); it != null; it = it.getNextSibling()) {
                if (it.is(SyntaxTypes.VAR_DEF)) {
                    parseGlobalVarDef(it);
                }
            }
        } else if (child.is(SyntaxTypes.CONST_DECL)) {
            for (var it = child.getFirstChild(); it != null; it = it.getNextSibling()) {
                if (it.is(SyntaxTypes.CONST_DEF)) {
                    parseGlobalConstantDef(it);
                }
            }
        } else {
            throw new IllegalStateException("Unexpected node type: " + child.getType());
        }
    }

    private GlobalVariable parseGlobalVarDef(SyntaxNode node) {
        String name = node.getFirstChild().getToken().lexeme;
        var entry = getSymbolTableBlock(node).findEntry(name);

        var type = getEntryType(entry);
        GlobalVariable value;

        if (node.getLastChild().is(SyntaxTypes.INIT_VAL)) {
            var initValue = parseGlobalInitValue(node.getLastChild());
            value = new GlobalVariable(type, false, name, initValue);
        } else {
            value = new GlobalVariable(type, false, name);
        }

        addValue(entry, value);
        module.addGlobalVariable(value);

        return value;
    }

    private GlobalVariable parseGlobalConstantDef(SyntaxNode node) {
        String name = node.getFirstChild().getToken().lexeme;
        var entry = getSymbolTableBlock(node).findEntry(name);
        var type = getEntryType(entry);
        GlobalVariable value;


        if (node.getLastChild().is(SyntaxTypes.CONST_INIT_VAL)) {
            var initValue = parseGlobalInitValue(node.getLastChild());
            value = new GlobalVariable(type, true, name, initValue);
        } else {
            throw new IllegalStateException("Constant must have init value");
        }

        addValue(entry, value);
        module.addGlobalVariable(value);

        return value;
    }

    private ConstantData parseGlobalInitValue(SyntaxNode node) {
        if (!node.getBoolAttribute("det")) {
            throw new IllegalStateException("Global init value must be deterministic");
        }

        int dim = node.getIntAttribute("dim");
        if (dim == 0) {
            return new ConstantData(IntegerType.get(module.getContext(), 32), node.getIntAttribute("value"));
        }

        ArrayList<ConstantData> values = new ArrayList<>();
        for (var it = node.getFirstChild(); it != null; it = it.getNextSibling()) {
            if (it.is(SyntaxTypes.CONST_INIT_VAL, SyntaxTypes.INIT_VAL)) {
                values.add(parseGlobalInitValue(it));
            }
        }
        return new ConstantData(values);
    }

    private void parseVariableDecl(SyntaxNode node) {
        if (!node.is(SyntaxTypes.VAR_DECL, SyntaxTypes.CONST_DECL)) {
            throw new IllegalStateException("Unexpected node type for variable decl: " + node.getType());
        }

        for (var it = node.getFirstChild(); it != null; it = it.getNextSibling()) {
            if (it.is(SyntaxTypes.VAR_DEF, SyntaxTypes.CONST_DEF)) {
                if (it.getIntAttribute("dim") == 0) {
                    parseVariableDef(it);
                } else {
                    parseArrayDef(it);
                }
            }
        }
    }

    private AllocaInst parseVariableDef(SyntaxNode node) {
        String name = node.getFirstChild().getToken().lexeme;
        var entry = getSymbolTableBlock(node).findEntry(name);
        var type = getEntryType(entry);

        AllocaInst address = new AllocaInst(type);
        insertInstruction(address);

        addValue(entry, address);

        if (node.getLastChild().is(SyntaxTypes.INIT_VAL, SyntaxTypes.CONST_INIT_VAL)) {
            var value = parseExpression(node.getLastChild().getFirstChild());
            if (value.getType().isIntegerTy()) {
                value = ensureInt32(value);
            }
            insertInstruction(new StoreInst(value, address));
        }

        return address;
    }

    private AllocaInst parseArrayDef(SyntaxNode node) {
        String name = node.getFirstChild().getToken().lexeme;
        var entry = getSymbolTableBlock(node).findEntry(name);
        var type = getEntryType(entry);

        AllocaInst array = new AllocaInst(type);
        insertInstruction(array);

        addValue(entry, array);

        if (!node.getLastChild().is(SyntaxTypes.INIT_VAL, SyntaxTypes.CONST_INIT_VAL)) {
            return array;
        }

        var expNodes = AstExt.getChildNodes(node.getLastChild(), SyntaxTypes.EXP, SyntaxTypes.CONST_EXP);
        initArray(array, expNodes, 0);

        return array;
    }

    private void initArray(Value base, List<SyntaxNode> initValues, int offset) {
        Type arrayType = base.getPointerType().getElementType();
        if (!arrayType.isArrayTy()) {
            throw new IllegalStateException("Base is not array pointer");
        }
        ArrayType type = ((ArrayType) arrayType);

        if (type.getElementType().isIntegerTy()) {
            int size = type.getElementCount();
            var inst = GetElementPtrInst.create(base, List.of(
                    new ConstantData(IntegerType.get(module.getContext(), 64), 0),
                    new ConstantData(IntegerType.get(module.getContext(), 64), 0)));
            insertInstruction(inst);
            storeArrayInit(inst, size, initValues, offset);
        } else {
            var inst = GetElementPtrInst.create(base, List.of(
                    new ConstantData(IntegerType.get(module.getContext(), 64), 0),
                    new ConstantData(IntegerType.get(module.getContext(), 64), 0)));
            insertInstruction(inst);
            initArray(inst, initValues, offset);
            int size = ((ArrayType) type.getElementType()).getSize();
            for (int i = 1; i < type.getElementCount(); i++) {
                inst = GetElementPtrInst.create(inst, List.of(new ConstantData(IntegerType.get(module.getContext(), 64), 1)));
                insertInstruction(inst);
                initArray(inst, initValues, offset + i * size);
            }
        }
    }

    private void storeArrayInit(Value base, int size, List<SyntaxNode> initValues, int offset) {
        var value = ensureInt32(parseExpression(initValues.get(offset)));
        insertInstruction(new StoreInst(value, base));
        Instruction inst = (Instruction) base;
        for (int i = 1; i < size; i++) {
            inst = GetElementPtrInst.create(inst, List.of(new ConstantData(IntegerType.get(module.getContext(), 64), 1)));
            insertInstruction(inst);
            value = ensureInt32(parseExpression(initValues.get(offset + i)));
            insertInstruction(new StoreInst(value, inst));
        }
    }

    private void parseReturnStmt(SyntaxNode node) {
        var exp = AstExt.getChildNode(node, SyntaxTypes.EXP);
        if (exp == null) {
            insertInstruction(new JumpInst(currentFunction.getReturnBlock(), true));
        } else {
            var value = parseExpression(exp);
            if (!value.getIntegerType().isInteger()) {
                value = insertInstruction(ZExtInst.toInt32(value));
            }
            insertInstruction(new StoreInst(value, currentFunction.getReturnValue()));
            insertInstruction(new JumpInst(currentFunction.getReturnBlock(), true));
        }
    }

    private void parseAssignStmt(SyntaxNode node) {
        var lVal = AstExt.getDirectChildNode(node, SyntaxTypes.LVAL);
//        var address = getLValValue(lVal);
        var address = parseLVal(lVal);

        var exp = AstExt.getDirectChildNode(node, SyntaxTypes.EXP);
        var value = parseExpression(exp);

        if (value.getIntegerType().isBoolean()) {
            value = insertInstruction(ZExtInst.toInt32(value));
        }

        insertInstruction(new StoreInst(value, address));
    }

    private Value parseExpression(SyntaxNode node) {
        if (node.is(SyntaxTypes.TERMINATOR)) {
            return null;
        }

        var context = module.getContext();

        if (node.getBoolAttribute("det")) {
            int value = node.getIntAttribute("value");
            var type = IntegerType.get(context, 32);
            return new ConstantData(type, value);
        }

        return parseAddExp(node.getFirstChild());
    }

    private Value parseAddExp(SyntaxNode node) {
        var context = module.getContext();

        if (node.getBoolAttribute("det")) {
            int value = node.getIntAttribute("value");
            var type = IntegerType.get(context, 32);
            return new ConstantData(type, value);
        }

        if (node.hasManyChildren()) {
            var lhs = ensureInt32(parseAddExp(node.getFirstChild()));
            var op = node.childAt(1).getToken().lexeme;
            var rhs = ensureInt32(parseMulExp(node.getLastChild()));
            return switch (op) {
                case "+" -> insertInstruction(new BinaryOperator(lhs, rhs, BinaryOperator.BinaryOpTypes.Add));
                case "-" -> insertInstruction(new BinaryOperator(lhs, rhs, BinaryOperator.BinaryOpTypes.Sub));
                default -> throw new IllegalStateException("Unexpected operator: " + op);
            };
        }

        return parseMulExp(node.getFirstChild());
    }

    private Value parseMulExp(SyntaxNode node) {
        var context = module.getContext();

        if (node.getBoolAttribute("det")) {
            int value = node.getIntAttribute("value");
            var type = IntegerType.get(context, 32);
            return new ConstantData(type, value);
        }

        if (node.hasManyChildren()) {
            var lhs = ensureInt32(parseMulExp(node.getFirstChild()));
            var op = node.childAt(1).getToken().lexeme;
            var rhs = ensureInt32(parseUnaryExp(node.getLastChild()));
            return switch (op) {
                case "*" -> insertInstruction(new BinaryOperator(lhs, rhs, BinaryOperator.BinaryOpTypes.Mul));
                case "/" -> insertInstruction(new BinaryOperator(lhs, rhs, BinaryOperator.BinaryOpTypes.Div));
                case "%" -> insertInstruction(new BinaryOperator(lhs, rhs, BinaryOperator.BinaryOpTypes.Mod));
                default -> throw new IllegalStateException("Unexpected operator: " + op);
            };
        }

        return parseUnaryExp(node.getFirstChild());
    }

    private Value parseUnaryExp(SyntaxNode node) {
        if (node.getFirstChild().is(SyntaxTypes.PRIMARY_EXP)) {
            return parsePrimaryExp(node.getFirstChild());
        } else if (node.getFirstChild().is(SyntaxTypes.FUNC_CALL)) {
            return parseFunctionCall(node.getFirstChild());
        }

        String op = node.getFirstChild().getAttribute("op");
        switch (op) {
            case "+" -> {
                return parseUnaryExp(node.getLastChild());
            }
            case "-" -> {
                var operand = parseUnaryExp(node.getLastChild());
                if (operand.getIntegerType().isBoolean()) {
                    var extInst = ZExtInst.toInt32(operand);
                    insertInstruction(extInst);
                    return insertInstruction(new UnaryOperator(extInst, UnaryOperator.UnaryOpTypes.Neg));
                } else {
                    return insertInstruction(new UnaryOperator(operand, UnaryOperator.UnaryOpTypes.Neg));
                }
            }
            case "!" -> {
                var operand = parseUnaryExp(node.getLastChild());
                if (operand.getIntegerType().isBoolean()) {
                    return insertInstruction(new UnaryOperator(operand, UnaryOperator.UnaryOpTypes.Not));
                } else {
                    // This will generate one xor less than standard LLVM.
                    return insertInstruction(new CompInst(operand, CompInst.CompOpTypes.Eq));
                }
            }
            default -> throw new IllegalStateException("Unexpected operator: " + op);
        }
    }

    private Value parsePrimaryExp(SyntaxNode node) {
        if (node.hasManyChildren()) {
            return parseExpression(node.childAt(1));
        }
        if (node.getFirstChild().is(SyntaxTypes.LVAL)) {
            return parseRVal(node.getFirstChild());
        }
        if (node.getFirstChild().is(SyntaxTypes.NUMBER)) {
            return parseNumber(node.getFirstChild());
        }

        throw new IllegalStateException("Unexpected node type: " + node.getFirstChild().getType());
    }

    private Value parseFunctionCall(SyntaxNode node) {
        Function function = getFunction(node);

        var params = AstExt.getChildNode(node, SyntaxTypes.FUNC_APARAMS);
        if (params != null && params.hasChildren()) {
            var paramNodes = AstExt.getChildNodes(params, SyntaxTypes.FUNC_APARAM);
            Collections.reverse(paramNodes);
            ArrayList<Value> paramValues = new ArrayList<>();
            for (var paramNode : paramNodes) {
                paramValues.add(0, parseExpression(paramNode.getFirstChild()));
            }
            return insertInstruction(new CallInst(function, paramValues));
        }

        return insertInstruction(new CallInst(function));
    }

    private Value parseRVal(SyntaxNode node) {
        var rawEntry = getLValEntry(node);
        int dim;
        if (rawEntry instanceof VariableEntry entry) {
            dim = entry.getDimension();
        } else if (rawEntry instanceof ConstantEntry entry) {
            dim = entry.getDimension();
        } else {
            throw new IllegalStateException("Unexpected entry type: " + rawEntry.getClass());
        }

        if (dim == 0) {
            return insertInstruction(new LoadInst(getLValValue(node)));
        }

        var lVal = getLValValue(node);
        var indexNodes = AstExt.getChildNodes(node, SyntaxTypes.EXP, SyntaxTypes.CONST_EXP);
        if (indexNodes.isEmpty()) {
            if (isDoublePointer(lVal)) {
                return insertInstruction(new LoadInst(lVal));
            } else {
                return insertInstruction(GetElementPtrInst.create(lVal,
                        List.of(
                                new ConstantData(IntegerType.get(module.getContext(), 32), 0),
                                new ConstantData(IntegerType.get(module.getContext(), 32), 0)
                        )));
            }
        }

        var indices = new ArrayList<Value>();
        Value inst = lVal;
        boolean doublePointer = isDoublePointer(inst);

        if (doublePointer) {
            inst = insertInstruction(new LoadInst(inst));
            indices.add(ensureInt32(parseExpression(indexNodes.get(0))));
            inst = insertInstruction(GetElementPtrInst.create(inst, indices));
        }

        for (
                int i = doublePointer ? 1 : 0; i < indexNodes.size(); i++) {
            indices.clear();
            indices.add(new ConstantData(IntegerType.get(module.getContext(), 32), 0));
            indices.add(ensureInt32(parseExpression(indexNodes.get(i))));
            inst = insertInstruction(GetElementPtrInst.create(inst, indices));
        }

        int i = indexNodes.size();
        while (i < dim) {
            indices.clear();
            indices.add(new ConstantData(IntegerType.get(module.getContext(), 32), 0));
            indices.add(new ConstantData(IntegerType.get(module.getContext(), 32), 0));
            inst = insertInstruction(GetElementPtrInst.create(inst, indices));
            i++;
        }

        /*
         * It is not wise to rely on AST attribute... but leave it here.
         */
        if (node.getIntAttribute("dim") == 0) {
            return insertInstruction(new LoadInst(inst));
        } else {
            return inst;
        }
    }

    private Value parseLVal(SyntaxNode node) {
        var rawEntry = getLValEntry(node);
        int dim;
        if (rawEntry instanceof VariableEntry entry) {
            dim = entry.getDimension();
        } else if (rawEntry instanceof ConstantEntry entry) {
            dim = entry.getDimension();
        } else {
            throw new IllegalStateException("Unexpected entry type: " + rawEntry.getClass());
        }

        if (dim == 0) {
            return getLValValue(node);
        }

        var lVal = getLValValue(node);
        var indexNodes = AstExt.getChildNodes(node, SyntaxTypes.EXP, SyntaxTypes.CONST_EXP);
        if (indexNodes.isEmpty()) {
            return insertInstruction(GetElementPtrInst.create(lVal,
                    List.of(
                            new ConstantData(IntegerType.get(module.getContext(), 32), 0),
                            new ConstantData(IntegerType.get(module.getContext(), 32), 0)
                    )));
        }

        var indices = new ArrayList<Value>();
        Value inst = lVal;
        boolean doublePointer = isDoublePointer(inst);

        if (doublePointer) {
            inst = insertInstruction(new LoadInst(inst));
            indices.add(ensureInt32(parseExpression(indexNodes.get(0))));
            inst = insertInstruction(GetElementPtrInst.create(inst, indices));
        }

        for (int i = doublePointer ? 1 : 0; i < indexNodes.size(); i++) {
            indices.clear();
            indices.add(new ConstantData(IntegerType.get(module.getContext(), 32), 0));
            indices.add(ensureInt32(parseExpression(indexNodes.get(i))));
            inst = insertInstruction(GetElementPtrInst.create(inst, indices));
        }

        int i = indexNodes.size();
        while (i < dim) {
            indices.clear();
            indices.add(new ConstantData(IntegerType.get(module.getContext(), 32), 0));
            indices.add(new ConstantData(IntegerType.get(module.getContext(), 32), 0));
            inst = insertInstruction(GetElementPtrInst.create(inst, indices));
            i++;
        }

        return inst;
    }

    private boolean isDoublePointer(Value value) {
        return value.getType().isPointerTy() && ((PointerType) value.getType()).getElementType().isPointerTy();
    }

    private Value parseNumber(SyntaxNode node) {
        if (!node.getBoolAttribute("det")) {
            throw new IllegalStateException("Number must be deterministic");
        }

        return new ConstantData(IntegerType.get(module.getContext(), 32), node.getIntAttribute("value"));
    }

    private void parseInputStmt(SyntaxNode node) {
        var value = new InputInst(module.getContext());
        insertInstruction(value);

        var address = parseLVal(node.getFirstChild());
        insertInstruction(new StoreInst(value, address));
    }

    private void parseOutputStmt(SyntaxNode node) {
        var context = module.getContext();
        var format = node.childAt(2).getToken().lexeme;
        int paramNo = 0;

        // First, parse all parameters.
        var expNodes = AstExt.getDirectChildNodes(node, SyntaxTypes.EXP);
        Collections.reverse(expNodes);
        ArrayList<Value> expValues = new ArrayList<>();
        for (var expNode : expNodes) {
            expValues.add(0, ensureInt32(parseExpression(expNode)));
        }

        format = format.substring(1, format.length() - 1);
        for (var str : format.split("(?<=%d)|(?=%d)")) {
            if (str.equals("%d")) {
                insertInstruction(new OutputInst(expValues.get(paramNo++)));
            } else {
                var value = GlobalString.getInstance(context, str);
                module.addGlobalString(value);
                insertInstruction(new OutputInst(value));
            }
        }
    }

    private Value parseEqExp(SyntaxNode node) {
        if (node.getBoolAttribute("det")) {
            int value = node.getIntAttribute("value");
            var type = IntegerType.get(module.getContext(), 32);
            return new ConstantData(type, value);
        }

        if (node.hasOnlyOneChild()) {
            return parseRelExp(node.getFirstChild());
        }

        var lhs = parseEqExp(node.getFirstChild());
        var op = node.childAt(1).getToken().lexeme;
        var rhs = parseRelExp(node.getLastChild());
        lhs = ensureBitWidth(lhs, lhs, rhs);
        rhs = ensureBitWidth(rhs, lhs, rhs);
        return switch (op) {
            case "==" -> insertInstruction(new CompInst(lhs, rhs, CompInst.CompOpTypes.Eq));
            case "!=" -> insertInstruction(new CompInst(lhs, rhs, CompInst.CompOpTypes.Ne));
            default -> throw new IllegalStateException("Unexpected operator: " + op);
        };
    }

    private Value parseRelExp(SyntaxNode node) {
        if (node.getBoolAttribute("det")) {
            int value = node.getIntAttribute("value");
            var type = IntegerType.get(module.getContext(), 32);
            return new ConstantData(type, value);
        }

        if (node.hasOnlyOneChild()) {
            return parseAddExp(node.getFirstChild());
        }

        var lhs = parseRelExp(node.getFirstChild());
        var op = node.childAt(1).getToken().lexeme;
        var rhs = parseAddExp(node.getLastChild());
        lhs = ensureBitWidth(lhs, lhs, rhs);
        rhs = ensureBitWidth(rhs, lhs, rhs);
        return switch (op) {
            case "<" -> insertInstruction(new CompInst(lhs, rhs, CompInst.CompOpTypes.Slt));
            case "<=" -> insertInstruction(new CompInst(lhs, rhs, CompInst.CompOpTypes.Sle));
            case ">" -> insertInstruction(new CompInst(lhs, rhs, CompInst.CompOpTypes.Sgt));
            case ">=" -> insertInstruction(new CompInst(lhs, rhs, CompInst.CompOpTypes.Sge));
            default -> throw new IllegalStateException("Unexpected operator: " + op);
        };
    }

    private void parseIfStmt(SyntaxNode node) {
        // Prepare blocks
        BasicBlock thenBlock = newBasicBlock();
        BasicBlock finalBlock = newBasicBlock();
        BasicBlock elseBlock;
        if (AstExt.countDirectTerminalNode(node, TokenTypes.ELSE) == 0) {
            elseBlock = finalBlock;
        } else {
            elseBlock = newBasicBlock();
        }

        // Parse condition
        parseCond(AstExt.getChildNode(node, SyntaxTypes.COND), thenBlock, elseBlock, currentBlock);

        // Parse true block
        setCurrentBasicBlock(thenBlock);
        AstExt.getDirectChildNode(node, SyntaxTypes.STMT).accept(this);
        // Jump to final block
        // Use currentBlock since it might be changed by nested if!
        insertInstruction(new JumpInst(finalBlock));


        // Parse else block
        if (elseBlock != finalBlock) {
            setCurrentBasicBlock(elseBlock);
            AstExt.getDirectChildNode(node, SyntaxTypes.STMT, 2).accept(this);
            insertInstruction(new JumpInst(finalBlock));
        }


        // Set final block
        setCurrentBasicBlock(finalBlock);
    }

    private void parseCond(SyntaxNode node, BasicBlock trueBlock, BasicBlock falseBlock, BasicBlock nextBlock) {
        parseOrExp(node.getFirstChild(), trueBlock, falseBlock, nextBlock);
    }

    private void parseOrExp(SyntaxNode node, BasicBlock trueBlock, BasicBlock falseBlock, BasicBlock nextBlock) {
        if (node.getBoolAttribute("det")) {
            if (nextBlock == null) {
                nextBlock = newBasicBlock();
            }
            setCurrentBasicBlock(nextBlock);
            int value = node.getIntAttribute("value");
            if (value != 0) {
                insertInstruction(new JumpInst(trueBlock));
            } else {
                insertInstruction(new JumpInst(falseBlock));
            }
            return;
        }

        if (node.hasOnlyOneChild()) {
            parseAndExp(node.getFirstChild(), trueBlock, falseBlock, nextBlock);
            return;
        }

        BasicBlock rightBranch = newBasicBlock();
        parseOrExp(node.getFirstChild(), trueBlock, rightBranch, nextBlock);
        parseAndExp(node.getLastChild(), trueBlock, falseBlock, rightBranch);
    }

    private void parseAndExp(SyntaxNode node, BasicBlock trueBlock, BasicBlock falseBlock, BasicBlock nextBlock) {
        if (node.getBoolAttribute("det")) {
            if (nextBlock == null) {
                nextBlock = currentFunction.newBasicBlock();
            }
            setCurrentBasicBlock(nextBlock);
            int value = node.getIntAttribute("value");
            if (value != 0) {
                insertInstruction(new JumpInst(trueBlock));
            } else {
                insertInstruction(new JumpInst(falseBlock));
            }
            return;
        }

        if (node.hasOnlyOneChild()) {
            if (nextBlock == null) {
                nextBlock = newBasicBlock();
            }

            setCurrentBasicBlock(nextBlock);
            var value = parseEqExp(node.getFirstChild());
            if (!value.getIntegerType().isBoolean()) {
                value = insertInstruction(new CompInst(value, CompInst.CompOpTypes.Ne));
            }
            insertInstruction(new BranchInst(value, trueBlock, falseBlock));
            return;
        }

        BasicBlock rightBranch = newBasicBlock();
        parseAndExp(node.getFirstChild(), rightBranch, falseBlock, nextBlock);

        setCurrentBasicBlock(rightBranch);
        var value = parseEqExp(node.getLastChild());
        if (!value.getIntegerType().isBoolean()) {
            value = insertInstruction(new CompInst(value, CompInst.CompOpTypes.Ne));
        }
        insertInstruction(new BranchInst(value, trueBlock, falseBlock));
    }

    private void parseForStmt(SyntaxNode node) {
        BasicBlock entryBlock = newBasicBlock();
        BasicBlock bodyBlock = newBasicBlock();
        BasicBlock stepBlock = newBasicBlock();
        BasicBlock elseBlock = newBasicBlock();

        // ForInit
        var initNode = AstExt.getDirectChildNode(node, SyntaxTypes.FOR_INIT_STMT);
        if (initNode != null) {
            parseAssignStmt(initNode);
        }
        currentBlock.insertInstruction(new JumpInst(entryBlock));

        // Condition
        var condNode = AstExt.getDirectChildNode(node, SyntaxTypes.COND);
        if (condNode != null) {
            parseCond(condNode, bodyBlock, elseBlock, entryBlock);
        } else {
            // We still have to add entry block.
            setCurrentBasicBlock(entryBlock);
            currentBlock.insertInstruction(new JumpInst(bodyBlock));
        }

        // Set current context for body.
        forCtxStack.push(new ForContext(entryBlock, stepBlock, elseBlock));

        // Body
        setCurrentBasicBlock(bodyBlock);
        AstExt.getDirectChildNode(node, SyntaxTypes.STMT).accept(this);
        insertInstruction(new JumpInst(stepBlock));

        // Pop context
        forCtxStack.pop();

        // Step
        setCurrentBasicBlock(stepBlock);
        var stepNode = AstExt.getDirectChildNode(node, SyntaxTypes.FOR_STEP_STMT);
        if (stepNode != null) {
            parseAssignStmt(stepNode);
        }
        stepBlock.insertInstruction(new JumpInst(entryBlock));

        // Else
        setCurrentBasicBlock(elseBlock);
    }

    private void parseBreakStmt(SyntaxNode node) {
        var context = forCtxStack.peek();
        insertInstruction(new JumpInst(context.elseBlock()));
    }

    private void parseContinueStmt(SyntaxNode node) {
        var context = forCtxStack.peek();
        insertInstruction(new JumpInst(context.stepBlock()));
    }

    /*
     * ==================== Utility functions ====================
     */
    private Value ensureInt32(Value value) {
        // If it is a pointer, it will not be converted.
        if (value.getType().isPointerTy()) {
            return value;
        }

        if (!value.getIntegerType().isInteger()) {
            var extInst = ZExtInst.toInt32(value);
            insertInstruction(extInst);
            return extInst;
        } else {
            return value;
        }
    }

    private Value ensureBitWidth(Value value, Value lhs, Value rhs) {
        int maxBitWidth = Math.max(lhs.getIntegerType().getBitWidth(), rhs.getIntegerType().getBitWidth());
        if (value.getIntegerType().getBitWidth() != maxBitWidth) {
            var extInst = ZExtInst.newInstance(value, maxBitWidth);
            insertInstruction(extInst);
            return extInst;
        } else {
            return value;
        }
    }

    /**
     * Record for context for break and continue.
     *
     * @param entryBlock The entry block of the loop.
     * @param stepBlock  The step block of the loop.
     * @param elseBlock  The else block of the loop.
     */
    private record ForContext(BasicBlock entryBlock, BasicBlock stepBlock, BasicBlock elseBlock) {}
}
