/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.mips.memory;

import tomic.llvm.mips.memory.IRegisterProfile;
import tomic.llvm.mips.memory.IStackProfile;

public class MemoryProfile {
    private final IRegisterProfile registerProfile;
    private final IStackProfile stackProfile;

    public MemoryProfile(IRegisterProfile registerProfile, IStackProfile stackProfile) {
        this.registerProfile = registerProfile;
        this.stackProfile = stackProfile;
    }

    public IRegisterProfile getRegisterProfile() {
        return registerProfile;
    }

    public IStackProfile getStackProfile() {
        return stackProfile;
    }

    public void tick() {
        registerProfile.tick();
    }
}
