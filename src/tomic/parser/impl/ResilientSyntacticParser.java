package tomic.parser.impl;

import lib.twio.ITwioReader;
import tomic.lexer.ILexicalParser;
import tomic.lexer.token.ITokenMapper;
import tomic.lexer.token.Token;
import tomic.lexer.token.TokenTypes;
import tomic.logger.debug.IDebugLogger;
import tomic.logger.debug.LogLevel;
import tomic.logger.error.ErrorTypes;
import tomic.logger.error.IErrorLogger;
import tomic.parser.ISyntacticParser;
import tomic.parser.ast.SyntaxNode;
import tomic.parser.ast.SyntaxTree;
import tomic.parser.ast.SyntaxTypes;
import tomic.parser.ast.mapper.ISyntaxMapper;
import tomic.parser.ast.trans.RightRecursiveTransformer;

import java.util.Set;

public class ResilientSyntacticParser implements ISyntacticParser {
    private final ILexicalParser lexicalParser;
    private final ITokenMapper tokenMapper;
    private final ISyntaxMapper syntaxMapper;
    private final IErrorLogger errorLogger;
    private final IDebugLogger debugLogger;

    private SyntaxTree tree;
    private int tryParse;

    public ResilientSyntacticParser(ILexicalParser lexicalParser, ITokenMapper tokenMapper, ISyntaxMapper syntaxMapper, IErrorLogger errorLogger, IDebugLogger debugLogger) {
        this.lexicalParser = lexicalParser;
        this.tokenMapper = tokenMapper;
        this.syntaxMapper = syntaxMapper;
        this.errorLogger = errorLogger;
        this.debugLogger = debugLogger;
    }

    @Override
    public ISyntacticParser setReader(ITwioReader reader) {
        lexicalParser.setReader(reader);
        return this;
    }

    @Override
    public SyntaxTree parse() {
        tree = new SyntaxTree();
        tryParse = 0;

        SyntaxNode compUnit = parseCompUnit();
        if (compUnit == null) {
            debugLogger.fatal("Failed to parse the source code");
            return null;
        }

        tree.setRoot(compUnit);

        return new RightRecursiveTransformer().transform(tree);
    }

    /*
     * ==================== Utility ====================
     */
    private Token getCurrent() {
        var current = lexicalParser.current();
        if (current == null) {
            current = getLookahead();
        }
        return current;
    }

    private Token getNext() {
        return lexicalParser.next();
    }

    private Token getLookahead() {
        return getLookahead(1);
    }

    private Token getLookahead(int n) {
        int i;
        Token token = null;

        for (i = 0; i < n; i++) {
            token = getNext();
            if (token.is(TokenTypes.TERMINATOR)) {
                break;
            }
        }

        for (int j = 0; j < i; j++) {
            lexicalParser.rewind();
        }

        return token;
    }

    private void postParseError(int checkpoint, SyntaxNode node) {
        if (checkpoint >= 0) {
            lexicalParser.rollBack(checkpoint);
        }
        if (node != null) {
            tree.deleteNode(node);
        }
    }

    private void setTryParse(boolean tryParse) {
        if (tryParse) {
            this.tryParse++;
        } else if (this.tryParse > 0) {
            this.tryParse--;
        }
    }

    private boolean isTryParse() {
        return tryParse > 0;
    }

    /*
     * ==================== Logging ====================
     */
    private void log(LogLevel level, Token position, String message) {
        if (isTryParse()) {
            return;
        }

        debugLogger.log(level, String.format("(%d:%d) %s", position.lineNo, position.charNo, message));
    }

    private void log(LogLevel level, String message) {
        log(level, getCurrent(), message);
    }

    private void logFailedToParse(SyntaxTypes type) {
        String descr = syntaxMapper.description(type);
        if (descr == null) {
            descr = "Missing";
        }
        log(LogLevel.ERROR, "Failed to parse " + descr);
    }

    private void logExpect(TokenTypes expected) {
        var actual = getLookahead();

        var descr = tokenMapper.lexeme(expected);
        if (descr == null) {
            descr = tokenMapper.description(expected);
            if (descr == null) {
                descr = "Missing";
            }
        }
        if (actual.is(TokenTypes.TERMINATOR)) {
            log(LogLevel.ERROR, actual, String.format("Expect %s, but got EOF", descr));
        } else {
            log(LogLevel.ERROR, actual, String.format("Expect %s, bug got %s", descr, actual));
        }
    }

    private void logExpectAfter(TokenTypes expected) {
        var cur = getCurrent();
        var descr = tokenMapper.lexeme(expected);
        if (descr == null) {
            descr = "Missing";
        }

        log(LogLevel.ERROR, cur, String.format("Expect %s after %s", descr, cur.lexeme));
    }

    private void recoverFromMissingToken(SyntaxNode node, TokenTypes expcted) {
        ErrorTypes type = switch (expcted) {
            case SEMICOLON -> ErrorTypes.MISSING_SEMICOLON;
            case RIGHT_PARENTHESIS -> ErrorTypes.MISSING_RIGHT_PARENTHESIS;
            case RIGHT_BRACKET -> ErrorTypes.MISSING_RIGHT_BRACKET;
            case RIGHT_BRACE -> ErrorTypes.MISSING_RIGHT_BRACE;
            default -> ErrorTypes.UNKNOWN;
        };

        var cur = getCurrent();
        if (cur != null) {
            errorLogger.log(cur.lineNo, cur.charNo, type, String.format("Missing %s after %s", tokenMapper.lexeme(expcted), cur.lexeme));
        } else {
            errorLogger.log(1, 1, type, String.format("Missing %s at the beginning of file", tokenMapper.lexeme(expcted)));
        }

        node.insertEndChild(tree.newTerminalNode(new Token(expcted)));
    }

    /*
     * ==================== Utility for Parsing ====================
     */

    private static final Set<TokenTypes> VAR_TYPE_FIRST = Set.of(TokenTypes.INT);
    private static final Set<TokenTypes> FUNC_TYPE_FIRST = Set.of(TokenTypes.INT, TokenTypes.VOID);
    private static final Set<TokenTypes> ADD_EXP_AUX_FIRST = Set.of(TokenTypes.PLUS, TokenTypes.MINUS);
    private static final Set<TokenTypes> MUL_EXP_AUX_FIRST = Set.of(TokenTypes.MULTIPLY, TokenTypes.DIVIDE, TokenTypes.MOD);
    private static final Set<TokenTypes> UNARY_OP_FIRST = Set.of(TokenTypes.PLUS, TokenTypes.MINUS, TokenTypes.NOT);
    private static final Set<TokenTypes> OR_EXP_AUX_FIRST = Set.of(TokenTypes.OR);
    private static final Set<TokenTypes> AND_EXP_AUX_FIRST = Set.of(TokenTypes.AND);
    private static final Set<TokenTypes> EQ_EXP_AUX_FIRST = Set.of(TokenTypes.EQUAL, TokenTypes.NOT_EQUAL);
    private static final Set<TokenTypes> REL_EXP_AUX_FIRST = Set.of(TokenTypes.LESS, TokenTypes.LESS_EQUAL, TokenTypes.GREATER, TokenTypes.GREATER_EQUAL);

    private boolean matchDecl() {
        var token = getLookahead();
        if (token.is(TokenTypes.CONST)) {
            return true;
        }

        if (getLookahead().is(VAR_TYPE_FIRST) && getLookahead(2).is(TokenTypes.IDENTIFIER)) {
            return !getLookahead(3).is(TokenTypes.LEFT_PARENTHESIS);
        }

        return false;
    }

    private boolean matchFuncDef() {
        if (!getLookahead().is(FUNC_TYPE_FIRST)) {
            return false;
        }

        return getLookahead(2).is(TokenTypes.IDENTIFIER) && getLookahead(3).is(TokenTypes.LEFT_PARENTHESIS);
    }

    /*
     * ==================== Parsing ====================
     */
    private SyntaxNode parseCompUnit() {
        var root = tree.newNonTerminalNode(SyntaxTypes.COMP_UNIT);
        int checkpoint = lexicalParser.setCheckPoint();

        // Parse Decl
        while (matchDecl()) {
            var decl = parseDecl();
            if (decl == null) {
                logFailedToParse(SyntaxTypes.DECL);
                postParseError(checkpoint, root);
                return null;
            }
            root.insertEndChild(decl);
        }

        // Parse FuncDef
        while (matchFuncDef()) {
            var funcDef = parseFuncDef();
            if (funcDef == null) {
                logFailedToParse(SyntaxTypes.FUNC_DEF);
                postParseError(checkpoint, root);
                return null;
            }
            root.insertEndChild(funcDef);
        }

        var mainFuncDef = parseMainFuncDef();
        if (mainFuncDef == null) {
            logFailedToParse(SyntaxTypes.MAIN_FUNC_DEF);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(mainFuncDef);

        return root;
    }

    private SyntaxNode parseDecl() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.DECL);

        Token lookahead = getLookahead();
        if (lookahead.is(TokenTypes.CONST)) {
            var constDecl = parseConstDecl();
            if (constDecl == null) {
                logFailedToParse(SyntaxTypes.CONST_DECL);
                postParseError(checkpoint, root);
                return null;
            }
            root.insertEndChild(constDecl);
        } else {
            var varDecl = parseVarDecl();
            if (varDecl == null) {
                logFailedToParse(SyntaxTypes.VAR_DECL);
                postParseError(checkpoint, root);
                return null;
            }
            root.insertEndChild(varDecl);
        }

        return root;
    }

    private SyntaxNode parseBType() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.BTYPE);

        SyntaxNode child;
        if (getLookahead().is(VAR_TYPE_FIRST)) {
            child = tree.newTerminalNode(getNext());
        } else {
            logExpect(TokenTypes.INT);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(child);

        return root;
    }

    private SyntaxNode parseConstDecl() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.CONST_DECL);

        // const
        if (!getLookahead().is(TokenTypes.CONST)) {
            logExpect(TokenTypes.CONST);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(tree.newTerminalNode(getNext()));

        // BType
        var type = parseBType();
        if (type == null) {
            logFailedToParse(SyntaxTypes.BTYPE);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(type);

        // ConstDef
        var constDef = parseConstDef();
        if (constDef == null) {
            logFailedToParse(SyntaxTypes.CONST_DEF);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(constDef);

        while (getLookahead().is(TokenTypes.COMMA)) {
            // skip ','
            root.insertEndChild(tree.newTerminalNode(getNext()));

            constDef = parseConstDef();
            if (constDef == null) {
                logFailedToParse(SyntaxTypes.CONST_DEF);
                postParseError(checkpoint, root);
                return null;
            }
            root.insertEndChild(constDef);
        }

        // Check ;
        if (!getLookahead().is(TokenTypes.SEMICOLON)) {
            logExpectAfter(TokenTypes.SEMICOLON);
            recoverFromMissingToken(root, TokenTypes.SEMICOLON);
        } else {
            root.insertEndChild(tree.newTerminalNode(getNext()));
        }

        return root;
    }

    private SyntaxNode parseConstDef() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.CONST_DEF);

        // Ident
        if (!getLookahead().is(TokenTypes.IDENTIFIER)) {
            logExpect(TokenTypes.IDENTIFIER);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(tree.newTerminalNode(getNext()));

        // dimension
        while (getLookahead().is(TokenTypes.LEFT_BRACKET)) {
            // skip '['
            root.insertEndChild(tree.newTerminalNode(getNext()));

            var constExp = parseConstExp();
            if (constExp == null) {
                logFailedToParse(SyntaxTypes.CONST_EXP);
                postParseError(checkpoint, root);
                return null;
            }
            root.insertEndChild(constExp);

            // check ']'
            if (!getLookahead().is(TokenTypes.RIGHT_BRACKET)) {
                logExpectAfter(TokenTypes.RIGHT_BRACKET);
                recoverFromMissingToken(root, TokenTypes.RIGHT_BRACKET);
            } else {
                // Skip ']'
                root.insertEndChild(tree.newTerminalNode(getNext()));
            }
        }

        // check '='
        if (!getLookahead().is(TokenTypes.ASSIGN)) {
            logExpect(TokenTypes.ASSIGN);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(tree.newTerminalNode(getNext()));

        // ConstInitVal
        var constInitVal = parseConstInitVal();
        if (constInitVal == null) {
            logFailedToParse(SyntaxTypes.CONST_INIT_VAL);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(constInitVal);

        return root;
    }

    private SyntaxNode parseConstInitVal() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.CONST_INIT_VAL);

        if (getLookahead().is(TokenTypes.LEFT_BRACE)) {
            // skip '{'
            root.insertEndChild(tree.newTerminalNode(getNext()));

            if (getLookahead().is(TokenTypes.RIGHT_BRACE)) {
                debugLogger.warning("Empty initializer list");
                root.insertEndChild(tree.newTerminalNode(getNext()));
                return root;
            }

            // ConstInitVal
            var constInitVal = parseConstInitVal();
            if (constInitVal == null) {
                logFailedToParse(SyntaxTypes.CONST_INIT_VAL);
                postParseError(checkpoint, root);
                return null;
            }
            root.insertEndChild(constInitVal);

            while (getLookahead().is(TokenTypes.COMMA)) {
                // skip ','
                root.insertEndChild(tree.newTerminalNode(getNext()));

                constInitVal = parseConstInitVal();
                if (constInitVal == null) {
                    logFailedToParse(SyntaxTypes.CONST_INIT_VAL);
                    postParseError(checkpoint, root);
                    return null;
                }
                root.insertEndChild(constInitVal);
            }

            // check '}'
            if (!getLookahead().is(TokenTypes.RIGHT_BRACE)) {
                logExpectAfter(TokenTypes.RIGHT_BRACE);
                recoverFromMissingToken(root, TokenTypes.RIGHT_BRACE);
            } else {
                // skip '}'
                root.insertEndChild(tree.newTerminalNode(getNext()));
            }
        } else {
            var constExp = parseConstExp();
            if (constExp == null) {
                logFailedToParse(SyntaxTypes.CONST_EXP);
                postParseError(checkpoint, root);
                return null;
            }
            root.insertEndChild(constExp);
        }

        return root;
    }

    private SyntaxNode parseVarDecl() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.VAR_DECL);

        // BType
        var type = parseBType();
        if (type == null) {
            logFailedToParse(SyntaxTypes.BTYPE);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(type);

        // VarDef
        var varDef = parseVarDef();
        if (varDef == null) {
            logFailedToParse(SyntaxTypes.VAR_DEF);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(varDef);

        while (getLookahead().is(TokenTypes.COMMA)) {
            // skip ','
            root.insertEndChild(tree.newTerminalNode(getNext()));

            varDef = parseVarDef();
            if (varDef == null) {
                logFailedToParse(SyntaxTypes.VAR_DEF);
                postParseError(checkpoint, root);
                return null;
            }
            root.insertEndChild(varDef);
        }

        // Check ;
        if (!getLookahead().is(TokenTypes.SEMICOLON)) {
            logExpectAfter(TokenTypes.SEMICOLON);
            recoverFromMissingToken(root, TokenTypes.SEMICOLON);
        } else {
            root.insertEndChild(tree.newTerminalNode(getNext()));
        }

        return root;
    }

    private SyntaxNode parseVarDef() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.VAR_DEF);

        // Ident
        if (!getLookahead().is(TokenTypes.IDENTIFIER)) {
            logExpect(TokenTypes.IDENTIFIER);
            postParseError(checkpoint, root);
            return null;
        }
        var ident = tree.newTerminalNode(getNext());
        root.insertEndChild(ident);

        // dimension
        while (getLookahead().is(TokenTypes.LEFT_BRACKET)) {
            // skip '['
            root.insertEndChild(tree.newTerminalNode(getNext()));

            var constExp = parseConstExp();
            if (constExp == null) {
                logFailedToParse(SyntaxTypes.CONST_EXP);
                postParseError(checkpoint, root);
                return null;
            }
            root.insertEndChild(constExp);

            // check ']'
            if (!getLookahead().is(TokenTypes.RIGHT_BRACKET)) {
                logExpectAfter(TokenTypes.RIGHT_BRACKET);
                recoverFromMissingToken(root, TokenTypes.RIGHT_BRACKET);
            } else {
                // Skip ']'
                root.insertEndChild(tree.newTerminalNode(getNext()));
            }
        }

        // VarInit
        if (!getLookahead().is(TokenTypes.ASSIGN)) {
            log(LogLevel.WARNING, "No initial value for variable " + ident.getToken().lexeme);
            return root;
        }
        root.insertEndChild(tree.newTerminalNode(getNext()));

        var initVal = parseInitVal();
        if (initVal == null) {
            logFailedToParse(SyntaxTypes.INIT_VAL);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(initVal);

        return root;
    }

    private SyntaxNode parseInitVal() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.INIT_VAL);

        if (getLookahead().is(TokenTypes.LEFT_BRACE)) {
            // Skip '{'
            root.insertEndChild(tree.newTerminalNode(getNext()));

            // Check if empty
            if (getLookahead().is(TokenTypes.RIGHT_BRACE)) {
                debugLogger.warning("Empty initializer list");
                root.insertEndChild(tree.newTerminalNode(getNext()));
                return root;
            }

            // InitVal
            var initVal = parseInitVal();
            if (initVal == null) {
                logFailedToParse(SyntaxTypes.INIT_VAL);
                postParseError(checkpoint, root);
                return null;
            }
            root.insertEndChild(initVal);

            while (getLookahead().is(TokenTypes.COMMA)) {
                // Skip ','
                root.insertEndChild(tree.newTerminalNode(getNext()));

                initVal = parseInitVal();
                if (initVal == null) {
                    logFailedToParse(SyntaxTypes.INIT_VAL);
                    postParseError(checkpoint, root);
                    return null;
                }
                root.insertEndChild(initVal);
            }

            // Check '}'
            if (!getLookahead().is(TokenTypes.RIGHT_BRACE)) {
                logExpectAfter(TokenTypes.RIGHT_BRACE);
                recoverFromMissingToken(root, TokenTypes.RIGHT_BRACE);
            } else {
                // Skip '}'
                root.insertEndChild(tree.newTerminalNode(getNext()));
            }
        } else {
            var exp = parseExp();
            if (exp == null) {
                logFailedToParse(SyntaxTypes.EXP);
                postParseError(checkpoint, root);
                return null;
            }
            root.insertEndChild(exp);
        }

        return root;
    }

    private SyntaxNode parseFuncDef() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.FUNC_DEF);

        // FuncDecl
        var funcDecl = parseFuncDecl();
        if (funcDecl == null) {
            logFailedToParse(SyntaxTypes.FUNC_DECL);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(funcDecl);

        // Block
        var block = parseBlock();
        if (block == null) {
            logFailedToParse(SyntaxTypes.BLOCK);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(block);

        return root;
    }

    private SyntaxNode parseFuncDecl() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.FUNC_DECL);

        // FuncType
        var funcType = parseFuncType();
        if (funcType == null) {
            logFailedToParse(SyntaxTypes.FUNC_TYPE);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(funcType);

        // Ident
        if (!getLookahead().is(TokenTypes.IDENTIFIER)) {
            logExpect(TokenTypes.IDENTIFIER);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(tree.newTerminalNode(getNext()));

        // '('
        if (!getLookahead().is(TokenTypes.LEFT_PARENTHESIS)) {
            logExpect(TokenTypes.LEFT_PARENTHESIS);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(tree.newTerminalNode(getNext()));

        // FuncFParams
        if (!getLookahead().is(TokenTypes.RIGHT_PARENTHESIS)) {
            var funcFParams = parseFuncFParams();
            if (funcFParams == null) {
                logFailedToParse(SyntaxTypes.FUNC_FPARAMS);
            } else {
                root.insertEndChild(funcFParams);
            }
        }

        // ')'
        if (!getLookahead().is(TokenTypes.RIGHT_PARENTHESIS)) {
            logExpect(TokenTypes.RIGHT_PARENTHESIS);
            recoverFromMissingToken(root, TokenTypes.RIGHT_PARENTHESIS);
        } else {
            root.insertEndChild(tree.newTerminalNode(getNext()));
        }

        return root;
    }

    private SyntaxNode parseFuncType() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.FUNC_TYPE);

        if (getLookahead().is(FUNC_TYPE_FIRST)) {
            root.insertEndChild(tree.newTerminalNode(getNext()));
        } else {
            logExpect(TokenTypes.INT);
            postParseError(checkpoint, root);
            return null;
        }

        return root;
    }

    private SyntaxNode parseFuncFParams() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.FUNC_FPARAMS);

        var funcFParam = parseFuncFParam();
        if (funcFParam == null) {
            logFailedToParse(SyntaxTypes.FUNC_FPARAM);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(funcFParam);

        while (getLookahead().is(TokenTypes.COMMA)) {
            // skip ','
            root.insertEndChild(tree.newTerminalNode(getNext()));

            funcFParam = parseFuncFParam();
            if (funcFParam == null) {
                logFailedToParse(SyntaxTypes.FUNC_FPARAM);
                postParseError(checkpoint, root);
                return null;
            }
            root.insertEndChild(funcFParam);
        }

        return root;
    }

    private SyntaxNode parseFuncFParam() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.FUNC_FPARAM);

        // BType
        var type = parseBType();
        if (type == null) {
            logFailedToParse(SyntaxTypes.BTYPE);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(type);

        // Ident
        if (!getLookahead().is(TokenTypes.IDENTIFIER)) {
            logExpect(TokenTypes.IDENTIFIER);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(tree.newTerminalNode(getNext()));

        // first dimension
        if (getLookahead().is(TokenTypes.LEFT_BRACKET)) {
            // skip '['
            root.insertEndChild(tree.newTerminalNode(getNext()));
            if (getLookahead().is(TokenTypes.RIGHT_BRACKET)) {
                // skip ']'
                root.insertEndChild(tree.newTerminalNode(getNext()));
            } else {
                logExpectAfter(TokenTypes.RIGHT_BRACKET);
                recoverFromMissingToken(root, TokenTypes.RIGHT_BRACKET);
            }

            // second dimension
            if (getLookahead().is(TokenTypes.LEFT_BRACKET)) {
                // skip '['
                root.insertEndChild(tree.newTerminalNode(getNext()));
                var constExp = parseConstExp();
                if (constExp == null) {
                    logFailedToParse(SyntaxTypes.CONST_EXP);
                    postParseError(checkpoint, root);
                    return null;
                }
                root.insertEndChild(constExp);

                // check ']'
                if (!getLookahead().is(TokenTypes.RIGHT_BRACKET)) {
                    logExpectAfter(TokenTypes.RIGHT_BRACKET);
                    recoverFromMissingToken(root, TokenTypes.RIGHT_BRACKET);
                } else {
                    // skip ']'
                    root.insertEndChild(tree.newTerminalNode(getNext()));
                }
            }
        }

        return root;
    }

    private SyntaxNode parseFuncAParams() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.FUNC_APARAMS);

        var param = parseFuncAParam();
        if (param == null) {
            logFailedToParse(SyntaxTypes.FUNC_APARAM);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(param);

        while (getLookahead().is(TokenTypes.COMMA)) {
            // skip ','
            root.insertEndChild(tree.newTerminalNode(getNext()));

            param = parseFuncAParam();
            if (param == null) {
                logFailedToParse(SyntaxTypes.FUNC_APARAM);
                postParseError(checkpoint, root);
                return null;
            }
            root.insertEndChild(param);
        }

        return root;
    }

    private SyntaxNode parseFuncAParam() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.FUNC_APARAM);

        var exp = parseExp();
        if (exp == null) {
            logFailedToParse(SyntaxTypes.EXP);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(exp);

        return root;
    }

    private SyntaxNode parseBlock() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.BLOCK);

        // '{'
        if (!getLookahead().is(TokenTypes.LEFT_BRACE)) {
            logExpect(TokenTypes.LEFT_BRACE);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(tree.newTerminalNode(getNext()));

        // If the block is empty, return directly
        if (getLookahead().is(TokenTypes.RIGHT_BRACE)) {
            // skip '}'
            root.insertEndChild(tree.newTerminalNode(getNext()));
            return root;
        }

        // BlockItem
        while (!getLookahead().is(TokenTypes.RIGHT_BRACE)) {
            var blockItem = parseBlockItem();
            if (blockItem == null) {
                logFailedToParse(SyntaxTypes.BLOCK_ITEM);
                postParseError(checkpoint, root);
                return null;
            }
            root.insertEndChild(blockItem);
        }

        // '}'
        if (!getLookahead().is(TokenTypes.RIGHT_BRACE)) {
            logExpectAfter(TokenTypes.RIGHT_BRACE);
            recoverFromMissingToken(root, TokenTypes.RIGHT_BRACE);
        } else {
            // skip '}'
            root.insertEndChild(tree.newTerminalNode(getNext()));
        }

        return root;
    }

    private SyntaxNode parseBlockItem() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.BLOCK_ITEM);

        Token lookahead = getLookahead();
        if (lookahead.is(TokenTypes.CONST)) {
            var constDecl = parseConstDecl();
            if (constDecl == null) {
                logFailedToParse(SyntaxTypes.CONST_DECL);
                postParseError(checkpoint, root);
                return null;
            }
            root.insertEndChild(constDecl);
        } else if (lookahead.is(TokenTypes.INT)) {
            var varDecl = parseVarDecl();
            if (varDecl == null) {
                logFailedToParse(SyntaxTypes.VAR_DECL);
                postParseError(checkpoint, root);
                return null;
            }
            root.insertEndChild(varDecl);
        } else {
            var stmt = parseStmt();
            if (stmt == null) {
                logFailedToParse(SyntaxTypes.STMT);
                postParseError(checkpoint, root);
                return null;
            }
            root.insertEndChild(stmt);
        }

        return root;
    }

    private SyntaxNode parseMainFuncDef() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.MAIN_FUNC_DEF);

        // 'int'
        if (!getLookahead().is(TokenTypes.INT)) {
            logExpect(TokenTypes.INT);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(tree.newTerminalNode(getNext()));

        // 'main'
        if (!getLookahead().is(TokenTypes.MAIN)) {
            logExpect(TokenTypes.MAIN);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(tree.newTerminalNode(getNext()));

        // '('
        if (!getLookahead().is(TokenTypes.LEFT_PARENTHESIS)) {
            logExpectAfter(TokenTypes.LEFT_PARENTHESIS);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(tree.newTerminalNode(getNext()));

        // ')'
        if (!getLookahead().is(TokenTypes.RIGHT_PARENTHESIS)) {
            logExpectAfter(TokenTypes.RIGHT_PARENTHESIS);
            recoverFromMissingToken(root, TokenTypes.RIGHT_PARENTHESIS);
        } else {
            root.insertEndChild(tree.newTerminalNode(getNext()));
        }

        // Block
        var block = parseBlock();
        if (block == null) {
            logFailedToParse(SyntaxTypes.BLOCK);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(block);

        return root;
    }

    private SyntaxNode parseStmt() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.STMT);
        Token lookahead = getLookahead();

        if (lookahead.is(TokenTypes.IDENTIFIER)) {
            setTryParse(true);
            var stmt = parseStmtAux();
            setTryParse(false);

            if (stmt == null) {
                logFailedToParse(SyntaxTypes.STMT);
                postParseError(checkpoint, root);
                return null;
            }
            root.insertEndChild(stmt);
            return root;
        }

        SyntaxNode child = null;
        if (lookahead.is(TokenTypes.IF)) {
            child = parseIfStmt();
            if (child == null) {
                logFailedToParse(SyntaxTypes.IF_STMT);
            }
        } else if (lookahead.is(TokenTypes.FOR)) {
            child = parseForStmt();
            if (child == null) {
                logFailedToParse(SyntaxTypes.FOR_STMT);
            }
        } else if (lookahead.is(TokenTypes.BREAK)) {
            child = parseBreakStmt();
            if (child == null) {
                logFailedToParse(SyntaxTypes.BREAK_STMT);
            }
        } else if (lookahead.is(TokenTypes.CONTINUE)) {
            child = parseContinueStmt();
            if (child == null) {
                logFailedToParse(SyntaxTypes.CONTINUE_STMT);
            }
        } else if (lookahead.is(TokenTypes.RETURN)) {
            child = parseReturnStmt();
            if (child == null) {
                logFailedToParse(SyntaxTypes.RETURN_STMT);
            }
        } else if (lookahead.is(TokenTypes.PRINTF)) {
            child = parseOutStmt();
            if (child == null) {
                logFailedToParse(SyntaxTypes.OUT_STMT);
            }
        } else if (lookahead.is(TokenTypes.LEFT_BRACE)) {
            child = parseBlock();
            if (child == null) {
                logFailedToParse(SyntaxTypes.BLOCK);
            }
        } else {
            child = parseExpStmt();
            if (child == null) {
                logFailedToParse(SyntaxTypes.EXP_STMT);
            }
        }

        if (child == null) {
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(child);

        return root;
    }

    private SyntaxNode parseStmtAux() {
        var inStmt = parseInStmt();
        if (inStmt != null) {
            return inStmt;
        }

        var assignStmt = parseAssignStmt();
        if (assignStmt != null) {
            return assignStmt;
        }

        var expStmt = parseExpStmt();
        if (expStmt != null) {
            return expStmt;
        }

        log(LogLevel.DEBUG, "Failed to parse StmtAux");
        return null;
    }

    private SyntaxNode parseAssignStmt() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.ASSIGNMENT_STMT);

        // LVal
        var lVal = parseLVal();
        if (lVal == null) {
            logFailedToParse(SyntaxTypes.LVAL);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(lVal);

        // '='
        if (!getLookahead().is(TokenTypes.ASSIGN)) {
            logExpect(TokenTypes.ASSIGN);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(tree.newTerminalNode(getNext()));

        // Exp
        var exp = parseExp();
        if (exp == null) {
            logFailedToParse(SyntaxTypes.EXP);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(exp);

        // ';'
        if (!getLookahead().is(TokenTypes.SEMICOLON)) {
            logExpectAfter(TokenTypes.SEMICOLON);
            recoverFromMissingToken(root, TokenTypes.SEMICOLON);
        } else {
            root.insertEndChild(tree.newTerminalNode(getNext()));
        }

        return root;
    }

    private SyntaxNode parseLVal() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.LVAL);

        // Ident
        if (!getLookahead().is(TokenTypes.IDENTIFIER)) {
            logExpect(TokenTypes.IDENTIFIER);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(tree.newTerminalNode(getNext()));

        // dimension
        while (getLookahead().is(TokenTypes.LEFT_BRACKET)) {
            // skip '['
            root.insertEndChild(tree.newTerminalNode(getNext()));

            var exp = parseExp();
            if (exp == null) {
                logFailedToParse(SyntaxTypes.EXP);
                postParseError(checkpoint, root);
                return null;
            }
            root.insertEndChild(exp);

            // check ']'
            if (!getLookahead().is(TokenTypes.RIGHT_BRACKET)) {
                logExpectAfter(TokenTypes.RIGHT_BRACKET);
                recoverFromMissingToken(root, TokenTypes.RIGHT_BRACKET);
            } else {
                // skip ']'
                root.insertEndChild(tree.newTerminalNode(getNext()));
            }
        }

        return root;
    }

    private SyntaxNode parseCond() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.COND);

        var orExp = parseOrExp();
        if (orExp == null) {
            logFailedToParse(SyntaxTypes.OR_EXP);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(orExp);

        return root;
    }

    private SyntaxNode parseIfStmt() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.IF_STMT);

        // 'if'
        if (!getLookahead().is(TokenTypes.IF)) {
            logExpect(TokenTypes.IF);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(tree.newTerminalNode(getNext()));

        // '('
        if (!getLookahead().is(TokenTypes.LEFT_PARENTHESIS)) {
            logExpectAfter(TokenTypes.LEFT_PARENTHESIS);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(tree.newTerminalNode(getNext()));

        // Cond
        var cond = parseCond();
        if (cond == null) {
            logFailedToParse(SyntaxTypes.COND);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(cond);

        // ')'
        if (!getLookahead().is(TokenTypes.RIGHT_PARENTHESIS)) {
            logExpectAfter(TokenTypes.RIGHT_PARENTHESIS);
            recoverFromMissingToken(root, TokenTypes.RIGHT_PARENTHESIS);
        } else {
            root.insertEndChild(tree.newTerminalNode(getNext()));
        }

        // Stmt
        var stmt = parseStmt();
        if (stmt == null) {
            logFailedToParse(SyntaxTypes.STMT);
            postParseError(checkpoint, root);
            return null;
        }

        // 'else'
        if (getLookahead().is(TokenTypes.ELSE)) {
            // skip 'else'
            root.insertEndChild(tree.newTerminalNode(getNext()));

            // Stmt
            var elseStmt = parseStmt();
            if (elseStmt == null) {
                logFailedToParse(SyntaxTypes.STMT);
                postParseError(checkpoint, root);
                return null;
            }
            root.insertEndChild(elseStmt);
        }

        return root;
    }

    private SyntaxNode parseForStmt() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.FOR_STMT);

        // 'for'
        if (!getLookahead().is(TokenTypes.FOR)) {
            logExpect(TokenTypes.FOR);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(tree.newTerminalNode(getNext()));

        // '('
        if (!getLookahead().is(TokenTypes.LEFT_PARENTHESIS)) {
            logExpectAfter(TokenTypes.LEFT_PARENTHESIS);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(tree.newTerminalNode(getNext()));

        // Check the existence of init part
        if (!getLookahead().is(TokenTypes.SEMICOLON)) {
            var forInitStmt = parseForInitStmt();
            if (forInitStmt == null) {
                logFailedToParse(SyntaxTypes.FOR_INIT_STMT);
                postParseError(checkpoint, root);
                return null;
            }
            root.insertEndChild(forInitStmt);
        }

        // ';'
        if (!getLookahead().is(TokenTypes.SEMICOLON)) {
            logExpectAfter(TokenTypes.SEMICOLON);
            recoverFromMissingToken(root, TokenTypes.SEMICOLON);
        } else {
            root.insertEndChild(tree.newTerminalNode(getNext()));
        }

        // Check the existence of cond part
        if (!getLookahead().is(TokenTypes.SEMICOLON)) {
            var cond = parseCond();
            if (cond == null) {
                logFailedToParse(SyntaxTypes.COND);
                postParseError(checkpoint, root);
                return null;
            }
            root.insertEndChild(cond);
        }

        // ';'
        if (!getLookahead().is(TokenTypes.SEMICOLON)) {
            logExpectAfter(TokenTypes.SEMICOLON);
            recoverFromMissingToken(root, TokenTypes.SEMICOLON);
        } else {
            root.insertEndChild(tree.newTerminalNode(getNext()));
        }

        // Check the existence of step part
        if (!getLookahead().is(TokenTypes.RIGHT_PARENTHESIS)) {
            var forStepStmt = parseForStepStmt();
            if (forStepStmt == null) {
                logFailedToParse(SyntaxTypes.FOR_STEP_STMT);
                postParseError(checkpoint, root);
                return null;
            }
            root.insertEndChild(forStepStmt);
        }

        // ')'
        if (!getLookahead().is(TokenTypes.RIGHT_PARENTHESIS)) {
            logExpectAfter(TokenTypes.RIGHT_PARENTHESIS);
            recoverFromMissingToken(root, TokenTypes.RIGHT_PARENTHESIS);
        } else {
            root.insertEndChild(tree.newTerminalNode(getNext()));
        }

        // Stmt
        var stmt = parseStmt();
        if (stmt == null) {
            logFailedToParse(SyntaxTypes.STMT);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(stmt);

        return root;
    }

    private SyntaxNode parseForInitStmt() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.FOR_INIT_STMT);

        // LVal
        var lVal = parseLVal();
        if (lVal == null) {
            logFailedToParse(SyntaxTypes.LVAL);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(lVal);

        // '='
        if (!getLookahead().is(TokenTypes.ASSIGN)) {
            logExpect(TokenTypes.ASSIGN);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(tree.newTerminalNode(getNext()));

        // Exp
        var exp = parseExp();
        if (exp == null) {
            logFailedToParse(SyntaxTypes.EXP);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(exp);

        return root;
    }

    private SyntaxNode parseForStepStmt() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.FOR_STEP_STMT);

        // LVal
        var lVal = parseLVal();
        if (lVal == null) {
            logFailedToParse(SyntaxTypes.LVAL);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(lVal);

        // '='
        if (!getLookahead().is(TokenTypes.ASSIGN)) {
            logExpect(TokenTypes.ASSIGN);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(tree.newTerminalNode(getNext()));

        // Exp
        var exp = parseExp();
        if (exp == null) {
            logFailedToParse(SyntaxTypes.EXP);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(exp);

        return root;
    }

    private SyntaxNode parseExpStmt() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.EXP_STMT);

        // Check existence of Exp
        if (!getLookahead().is(TokenTypes.SEMICOLON)) {
            var exp = parseExp();
            if (exp == null) {
                lexicalParser.rollBack(checkpoint);
                getNext();
            } else {
                root.insertEndChild(exp);
            }
        }

        // ';'
        if (!getLookahead().is(TokenTypes.SEMICOLON)) {
            logExpectAfter(TokenTypes.SEMICOLON);
            recoverFromMissingToken(root, TokenTypes.SEMICOLON);
        } else {
            root.insertEndChild(tree.newTerminalNode(getNext()));
        }

        return root;
    }

    private SyntaxNode parseBreakStmt() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.BREAK_STMT);

        // 'break'
        if (!getLookahead().is(TokenTypes.BREAK)) {
            logExpect(TokenTypes.BREAK);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(tree.newTerminalNode(getNext()));

        // ';'
        if (!getLookahead().is(TokenTypes.SEMICOLON)) {
            logExpectAfter(TokenTypes.SEMICOLON);
            recoverFromMissingToken(root, TokenTypes.SEMICOLON);
        } else {
            root.insertEndChild(tree.newTerminalNode(getNext()));
        }

        return root;
    }

    private SyntaxNode parseContinueStmt() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.CONTINUE_STMT);

        // 'continue'
        if (!getLookahead().is(TokenTypes.CONTINUE)) {
            logExpect(TokenTypes.CONTINUE);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(tree.newTerminalNode(getNext()));

        // ';'
        if (!getLookahead().is(TokenTypes.SEMICOLON)) {
            logExpectAfter(TokenTypes.SEMICOLON);
            recoverFromMissingToken(root, TokenTypes.SEMICOLON);
        } else {
            root.insertEndChild(tree.newTerminalNode(getNext()));
        }

        return root;
    }

    private SyntaxNode parseReturnStmt() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.RETURN_STMT);

        // 'return'
        if (!getLookahead().is(TokenTypes.RETURN)) {
            logExpect(TokenTypes.RETURN);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(tree.newTerminalNode(getNext()));

        // Check the existence of Exp
        if (!getLookahead().is(TokenTypes.SEMICOLON)) {
            var exp = parseExp();
            if (exp == null) {
                logFailedToParse(SyntaxTypes.EXP);
            } else {
                root.insertEndChild(exp);
            }
        }

        // ';'
        if (!getLookahead().is(TokenTypes.SEMICOLON)) {
            logExpectAfter(TokenTypes.SEMICOLON);
            recoverFromMissingToken(root, TokenTypes.SEMICOLON);
        } else {
            root.insertEndChild(tree.newTerminalNode(getNext()));
        }

        return root;
    }

    private SyntaxNode parseInStmt() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.IN_STMT);

        // LVal
        var lVal = parseLVal();
        if (lVal == null) {
            logFailedToParse(SyntaxTypes.LVAL);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(lVal);

        // '='
        if (!getLookahead().is(TokenTypes.ASSIGN)) {
            logExpect(TokenTypes.ASSIGN);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(tree.newTerminalNode(getNext()));

        // 'getint'
        if (!getLookahead().is(TokenTypes.GETINT)) {
            logExpect(TokenTypes.GETINT);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(tree.newTerminalNode(getNext()));

        // '('
        if (!getLookahead().is(TokenTypes.LEFT_PARENTHESIS)) {
            logExpectAfter(TokenTypes.LEFT_PARENTHESIS);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(tree.newTerminalNode(getNext()));

        // ')'
        if (!getLookahead().is(TokenTypes.RIGHT_PARENTHESIS)) {
            logExpectAfter(TokenTypes.RIGHT_PARENTHESIS);
            recoverFromMissingToken(root, TokenTypes.RIGHT_PARENTHESIS);
        } else {
            root.insertEndChild(tree.newTerminalNode(getNext()));
        }

        // ';'
        if (!getLookahead().is(TokenTypes.SEMICOLON)) {
            logExpectAfter(TokenTypes.SEMICOLON);
            recoverFromMissingToken(root, TokenTypes.SEMICOLON);
        } else {
            root.insertEndChild(tree.newTerminalNode(getNext()));
        }

        return root;
    }

    private SyntaxNode parseOutStmt() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.OUT_STMT);

        // 'printf'
        if (!getLookahead().is(TokenTypes.PRINTF)) {
            logExpect(TokenTypes.PRINTF);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(tree.newTerminalNode(getNext()));

        // '('
        if (!getLookahead().is(TokenTypes.LEFT_PARENTHESIS)) {
            logExpectAfter(TokenTypes.LEFT_PARENTHESIS);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(tree.newTerminalNode(getNext()));

        // FormatString
        if (getLookahead().is(TokenTypes.FORMAT)) {
            root.insertEndChild(tree.newTerminalNode(getNext()));
        } else {
            logExpect(TokenTypes.FORMAT);
        }

        // ',' Exp
        while (getLookahead().is(TokenTypes.COMMA)) {
            // skip ','
            root.insertEndChild(tree.newTerminalNode(getNext()));

            var exp = parseExp();
            if (exp == null) {
                logFailedToParse(SyntaxTypes.EXP);
                postParseError(checkpoint, root);
                return null;
            }
            root.insertEndChild(exp);
        }

        // ')'
        if (!getLookahead().is(TokenTypes.RIGHT_PARENTHESIS)) {
            logExpectAfter(TokenTypes.RIGHT_PARENTHESIS);
            recoverFromMissingToken(root, TokenTypes.RIGHT_PARENTHESIS);
        } else {
            root.insertEndChild(tree.newTerminalNode(getNext()));
        }

        // ';'
        if (!getLookahead().is(TokenTypes.SEMICOLON)) {
            logExpectAfter(TokenTypes.SEMICOLON);
            recoverFromMissingToken(root, TokenTypes.SEMICOLON);
        } else {
            root.insertEndChild(tree.newTerminalNode(getNext()));
        }

        return root;
    }

    private SyntaxNode parseExp() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.EXP);

        var addExp = parseAddExp();
        if (addExp == null) {
            logFailedToParse(SyntaxTypes.ADD_EXP);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(addExp);

        return root;
    }

    private SyntaxNode parseConstExp() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.CONST_EXP);

        var addExp = parseAddExp();
        if (addExp == null) {
            logFailedToParse(SyntaxTypes.ADD_EXP);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(addExp);

        return root;
    }

    private SyntaxNode parseAddExp() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.ADD_EXP);

        var mulExp = parseMulExp();
        if (mulExp == null) {
            logFailedToParse(SyntaxTypes.MUL_EXP);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(mulExp);

        var addExpAux = parseAddExpAux();
        if (addExpAux == null) {
            logFailedToParse(SyntaxTypes.ADD_EXP);
            postParseError(checkpoint, root);
            return null;
        }
        if (!addExpAux.isEpsilon()) {
            root.insertEndChild(addExpAux);
        }

        return root;
    }

    private SyntaxNode parseAddExpAux() {
        if (!getLookahead().is(ADD_EXP_AUX_FIRST)) {
            return tree.newEpsilonNode();
        }

        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.ADD_EXP);

        // '+' or '-'
        root.insertEndChild(tree.newTerminalNode(getNext()));

        var mulExp = parseMulExp();
        if (mulExp == null) {
            logFailedToParse(SyntaxTypes.MUL_EXP);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(mulExp);

        var addExpAux = parseAddExpAux();
        if (addExpAux == null) {
            logFailedToParse(SyntaxTypes.ADD_EXP);
            postParseError(checkpoint, root);
            return null;
        }

        if (!addExpAux.isEpsilon()) {
            root.insertEndChild(addExpAux);
        }

        return root;
    }

    private SyntaxNode parseMulExp() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.MUL_EXP);

        var unaryExp = parseUnaryExp();
        if (unaryExp == null) {
            logFailedToParse(SyntaxTypes.UNARY_EXP);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(unaryExp);

        var mulExpAux = parseMulExpAux();
        if (mulExpAux == null) {
            logFailedToParse(SyntaxTypes.MUL_EXP);
            postParseError(checkpoint, root);
            return null;
        }
        if (!mulExpAux.isEpsilon()) {
            root.insertEndChild(mulExpAux);
        }

        return root;
    }

    private SyntaxNode parseMulExpAux() {
        if (!getLookahead().is(MUL_EXP_AUX_FIRST)) {
            return tree.newEpsilonNode();
        }

        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.MUL_EXP);

        // '*' or '/' or '%'
        root.insertEndChild(tree.newTerminalNode(getNext()));

        var unaryExp = parseUnaryExp();
        if (unaryExp == null) {
            logFailedToParse(SyntaxTypes.UNARY_EXP);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(unaryExp);

        var mulExpAux = parseMulExpAux();
        if (mulExpAux == null) {
            logFailedToParse(SyntaxTypes.MUL_EXP);
            postParseError(checkpoint, root);
            return null;
        }
        if (!mulExpAux.isEpsilon()) {
            root.insertEndChild(mulExpAux);
        }

        return root;
    }

    private SyntaxNode parseUnaryExp() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.UNARY_EXP);

        // UnaryExp -> UnaryOp UnaryExp
        var unaryOp = parseUnaryOp();
        if (unaryOp != null) {
            root.insertEndChild(unaryOp);
            var unaryExp = parseUnaryExp();
            if (unaryExp == null) {
                logFailedToParse(SyntaxTypes.UNARY_EXP);
                postParseError(checkpoint, root);
                return null;
            }
            root.insertEndChild(unaryExp);
            return root;
        }

        // UnaryExp -> Ident '(' FuncAParams ')'
        if (getLookahead().is(TokenTypes.IDENTIFIER) && getLookahead(2).is(TokenTypes.LEFT_PARENTHESIS)) {
            var functionCall = parseFunctionCall();
            if (functionCall == null) {
                logFailedToParse(SyntaxTypes.FUNC_CALL);
                postParseError(checkpoint, root);
                return null;
            }
            root.insertEndChild(functionCall);
            return root;
        }

        // UnaryExp -> PrimaryExp
        var primaryExp = parsePrimaryExp();
        if (primaryExp == null) {
            logFailedToParse(SyntaxTypes.PRIMARY_EXP);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(primaryExp);

        return root;
    }

    private SyntaxNode parseUnaryOp() {
        var root = tree.newNonTerminalNode(SyntaxTypes.UNARY_OP);

        if (getLookahead().is(UNARY_OP_FIRST)) {
            root.insertEndChild(tree.newTerminalNode(getNext()));
        }

        return null;
    }

    private SyntaxNode parsePrimaryExp() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.PRIMARY_EXP);

        // PrimaryExp -> Number
        if (getLookahead().is(TokenTypes.INTEGER)) {
            var number = parseNumber();
            if (number == null) {
                logFailedToParse(SyntaxTypes.NUMBER);
                postParseError(checkpoint, root);
                return null;
            }
            root.insertEndChild(number);
            return root;
        }

        // PrimaryExp -> '(' Exp ')'
        if (getLookahead().is(TokenTypes.LEFT_PARENTHESIS)) {
            // skip '('
            root.insertEndChild(tree.newTerminalNode(getNext()));

            var exp = parseExp();
            if (exp == null) {
                logFailedToParse(SyntaxTypes.EXP);
                postParseError(checkpoint, root);
                return null;
            }
            root.insertEndChild(exp);

            // ')'
            if (!getLookahead().is(TokenTypes.RIGHT_PARENTHESIS)) {
                logExpectAfter(TokenTypes.RIGHT_PARENTHESIS);
                recoverFromMissingToken(root, TokenTypes.RIGHT_PARENTHESIS);
            } else {
                root.insertEndChild(tree.newTerminalNode(getNext()));
            }

            return root;
        }

        // PrimaryExp -> LVal
        var lVal = parseLVal();
        if (lVal != null) {
            root.insertEndChild(lVal);
            return root;
        }

        log(LogLevel.ERROR, "PrimaryExp does not match any production");

        return null;
    }

    private SyntaxNode parseFunctionCall() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.FUNC_CALL);

        // Ident
        if (!getLookahead().is(TokenTypes.IDENTIFIER)) {
            logExpect(TokenTypes.IDENTIFIER);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(tree.newTerminalNode(getNext()));

        // '('
        if (!getLookahead().is(TokenTypes.LEFT_PARENTHESIS)) {
            logExpectAfter(TokenTypes.LEFT_PARENTHESIS);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(tree.newTerminalNode(getNext()));

        // FuncAParams
        if (!getLookahead().is(TokenTypes.RIGHT_PARENTHESIS)) {
            var funcAParams = parseFuncAParams();
            if (funcAParams != null) {
                root.insertEndChild(funcAParams);
            } else {
                logFailedToParse(SyntaxTypes.FUNC_APARAMS);
            }
        }

        // ')'
        if (!getLookahead().is(TokenTypes.RIGHT_PARENTHESIS)) {
            logExpectAfter(TokenTypes.RIGHT_PARENTHESIS);
            recoverFromMissingToken(root, TokenTypes.RIGHT_PARENTHESIS);
        } else {
            root.insertEndChild(tree.newTerminalNode(getNext()));
        }

        return root;
    }

    private SyntaxNode parseNumber() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.NUMBER);

        // Integer
        if (!getLookahead().is(TokenTypes.INTEGER)) {
            logExpect(TokenTypes.INTEGER);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(tree.newTerminalNode(getNext()));

        return root;
    }

    private SyntaxNode parseOrExp() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.OR_EXP);

        var andExp = parseAndExp();
        if (andExp == null) {
            logFailedToParse(SyntaxTypes.AND_EXP);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(andExp);

        var orExpAux = parseOrExpAux();
        if (orExpAux == null) {
            logFailedToParse(SyntaxTypes.OR_EXP);
            postParseError(checkpoint, root);
            return null;
        }
        if (!orExpAux.isEpsilon()) {
            root.insertEndChild(orExpAux);
        }

        return root;
    }

    private SyntaxNode parseOrExpAux() {
        if (!getLookahead().is(OR_EXP_AUX_FIRST)) {
            return tree.newEpsilonNode();
        }

        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.OR_EXP);

        // '||'
        root.insertEndChild(tree.newTerminalNode(getNext()));

        var andExp = parseAndExp();
        if (andExp == null) {
            logFailedToParse(SyntaxTypes.AND_EXP);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(andExp);

        var orExpAux = parseOrExpAux();
        if (orExpAux == null) {
            logFailedToParse(SyntaxTypes.OR_EXP);
            postParseError(checkpoint, root);
            return null;
        }
        if (!orExpAux.isEpsilon()) {
            root.insertEndChild(orExpAux);
        }

        return root;
    }

    private SyntaxNode parseAndExp() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.AND_EXP);

        var eqExp = parseEqExp();
        if (eqExp == null) {
            logFailedToParse(SyntaxTypes.EQ_EXP);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(eqExp);

        var andExpAux = parseAndExpAux();
        if (andExpAux == null) {
            logFailedToParse(SyntaxTypes.AND_EXP);
            postParseError(checkpoint, root);
            return null;
        }
        if (!andExpAux.isEpsilon()) {
            root.insertEndChild(andExpAux);
        }

        return root;
    }

    private SyntaxNode parseAndExpAux() {
        if (!getLookahead().is(AND_EXP_AUX_FIRST)) {
            return tree.newEpsilonNode();
        }

        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.AND_EXP);

        // '&&'
        root.insertEndChild(tree.newTerminalNode(getNext()));

        var eqExp = parseEqExp();
        if (eqExp == null) {
            logFailedToParse(SyntaxTypes.EQ_EXP);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(eqExp);

        var andExpAux = parseAndExpAux();
        if (andExpAux == null) {
            logFailedToParse(SyntaxTypes.AND_EXP);
            postParseError(checkpoint, root);
            return null;
        }
        if (!andExpAux.isEpsilon()) {
            root.insertEndChild(andExpAux);
        }

        return root;
    }

    private SyntaxNode parseEqExp() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.EQ_EXP);

        var relExp = parseRelExp();
        if (relExp == null) {
            logFailedToParse(SyntaxTypes.REL_EXP);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(relExp);

        var eqExpAux = parseEqExpAux();
        if (eqExpAux == null) {
            logFailedToParse(SyntaxTypes.EQ_EXP);
            postParseError(checkpoint, root);
            return null;
        }
        if (!eqExpAux.isEpsilon()) {
            root.insertEndChild(eqExpAux);
        }

        return root;
    }

    private SyntaxNode parseEqExpAux() {
        if (!getLookahead().is(EQ_EXP_AUX_FIRST)) {
            return tree.newEpsilonNode();
        }

        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.EQ_EXP);

        // '==' or '!='
        root.insertEndChild(tree.newTerminalNode(getNext()));

        var relExp = parseRelExp();
        if (relExp == null) {
            logFailedToParse(SyntaxTypes.REL_EXP);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(relExp);

        var eqExpAux = parseEqExpAux();
        if (eqExpAux == null) {
            logFailedToParse(SyntaxTypes.EQ_EXP);
            postParseError(checkpoint, root);
            return null;
        }
        if (!eqExpAux.isEpsilon()) {
            root.insertEndChild(eqExpAux);
        }

        return root;
    }

    private SyntaxNode parseRelExp() {
        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.REL_EXP);

        var addExp = parseAddExp();
        if (addExp == null) {
            logFailedToParse(SyntaxTypes.ADD_EXP);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(addExp);

        var relExpAux = parseRelExpAux();
        if (relExpAux == null) {
            logFailedToParse(SyntaxTypes.REL_EXP);
            postParseError(checkpoint, root);
            return null;
        }
        if (!relExpAux.isEpsilon()) {
            root.insertEndChild(relExpAux);
        }

        return root;
    }

    private SyntaxNode parseRelExpAux() {
        if (!getLookahead().is(REL_EXP_AUX_FIRST)) {
            return tree.newEpsilonNode();
        }

        int checkpoint = lexicalParser.setCheckPoint();
        var root = tree.newNonTerminalNode(SyntaxTypes.REL_EXP);

        // '<' or '<=' or '>' or '>='
        root.insertEndChild(tree.newTerminalNode(getNext()));

        var addExp = parseAddExp();
        if (addExp == null) {
            logFailedToParse(SyntaxTypes.ADD_EXP);
            postParseError(checkpoint, root);
            return null;
        }
        root.insertEndChild(addExp);

        var relExpAux = parseRelExpAux();
        if (relExpAux == null) {
            logFailedToParse(SyntaxTypes.REL_EXP);
            postParseError(checkpoint, root);
            return null;
        }
        if (!relExpAux.isEpsilon()) {
            root.insertEndChild(relExpAux);
        }

        return root;
    }
}