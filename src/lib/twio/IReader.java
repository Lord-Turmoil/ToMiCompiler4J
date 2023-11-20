package lib.twio;

public interface IReader {
    boolean hasNext();
    int read();
    int rewind();

    int getLineNo();
    int getCharNo();
}
