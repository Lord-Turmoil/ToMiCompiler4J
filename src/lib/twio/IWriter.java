package lib.twio;

import java.io.InputStream;
import java.io.OutputStream;

public interface IWriter {
    void write(int ch);
    void write(char ch);
    void write(String str);
    void writeLine(String str);
    void writeLine();

    /**
     * This is different from ToMiC, which terminates the stream.
     * It does not terminate the stream.
     * @return InputStream
     */
    InputStream yield();
}
