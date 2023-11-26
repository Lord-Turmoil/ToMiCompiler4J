/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.mips.memory;

import tomic.llvm.ir.value.Value;

/**
 * Providing register profile for MIPS.
 * It depends on StackProfile.
 */
public interface IRegisterProfile {
    /**
     * Acquire a register for the given value. The value
     * doesn't need to be allocated in memory.
     *
     * @param value The value asking for a register.
     * @return The register allocated for the value.
     */
    Register acquire(Value value);

    /**
     * Force retain a register so that it won't be swapped out
     * in the current instruction. If the register is not in
     * register, it will be loaded from memory.
     *
     * @param value The value asking for retaining.
     * @return The register retained.
     */
    Register retain(Value value);

    /**
     * Force yield a register so that it can be swapped out
     * easily by setting its priority to really low. If it is
     * already in memory, it will do nothing.
     *
     * @param value The value asking for yielding.
     */
    void yield(Value value);


    /**
     * Release a register. It has nothing to do with the memory.
     * It just unlinks the register from the value, indicating
     * that the value will no longer be used. Does nothing if
     * the value doesn't have a register.
     *
     * @param value The value asking for releasing.
     */
    void release(Value value);
}
