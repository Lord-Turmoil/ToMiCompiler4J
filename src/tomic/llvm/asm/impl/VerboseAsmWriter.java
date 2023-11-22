package tomic.llvm.asm.impl;

import lib.twio.ITwioWriter;
import tomic.llvm.asm.IAsmWriter;

public class VerboseAsmWriter implements IAsmWriter {
    private final ITwioWriter impl;

    public VerboseAsmWriter(ITwioWriter impl) {
        this.impl = impl;
    }

    @Override
    public void push(char ch) {
        impl.write(ch);
    }

    @Override
    public void push(String str) {
        impl.write(str);
    }

    @Override
    public void pushComment(String comment) {
        push("; ");
        push(comment);
        pushNewLine();
    }

    @Override
    public void commentBegin() {
        push("; ");
    }

    @Override
    public void commentEnd() {
        pushNewLine();
    }
}
