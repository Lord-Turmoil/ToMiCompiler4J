package tomic.llvm.ir.value;

import tomic.llvm.ir.value.inst.Instruction;

import java.util.LinkedList;

public class BasicBlock extends Value {
    private Function parent;
    private final LinkedList<Instruction> instructions;

    public BasicBlock(Function parent) {
        super(ValueTypes.BasicBlockTy, parent.getContext().getLabelTy());
        this.parent = parent;
        instructions = new LinkedList<>();
    }

    public void setParent(Function function) {
        this.parent = function;
    }

    public Function getParent() {
        return parent;
    }

    public void insertInstruction(Instruction instruction) {
        instruction.setParent(this);
        instructions.add(instruction);
    }

    public void removeInstruction(Instruction instruction) {
        instructions.remove(instruction);
    }

    public LinkedList<Instruction> getInstructions() {
        return instructions;
    }
}
