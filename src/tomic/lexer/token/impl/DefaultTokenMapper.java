package tomic.lexer.token.impl;

import tomic.lexer.token.ITokenMapper;
import tomic.lexer.token.TokenType;

import java.util.HashMap;
import java.util.Map;

public class DefaultTokenMapper implements ITokenMapper {
    private Map<String, TokenType> lexemeToType;
    private Map<TokenType, String> typeToLexeme;
    private Map<TokenType, String> typeToDescription;
    private Map<String, Boolean> lexemeToIsKeyword;

    public DefaultTokenMapper() {
        init();
    }

    @Override
    public TokenType type(String lexeme) {
        return lexemeToType.getOrDefault(lexeme, TokenType.UNKNOWN);
    }

    @Override
    public String lexeme(TokenType type) {
        return typeToLexeme.getOrDefault(type, null);
    }

    @Override
    public String description(TokenType type) {
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

        lexemeToType.put("main", TokenType.MAIN);
        lexemeToType.put("return", TokenType.RETURN);
        lexemeToType.put("getint", TokenType.GETINT);
        lexemeToType.put("printf", TokenType.PRINTF);

        lexemeToType.put("if", TokenType.IF);
        lexemeToType.put("else", TokenType.ELSE);

        lexemeToType.put("for", TokenType.FOR);
        lexemeToType.put("break", TokenType.BREAK);
        lexemeToType.put("continue", TokenType.CONTINUE);

        lexemeToType.put("const", TokenType.CONST);
        lexemeToType.put("int", TokenType.INT);
        lexemeToType.put("void", TokenType.VOID);

        lexemeToType.put("!", TokenType.NOT);
        lexemeToType.put("&&", TokenType.AND);
        lexemeToType.put("||", TokenType.OR);
        lexemeToType.put("+", TokenType.PLUS);
        lexemeToType.put("-", TokenType.MINUS);
        lexemeToType.put("*", TokenType.MULTIPLY);
        lexemeToType.put("/", TokenType.DIVIDE);
        lexemeToType.put("%", TokenType.MOD);
        lexemeToType.put("<", TokenType.LESS);
        lexemeToType.put("<=", TokenType.LESS_EQUAL);
        lexemeToType.put(">", TokenType.GREATER);
        lexemeToType.put(">=", TokenType.GREATER_EQUAL);
        lexemeToType.put("==", TokenType.EQUAL);
        lexemeToType.put("!=", TokenType.NOT_EQUAL);
        lexemeToType.put("=", TokenType.ASSIGN);

        lexemeToType.put(";", TokenType.SEMICOLON);
        lexemeToType.put(",", TokenType.COMMA);

        lexemeToType.put("(", TokenType.LEFT_PARENTHESIS);
        lexemeToType.put(")", TokenType.RIGHT_PARENTHESIS);
        lexemeToType.put("{", TokenType.LEFT_BRACE);
        lexemeToType.put("}", TokenType.RIGHT_BRACE);
        lexemeToType.put("[", TokenType.LEFT_BRACKET);
        lexemeToType.put("]", TokenType.RIGHT_BRACKET);
    }

    private void initTypeToLexeme() {
        typeToLexeme = new HashMap<>();
        for (Map.Entry<String, TokenType> entry : lexemeToType.entrySet()) {
            typeToLexeme.put(entry.getValue(), entry.getKey());
        }
    }

    private void initTypeToDescription() {
        typeToDescription = new HashMap<>();

        typeToDescription.put(TokenType.UNKNOWN, "UNKNOWN");
        typeToDescription.put(TokenType.IDENTIFIER, "IDENFR");
        typeToDescription.put(TokenType.INTEGER, "INTCON");
        typeToDescription.put(TokenType.FORMAT, "STRCON");
        typeToDescription.put(TokenType.MAIN, "MAINTK");
        typeToDescription.put(TokenType.RETURN, "RETURNTK");
        typeToDescription.put(TokenType.GETINT, "GETINTTK");
        typeToDescription.put(TokenType.PRINTF, "PRINTFTK");

        typeToDescription.put(TokenType.IF, "IFTK");
        typeToDescription.put(TokenType.ELSE, "ELSETK");

        typeToDescription.put(TokenType.FOR, "FORTK");
        typeToDescription.put(TokenType.BREAK, "BREAKTK");
        typeToDescription.put(TokenType.CONTINUE, "CONTINUETK");

        typeToDescription.put(TokenType.CONST, "CONSTTK");
        typeToDescription.put(TokenType.INT, "INTTK");
        typeToDescription.put(TokenType.VOID, "VOIDTK");

        typeToDescription.put(TokenType.NOT, "NOT");
        typeToDescription.put(TokenType.AND, "AND");
        typeToDescription.put(TokenType.OR, "OR");
        typeToDescription.put(TokenType.PLUS, "PLUS");
        typeToDescription.put(TokenType.MINUS, "MINU");
        typeToDescription.put(TokenType.MULTIPLY, "MULT");
        typeToDescription.put(TokenType.DIVIDE, "DIV");
        typeToDescription.put(TokenType.MOD, "MOD");
        typeToDescription.put(TokenType.LESS, "LSS");
        typeToDescription.put(TokenType.LESS_EQUAL, "LEQ");
        typeToDescription.put(TokenType.GREATER, "GRE");
        typeToDescription.put(TokenType.GREATER_EQUAL, "GEQ");
        typeToDescription.put(TokenType.EQUAL, "EQL");
        typeToDescription.put(TokenType.NOT_EQUAL, "NEQ");
        typeToDescription.put(TokenType.ASSIGN, "ASSIGN");

        typeToDescription.put(TokenType.SEMICOLON, "SEMICN");
        typeToDescription.put(TokenType.COMMA, "COMMA");

        typeToDescription.put(TokenType.LEFT_PARENTHESIS, "LPARENT");
        typeToDescription.put(TokenType.RIGHT_PARENTHESIS, "RPARENT");
        typeToDescription.put(TokenType.LEFT_BRACE, "LBRACE");
        typeToDescription.put(TokenType.RIGHT_BRACE, "RBRACE");
        typeToDescription.put(TokenType.LEFT_BRACKET, "LBRACK");
        typeToDescription.put(TokenType.RIGHT_BRACKET, "RBRACK");
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
