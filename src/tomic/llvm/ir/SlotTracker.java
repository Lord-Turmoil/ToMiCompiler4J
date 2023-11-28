/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.ir;

import tomic.llvm.ir.value.Function;
import tomic.llvm.ir.value.Value;

import java.util.HashMap;
import java.util.Map;

public class SlotTracker {
    private final Map<Value, Integer> valueSlot = new HashMap<>();

    public void trace(Function function) {
        valueSlot.clear();
        int slot = 0;

        for (var arg : function.getArguments()) {
            valueSlot.put(arg, slot++);
        }

        for (var block : function.getBasicBlocks()) {
            valueSlot.put(block, slot++);
            for (var inst : block.getInstructions()) {
                if (!inst.getType().isVoidTy()) {
                    valueSlot.put(inst, slot++);
                }
            }
        }
    }

    public int slot(Value value) {
        if (!valueSlot.containsKey(value)) {
            throw new IllegalStateException("Value not found in slot tracker: " + value);
        }
        return valueSlot.get(value);
    }
}
