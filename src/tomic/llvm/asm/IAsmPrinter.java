package tomic.llvm.asm;

import lib.twio.ITwioWriter;
import tomic.llvm.ir.Module;

public interface IAsmPrinter {
    void print(Module module, ITwioWriter writer);
}
