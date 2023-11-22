package tomic.llvm.asm;

public interface IAsmWriter {
    void push(char ch);

    void push(String str);

    default void pushNext(char ch) {
        pushSpace();
        push(ch);
    }

    default void pushNext(String str) {
        pushSpace();
        push(str);
    }

    default void pushSpace() {
        push(' ');
    }

    default void pushSpaces(int count) {
        for (int i = 0; i < count; i++) {
            pushSpace();
        }
    }

    default void pushNewLine() {
        push('\n');
    }

    default void pushNewLines(int count) {
        for (int i = 0; i < count; i++) {
            pushNewLine();
        }
    }

    void pushComment(String comment);

    void commentBegin();

    void commentEnd();
}
