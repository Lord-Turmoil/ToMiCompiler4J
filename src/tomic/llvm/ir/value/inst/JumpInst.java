package tomic.llvm.ir.value.inst;

import tomic.llvm.asm.IAsmWriter;
import tomic.llvm.ir.value.BasicBlock;
import tomic.llvm.ir.value.ValueTypes;

// Just BranchInst without condition.
public class JumpInst extends Instruction {
    private BasicBlock target;

    public JumpInst(BasicBlock target) {
        super(ValueTypes.JumpInstTy, target.getContext().getVoidTy());
        this.target = target;
        addOperand(target);
    }

    public BasicBlock getTarget() {
        return target;
    }

    /**
     * br label %25
     */
    @Override
    public IAsmWriter printAsm(IAsmWriter out) {
        out.push("br").pushSpace();
        return target.printUse(out).pushNewLine();
    }
}
