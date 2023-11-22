package tomic.llvm.asm;

public interface IAsmWriter {
    IAsmWriter push(char ch);

    IAsmWriter push(String str);

    default IAsmWriter pushNext(char ch) {
        return pushSpace().push(ch);
    }

    default IAsmWriter pushNext(String str) {
        return pushSpace().push(str);
    }

    default IAsmWriter pushSpace() {
        return push(' ');
    }

    default IAsmWriter pushSpaces(int count) {
        for (int i = 0; i < count; i++) {
            pushSpace();
        }
        return this;
    }

    default IAsmWriter pushNewLine() {
        return push('\n');
    }

    default IAsmWriter pushNewLines(int count) {
        for (int i = 0; i < count; i++) {
            pushNewLine();
        }
        return this;
    }

    IAsmWriter pushComment(String comment);

    IAsmWriter commentBegin();

    IAsmWriter commentEnd();
}
