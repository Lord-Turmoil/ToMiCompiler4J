package tomic.llvm.ir.value;

import tomic.llvm.ir.type.Type;

import java.util.LinkedList;

public class User extends Value {
    LinkedList<Value> operands;

    protected User(ValueTypes valueType, Type type) {
        super(valueType, type);
        operands = new LinkedList<>();
    }

    public void addOperand(Value operand) {
        operands.add(operand);
        operand.addUser(this);
    }

    public void removeOperand(Value operand) {
        operands.remove(operand);
        operand.removeUser(this);
    }

    public Value getOperand(int index) {
        return operands.get(index);
    }
}
