package tomic.lexer.impl;

import lib.twio.ITwioReader;
import tomic.lexer.ILexicalAnalyzer;
import tomic.lexer.ILexicalParser;
import tomic.lexer.token.Token;
import tomic.lexer.token.TokenTypes;
import tomic.logger.debug.IDebugLogger;
import tomic.logger.error.ErrorTypes;
import tomic.logger.error.IErrorLogger;

import java.util.ArrayList;

public class DefaultLexicalParser implements ILexicalParser {
    private final ILexicalAnalyzer analyzer;
    private final IErrorLogger errorLogger;
    private final IDebugLogger logger;

    private final ArrayList<Token> tokens;
    private int cursor;

    public DefaultLexicalParser(ILexicalAnalyzer analyzer, IErrorLogger errorLogger, IDebugLogger logger) {
        this.analyzer = analyzer;
        this.errorLogger = errorLogger;
        this.logger = logger;

        tokens = new ArrayList<>();
        cursor = 0;
    }

    @Override
    public DefaultLexicalParser setReader(ITwioReader reader) {
        analyzer.setReader(reader);
        return this;
    }

    @Override
    public Token current() {
        if (cursor == 0) {
            return null;
        }
        return tokens.get(cursor - 1);
    }

    @Override
    public Token next() {
        if (cursor == tokens.size()) {
            Token token = analyzer.next();
            while (token.type == TokenTypes.UNKNOWN) {
                logUnexpectedToken(token);
                raiseUnexpectedTokenError(token);
                token = analyzer.next();
            }

            if (token.type == TokenTypes.TERMINATOR) {
                return token;
            }

            tokens.add(token);
        }

        return tokens.get(cursor++);
    }

    @Override
    public Token rewind() {
        if (cursor > 0) {
            return tokens.get(--cursor);
        }

        return null;
    }

    @Override
    public int setCheckPoint() {
        return cursor;
    }

    @Override
    public void rollBack(int checkpoint) {
        cursor = checkpoint;
    }

    private void logUnexpectedToken(Token token) {
        logger.error(String.format("(%d:%d) Unexpected token %s", token.lineNo, token.charNo, token.lexeme));
    }

    private void raiseUnexpectedTokenError(Token token) {
        errorLogger.log(token.lineNo, token.charNo, ErrorTypes.UNEXPECTED_TOKEN, "Unexpected token " + token.lexeme);
    }
}
