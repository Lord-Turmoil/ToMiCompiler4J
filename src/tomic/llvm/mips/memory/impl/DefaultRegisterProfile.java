/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.mips.memory.impl;

import tomic.llvm.asm.IAsmWriter;
import tomic.llvm.ir.value.Value;
import tomic.llvm.mips.IMipsGenerator;
import tomic.llvm.mips.IMipsWriter;
import tomic.llvm.mips.memory.IRegisterProfile;
import tomic.llvm.mips.memory.IStackProfile;
import tomic.llvm.mips.memory.Register;
import tomic.llvm.mips.memory.Registers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DefaultRegisterProfile implements IRegisterProfile {
    private final Set<Integer> ALL_REGISTERS = Set.of(
            Registers.T0, Registers.T1, Registers.T2, Registers.T3,
            Registers.T4, Registers.T5, Registers.T6, Registers.T7,
            Registers.S0, Registers.S1, Registers.S2, Registers.S3,
            Registers.S4, Registers.S5, Registers.S6, Registers.S7);

    private final Set<Integer> availableRegisters;
    private final Set<Integer> activeRegisters;
    private final Map<Value, Register> valueRegisterMap;
    private final Map<Integer, Register> registerMap;

    private final IStackProfile stackProfile;
    private final IMipsWriter out;

    public DefaultRegisterProfile(IStackProfile stackProfile, IMipsWriter out) {
        this.availableRegisters = new HashSet<>(ALL_REGISTERS);
        this.activeRegisters = new HashSet<>();
        this.valueRegisterMap = new HashMap<>();
        this.registerMap = new HashMap<>();

        this.stackProfile = stackProfile;
        this.out = out;
    }

    @Override
    public Register acquire(Value value) {
        var register = valueRegisterMap.getOrDefault(value, null);
        if (register != null && register.isActive()) {
            register.setHot();
            return register;
        }

        if (register == null) {
            register = allocateRegister(value);
            valueRegisterMap.put(value, register);
        }

        // swapIn ensures that the register is active.
        swapIn(register);

        return register;
    }

    @Override
    public Register retain(Value value) {
        return acquire(value);
    }

    @Override
    public void yield(Value value) {
        var register = valueRegisterMap.getOrDefault(value, null);
        if (register == null) {
            throw new IllegalStateException("Value " + value + " is not allocated a register.");
        }

        if (register.isActive()) {
            register.setCold();
        }
    }

    @Override
    public void release(Value value) {
        var register = valueRegisterMap.getOrDefault(value, null);
        if (register != null) {
            swapOut(register);
            valueRegisterMap.remove(value);
        }
    }

    private Register allocateRegister(Value value) {
        return new Register(value);
    }

    private void swapIn(Register register) {
        if (register.isActive()) {
            return;
        }

        // If there is no available register, then we have to swap out
        int id;
        if (availableRegisters.isEmpty()) {
            var candidate = findSwapOutCandidate();
            swapOut(candidate);
            id = candidate.getId();
        } else {
            id = availableRegisters.iterator().next();
        }

        register.activate(id);
        register.setHot();

        availableRegisters.remove(id);
        registerMap.put(id, register);

        // TODO: load value from memory if exists in StackProfile
    }

    private void swapOut(Register register) {
        if (!register.isActive()) {
            return;
        }

        // TODO: write back value to memory, and allocate memory
        //      in StackProfile if necessary
        register.deactivate();

        availableRegisters.add(register.getId());
        registerMap.remove(register.getId());
    }

    private Register findSwapOutCandidate() {
        int minPriority = 0;
        int candidate = Registers.INVALID;
        for (var entry : registerMap.entrySet()) {
            if (!entry.getValue().isActive()) {
                continue;
            }

            var register = entry.getValue();
            if (register.getPriority() > minPriority) {
                minPriority = register.getPriority();
                candidate = register.getId();
            }
        }

        if (candidate == Registers.INVALID) {
            throw new IllegalStateException("No available register to swap out.");
        }

        return registerMap.get(candidate);
    }
}
