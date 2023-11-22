package tomic.llvm.ir.value.inst;

import tomic.llvm.asm.IAsmWriter;
import tomic.llvm.ir.type.PointerType;
import tomic.llvm.ir.value.Value;
import tomic.llvm.ir.value.ValueTypes;

public class OutputInst extends UnaryInstruction {
    public OutputInst(Value operand) {
        super(ValueTypes.OutputInstTy, operand.getContext().getVoidTy(), operand);
        if (isInteger()) {
            setName("putint");
        } else {
            setName("putstr");
        }
    }

    public boolean isInteger() {
        return operand.getType().isIntegerTy();
    }

    @Override
    public IAsmWriter printAsm(IAsmWriter out) {
        out.push("call").pushSpace();
        getType().printAsm(out).pushSpace();

        out.push("@").push(getName()).push('(');
        if (isInteger()) {
            getOperand().printUse(out);
        } else {
            out.push("i8* getelementptr inbounds (");
            ((PointerType) getOperand().getType()).getElementType().printAsm(out);
            out.push(", ");
            getOperand().printUse(out);
            out.push(", i64 0, i64 0)");
        }
        return out.push(')').pushNewLine();
    }
}
