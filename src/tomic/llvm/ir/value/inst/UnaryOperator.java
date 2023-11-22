package tomic.llvm.ir.value.inst;

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

    public UnaryOperator(Type type, Value operand, UnaryOpTypes opType) {
        super(ValueTypes.UnaryOperatorTy, type, operand);
        this.opType = opType;
    }

    public UnaryOpTypes getOpType() {
        return opType;
    }
}
