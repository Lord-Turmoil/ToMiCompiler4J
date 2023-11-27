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
import tomic.llvm.ir.value.inst.*;
import tomic.llvm.mips.IMipsGenerator;
import tomic.llvm.mips.IMipsWriter;
import tomic.llvm.mips.memory.MemoryProfile;
import tomic.llvm.mips.memory.Registers;
import tomic.llvm.mips.memory.impl.DefaultRegisterProfile;
import tomic.llvm.mips.memory.impl.DefaultStackProfile;

import java.util.ArrayList;
import java.util.List;

public class StandardMipsGenerator implements IMipsGenerator {
    private IMipsWriter out;
    private Module module;
    private MemoryProfile memoryProfile;

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
        out.setIndent(1);
        module.getGlobalVariables().forEach(this::generateGlobalVariable);
        module.getGlobalStrings().forEach(this::generateGlobalString);
        out.setIndent(0);
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

    /**
     * MIPS support label with '.', so we don't need to rename the label.
     */
    private void generateGlobalString(GlobalString globalString) {
        out.push(globalString.getName()).push(":").pushSpace();
        out.push(".asciiz").pushSpace();
        out.push("\"").push(globalString.getValue().replace("\n", "\\n")).push("\"").pushNewLine();
    }

    private void generateFunction(Function function) {
        generateFunctionPreamble();
        out.pushNewLine();
        out.push(function.getName()).push(":").pushNewLine();
        for (var basicBlock : function.getBasicBlocks()) {
            generateBasicBlock(basicBlock);
        }
    }

    /**
     * Initialize the memory profile for the function.
     */
    private void generateFunctionPreamble() {
        var stackProfile = new DefaultStackProfile();
        var registerProfile = new DefaultRegisterProfile(stackProfile, out);
        memoryProfile = new MemoryProfile(registerProfile, stackProfile);
    }

    private void generateBasicBlock(BasicBlock basicBlock) {
        if (basicBlock.getIndex() != 0) {
            out.pushNewLine();
        }

        out.pushIndent().push(getLabelName(basicBlock)).push(":").pushNewLine();
        out.setIndent(2);
        for (var instruction : basicBlock.getInstructions()) {
            generateInstruction(instruction);
        }
        out.setIndent(0);
    }

    private String getLabelName(BasicBlock basicBlock) {
        return ".L." + basicBlock.getIndex();
    }

    private void generateInstruction(Instruction instruction) {
        if (instruction instanceof AllocaInst inst) {
            generateAllocaInst(inst);
        } else if (instruction instanceof LoadInst inst) {
            generateLoadInst(inst);
        } else if (instruction instanceof StoreInst inst) {
            generateStoreInst(inst);
        } else if (instruction instanceof OutputInst inst) {
            generateOutputInst(inst);
        }
        memoryProfile.tick();
    }

    private void generateAllocaInst(AllocaInst inst) {
        memoryProfile.getStackProfile().allocateOnStack(inst, inst.getAllocatedType());
    }

    /**
     * la $t0, {globalStringName} <br />
     * lw $t0, 0($t1)
     */
    private void generateLoadInst(LoadInst inst) {
        if (inst.getType().isIntegerTy()) {
            generateLoadWord(inst, inst.getAddress());
        } else if (inst.getType().isPointerTy()) {
            generateLoadAddress(inst, inst.getAddress());
        } else {
            throw new UnsupportedOperationException("Unsupported load type: " + inst.getType());
        }
    }

    /**
     * Generate lw instruction. <br />
     * lw $t0, 0($t1)
     * lw $t0, ($t1)
     *
     * @param value   The value to be loaded.
     * @param address The address of the value.
     */
    private void generateLoadWord(Value value, Value address) {
        var profile = memoryProfile.getRegisterProfile();
        out.push("lw").pushSpace();
        var op1 = profile.acquire(value);
        out.pushRegister(op1.getId()).pushComma().pushSpace();
        generateAddress(address);
        out.pushNewLine();
    }

    /**
     * Generate la instruction. <br />
     * la $t0, 0($t1)
     * la $t0, ($t1)
     *
     * @param value   The value to be loaded.
     * @param address The address of the value.
     */
    private void generateLoadAddress(Value value, Value address) {
        var profile = memoryProfile.getRegisterProfile();
        out.push("la").pushSpace();
        var op1 = profile.acquire(value);
        out.pushRegister(op1.getId()).pushComma();
        generateAddress(address);
        out.pushNewLine();
    }

    /**
     * Generate li instruction. <br />
     * li $t0, 66
     *
     * @param value     The value to be loaded.
     * @param immediate The immediate value.
     */
    private void generateLoadImmediate(Value value, int immediate) {
        var profile = memoryProfile.getRegisterProfile();
        out.push("li").pushSpace();
        var reg = profile.acquire(value, true);
        out.pushRegister(reg.getId()).pushComma();
        out.pushNext(String.valueOf(immediate)).pushNewLine();
    }

    private void generateStoreInst(StoreInst inst) {
        var lhs = inst.getLeftOperand();
        /*
         * Generate li for immediate value. We are sure here
         * we won't meet an array or pointer.
         */
        if (lhs instanceof ConstantData constant) {
            generateLoadImmediate(constant, constant.getValue());
        }

        var rhs = inst.getRightOperand();
        generateStoreWord(lhs, rhs);
    }

    /**
     * Generate sw instruction. <br />
     * sw $t0, 0($t1) <br />
     * sw $t0, ($t1) <br />
     * sw $t0, -4($sp) <br />
     * sw $t0, 4($fp)
     */
    private void generateStoreWord(Value value, Value address) {
        var registerProfile = memoryProfile.getRegisterProfile();

        out.push("sw").pushSpace();
        var op1 = registerProfile.acquire(value);
        out.pushRegister(op1.getId()).pushComma().pushSpace();
        generateAddress(address);
        out.pushNewLine();
    }

    private void generateOutputInst(OutputInst inst) {
        var value = inst.getOperand();
        if (value instanceof GlobalString string) {
            out.push("la").pushSpace();
            out.pushRegister(Registers.A0).pushComma();
            out.pushNext(string.getName()).pushNewLine();
            generateSysCall(SYS_PRINT_STRING);
        } else {
        }
    }

    private void generateHeader() {
        out.pushComment("This file is generated by ToMiC4J");
        out.pushComment("MIPS Version: 1.0.2").pushNewLine();
    }

    /**
     * li $v0, {service} <br />
     * syscall
     */
    private void generateSysCall(int service) {
        out.push("li").pushSpace();
        out.pushRegister(Registers.V0).pushComma();
        out.pushNext(String.valueOf(service)).pushNewLine();
        out.push("syscall").pushNewLine();
    }

    private void generateAddress(Value address) {
        if (address instanceof GlobalVariable) {
            out.push(address.getName());
        } else if (address instanceof AllocaInst) {
            var add = memoryProfile.getStackProfile().getAddress(address);
            if (add.base() == Registers.SP) {
                out.push(String.valueOf(-add.offset()));
            } else {
                out.push(String.valueOf(add.offset()));
            }
            out.push('(');
            out.pushRegister(add.base()).push(')');
        } else {
            var reg = memoryProfile.getRegisterProfile().acquire(address);
            out.pushRegister(reg.getId());
        }
    }

    public static final int SYS_EXIT = 10;
    public static final int SYS_EXIT2 = 17;
    public static final int SYS_READ_INT = 5;
    public static final int SYS_PRINT_INT = 1;
    public static final int SYS_PRINT_STRING = 4;
}
