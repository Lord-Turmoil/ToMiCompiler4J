/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.mips.memory;

import tomic.llvm.ir.type.Type;
import tomic.llvm.ir.value.Value;

/**
 * Providing stack profile for MIPS.
 */
public interface IStackProfile {
    /**
     * Allocate a value on stack.
     *
     * @param value The value to be allocated.
     * @return The offset from $sp. $sp - offset = address.
     */
    StackAddress allocate(Value value);

    StackAddress allocate(Value value, Type type);

    /**
     * Force allocate a value on stack at a specific address.
     *
     * @param value   The value to be allocated.
     * @param address The address to be allocated.
     * @return The address of the value.
     */
    StackAddress allocate(Value value, StackAddress address);

    /**
     * Deallocate a value on stack.
     *
     * @param value The value to be deallocated.
     */
    void deallocate(Value value);

    /**
     * Get the offset from $sp to the top of the stack. Used for
     * calculating the size of stack frame for function call.
     *
     * @return The offset from $sp to the top of the stack.
     */
    int getTotalOffset();

    /**
     * Get the address of a value.
     *
     * @param value The probably allocated value.
     * @return The address of the value. null if the value is not allocated.
     */
    StackAddress getAddress(Value value);
}
