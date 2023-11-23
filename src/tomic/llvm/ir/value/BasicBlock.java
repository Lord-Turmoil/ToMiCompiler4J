package tomic.llvm.ir.value;

import tomic.llvm.asm.IAsmWriter;
import tomic.llvm.ir.LlvmContext;
import tomic.llvm.ir.value.inst.Instruction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

public class BasicBlock extends Value {
    private Function parent;
    private final LinkedList<Instruction> instructions;

    public BasicBlock(LlvmContext context) {
        super(ValueTypes.BasicBlockTy, context.getLabelTy());
        this.parent = null;
        instructions = new LinkedList<>();
    }

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
            String slot = String.valueOf(getParent().slot(this));
            out.pushNewLine().push(slot).push(':');

            if (!getUsers().isEmpty()) {
                out.pushSpaces(50 - slot.length() - 1);
                out.commentBegin();
                out.push("preds = ");
                boolean first = true;
                ArrayList<BasicBlock> preds = getUsers().stream()
                        .filter(user -> user instanceof Instruction inst && inst.getParent() != this)
                        .collect(ArrayList::new, (list, user) -> list.add(((Instruction) user).getParent()), ArrayList::addAll);
                Collections.reverse(preds);
                for (var pred : preds) {
                    if (!first) {
                        out.push(", ");
                    }
                    pred.printName(out);
                    first = false;
                }
            }
            out.commentEnd();
        }

        getInstructions().forEach(inst -> inst.printAsm(out.pushSpaces(4)));

        return out;
    }

    @Override
    public IAsmWriter printName(IAsmWriter out) {
        return out.push('%').push(String.valueOf(getParent().slot(this)));
    }

    @Override
    public IAsmWriter printUse(IAsmWriter out) {
        getType().printAsm(out).pushSpace();
        return printName(out);
    }
}
