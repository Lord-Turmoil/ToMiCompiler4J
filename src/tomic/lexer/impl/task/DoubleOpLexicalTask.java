package tomic.lexer.impl.task;

import lib.twio.ITwioReader;
import tomic.lexer.token.ITokenMapper;
import tomic.lexer.token.Token;
import tomic.utils.Constants;

public class DoubleOpLexicalTask extends LexicalTask {
    public DoubleOpLexicalTask(ITokenMapper mapper) {
        super(mapper);
    }

    @Override
    public boolean beginsWith(int begin) {
        return "&|=<>!".indexOf(begin) != -1;
    }

    @Override
    public boolean endsWith(int end) {
        return true;
    }

    @Override
    public Token analyze(ITwioReader reader) {
        int ch = reader.read();
        int lineNo = reader.getLineNo();
        int charNo = reader.getCharNo();
        StringBuilder lexeme = new StringBuilder();
        int next;

        lexeme.append((char) ch);
        switch (ch) {
            case '&', '|', '=' -> {
                next = reader.read();
                if (next == ch) {
                    lexeme.append((char) ch);
                } else if (next != Constants.EOF) {
                    reader.rewind();
                }
            }
            case '<', '>', '!' -> {
                next = reader.read();
                if (next == '=') {
                    lexeme.append((char) next);
                } else if (next != Constants.EOF) {
                    reader.rewind();
                }
            }
            default -> throw new IllegalStateException("Unknown double-character operator");
        }

        return new Token(mapper.type(lexeme.toString()), lexeme.toString(), lineNo, charNo);
    }
}
