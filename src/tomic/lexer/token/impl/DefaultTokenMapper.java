package tomic.lexer.token.impl;

import tomic.lexer.token.ITokenMapper;
import tomic.lexer.token.TokenTypes;

import java.util.HashMap;
import java.util.Map;

public class DefaultTokenMapper implements ITokenMapper {
    private Map<String, TokenTypes> lexemeToType;
    private Map<TokenTypes, String> typeToLexeme;
    private Map<TokenTypes, String> typeToDescription;
    private Map<String, Boolean> lexemeToIsKeyword;

    public DefaultTokenMapper() {
        init();
    }

    @Override
    public TokenTypes type(String lexeme) {
        return lexemeToType.getOrDefault(lexeme, TokenTypes.UNKNOWN);
    }

    @Override
    public String lexeme(TokenTypes type) {
        return typeToLexeme.getOrDefault(type, null);
    }

    @Override
    public String description(TokenTypes type) {
        return typeToDescription.getOrDefault(type, null);
    }

    @Override
    public boolean isKeyword(String lexeme) {
        return lexemeToIsKeyword.getOrDefault(lexeme, false);
    }

    private void init() {
        initLexemeToType();
        initTypeToLexeme();
        initTypeToDescription();
        initLexemeToIsKeyword();
    }

    private void initLexemeToType() {
        lexemeToType = new HashMap<>();

        lexemeToType.put("main", TokenTypes.MAIN);
        lexemeToType.put("return", TokenTypes.RETURN);
        lexemeToType.put("getint", TokenTypes.GETINT);
        lexemeToType.put("printf", TokenTypes.PRINTF);

        lexemeToType.put("if", TokenTypes.IF);
        lexemeToType.put("else", TokenTypes.ELSE);

        lexemeToType.put("for", TokenTypes.FOR);
        lexemeToType.put("break", TokenTypes.BREAK);
        lexemeToType.put("continue", TokenTypes.CONTINUE);

        lexemeToType.put("const", TokenTypes.CONST);
        lexemeToType.put("int", TokenTypes.INT);
        lexemeToType.put("void", TokenTypes.VOID);

        lexemeToType.put("!", TokenTypes.NOT);
        lexemeToType.put("&&", TokenTypes.AND);
        lexemeToType.put("||", TokenTypes.OR);
        lexemeToType.put("+", TokenTypes.PLUS);
        lexemeToType.put("-", TokenTypes.MINUS);
        lexemeToType.put("*", TokenTypes.MULTIPLY);
        lexemeToType.put("/", TokenTypes.DIVIDE);
        lexemeToType.put("%", TokenTypes.MOD);
        lexemeToType.put("<", TokenTypes.LESS);
        lexemeToType.put("<=", TokenTypes.LESS_EQUAL);
        lexemeToType.put(">", TokenTypes.GREATER);
        lexemeToType.put(">=", TokenTypes.GREATER_EQUAL);
        lexemeToType.put("==", TokenTypes.EQUAL);
        lexemeToType.put("!=", TokenTypes.NOT_EQUAL);
        lexemeToType.put("=", TokenTypes.ASSIGN);

        lexemeToType.put(";", TokenTypes.SEMICOLON);
        lexemeToType.put(",", TokenTypes.COMMA);

        lexemeToType.put("(", TokenTypes.LEFT_PARENTHESIS);
        lexemeToType.put(")", TokenTypes.RIGHT_PARENTHESIS);
        lexemeToType.put("{", TokenTypes.LEFT_BRACE);
        lexemeToType.put("}", TokenTypes.RIGHT_BRACE);
        lexemeToType.put("[", TokenTypes.LEFT_BRACKET);
        lexemeToType.put("]", TokenTypes.RIGHT_BRACKET);
    }

    private void initTypeToLexeme() {
        typeToLexeme = new HashMap<>();
        for (Map.Entry<String, TokenTypes> entry : lexemeToType.entrySet()) {
            typeToLexeme.put(entry.getValue(), entry.getKey());
        }
    }

    private void initTypeToDescription() {
        typeToDescription = new HashMap<>();

        typeToDescription.put(TokenTypes.UNKNOWN, "UNKNOWN");
        typeToDescription.put(TokenTypes.IDENTIFIER, "IDENFR");
        typeToDescription.put(TokenTypes.INTEGER, "INTCON");
        typeToDescription.put(TokenTypes.FORMAT, "STRCON");
        typeToDescription.put(TokenTypes.MAIN, "MAINTK");
        typeToDescription.put(TokenTypes.RETURN, "RETURNTK");
        typeToDescription.put(TokenTypes.GETINT, "GETINTTK");
        typeToDescription.put(TokenTypes.PRINTF, "PRINTFTK");

        typeToDescription.put(TokenTypes.IF, "IFTK");
        typeToDescription.put(TokenTypes.ELSE, "ELSETK");

        typeToDescription.put(TokenTypes.FOR, "FORTK");
        typeToDescription.put(TokenTypes.BREAK, "BREAKTK");
        typeToDescription.put(TokenTypes.CONTINUE, "CONTINUETK");

        typeToDescription.put(TokenTypes.CONST, "CONSTTK");
        typeToDescription.put(TokenTypes.INT, "INTTK");
        typeToDescription.put(TokenTypes.VOID, "VOIDTK");

        typeToDescription.put(TokenTypes.NOT, "NOT");
        typeToDescription.put(TokenTypes.AND, "AND");
        typeToDescription.put(TokenTypes.OR, "OR");
        typeToDescription.put(TokenTypes.PLUS, "PLUS");
        typeToDescription.put(TokenTypes.MINUS, "MINU");
        typeToDescription.put(TokenTypes.MULTIPLY, "MULT");
        typeToDescription.put(TokenTypes.DIVIDE, "DIV");
        typeToDescription.put(TokenTypes.MOD, "MOD");
        typeToDescription.put(TokenTypes.LESS, "LSS");
        typeToDescription.put(TokenTypes.LESS_EQUAL, "LEQ");
        typeToDescription.put(TokenTypes.GREATER, "GRE");
        typeToDescription.put(TokenTypes.GREATER_EQUAL, "GEQ");
        typeToDescription.put(TokenTypes.EQUAL, "EQL");
        typeToDescription.put(TokenTypes.NOT_EQUAL, "NEQ");
        typeToDescription.put(TokenTypes.ASSIGN, "ASSIGN");

        typeToDescription.put(TokenTypes.SEMICOLON, "SEMICN");
        typeToDescription.put(TokenTypes.COMMA, "COMMA");

        typeToDescription.put(TokenTypes.LEFT_PARENTHESIS, "LPARENT");
        typeToDescription.put(TokenTypes.RIGHT_PARENTHESIS, "RPARENT");
        typeToDescription.put(TokenTypes.LEFT_BRACE, "LBRACE");
        typeToDescription.put(TokenTypes.RIGHT_BRACE, "RBRACE");
        typeToDescription.put(TokenTypes.LEFT_BRACKET, "LBRACK");
        typeToDescription.put(TokenTypes.RIGHT_BRACKET, "RBRACK");
    }

    private void initLexemeToIsKeyword() {
        lexemeToIsKeyword = new HashMap<>();

        lexemeToIsKeyword.put("main", true);
        lexemeToIsKeyword.put("return", true);
        lexemeToIsKeyword.put("getint", true);
        lexemeToIsKeyword.put("printf", true);

        lexemeToIsKeyword.put("if", true);
        lexemeToIsKeyword.put("else", true);

        lexemeToIsKeyword.put("for", true);
        lexemeToIsKeyword.put("break", true);
        lexemeToIsKeyword.put("continue", true);

        lexemeToIsKeyword.put("const", true);
        lexemeToIsKeyword.put("int", true);
        lexemeToIsKeyword.put("void", true);
    }
}
