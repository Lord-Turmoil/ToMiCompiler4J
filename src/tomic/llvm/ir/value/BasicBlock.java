package tomic.llvm.ir.value;

public class BasicBlock extends Value {
    private final Function parent;

    public BasicBlock(Function parent) {
        super(ValueTypes.BasicBlockTy, parent.getContext().getLabelTy());
        this.parent = parent;
    }

    public Function getParent() {
        return parent;
    }
}
