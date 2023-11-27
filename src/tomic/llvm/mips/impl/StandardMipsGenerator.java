/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.mips.impl;

import lib.twio.ITwioWriter;
import tomic.llvm.ir.Module;
import tomic.llvm.ir.type.Type;
import tomic.llvm.ir.value.*;
import tomic.llvm.ir.value.inst.Instruction;
import tomic.llvm.mips.IMipsGenerator;
import tomic.llvm.mips.IMipsWriter;
import tomic.llvm.mips.memory.MemoryProfile;
import tomic.llvm.mips.memory.impl.DefaultRegisterProfile;
import tomic.llvm.mips.memory.impl.DefaultStackProfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StandardMipsGenerator implements IMipsGenerator {
    private IMipsWriter out;
    private Module module;
    private MemoryProfile memoryProfile;
    private final Map<GlobalString, String> globalStringNameMap = new HashMap<>();

    @Override
    public void generate(Module module, ITwioWriter output) {
        this.module = module;
        this.out = new VerboseMipsWriter(output);

        generateHeader();

        generateData();
        out.pushNewLine();

        generateText();
    }

    private void generateData() {
        out.push(".data").pushNewLine();
        for (var variable : module.getGlobalVariables()) {
            out.pushIndent();
            generateGlobalVariable(variable);
        }
        for (var globalString : module.getGlobalStrings()) {
            out.pushIndent();
            generateGlobalString(globalString);
        }
    }

    private void generateText() {
        out.push(".text").pushNewLine();
        module.getFunctions().forEach(this::generateFunction);
        generateFunction(module.getMainFunction());
    }

    private void generateGlobalVariable(GlobalVariable variable) {
        out.push(variable.getName()).push(":").pushSpace();
        out.push(".word").pushSpace();
        if (variable.getInitializer() != null) {
            generateInitializer(variable.getInitializer());
        } else {
            generateConsistentInitializer(variable.getPointerType().getElementType(), 0);
        }
        out.pushNewLine();
    }

    private void generateInitializer(ConstantData data) {
        if (data.isAllZero()) {
            generateConsistentInitializer(data.getType(), 0);
        } else {
            List<Integer> values = getValues(data);
            boolean first = true;
            for (var value : values) {
                if (first) {
                    first = false;
                } else {
                    out.push(',').pushSpace();
                }
                out.push(String.valueOf(value));
            }
        }
    }

    private void generateConsistentInitializer(Type type, int value) {
        int n = type.getBytes() / 4;
        if (n == 1) {
            out.push(String.valueOf(value));
        } else {
            out.push(String.valueOf(value)).push(':').push(String.valueOf(n));
        }
    }

    private List<Integer> getValues(ConstantData data) {
        if (data.isArray()) {
            var values = new ArrayList<Integer>();
            for (var value : data.getValues()) {
                values.addAll(getValues(value));
            }
            return values;
        } else {
            return new ArrayList<>(List.of(data.getValue()));
        }
    }

    private void generateGlobalString(GlobalString globalString) {
        out.push(getGlobalStringName(globalString)).push(":").pushSpace();
        out.push(".asciiz").pushSpace();
        out.push("\"").push(globalString.getValue().replace("\n", "\\n")).push("\"").pushNewLine();
    }

    private String getGlobalStringName(GlobalString globalString) {
        if (globalStringNameMap.containsKey(globalString)) {
            return globalStringNameMap.get(globalString);
        }

        String name = "__" + globalString.getName().replace('.', '_');
        while (isGlobalStringNameUsed(name)) {
            name += "_";
        }
        globalStringNameMap.put(globalString, name);
        return name;
    }

    /**
     * We only need to check if global variable or function takes the name.
     * Since global string name itself won't clash with other global strings.
     *
     * @param name Name to check.
     * @return True if the name is used.
     */
    private boolean isGlobalStringNameUsed(String name) {
        if (module.getGlobalVariables().stream().anyMatch(variable -> variable.getName().equals(name))) {
            return true;
        }
        return module.getFunctions().stream().anyMatch(function -> function.getName().equals(name));
    }

    private void generateFunction(Function function) {
        parseFunctionPreamble();
    }

    /**
     * Initialize the memory profile for the function.
     */
    private void parseFunctionPreamble() {
        var stackProfile = new DefaultStackProfile();
        var registerProfile = new DefaultRegisterProfile(stackProfile, out);
        memoryProfile = new MemoryProfile(registerProfile, stackProfile);
    }

    private void parseBasicBlock(BasicBlock basicBlock) {
        // TODO
    }

    private void parseInstruction(Instruction instruction) {
        // TODO
    }

    private void generateHeader() {
        out.pushComment("This file is generated by ToMiC4J");
        out.pushComment("MIPS Version: 1.0.2").pushNewLine();
    }
}
