package tomic.llvm.ir.value.inst;

import tomic.llvm.asm.IAsmWriter;
import tomic.llvm.ir.LlvmContext;
import tomic.llvm.ir.value.ValueTypes;

public class InputInst extends Instruction {
    public InputInst(LlvmContext context) {
        super(ValueTypes.InputInstTy, context.getVoidTy());
        setName("getint");
    }

    @Override
    public IAsmWriter printAsm(IAsmWriter out) {
        printName(out).pushNext('=').pushNext("call").pushSpace();
        getType().printAsm(out);
        return out.pushNext('@').push(getName()).push("()").pushNewLine();
    }
}
