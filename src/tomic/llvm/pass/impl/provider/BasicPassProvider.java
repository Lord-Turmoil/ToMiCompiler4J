/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.pass.impl.provider;

import tomic.llvm.pass.IPassProvider;
import tomic.llvm.pass.PassManager;

/**
 * Provide a set of basic passes for LLVM Pass Manager.
 * Does not do anything, actually. :P
 */
public class BasicPassProvider implements IPassProvider {
    @Override
    public void registerPasses(PassManager manager) {
    }
}
