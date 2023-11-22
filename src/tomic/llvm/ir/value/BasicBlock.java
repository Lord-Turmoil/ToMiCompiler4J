package tomic.llvm.ir.value;

import tomic.llvm.asm.IAsmWriter;
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

    @Override
    public IAsmWriter printAsm(IAsmWriter out) {
        var func = getParent();

        if (this != func.getBasicBlocks().getFirst()) {
            printName(out).pushNewLine();
        }

        getInstructions().forEach(inst -> inst.printAsm(out.pushSpaces(4)));

        return out;
    }

    @Override
    public IAsmWriter printName(IAsmWriter out) {
        return out.push(String.valueOf(getParent().slot(this)));
    }

    @Override
    public IAsmWriter printUse(IAsmWriter out) {
        getType().printAsm(out).pushSpace();
        return printName(out);
    }
}
