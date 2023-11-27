/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.mips.memory.impl;

import tomic.llvm.ir.type.Type;
import tomic.llvm.ir.value.Value;
import tomic.llvm.mips.memory.IStackProfile;
import tomic.llvm.mips.memory.Registers;
import tomic.llvm.mips.memory.StackAddress;

import java.util.HashMap;
import java.util.Map;

public class DefaultStackProfile implements IStackProfile {
    private final Map<Value, StackAddress> addressMap = new HashMap<>();
    private int stackTopOffset = 0;
    private int transientTopOffset = 0;

    @Override
    public int allocateOnStack(Value value) {
        var address = new StackAddress(value, Registers.SP, stackTopOffset);
        addressMap.put(value, address);
        stackTopOffset += value.getBytes();
        return address.offset();
    }

    @Override
    public int allocateOnStack(Value value, Type type) {
        var address = new StackAddress(value, Registers.SP, stackTopOffset);
        addressMap.put(value, address);
        stackTopOffset += type.getBytes();
        return address.offset();
    }


    @Override
    public int getStackTopOffset() {
        return stackTopOffset;
    }

    @Override
    public int allocateTransient(Value value) {
        var address = new StackAddress(value, Registers.FP, transientTopOffset);
        addressMap.put(value, address);
        transientTopOffset += value.getBytes();
        return address.offset();
    }

    @Override
    public int allocateTransient(Value value, Type type) {
        var address = new StackAddress(value, Registers.FP, transientTopOffset);
        addressMap.put(value, address);
        transientTopOffset += type.getBytes();
        return address.offset();
    }

    @Override
    public int getTransientTopOffset() {
        return transientTopOffset;
    }

    @Override
    public StackAddress getAddress(Value value) {
        return addressMap.getOrDefault(value, null);
    }
}
