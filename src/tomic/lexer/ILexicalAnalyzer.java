package tomic.lexer;

import lib.twio.ITwioReader;
import tomic.lexer.token.Token;

public interface ILexicalAnalyzer {
    void setReader(ITwioReader reader);

    Token next();
}
