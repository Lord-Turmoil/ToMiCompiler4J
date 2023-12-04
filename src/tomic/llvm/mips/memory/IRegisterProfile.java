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

    Register acquire(Value value, boolean temporary);

    /**
     * Acquire a specific register for the given value.
     * Use with caution as it may cause register conflict.
     *
     * @param value    The value asking for a register.
     * @param registerId The register to be allocated.
     * @return The register allocated for the value.
     */
    Register acquire(Value value, int registerId);

    Register acquire(Value value, int registerId, boolean temporary);

    Register get(Value value);

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
     * Force yield a register to memory.
     *
     * @param value The value asking for yielding.
     */
    void yield(Value value);

    /**
     * Try to yield register to memory. This will not really
     * yield the register, but only allocate memory for it.
     */
    void tryYield(Value value);

    void yieldAll();

    void tryYieldAll();

    /**
     * Release a register. It has nothing to do with the memory.
     * It just unlinks the register from the value, indicating
     * that the value will no longer be used. Does nothing if
     * the value doesn't have a register.
     *
     * @param value The value asking for releasing.
     */
    void release(Value value);

    /**
     * Tick the register profile. It will update the priority
     * of all registers.
     */
    void tick();

    int getReservedRegisterId();
}
