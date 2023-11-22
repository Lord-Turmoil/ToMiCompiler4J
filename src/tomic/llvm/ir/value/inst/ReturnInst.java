package tomic.llvm.ir.value.inst;

import tomic.llvm.asm.IAsmWriter;
import tomic.llvm.ir.LlvmContext;
import tomic.llvm.ir.value.Value;
import tomic.llvm.ir.value.ValueTypes;

public class ReturnInst extends Instruction {
    private Value value;

    public ReturnInst(Value value) {
        super(ValueTypes.ReturnInstTy, value.getContext().getVoidTy());
        this.value = value;
    }

    public ReturnInst(LlvmContext context) {
        super(ValueTypes.ReturnInstTy, context.getVoidTy());
        this.value = null;
    }

    public boolean hasValue() {
        return value != null;
    }

    @Override
    public IAsmWriter printAsm(IAsmWriter out) {
        out.push("ret");
        if (value != null && !value.getType().isVoidTy()) {
            out.pushSpace();
            value.printAsm(out);
        } else {
            out.pushNext("void");
        }
        return out.pushNewLine();
    }
}
