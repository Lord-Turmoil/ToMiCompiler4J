package tomic.lexer.impl.task;

import lib.twio.ITwioReader;
import tomic.lexer.token.ITokenMapper;
import tomic.lexer.token.Token;
import tomic.lexer.token.TokenTypes;
import tomic.utils.Constants;

public class NumberLexicalTask extends LexicalTask {
    public NumberLexicalTask(ITokenMapper mapper) {
        super(mapper);
    }

    @Override
    public boolean beginsWith(int begin) {
        return Constants.DIGITS.indexOf(begin) != -1;
    }

    @Override
    public boolean endsWith(int end) {
        return (end == Constants.EOF) ||
                (Constants.WHITESPACES.indexOf(end) != -1) ||
                (Constants.DELIMITERS.indexOf(end) != -1) ||
                (Constants.OPERATORS.indexOf(end) != -1);
    }

    @Override
    public Token analyze(ITwioReader reader) {
        int ch = reader.read();
        int lineNo = reader.getLineNo();
        int charNo = reader.getCharNo();
        StringBuilder lexeme = new StringBuilder();

        while (ch != Constants.EOF && Constants.DIGITS.indexOf(ch) != -1) {
            lexeme.append((char) ch);
            ch = reader.read();
        }

        if (!endsWith(ch)) {
            while (!endsWith(ch)) {
                lexeme.append((char) ch);
                ch = reader.read();
            }
            if (ch != Constants.EOF) {
                reader.rewind();
            }

            // TODO: Error handling.
            return new Token(TokenTypes.UNKNOWN, lexeme.toString(), lineNo, charNo);
        }

        if (ch != Constants.EOF) {
            reader.rewind();
        }

        return new Token(TokenTypes.INTEGER, lexeme.toString(), lineNo, charNo);
    }
}
