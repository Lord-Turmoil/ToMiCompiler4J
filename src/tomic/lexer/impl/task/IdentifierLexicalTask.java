package tomic.lexer.impl.task;

import lib.twio.ITwioReader;
import tomic.lexer.token.ITokenMapper;
import tomic.lexer.token.Token;
import tomic.lexer.token.TokenTypes;

public class IdentifierLexicalTask extends LexicalTask {
    public IdentifierLexicalTask(ITokenMapper mapper) {
        super(mapper);
    }

    @Override
    public boolean beginsWith(int begin) {
        return Character.isLetter(begin) || begin == '_';
    }

    @Override
    public boolean endsWith(int end) {
        return (end == EOF) ||
                (WHITESPACES.indexOf(end) != -1) ||
                (DELIMITERS.indexOf(end) != -1) ||
                (OPERATORS.indexOf(end) != -1);
    }

    @Override
    public Token analyze(ITwioReader reader) {
        int ch = reader.read();
        int lineNo = reader.getLineNo();
        int charNo = reader.getCharNo();
        StringBuilder lexeme = new StringBuilder();

        while (ch != EOF && (Character.isLetterOrDigit(ch) || ch == '_')) {
            lexeme.append((char) ch);
            ch = reader.read();
        }

        if (!endsWith(ch)) {
            while (!endsWith(ch)) {
                lexeme.append((char) ch);
                ch = reader.read();
            }
            if (ch != EOF) {
                reader.rewind();
            }

            return new Token(TokenTypes.UNKNOWN, lexeme.toString(), lineNo, charNo);
        }

        if (ch != EOF) {
            reader.rewind();
        }

        String lexemeString = lexeme.toString();
        var token = new Token(mapper.type(lexemeString), lexemeString, lineNo, charNo);
        if (token.type == TokenTypes.UNKNOWN) {
            token.type = TokenTypes.IDENTIFIER;
        }

        return token;
    }
}
