package lib.twio;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public class TwioFileWriter implements IWriter {
    private final PrintStream impl;

    public TwioFileWriter(OutputStream stream) {
        impl = new PrintStream(stream);
    }

    @Override
    public void write(int ch) {
        impl.print((char) ch);
    }

    @Override
    public void write(char ch) {
        impl.print(ch);
    }

    @Override
    public void write(String str) {
        impl.print(str);
    }

    @Override
    public void writeLine(String str) {
        impl.println(str);
    }

    @Override
    public void writeLine() {
        impl.println();
    }

    @Override
    public InputStream yield() {
        throw new UnsupportedOperationException("yield() is not supported in TwioFileWriter");
    }
}
