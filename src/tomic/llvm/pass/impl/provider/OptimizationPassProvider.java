/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.pass.impl.provider;

import tomic.llvm.pass.IPassProvider;
import tomic.llvm.pass.PassManager;
import tomic.llvm.pass.impl.pass.*;

/**
 * Provide a set of optimization passes for LLVM Pass Manager.
 */
public class OptimizationPassProvider implements IPassProvider {
    @Override
    public void registerPasses(PassManager manager) {
        manager.registerPass(new RemoveRedundantLoadPass())
                .registerPass(new RemoveRedundantStorePass())
                .registerPass(new RemoveStoreLoadPass())
                .registerPass(new CombineUnaryOperatorPass())
                .registerPass(new CombineCommonExpressionPass())
                .registerPass(new RemoveUnusedInstPass())
                .registerPass(new RemoveEmptyBasicBlocksPass());
    }
}
