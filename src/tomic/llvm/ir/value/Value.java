package tomic.llvm.ir.value;

import tomic.llvm.asm.IAsmWriter;
import tomic.llvm.ir.LlvmContext;
import tomic.llvm.ir.type.Type;

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
        users.add(user);
    }

    public void removeUser(User user) {
        users.remove(user);
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
}
