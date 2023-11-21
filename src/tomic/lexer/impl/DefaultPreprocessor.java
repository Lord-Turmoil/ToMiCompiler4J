package tomic.lexer.impl;

import lib.twio.ITwioReader;
import lib.twio.ITwioWriter;
import tomic.lexer.IPreprocessor;
import tomic.utils.Constants;

public class DefaultPreprocessor implements IPreprocessor {
    private ITwioReader reader;
    private ITwioWriter writer;
    private final State state = new State();
    private static final char FILLING = ' ';

    @Override
    public DefaultPreprocessor setReader(ITwioReader reader) {
        this.reader = reader;
        return this;
    }

    @Override
    public DefaultPreprocessor setWriter(ITwioWriter writer) {
        this.writer = writer;
        return this;
    }

    @Override
    public ITwioReader getReader() {
        return reader;
    }

    @Override
    public ITwioWriter getWriter() {
        return writer;
    }

    @Override
    public void process() {
        if (reader == null || writer == null) {
            throw new IllegalStateException("Reader or writer not set");
        }

        int ch;
        do {
            ch = reader.read(); // EOF is valid here!
            if (ch != '\r') {
                processImpl(ch);
            }
        } while (ch != Constants.EOF);
    }

    private void processImpl(int ch) {
        switch (state.type) {
            case ANY -> processAny(ch);
            case SLASH -> processSlash(ch);
            case LINE_COMMENT -> processLineComment(ch);
            case BLOCK_COMMENT_LEFT -> processBlockCommentLeft(ch);
            case BLOCK_COMMENT_RIGHT -> processBlockCommentRight(ch);
            case QUOTE -> processQuote(ch);
        }
    }

    private void processAny(int ch) {
        switch (ch) {
            case '/' -> state.type = StateTypes.SLASH;
            case '\'', '"' -> {
                state.type = StateTypes.QUOTE;
                state.value = ch;
                writer.write(ch);
            }
            case '#' -> {
                state.type = StateTypes.LINE_COMMENT;
                writer.write(FILLING);
            }
            case Constants.EOF -> {
            }
            default -> writer.write(ch);
        }
    }

    private void processSlash(int ch) {
        switch (ch) {
            case '/' -> {
                state.type = StateTypes.LINE_COMMENT;
                writer.write(FILLING);
                writer.write(FILLING);
            }
            case '*' -> {
                state.type = StateTypes.BLOCK_COMMENT_LEFT;
                writer.write(FILLING);
                writer.write(FILLING);
            }
            case '\'', '"' -> {
                state.type = StateTypes.QUOTE;
                state.value = ch;
                writer.write('/');
                writer.write(ch);
            }
            case Constants.EOF -> {
                state.type = StateTypes.ANY;
                writer.write('/');
            }
            default -> {
                state.type = StateTypes.ANY;
                writer.write('/');
                writer.write(ch);
            }
        }
    }

    private void processLineComment(int ch) {
        switch (ch) {
            case '\n' -> {
                state.type = StateTypes.ANY;
                writer.write('\n');
            }
            case Constants.EOF -> state.type = StateTypes.ANY;
            default -> writer.write(FILLING);
        }
    }

    private void processBlockCommentLeft(int ch) {
        switch (ch) {
            case '*' -> state.type = StateTypes.BLOCK_COMMENT_RIGHT;
            case Constants.EOF -> state.type = StateTypes.ANY;
            default -> writer.write(FILLING);
        }
    }

    private void processBlockCommentRight(int ch) {
        switch (ch) {
            case '/' -> state.type = StateTypes.ANY;
            case '*' -> {
                state.type = StateTypes.BLOCK_COMMENT_RIGHT;
                writer.write(FILLING);
            }
            case '\n' -> {
                state.type = StateTypes.BLOCK_COMMENT_LEFT;
                writer.write(FILLING);
                writer.write('\n');
            }
            case Constants.EOF -> writer.write(FILLING);
            default -> {
                state.type = StateTypes.BLOCK_COMMENT_LEFT;
                writer.write(FILLING);
                writer.write(FILLING);
            }
        }
    }

    private void processQuote(int ch) {
        if (ch == Constants.EOF) {
            state.type = StateTypes.ANY;
            return;
        }

        switch (ch) {
            case '\'', '"' -> {
                if (ch == state.value) {
                    state.type = StateTypes.ANY;
                }
            }
            default -> {
            }
        }

        writer.write(ch);
    }

    enum StateTypes {
        ANY,                    // any state
        SLASH,                  // '/'
        LINE_COMMENT,           // '//'
        BLOCK_COMMENT_LEFT,     // '/*'
        BLOCK_COMMENT_RIGHT,    // '*' (waiting for '/')
        QUOTE                   // ' or ", with value set
    }

    static class State {
        public StateTypes type;
        public int value;

        public State() {
            type = StateTypes.ANY;
            value = 0;
        }
    }
}
