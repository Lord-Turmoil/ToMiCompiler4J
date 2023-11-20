package lib.twio;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Stack;

public class TwioReader implements ITwioReader {
    private final ArrayList<Character> buffer = new ArrayList<>();
    private final Stack<Integer> lastChar = new Stack<>();

    private int cursor = 0;
    private final int total;
    private int lineNo = 0;
    private int charNo = 0;

    public TwioReader(InputStream stream) {
        InputStreamReader isr = new InputStreamReader(stream);
        try {
            int ch = isr.read();
            while (ch != -1) {
                buffer.add((char) ch);
                ch = isr.read();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        total = buffer.size();
    }

    @Override
    public boolean hasNext() {
        return cursor < total;
    }

    @Override
    public int read() {
        if (cursor >= total) {
            return -1;
        }
        char ch = buffer.get(cursor++);
        moveForward(ch);
        return ch;
    }

    @Override
    public int rewind() {
        if (cursor <= 0) {
            return -1;
        }
        char ch = buffer.get(--cursor);
        moveBackward(ch);
        return ch;
    }

    @Override
    public int getLineNo() {
        return lineNo;
    }

    @Override
    public int getCharNo() {
        return charNo;
    }

    private void moveForward(char ch) {
        if (ch == '\n') {
            lineNo++;
            lastChar.push(charNo);
            charNo = 0;
        } else if (ch != '\r') {
            charNo++;
        }
    }

    private void moveBackward(char ch) {
        if (ch == '\n') {
            lineNo--;
            charNo = lastChar.pop() + 1;
        } else if (ch != '\r') {
            charNo--;
        }
    }
}
