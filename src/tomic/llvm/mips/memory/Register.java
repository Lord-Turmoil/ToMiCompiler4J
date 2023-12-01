/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.mips.memory;

import tomic.llvm.ir.value.Value;

/**
 * Represent a virtual register for each LLVM value.
 */
public class Register {
    /**
     * The value this register represents.
     */
    private Value value;

    /**
     * The priority of this register. This is the core of this
     * register allocation algorithm. The higher the priority is,
     * the more likely this register will be in register.
     * Zero is the highest priority, which at the same time, indicates
     * that this register CANNOT be swapped out in the current instruction.
     */
    private int priority;
    private boolean active;   // whether in register or memory

    /**
     * The id of this register, which may change during the
     * register allocation process. And it is only valid when
     * isActive is true.
     */
    private int id;

    /**
     * Whether this register is dirty. If it is, then it should be
     * written back to memory when it is swapped out.
     */
    private boolean dirty;

    /**
     * Indicate whether this register is a temporary register.
     * If it is, then it will be swapped out more easily, which
     * means it will not be saved when it is swapped out.
     */
    private boolean temporary;

    public Register(Value value, boolean temporary) {
        this.value = value;
        this.priority = 0;
        this.active = false;
        this.id = Registers.INVALID;
        this.dirty = false;
        this.temporary = temporary;
    }

    public Value getValue() {
        return value;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        if (priority < 0) {
            priority = 0;
        }
        this.priority = priority;
    }

    public void setHot() {
        this.priority = Registers.HOT_PRIORITY;
    }

    public void setCold() {
        this.priority = Registers.HIGH_PRIORITY;
    }

    public void tick() {
        priority++;
    }

    public void activate(int id) {
        this.active = true;
        this.id = id;
    }

    public void deactivate() {
        this.active = false;
        this.id = Registers.INVALID;
    }

    public boolean isActive() {
        return active;
    }

    public int getId() {
        return id;
    }

    public boolean isDirty() {
        return dirty;
    }

    public boolean isTemporary() {
        return temporary;
    }

    public void setTemporary(boolean temporary) {
        this.temporary = temporary;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
}
