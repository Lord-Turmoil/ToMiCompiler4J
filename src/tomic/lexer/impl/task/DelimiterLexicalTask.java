package tomic.lexer.impl.task;

import lib.twio.ITwioReader;
import tomic.lexer.token.ITokenMapper;
import tomic.lexer.token.Token;
import tomic.utils.Constants;

public class DelimiterLexicalTask extends LexicalTask {
    public DelimiterLexicalTask(ITokenMapper mapper) {
        super(mapper);
    }

    @Override
    public boolean beginsWith(int begin) {
        return Constants.DELIMITERS.indexOf(begin) != -1;
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
        String lexeme = String.valueOf((char) ch);

        return new Token(mapper.type(lexeme), lexeme, lineNo, charNo);
    }
}
