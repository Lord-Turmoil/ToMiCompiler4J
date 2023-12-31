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

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

public class DefaultStackProfile implements IStackProfile {
    private final Map<Value, StackAddress> addressMap = new HashMap<>();
    private int totalOffset = 0;
    private final BitSet allocated = new BitSet();

    @Override
    public StackAddress allocate(Value value) {
        return allocate(value, value.getBytes());
    }

    @Override
    public StackAddress allocate(Value value, Type type) {
        return allocate(value, type.getBytes());
    }

    @Override
    public StackAddress allocate(Value value, StackAddress address) {
        if (addressMap.containsKey(value)) {
            deallocate(value);
        }
        addressMap.put(value, address);

        return address;
    }

    private StackAddress allocate(Value value, int size) {
        // Round up to 4 bytes.
        size = Math.max(1, size);
        size = (size + 3) & ~3;

        int offset = findAvailableOffset(size);
        StackAddress address;
        if (offset == -1) {
            totalOffset -= size;
            offset = totalOffset;
        } else {
            offset = -offset;
        }

        address = new StackAddress(value, Registers.SP, offset, size);
        allocated.set(-offset - size, -offset);
        addressMap.put(value, address);

        return address;
    }

    @Override
    public void deallocate(Value value) {
        var address = addressMap.get(value);
        if (address == null) {
            return;
        }

        addressMap.remove(value);
        allocated.clear(-address.offset(), -address.offset() + address.size());
        while (totalOffset > 0 && !allocated.get(totalOffset - 1)) {
            totalOffset--;
        }
    }

    private int findAvailableOffset(int size) {
        int offset = 0;
        while (offset < Math.abs(totalOffset)) {
            if (allocated.get(offset)) {
                offset++;
            } else {
                int i = 0;
                while (i < size) {
                    if (allocated.get(offset + i)) {
                        break;
                    }
                    i++;
                }
                if (i == size) {
                    return offset + size;
                } else {
                    offset += i;
                }
            }
        }
        return -1;
    }

    @Override
    public int getTotalOffset() {
        return totalOffset;
    }

    @Override
    public StackAddress getAddress(Value value) {
        return addressMap.getOrDefault(value, null);
    }
}
