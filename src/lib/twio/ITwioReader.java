package lib.twio;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public interface ITwioReader {
    boolean hasNext();
    int read();
    int rewind();

    int getLineNo();
    int getCharNo();
}
