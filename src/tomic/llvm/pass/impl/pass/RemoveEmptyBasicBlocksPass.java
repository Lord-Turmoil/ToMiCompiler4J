/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.pass.impl.pass;

import tomic.llvm.ir.Module;
import tomic.llvm.ir.value.BasicBlock;
import tomic.llvm.ir.value.Function;
import tomic.llvm.ir.value.inst.BranchInst;
import tomic.llvm.ir.value.inst.JumpInst;
import tomic.llvm.pass.ILlvmPass;

import java.util.ArrayList;

/**
 * Clean up LLVM IR. This should be the last pass.
 * Remove empty basic blocks.
 */
public class RemoveEmptyBasicBlocksPass implements ILlvmPass {
    @Override
    public void run(Module module) {
        for (var function : module.getAllFunctions()) {
            if (cleanUpFunction(function)) {
                while (cleanUpFunction(function)) {
                    continue;
                }
                replaceBranchWithSameTarget(function);
            }
        }
    }

    /**
     * Remove basic block that only has one jump or branch instruction.
     * Previous refactor already ensured that there is no really
     * empty basic block. This might be done many times since clean up
     * will cause more empty blocks, which is ok to clear.
     *
     * @param function The function to clean up
     * @return If clean up happens.
     */
    private boolean cleanUpFunction(Function function) {
        var basicBlocks = function.getBasicBlocks();
        ArrayList<JumpPair> blocksToRemove = new ArrayList<>();

        for (int i = 0; i < basicBlocks.size() - 1; i++) {
            var basicBlock = basicBlocks.get(i);
            var nextBlock = basicBlocks.get(i + 1);

            var instructions = basicBlock.getInstructions();
            if (instructions.size() > 1) {
                continue;
            }

            var instruction = instructions.get(0);
            if (instruction instanceof JumpInst inst) {
                if (inst.getTarget() == nextBlock) {
                    inst.removeOperands();
                    blocksToRemove.add(new JumpPair(basicBlock, nextBlock));
                }
            }
        }

        if (blocksToRemove.isEmpty()) {
            return false;
        }

        removeBasicBlocks(function, blocksToRemove);

        return true;
    }

    private void removeBasicBlocks(Function function, ArrayList<JumpPair> blocksToRemove) {
        // First rewire the jump instructions to these blocks.
        for (var block : blocksToRemove) {
            var preds = block.from().getPredecessors();
            for (var pred : preds) {
                var instruction = pred.getInstructions().getLast();
                if (instruction instanceof JumpInst inst) {
                    if (inst.getTarget() == block.from()) {
                        inst.setTarget(block.to());
                    } else {
                        throw new RuntimeException("JumpInst target not match");
                    }
                } else if (instruction instanceof BranchInst inst) {
                    if (inst.getTrueBlock() == block.from()) {
                        inst.setTrueBlock(block.to());
                    }
                    if (inst.getFalseBlock() == block.from()) {
                        inst.setFalseBlock(block.to());
                    }
                }
            }
        }

        for (var block : blocksToRemove) {
            function.removeBasicBlock(block.from());
        }
    }

    private void replaceBranchWithSameTarget(Function function) {
        for (var block : function.getBasicBlocks()) {
            var instruction = block.getInstructions().getLast();
            if (instruction instanceof BranchInst inst) {
                if (inst.getTrueBlock() == inst.getFalseBlock()) {
                    var target = inst.getTrueBlock();
                    block.removeInstruction(inst);
                    block.insertInstruction(new JumpInst(target));
                }
            }
        }
    }

    private record JumpPair(BasicBlock from, BasicBlock to) {}
}
