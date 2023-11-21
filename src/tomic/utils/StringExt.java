package tomic.utils;

public class StringExt {
    private StringExt() {}

    public static boolean contains(String source, int ch) {
        return source.indexOf(ch) != -1;
    }
}
