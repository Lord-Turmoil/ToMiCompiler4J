/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.pass;

import tomic.llvm.ir.Module;
import tomic.logger.debug.IDebugLogger;

import java.util.ArrayList;

/**
 * Manage the passes.
 */
public class PassManager {
    private final ArrayList<ILlvmPass> passes;
    private final IPassProvider provider;
    private final IDebugLogger logger;

    public PassManager(IPassProvider provider, IDebugLogger logger) {
        this.passes = new ArrayList<>();
        this.provider = provider;
        this.logger = logger;
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
        for (var pass : passes) {
            logger.info("Running pass: " + pass.getClass().getSimpleName());
            pass.run(module);
        }
        module.trace();
    }
}
