package tomic.llvm.asm.impl;

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

import java.util.ArrayList;
import java.util.Map;

public class StandardAsmGenerator implements IAsmGenerator, IAstVisitor {
    private SyntaxTree syntaxTree;
    private SymbolTable symbolTable;
    private Module module;

    private Function currentFunction;
    private BasicBlock currentBlock;

    private Map<SymbolTableEntry, Value> valueMap;

    @Override
    public Module generate(SyntaxTree syntaxTree, SymbolTable symbolTable, String name) {
        this.syntaxTree = syntaxTree;
        this.symbolTable = symbolTable;

        this.module = new Module(name);

        if (!parseCompilationUnit()) {
            return null;
        }

        return module;
    }

    @Override
    public boolean visitEnter(SyntaxNode node) {
        if (node.is(SyntaxTypes.BLOCK_ITEM)) {
            return parseInstructions(node);
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

        Function function = new Function(IntegerType.get(context, 32), "main");
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
        Function function = new Function(returnType, name, args);
        var body = initFunctionParams(function, block);

        setCurrentFunction(function);
        setCurrentBasicBlock(body);

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

    private boolean parseInstructions(SyntaxNode node) {
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
        return null;
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
            currentFunction.insertBasicBlock(block);
        }

        return old;
    }

    private Instruction insertInstruction(Instruction instruction) {
        currentBlock.insertInstruction(instruction);
        return instruction;
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

        if (node.getLastChild().is(SyntaxTypes.INIT_VAL, SyntaxTypes.CONST_INIT_VAL)) {
            var value = parseExpression(node.getLastChild().getFirstChild());
            insertInstruction(new StoreInst(value, address));
        }

        addValue(entry, address);

        return address;
    }

    private AllocaInst parseArrayDef(SyntaxNode node) {
        throw new IllegalStateException("Not implemented");
    }

    private ReturnInst parseReturnStmt(SyntaxNode node) {
        var context = module.getContext();
        ReturnInst inst;

        var exp = AstExt.getChildNode(node, SyntaxTypes.EXP);
        if (exp == null) {
            inst = new ReturnInst(context);
        } else {
            var value = parseExpression(exp);
            inst = new ReturnInst(value);
        }
        insertInstruction(inst);
        return inst;
    }

    private void parseAssignStmt(SyntaxNode node) {
        var address = getLValValue(node.getFirstChild());
        var value = parseExpression(node.getLastChild().getPrevSibling());

        insertInstruction(new StoreInst(value, address));
    }

    private Value parseExpression(SyntaxNode node) {
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
            var lhs = parseAddExp(node.getFirstChild());
            var op = node.childAt(1).getToken().lexeme;
            var rhs = parseMulExp(node.getLastChild());
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
            var lhs = parseMulExp(node.getFirstChild());
            var op = node.childAt(1).getToken().lexeme;
            var rhs = parseUnaryExp(node.getLastChild());
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

        return switch (node.getFirstChild().getAttribute("op")) {
            case "+" -> parseUnaryExp(node.getLastChild());
            case "-" ->
                    insertInstruction(new UnaryOperator(parseUnaryExp(node.getLastChild()), UnaryOperator.UnaryOpTypes.Neg));
            case "!" -> throw new IllegalStateException("Not implemented");
            default ->
                    throw new IllegalStateException("Unexpected operator: " + node.getFirstChild().getAttribute("op"));
        };
    }

    private Value parsePrimaryExp(SyntaxNode node) {
        if (node.hasManyChildren()) {
            return parseExpression(node.childAt(1));
        }
        if (node.getFirstChild().is(SyntaxTypes.LVAL)) {
            return parseLVal(node.getFirstChild());
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
            ArrayList<Value> parameters = new ArrayList<>();
            for (var it = params.getFirstChild(); it != null; it = it.getNextSibling()) {
                if (it.is(SyntaxTypes.FUNC_APARAM)) {
                    parameters.add(parseExpression(it.getFirstChild()));
                }
            }
            return insertInstruction(new CallInst(function, parameters));
        }

        return insertInstruction(new CallInst(function));
    }

    private Value parseLVal(SyntaxNode node) {
        if (node.getIntAttribute("dim") != 0) {
            throw new IllegalStateException("Array not supported");
        }

        return insertInstruction(new LoadInst(getLValValue(node)));
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

        var address = getLValValue(node.getFirstChild());
        insertInstruction(new StoreInst(value, address));
    }

    private void parseOutputStmt(SyntaxNode node) {
        var context = module.getContext();
        var format = node.childAt(2).getToken().lexeme;
        int paramNo = 0;

        for (var str : format.split("(?<=%d)|(?=%d)")) {
            if (str.equals("%d")) {
                var exp = AstExt.getDirectChildNode(node, SyntaxTypes.EXP, ++paramNo);
                var value = parseExpression(exp);
                insertInstruction(new OutputInst(value));
            } else {
                var value = GlobalString.getInstance(context, str);
                module.addGlobalString(value);
                insertInstruction(new OutputInst(value));
            }
        }
    }
}
