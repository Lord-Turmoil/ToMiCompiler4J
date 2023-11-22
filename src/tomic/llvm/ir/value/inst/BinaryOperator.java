package tomic.llvm.ir.value.inst;

import tomic.llvm.ir.type.Type;
import tomic.llvm.ir.value.Value;
import tomic.llvm.ir.value.ValueTypes;

public class BinaryOperator extends BinaryInstruction {
    public enum BinaryOpTypes {
        Add,
        Sub,
        Mul,
        Div,
        Mod
    }

    private final BinaryOpTypes opType;

    public BinaryOperator(Value lhs, Value rhs, BinaryOpTypes opType) {
        super(ValueTypes.BinaryOperatorTy, lhs.getType(), lhs, rhs);
        this.opType = opType;
    }

    public BinaryOpTypes getOpType() {
        return opType;
    }
}
