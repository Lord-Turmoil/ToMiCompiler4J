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
    int allocateOnStack(Value value);
    int allocateOnStack(Value value, Type type);

    /**
     * Get the offset from $sp to the top of the stack. Used for
     * calculating the size of stack frame for function call.
     *
     * @return The offset from $sp to the top of the stack.
     */
    int getStackTopOffset();

    /**
     * Allocate a value to $fp.
     *
     * @param value The value to be allocated.
     * @return The offset from $fp. $fp + offset = address.
     */
    int allocateTransient(Value value);
    int allocateTransient(Value value, Type type);

    /**
     * Get the offset from $fp to the top of the transient stack. Used for
     * calculating the size of function frame for function call.
     *
     * @return The offset from $fp to the top of the transient stack.
     */
    int getTransientTopOffset();

    /**
     * Get the address of a value.
     *
     * @param value The probably allocated value.
     * @return The address of the value. null if the value is not allocated.
     */
    StackAddress getAddress(Value value);
}
