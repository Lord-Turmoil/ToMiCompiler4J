package tomic.llvm.ir.value;

import tomic.llvm.asm.IAsmWriter;
import tomic.llvm.ir.SlotTracker;
import tomic.llvm.ir.type.FunctionType;
import tomic.llvm.ir.type.Type;
import tomic.llvm.ir.value.inst.ReturnInst;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Function extends GlobalValue {
    private final ArrayList<Argument> arguments;
    private final LinkedList<BasicBlock> basicBlocks;
    private final SlotTracker slotTracker = new SlotTracker();

    public static Function newInstance(Type returnType, String name, List<Argument> arguments) {
        ArrayList<Type> argTypes = new ArrayList<>();
        for (var arg : arguments) {
            argTypes.add(arg.getType());
        }

        return new Function(FunctionType.get(returnType, argTypes), name, arguments);
    }

    public static Function newInstance(Type returnType, String name) {
        return new Function(FunctionType.get(returnType), name);
    }

    private Function(Type type, String name, List<Argument> arguments) {
        super(ValueTypes.FunctionTy, type, name);
        this.arguments = new ArrayList<>(arguments);
        this.basicBlocks = new LinkedList<>();

        this.arguments.forEach(arg -> arg.setParent(this));
    }

    private Function(Type type, String name) {
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

    @Override
    public IAsmWriter printAsm(IAsmWriter out) {
        var type = (FunctionType) getType();
        getSlotTracker().trace(this);

        if (type.getReturnType().isVoidTy()) {
            var block = getBasicBlocks().getLast();
            if (block.getInstructions().isEmpty()) {
                block.insertInstruction(new ReturnInst(getContext()));
            } else {
                if (!(block.getInstructions().getLast() instanceof ReturnInst)) {
                    block.insertInstruction(new ReturnInst(getContext()));
                }
            }
        }

        out.pushNewLine();

        out.push("define").pushNext("dso_local").pushSpace();
        type.getReturnType().printAsm(out).pushSpace();

        printName(out).push('(');
        for (var arg : getArguments()) {
            if (arg != getArguments().get(0)) {
                out.push(", ");
            }
            arg.printAsm(out);
        }
        out.push(')');

        out.pushNext('{').pushNewLine();

        getBasicBlocks().forEach(block -> block.printAsm(out));

        return out.push('}').pushNewLine();
    }
}
