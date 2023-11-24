/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

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

    public void removeOperands() {
        operands.forEach(operand -> operand.removeUser(this));
        operands.clear();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        removeOperands();
    }

    public Value getOperand(int index) {
        return operands.get(index);
    }
}
