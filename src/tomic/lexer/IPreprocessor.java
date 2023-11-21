package tomic.lexer;

import lib.twio.ITwioReader;
import lib.twio.ITwioWriter;

public interface IPreprocessor {
    void setReader(ITwioReader reader);
    void setWriter(ITwioWriter writer);

    ITwioReader getReader();
    ITwioWriter getWriter();

    void process();
}
