package tomic.lexer;

import lib.twio.ITwioReader;
import tomic.lexer.token.Token;

public interface ILexicalParser {
    ILexicalParser setReader(ITwioReader reader);

    Token current();

    Token next();

    Token rewind();

    int setCheckPoint();

    void rollBack(int checkpoint);
}
