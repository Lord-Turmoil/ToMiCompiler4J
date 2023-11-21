package lib.twio;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TwioBufferWriter implements ITwioWriter {
    private final StringBuffer buffer = new StringBuffer();


    @Override
    public void write(int ch) {
        buffer.append((char) ch);
    }

    @Override
    public void write(char ch) {
        buffer.append(ch);
    }

    @Override
    public void write(String str) {
        buffer.append(str);
    }

    @Override
    public void writeLine(String str) {
        buffer.append(str);
        buffer.append('\n');
    }

    @Override
    public void writeLine() {
        buffer.append('\n');
    }

    @Override
    public InputStream yield() {
        return new ByteArrayInputStream(buffer.toString().getBytes(StandardCharsets.UTF_8));
    }
}
