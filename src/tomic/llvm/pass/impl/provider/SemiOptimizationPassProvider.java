/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.pass.impl.provider;

import tomic.llvm.pass.IPassProvider;
import tomic.llvm.pass.PassManager;
import tomic.llvm.pass.impl.pass.CombineUnaryOperatorPass;
import tomic.llvm.pass.impl.pass.RemoveEmptyBasicBlocksPass;
import tomic.llvm.pass.impl.pass.RemoveRedundantLoadPass;
import tomic.llvm.pass.impl.pass.RemoveRedundantStorePass;

public class SemiOptimizationPassProvider implements IPassProvider {
    @Override
    public void registerPasses(PassManager manager) {
        manager.registerPass(new RemoveRedundantLoadPass());
        manager.registerPass(new RemoveRedundantStorePass());
        manager.registerPass(new CombineUnaryOperatorPass());
        manager.registerPass(new RemoveEmptyBasicBlocksPass());
    }
}
