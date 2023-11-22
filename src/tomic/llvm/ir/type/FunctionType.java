package tomic.llvm.ir.type;

import tomic.llvm.asm.IAsmWriter;

import java.util.ArrayList;
import java.util.List;

public class FunctionType extends Type {
    private final Type returnType;
    private final List<Type> paramTypes;

    public FunctionType(Type returnType, List<Type> paramTypes) {
        super(returnType.getContext(), TypeID.FunctionTyID);
        this.returnType = returnType;
        this.paramTypes = paramTypes == null ? new ArrayList<>() : paramTypes;
    }

    public FunctionType(Type returnType) {
        this(returnType, new ArrayList<>());
    }

    public static FunctionType get(Type returnType, List<Type> paramTypes) {
        return returnType.getContext().getFunctionType(returnType, paramTypes);
    }

    public static FunctionType get(Type returnType) {
        return returnType.getContext().getFunctionType(returnType);
    }

    public Type getReturnType() {
        return returnType;
    }

    public List<Type> getParamTypes() {
        return paramTypes;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FunctionType other) {
            return returnType.equals(other.returnType) && paramTypes.equals(other.paramTypes);
        }
        return false;
    }

    public boolean match(Type returnType) {
        return this.returnType.equals(returnType) && paramTypes.isEmpty();
    }

    public boolean match(Type returnType, List<Type> paramTypes) {
        return this.returnType.equals(returnType) && this.paramTypes.equals(paramTypes);
    }

    @Override
    public void printAsm(IAsmWriter out) {
        getReturnType().printAsm(out);
        out.pushNext('(');
        boolean first = true;
        for (var arg : getParamTypes()) {
            if (!first) {
                out.push(", ");
            }
            arg.printAsm(out);
        }
        out.push(')');
    }
}
