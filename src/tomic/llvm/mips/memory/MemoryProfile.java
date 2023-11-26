/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.mips.memory;

import lib.twio.ITwioWriter;
import tomic.llvm.asm.impl.VerboseAsmWriter;
import tomic.llvm.mips.memory.impl.DefaultRegisterProfile;
import tomic.llvm.mips.memory.impl.DefaultStackProfile;

public class MemoryProfile {
    private final IRegisterProfile registerProfile;
    private final IStackProfile stackProfile;

    public MemoryProfile(ITwioWriter out) {
        this.stackProfile = new DefaultStackProfile();
        this.registerProfile = new DefaultRegisterProfile(stackProfile, new VerboseAsmWriter(out));
    }

    public IRegisterProfile getRegisterProfile() {
        return registerProfile;
    }

    public IStackProfile getStackProfile() {
        return stackProfile;
    }
}
