package tomic.llvm.ir;

import tomic.llvm.ir.value.Function;
import tomic.llvm.ir.value.GlobalString;
import tomic.llvm.ir.value.GlobalVariable;

import java.util.ArrayList;

public class Module {
    private final String name;
    private final LlvmContext context = new LlvmContext();
    private final ArrayList<GlobalVariable> globalVariables = new ArrayList<>();
    private final ArrayList<GlobalString> globalStrings = new ArrayList<>();
    private final ArrayList<Function> functions = new ArrayList<>();
    Function mainFunction;

    public Module(String name) {
        this.name = name;
    }

    public LlvmContext getContext() {
        return context;
    }

    public ArrayList<GlobalVariable> getGlobalVariables() {
        return globalVariables;
    }

    public void addGlobalVariable(GlobalVariable globalVariable) {
        globalVariables.add(globalVariable);
    }

    public ArrayList<GlobalString> getGlobalStrings() {
        return globalStrings;
    }

    public void addGlobalString(GlobalString globalString) {
        globalStrings.add(globalString);
    }

    public ArrayList<Function> getFunctions() {
        return functions;
    }

    public void addFunction(Function function) {
        if (function.getName().equals("main")) {
            mainFunction = function;
        } else {
            functions.add(function);
        }
    }
}
