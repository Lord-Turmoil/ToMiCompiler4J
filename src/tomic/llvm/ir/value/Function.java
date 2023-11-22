package tomic.llvm.ir.value;

import tomic.llvm.ir.SlotTracker;
import tomic.llvm.ir.type.FunctionType;
import tomic.llvm.ir.type.Type;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Function extends GlobalValue {
    private final ArrayList<Argument> arguments;
    private final LinkedList<BasicBlock> basicBlocks;
    private final SlotTracker slotTracker = new SlotTracker();

    public Function(Type type, String name, List<Argument> arguments) {
        super(ValueTypes.FunctionTy, type, name);
        this.arguments = new ArrayList<>(arguments);
        this.basicBlocks = new LinkedList<>();
    }

    public Function(Type type, String name) {
        this(type, name, new ArrayList<>());
    }

    public BasicBlock newBasicBlock() {
        var block = new BasicBlock(this);
        insertBasicBlock(block);
        return block;
    }

    public void insertBasicBlock(BasicBlock basicBlock) {
        basicBlock.setParent(this);
        basicBlocks.add(basicBlock);
    }

    public void removeBasicBlock(BasicBlock basicBlock) {
        basicBlocks.remove(basicBlock);
    }

    public LinkedList<BasicBlock> getBasicBlocks() {
        return basicBlocks;
    }

    public int getArgumentCount() {
        return arguments.size();
    }

    public Argument getArgument(int index) {
        return arguments.get(index);
    }

    public void addArgument(Argument argument) {
        arguments.add(argument);
    }

    public ArrayList<Argument> getArguments() {
        return arguments;
    }

    public Type getReturnType() {
        return ((FunctionType) getType()).getReturnType();
    }

    public SlotTracker getSlotTracker() {
        return slotTracker;
    }

    // A utility method to get the slot of a value in this function.
    public int slot(Value value) {
        return slotTracker.slot(value);
    }
}
