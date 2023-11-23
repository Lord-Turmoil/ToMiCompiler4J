package tomic.llvm.ir.value.inst;

import tomic.llvm.asm.IAsmWriter;
import tomic.llvm.ir.type.IntegerType;
import tomic.llvm.ir.type.Type;
import tomic.llvm.ir.value.Value;
import tomic.llvm.ir.value.ValueTypes;

public class ZExtInst extends CastInst {
    public ZExtInst(Type type, Value operand) {
        super(ValueTypes.ZExtInstTy, type, operand);
    }

    public static ZExtInst newInstance(Value operand, int bitWidth) {
        return new ZExtInst(IntegerType.get(operand.getContext(), bitWidth), operand);
    }

    public static ZExtInst toInt32(Value operand) {
        return newInstance(operand, 32);
    }

    /**
     * %4 = zext i1 %3 to i32
     */
    @Override
    public IAsmWriter printAsm(IAsmWriter out) {
        printName(out).pushNext('=').pushNext("zext").pushSpace();
        getOperand().printUse(out);
        out.pushNext("to").pushSpace();
        return getType().printAsm(out).pushNewLine();
    }
}
