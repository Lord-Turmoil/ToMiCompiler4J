package tomic.llvm.ir.value.inst;

import tomic.llvm.asm.IAsmWriter;
import tomic.llvm.ir.type.Type;
import tomic.llvm.ir.value.Value;
import tomic.llvm.ir.value.ValueTypes;

public class UnaryOperator extends UnaryInstruction {
    public enum UnaryOpTypes {
        Not,
        Neg,
        Pos
    }

    private final UnaryOpTypes opType;

    public UnaryOperator(Value operand, UnaryOpTypes opType) {
        super(ValueTypes.UnaryOperatorTy, operand.getType(), operand);
        this.opType = opType;
    }

    public UnaryOpTypes getOpType() {
        return opType;
    }


    @Override
    public IAsmWriter printAsm(IAsmWriter out) {
        String op = switch (opType) {
            case Not -> "add nsw";
            case Neg -> "sub nsw";
            default -> throw new IllegalStateException("Unexpected value: " + opType);
        };

        printName(out).pushNext('=').pushNext(op).pushSpace();

        getType().printAsm(out);
        out.pushNext('0').push(',').pushSpace();
        getOperand().printName(out);

        return out.pushNewLine();
    }
}
