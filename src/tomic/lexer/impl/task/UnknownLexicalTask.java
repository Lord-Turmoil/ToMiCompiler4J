package tomic.lexer.impl.task;

import lib.twio.ITwioReader;
import tomic.lexer.token.ITokenMapper;
import tomic.lexer.token.Token;
import tomic.lexer.token.TokenTypes;

public class UnknownLexicalTask extends LexicalTask {
    public UnknownLexicalTask(ITokenMapper mapper) {
        super(mapper);
    }

    @Override
    public boolean beginsWith(int begin) {
        return true;
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

        return new Token(TokenTypes.UNKNOWN, String.valueOf((char) ch), lineNo, charNo);
    }
}
