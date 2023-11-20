package lib.twio;

public interface ITwioReader {
    boolean hasNext();
    int read();
    int rewind();

    int getLineNo();
    int getCharNo();
}
