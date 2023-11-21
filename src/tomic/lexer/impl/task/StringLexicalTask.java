package tomic.lexer.impl.task;

import lib.twio.ITwioReader;
import tomic.lexer.token.ITokenMapper;
import tomic.lexer.token.Token;
import tomic.lexer.token.TokenTypes;
import tomic.utils.Constants;

public class StringLexicalTask extends LexicalTask {

    public StringLexicalTask(ITokenMapper mapper) {
        super(mapper);
    }

    @Override
    public boolean beginsWith(int begin) {
        return begin == '"';
    }

    @Override
    public boolean endsWith(int end) {
        return end == '"';
    }

    @Override
    public Token analyze(ITwioReader reader) {
        int ch = reader.read();
        int lineNo = reader.getLineNo();
        int charNo = reader.getCharNo();
        StringBuilder lexeme = new StringBuilder();
        boolean error = false;

        lexeme.append((char) ch);
        ch = reader.read();
        while (ch != Constants.EOF && ch != '"') {
            if (isNormalChar(ch)) {
                lexeme.append((char) ch);
            } else if (isNewLineChar(ch, reader)) {
                reader.read();
                lexeme.append('\n');
            } else if (isFormatChar(ch, reader)) {
                lexeme.append((char) ch);
                lexeme.append((char) reader.read());
            } else {
                lexeme.append((char) ch);
                error = true;
            }
            ch = reader.read();
        }

        if (ch != Constants.EOF) {
            lexeme.append((char) ch);
        } else {
            error = true;
        }

        if (error) {
            return new Token(TokenTypes.UNKNOWN, lexeme.toString(), lineNo, charNo);
        }

        return new Token(TokenTypes.FORMAT, lexeme.toString(), lineNo, charNo);
    }

    private boolean isNormalChar(int ch) {
        return (ch == 32) || (ch == 33) || (40 <= ch && ch < 92) || (92 < ch && ch <= 126);
    }

    private boolean isNewLineChar(int ch, ITwioReader reader) {
        boolean ret = false;

        if (ch == '\\') {
            ch = reader.read();
            if (ch == 'n') {
                ret = true;
            }
            if (ch != Constants.EOF) {
                reader.rewind();
            }
        }

        return ret;
    }

    private boolean isFormatChar(int ch, ITwioReader reader) {
        boolean ret = false;

        if (ch == '%') {
            ch = reader.read();
            if (ch == 'd') {
                ret = true;
            }
            if (ch != Constants.EOF) {
                reader.rewind();
            }
        }

        return ret;
    }
}
