/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.pass;

import tomic.llvm.ir.Module;

import java.util.ArrayList;

/**
 * Manage the passes.
 */
public class PassManager {
    private final ArrayList<ILlvmPass> passes;
    private final IPassProvider provider;

    public PassManager(IPassProvider provider) {
        this.passes = new ArrayList<>();
        this.provider = provider;
        provider.registerPasses(this);
    }

    /**
     * Register a pass.
     */
    public PassManager registerPass(ILlvmPass pass) {
        passes.add(pass);
        return this;
    }

    /**
     * Run all the passes.
     */
    public void run(Module module) {
        passes.forEach(pass -> pass.run(module));
        module.trace();
    }
}
