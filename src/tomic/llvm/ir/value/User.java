/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.ir.value;

import tomic.llvm.ir.type.Type;

import java.util.LinkedList;
import java.util.List;

public abstract class User extends Value {
    LinkedList<Value> operands;

    protected User(ValueTypes valueType, Type type) {
        super(valueType, type);
        operands = new LinkedList<>();
    }

    public void addOperand(Value operand) {
        if (operands.contains(operand)) {
            return;
        }
        operands.add(operand);
        operand.addUser(this);
    }

    public void addOperands(Iterable<Value> operands) {
        operands.forEach(this::addOperand);
    }

    public boolean removeOperand(Value operand) {
        if (operands.contains(operand)) {
            operands.remove(operand);
            operand.removeUser(this);
            return true;
        }
        return false;
    }

    public boolean replaceOperand(Value oldOperand, Value newOperand) {
        if (removeOperand(oldOperand)) {
            addOperand(newOperand);
            return true;
        }
        return false;
    }

    public void removeOperands() {
        operands.forEach(operand -> operand.removeUser(this));
        operands.clear();
    }

    public Value getOperand(int index) {
        return operands.get(index);
    }

    public List<Value> getOperands() {
        return operands;
    }
}
