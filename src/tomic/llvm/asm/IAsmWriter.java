/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

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

    default IAsmWriter pushComment(String comment) {
        commentBegin();
        push(comment);
        commentEnd();
        return this;
    }

    IAsmWriter commentBegin();

    IAsmWriter commentEnd();

    IAsmWriter pushIndent();
}
