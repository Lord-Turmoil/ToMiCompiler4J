/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.ir;

import tomic.llvm.ir.value.Function;
import tomic.llvm.ir.value.GlobalString;
import tomic.llvm.ir.value.GlobalVariable;
import tomic.llvm.ir.value.Value;

import java.util.ArrayList;

public class Module {
    private final String name;
    private final LlvmContext context = new LlvmContext();
    private final ArrayList<GlobalVariable> globalVariables = new ArrayList<>();
    private final ArrayList<GlobalString> globalStrings = new ArrayList<>();
    private final ArrayList<Function> functions = new ArrayList<>();
    private ArrayList<Function> allFunctions;

    Function mainFunction;

    public Module(String name) {
        this.name = name;
    }

    public void trace() {
        for (var function : functions) {
            function.trace();
        }
        mainFunction.trace();
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
        if (globalStrings.contains(globalString)) {
            return;
        }
        globalStrings.add(globalString);
    }

    public ArrayList<Function> getFunctions() {
        return functions;
    }

    public Function getMainFunction() {
        return mainFunction;
    }

    public ArrayList<Function> getAllFunctions() {
        if (allFunctions != null) {
            return allFunctions;
        }
        allFunctions = new ArrayList<>();
        allFunctions.addAll(functions);
        allFunctions.add(mainFunction);
        return allFunctions;
    }

    public void addFunction(Function function) {
        if (function.getName().equals("main")) {
            mainFunction = function;
        } else {
            functions.add(function);
        }
    }

    public String getName() {
        return name;
    }

    public void refactor() {
        globalVariables.forEach(Value::refactor);
        globalStrings.forEach(Value::refactor);
        functions.forEach(Value::refactor);
        mainFunction.refactor();
    }
}
