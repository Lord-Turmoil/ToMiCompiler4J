package tomic.llvm.ir.value.inst;

import tomic.llvm.asm.IAsmWriter;
import tomic.llvm.ir.value.BasicBlock;
import tomic.llvm.ir.value.Value;
import tomic.llvm.ir.value.ValueTypes;

public class BranchInst extends Instruction {
    private Value condition;
    private BasicBlock trueBlock;
    private BasicBlock falseBlock;

    public BranchInst(Value condition, BasicBlock trueBlock, BasicBlock falseBlock) {
        super(ValueTypes.BranchInstTy, condition.getContext().getVoidTy());
        this.condition = condition;
        this.trueBlock = trueBlock;
        this.falseBlock = falseBlock;

        addOperand(condition);
        addOperand(trueBlock);
        addOperand(falseBlock);
    }

    public Value getCondition() {
        return condition;
    }

    public BasicBlock getTrueBlock() {
        return trueBlock;
    }

    public BasicBlock getFalseBlock() {
        return falseBlock;
    }

    /**
     * br i1 %8, label %19, label %9
     */
    @Override
    public IAsmWriter printAsm(IAsmWriter out) {
        out.push("br").pushSpace();
        condition.printUse(out).push(',').pushSpace();
        trueBlock.printUse(out).push(',').pushSpace();
        return falseBlock.printUse(out).pushNewLine();
    }
}