/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.pass;

/**
 * Provide a set of passes for LLVM Pass Manager.
 */
public interface IPassProvider {
    void registerPasses(PassManager manager);
}
