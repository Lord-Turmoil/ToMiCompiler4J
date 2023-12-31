/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.ir.value;

import tomic.llvm.asm.IAsmWriter;
import tomic.llvm.ir.LlvmContext;
import tomic.llvm.ir.type.*;

import java.util.LinkedList;

public class Value {
    private final ValueTypes valueType;
    private final Type type;
    protected final LinkedList<User> users;
    private String name;

    protected Value(ValueTypes valueType, Type type) {
        this.valueType = valueType;
        this.type = type;
        this.name = null;
        this.users = new LinkedList<>();
    }

    public void addUser(User user) {
        if (users.contains(user)) {
            return;
        }
        users.add(user);
    }

    public void addUsers(Iterable<User> users) {
        users.forEach(this::addUser);
    }

    public boolean removeUser(User user) {
        return users.remove(user);
    }

    public void removeUsers() {
        users.forEach(user -> user.removeOperand(this));
        users.clear();
    }

    public LinkedList<User> getUsers() {
        return users;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public IntegerType getIntegerType() {
        return (IntegerType) type;
    }

    public FunctionType getFunctionType() {
        return (FunctionType) type;
    }

    public ArrayType getArrayType() {
        return (ArrayType) type;
    }

    public PointerType getPointerType() {
        return (PointerType) type;
    }

    public LlvmContext getContext() {
        return type.getContext();
    }

    public IAsmWriter printAsm(IAsmWriter out) {
        throw new UnsupportedOperationException();
    }

    public IAsmWriter printName(IAsmWriter out) {
        throw new UnsupportedOperationException();
    }

    public IAsmWriter printUse(IAsmWriter out) {
        throw new UnsupportedOperationException();
    }

    /**
     * I cannot come up with a better name for this method.
     * After refactor, some getters may become invalid!
     */
    public void refactor() {
        // default behavior is doing nothing
    }

    public int getBytes() {
        return type.getBytes();
    }
}
