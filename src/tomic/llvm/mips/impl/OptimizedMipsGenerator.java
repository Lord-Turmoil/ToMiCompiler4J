/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.mips.impl;

import lib.twio.ITwioWriter;
import lib.twio.TwioBufferWriter;
import tomic.llvm.ir.Module;
import tomic.llvm.ir.type.Type;
import tomic.llvm.ir.value.*;
import tomic.llvm.ir.value.inst.*;
import tomic.llvm.mips.IMipsGenerator;
import tomic.llvm.mips.IMipsPrinter;
import tomic.llvm.mips.IMipsWriter;
import tomic.llvm.mips.memory.MemoryProfile;
import tomic.llvm.mips.memory.Registers;
import tomic.llvm.mips.memory.impl.DefaultRegisterProfile;
import tomic.llvm.mips.memory.impl.DefaultStackProfile;

import java.util.ArrayList;
import java.util.List;

/**
 * With some little optimizations.
 */
public class OptimizedMipsGenerator implements IMipsGenerator {
    private IMipsWriter out;
    private Module module;
    private MemoryProfile memoryProfile;
    private final IMipsPrinter printer = new StandardMipsPrinter();
    private int reservedRegisterId;

    @Override
    public void generate(Module module, ITwioWriter output) {
        this.module = module;
        this.out = new VerboseMipsWriter(output);

        generateHeader();

        generateData();
        out.pushNewLine();

        generateText();
    }

    private void generateData() {
        out.push(".data").pushNewLine();
        out.setIndent(1);
        module.getGlobalVariables().forEach(this::generateGlobalVariable);
        module.getGlobalStrings().forEach(this::generateGlobalString);
        out.setIndent(0);
    }

    private void generateText() {
        out.push(".text").pushNewLine();
        // Generate main function first.
        generateFunction(module.getMainFunction());
        module.getFunctions().forEach(this::generateFunction);
    }

    private void generateGlobalVariable(GlobalVariable variable) {
        out.push(variable.getName()).push(":").pushSpace();
        out.push(".word").pushSpace();
        if (variable.getInitializer() != null) {
            generateInitializer(variable.getInitializer());
        } else {
            generateConsistentInitializer(variable.getPointerType().getElementType(), 0);
        }
        out.pushNewLine();
    }

    private void generateInitializer(ConstantData data) {
        if (data.isAllZero()) {
            generateConsistentInitializer(data.getType(), 0);
        } else {
            List<Integer> values = getValues(data);
            boolean first = true;
            for (var value : values) {
                if (first) {
                    first = false;
                } else {
                    out.push(',').pushSpace();
                }
                out.push(String.valueOf(value));
            }
        }
    }

    private void generateConsistentInitializer(Type type, int value) {
        int n = type.getBytes() / 4;
        if (n == 1) {
            out.push(String.valueOf(value));
        } else {
            out.push(String.valueOf(value)).push(':').push(String.valueOf(n));
        }
    }

    private List<Integer> getValues(ConstantData data) {
        if (data.isArray()) {
            var values = new ArrayList<Integer>();
            for (var value : data.getValues()) {
                values.addAll(getValues(value));
            }
            return values;
        } else {
            return new ArrayList<>(List.of(data.getValue()));
        }
    }

    /**
     * MIPS support label with '.', so we don't need to rename the label.
     */
    private void generateGlobalString(GlobalString globalString) {
        out.push(globalString.getName()).push(":").pushSpace();
        out.push(".asciiz").pushSpace();
        out.push("\"").push(globalString.getValue().replace("\n", "\\n")).push("\"").pushNewLine();
    }

    private void generateFunction(Function function) {
        generateFunctionPreamble();

        out.pushNewLine();
        out.push(function.getName()).push(":").pushNewLine();
        // Store $ra register if not main.
        if (!function.getName().equals("main")) {
            out.pushIndent().pushIndent();
            printer.printSaveStack(out, Registers.RA, 0);
        }
        function.getBasicBlocks().forEach(this::generateBasicBlock);
    }

    /**
     * Initialize the memory profile for the function.
     */
    private void generateFunctionPreamble() {
        var stackProfile = new DefaultStackProfile();
        var registerProfile = new DefaultRegisterProfile(stackProfile, out);
        memoryProfile = new MemoryProfile(registerProfile, stackProfile);
        reservedRegisterId = registerProfile.getReservedRegisterId();
    }

    private void generateBasicBlock(BasicBlock basicBlock) {
        if (basicBlock.getIndex() != 0) {
            out.pushNewLine();
        }

        out.pushIndent();
        printer.printLabel(out, getLabelName(basicBlock));
        out.setIndent(2);
        basicBlock.getInstructions().forEach(this::generateInstruction);
        out.setIndent(0);
    }

    private String getLabelName(BasicBlock basicBlock) {
        return ".L." + basicBlock.getParent().getName() + "." + basicBlock.getIndex();
    }

    private void generateInstruction(Instruction instruction) {
        if (instruction instanceof BinaryOperator inst) {
            generateBinaryOperator(inst);
        } else if (instruction instanceof UnaryOperator inst) {
            generateUnaryOperator(inst);
        } else if (instruction instanceof CompInst inst) {
            generateCompareInst(inst);
        } else if (instruction instanceof AllocaInst inst) {
            generateAllocaInst(inst);
        } else if (instruction instanceof LoadInst inst) {
            generateLoadInst(inst);
        } else if (instruction instanceof StoreInst inst) {
            generateStoreInst(inst);
        } else if (instruction instanceof GetElementPtrInst inst) {
            generateGetElementPtrInst(inst);
        } else if (instruction instanceof InputInst inst) {
            generateInputInst(inst);
        } else if (instruction instanceof JumpInst inst) {
            generateJumpInst(inst);
        } else if (instruction instanceof BranchInst inst) {
            generateBranchInst(inst);
        } else if (instruction instanceof OutputInst inst) {
            generateOutputInst(inst);
        } else if (instruction instanceof CallInst inst) {
            generateCall(inst);
        } else if (instruction instanceof ReturnInst inst) {
            generateReturnInst(inst);
        } else if (instruction instanceof ZExtInst inst) {
            generateZEInst(inst);
        } else {
            throw new UnsupportedOperationException("Unsupported instruction: " + instruction);
        }

        postGenerateInstruction(instruction);

        memoryProfile.tick();
    }

    private void generateAllocaInst(AllocaInst inst) {
        memoryProfile.getStackProfile().allocate(inst, inst.getAllocatedType());
    }

    /**
     * la $t0, {globalStringName} <br />
     * lw $t0, 0($t1)
     */
    private void generateLoadInst(LoadInst inst) {
        if (inst.getType().isIntegerTy()) {
            generateLoadWord(inst, inst.getAddress());
        } else if (inst.getType().isPointerTy()) {
            generateLoadWord(inst, inst.getAddress());
        } else {
            throw new UnsupportedOperationException("Unsupported load type: " + inst.getType());
        }
    }

    /**
     * Generate lw instruction. <br />
     * lw $t0, 0($t1)
     * lw $t0, ($t1)
     *
     * @param value   The value to be loaded.
     * @param address The address of the value.
     */
    private void generateLoadWord(Value value, Value address) {
        var profile = memoryProfile.getRegisterProfile();
        var reg = profile.acquire(value);
        generateLoadWord(reg.getId(), address);
    }

    private void generateLoadWord(int register, Value address) {
        var addStr = generateAddress(address);

        out.push("lw").pushSpace();
        out.pushRegister(register).pushComma().pushSpace();
        out.push(addStr);
        out.pushNewLine();
    }

    /**
     * Generate la instruction. <br />
     * la $t0, 0($t1)
     * la $t0, ($t1)
     *
     * @param value   The value to be loaded.
     * @param address The address of the value.
     */
    private void generateLoadAddress(Value value, Value address) {
        var profile = memoryProfile.getRegisterProfile();
        var reg = profile.acquire(value);
        if (address instanceof GlobalVariable) {
            out.push("la").pushSpace();
            out.pushRegister(reg.getId()).pushComma();
            out.pushNext(address.getName()).pushNewLine();
        } else if (address instanceof AllocaInst) {
            var add = memoryProfile.getStackProfile().getAddress(address);
            generateLoadImmediate(reservedRegisterId, -add.offset());
            printer.printBinaryOperator(out, "subu", reg.getId(), add.base(), reservedRegisterId);
        } else {
            var reg2 = memoryProfile.getRegisterProfile().acquire(address);
            printer.printMove(out, reg.getId(), reg2.getId());
        }
    }

    /**
     * Generate li instruction. <br />
     * li $t0, 66
     *
     * @param value     The value to be loaded.
     * @param immediate The immediate value.
     */
    private void generateLoadImmediate(Value value, int immediate) {
        var profile = memoryProfile.getRegisterProfile();
        var reg = profile.acquire(value, true);

        out.push("li").pushSpace();
        out.pushRegister(reg.getId()).pushComma();
        out.pushNext(String.valueOf(immediate)).pushNewLine();
    }

    private void generateLoadImmediate(int register, int immediate) {
        out.push("li").pushSpace();
        out.pushRegister(register).pushComma();
        out.pushNext(String.valueOf(immediate)).pushNewLine();
    }

    private void generateStoreInst(StoreInst inst) {
        var lhs = inst.getLeftOperand();

        if (lhs instanceof Argument argument) {
            if (argument.getArgNo() < 4) {
                generateStoreWord(Registers.A0 + argument.getArgNo(), inst.getRightOperand());
            }
            return;
        }

        /*
         * Generate li for immediate value. We are sure here
         * we won't meet an array or pointer.
         */
        int lhsRegId = acquireRegisterId(lhs);

        var rhs = inst.getRightOperand();
        generateStoreWord(lhsRegId, rhs);
    }

    private void generateStoreWord(int register, Value address) {
        var addStr = generateAddress(address);
        out.push("sw").pushSpace();
        out.pushRegister(register).pushComma().pushSpace();
        out.push(addStr);
        out.pushNewLine();
    }

    private void generateInputInst(InputInst inst) {
        var reg = memoryProfile.getRegisterProfile().acquire(inst);
        generateSysCall(SYS_READ_INT);

        printer.printMove(out, reg.getId(), Registers.V0);
    }

    private void generateOutputInst(OutputInst inst) {
        var value = inst.getOperand();
        if (value instanceof GlobalString string) {
            out.push("la").pushSpace();
            out.pushRegister(Registers.A0).pushComma();
            out.pushNext(string.getName()).pushNewLine();
            generateSysCall(SYS_PRINT_STRING);
        } else if (value instanceof ConstantData constant) {
            generateLoadImmediate(Registers.A0, constant.getValue());
            generateSysCall(SYS_PRINT_INT);
        } else {
            var reg = memoryProfile.getRegisterProfile().acquire(value);
            printer.printMove(out, Registers.A0, reg.getId());
            generateSysCall(SYS_PRINT_INT);
        }
    }

    private void generateReturnInst(ReturnInst inst) {
        boolean isMain = inst.getParentFunction().getName().equals("main");
        int retReg = isMain ? Registers.A0 : Registers.V0;
        if (inst.hasValue()) {
            var value = inst.getValue();
            if (value instanceof ConstantData constant) {
                generateLoadImmediate(retReg, constant.getValue());
            } else if (value.getType().isPointerTy()) {
                generateLoadWord(retReg, value);
            } else {
                var reg = memoryProfile.getRegisterProfile().acquire(value);
                printer.printMove(out, retReg, reg.getId());
            }
        }
        if (inst.getParentFunction().getName().equals("main")) {
            generateSysCall(SYS_EXIT2);
        } else {
            // Load $ra register if not main.
            printer.printLoadStack(out, Registers.RA, 0);
            printer.printReturn(out);
        }
    }

    /*
     * ==================== Function Call ====================
     */

    private void generateCall(CallInst inst) {
        var profile = memoryProfile.getRegisterProfile();

        // First, try yield all registers to ensure all values
        // are allocated in memory.
        profile.tryYieldAll();

        /*
         * We get the offset, but not yet grow the stack.
         * Since we still need to calculate all parameters.
         * Here -4 is to reserve $ra for callee.
         */
        int stackOffset = memoryProfile.getStackProfile().getTotalOffset() - 4;

        // Push parameters in reverse order.
        for (int i = inst.getParamCount() - 1; i >= 0; i--) {
            var param = inst.getParam(i);
            // The first four registers can be stored to $a0 ~ $a3
            if (i < 4) {
                if (param instanceof ConstantData constant) {
                    generateLoadImmediate(Registers.A0 + i, constant.getValue());
                } else {
                    var reg = acquireRegisterId(param);
                    printer.printMove(out, Registers.A0 + i, reg);
                }
            } else {
                var reg = acquireRegisterId(param);
                // Should reserve stack for $a0 ~ $a3
                printer.printSaveStack(out, reg, stackOffset - (i + 1) * 4);
            }
        }

        // Then we really yield all registers.
        profile.yieldAll();

        // At last, expand the stack.
        generateLoadImmediate(reservedRegisterId, -stackOffset);
        printer.printStackGrow(out, reservedRegisterId);

        // Call function
        printer.printCall(out, inst.getFunction().getName());

        // Restore stack
        generateLoadImmediate(reservedRegisterId, -stackOffset);
        printer.printStackShrink(out, reservedRegisterId);

        // Get return value.
        if (!inst.getType().isVoidTy()) {
            var reg = profile.acquire(inst);
            printer.printMove(out, reg.getId(), Registers.V0);
        }

        /*
         * No need to restore registers, as they will be
         * restored when they are used again.
         */
    }

    /*
     * ==================== Arithmetic Instructions ====================
     */

    /**
     * Generate binary instruction. We always use register override instead
     * of immediate value.
     *
     * @param inst The instruction.
     */
    private void generateBinaryOperator(BinaryOperator inst) {
        if (inst.getOpType() == BinaryOperator.BinaryOpTypes.Mul) {
            generateMulInst(inst);
        } else if (inst.getOpType() == BinaryOperator.BinaryOpTypes.Div) {
            generateDivInst(inst);
        } else {
            int lhsRegId = acquireRegisterId(inst.getLeftOperand());
            int rhsRegId = acquireRegisterId(inst.getRightOperand());
            var reg = memoryProfile.getRegisterProfile().acquire(inst);
            String op = switch (inst.getOpType()) {
                case Add -> "addu";
                case Sub -> "subu";
                case Mod -> "rem";
                default -> throw new UnsupportedOperationException("Unsupported binary type: " + inst.getOpType());
            };
            printer.printBinaryOperator(out, op, reg.getId(), lhsRegId, rhsRegId);
        }
    }

    private void generateMulInst(BinaryOperator inst) {
        Value lhs = inst.getLeftOperand();
        Value rhs = inst.getRightOperand();

        // Only one of them can be constant.
        if (lhs instanceof ConstantData) {
            Value temp = lhs;
            lhs = rhs;
            rhs = temp;
        }

        var reg = memoryProfile.getRegisterProfile().acquire(inst);
        if (rhs instanceof ConstantData constant) {
            int value = constant.getValue();
            if (value == 0) {
                printer.printMove(out, reg.getId(), Registers.ZERO);
                return;
            } else if (Math.abs(value) == 1) {
                int lhsRegId = acquireRegisterId(lhs);
                printer.printMove(out, reg.getId(), lhsRegId);
                if (value < 0) {
                    printer.printBinaryOperator(out, "subu", reg.getId(), Registers.ZERO, reg.getId());
                }
                return;
            } else if (isPowerOfTwo(Math.abs(value))) {
                int lhsRegId = acquireRegisterId(lhs);
                String rhsValue = String.valueOf(getPowerOfTwo(Math.abs(value)));
                printer.printBinaryOperator(out, "sll", reg.getId(), lhsRegId, rhsValue);
                if (value < 0) {
                    printer.printBinaryOperator(out, "subu", reg.getId(), Registers.ZERO, reg.getId());
                }
                return;
            }
        }

        int lhsRegId = acquireRegisterId(inst.getLeftOperand());
        int rhsRegId = acquireRegisterId(inst.getRightOperand());
        printer.printBinaryOperator(out, "mul", reg.getId(), lhsRegId, rhsRegId);
    }

    /**
     * Warning: Negative integer cannot shift right!
     *
     * @param inst Division instruction.
     */
    private void generateDivInst(BinaryOperator inst) {
        Value lhs = inst.getLeftOperand();
        Value rhs = inst.getRightOperand();

        var reg = memoryProfile.getRegisterProfile().acquire(inst);
        if (lhs instanceof ConstantData constant) {
            if (constant.isAllZero()) {
                printer.printMove(out, reg.getId(), Registers.ZERO);
                return;
            }
        }

        if (rhs instanceof ConstantData constant) {
            int value = constant.getValue();
            if (Math.abs(value) == 1) {
                int lhsRegId = acquireRegisterId(inst.getLeftOperand());
                printer.printMove(out, reg.getId(), lhsRegId);
                if (value < 0) {
                    printer.printBinaryOperator(out, "subu", reg.getId(), Registers.ZERO, reg.getId());
                }
                return;
            }
        }

        int lhsRegId = acquireRegisterId(inst.getLeftOperand());
        int rhsRegId = acquireRegisterId(inst.getRightOperand());
        printer.printBinaryOperator(out, "div", reg.getId(), lhsRegId, rhsRegId);
    }

    private void generateUnaryOperator(UnaryOperator inst) {
        int operandRegId = acquireRegisterId(inst.getOperand());
        var reg = memoryProfile.getRegisterProfile().acquire(inst);
        String op = switch (inst.getOpType()) {
            case Pos -> "addu";
            case Neg -> "subu";
            case Not -> "xor";
        };

        if (inst.getOpType() == UnaryOperator.UnaryOpTypes.Not) {
            printer.printBinaryOperator(out, op, reg.getId(), operandRegId, Registers.ONE);
        } else {
            printer.printBinaryOperator(out, op, reg.getId(), Registers.ZERO, operandRegId);
        }
    }

    private void generateCompareInst(CompInst inst) {
        int lhsRegId = acquireRegisterId(inst.getLeftOperand());
        int rhsRegId = acquireRegisterId(inst.getRightOperand());
        var reg = memoryProfile.getRegisterProfile().acquire(inst);
        String op = switch (inst.getOpType()) {
            case Eq -> "seq";
            case Ne -> "sne";
            case Slt -> "slt";
            case Sle -> "sle";
            case Sgt -> "sgt";
            case Sge -> "sge";
        };
        printer.printBinaryOperator(out, op, reg.getId(), lhsRegId, rhsRegId);
    }

    private void generateBranchInst(BranchInst inst) {
        int flag = acquireRegisterId(inst.getCondition());
        BasicBlock from = inst.getParent();
        BasicBlock trueBlock = inst.getTrueBlock();
        BasicBlock falseBlock = inst.getFalseBlock();

        if (trueBlock == falseBlock) {
            generateJump(from, trueBlock);
        } else if (isJumpToNeighboring(from, trueBlock)) {
            printer.printInverseBranch(out, flag, getLabelName(falseBlock), null);
        } else if (isJumpToNeighboring(from, falseBlock)) {
            printer.printBranch(out, flag, getLabelName(trueBlock), null);
        } else {
            printer.printBranch(out, flag, getLabelName(trueBlock), getLabelName(falseBlock));
        }
    }

    private void generateJumpInst(JumpInst inst) {
        generateJump(inst.getParent(), inst.getTarget());
    }

    private void generateJump(BasicBlock from, BasicBlock to) {
        if (isJumpToNeighboring(from, to)) {
            return;
        }
        String label = getLabelName(to);
        printer.printJump(out, label);
    }

    /*
     * ==================== Array Operations ====================
     */

    /**
     * Generate getelementptr instruction. <br />
     * |----|<- $sp         <br />
     * | a2 |               <br />
     * |----|               <br />
     * | a1 |               <br />
     * |----|               <br />
     * | a0 |               <br />
     * |----|<- offset      <br />
     * | .. |
     *
     * @param inst
     */
    private void generateGetElementPtrInst(GetElementPtrInst inst) {
        if (isFixedGetElementPtrInst(inst)) {
            return;
        }

        // First dimension offset.
        var address = inst.getAddress();
        var type = address.getPointerType().getElementType();

        generateLoadAddress(inst, address);
        int dstReg = acquireRegisterId(inst);
        for (var subscript : inst.getSubscripts()) {
            // Calculate subscript offset.
            int size = type.getBytes();
            int offsetRegId = Registers.INVALID;
            if (subscript instanceof ConstantData constant) {
                var offset = constant.getValue() * size;
                if (offset != 0) {
                    generateLoadImmediate(reservedRegisterId, offset);
                    offsetRegId = reservedRegisterId;
                }
            } else {
                var reg = memoryProfile.getRegisterProfile().acquire(subscript);
                if (size == 1) {
                    offsetRegId = reg.getId();
                } else {
                    generateLoadImmediate(reservedRegisterId, size);
                    printer.printBinaryOperator(out, "mul", reservedRegisterId, reservedRegisterId, reg.getId());
                    offsetRegId = reservedRegisterId;
                }
            }

            if (offsetRegId != Registers.INVALID) {
                printer.printBinaryOperator(out, "addu", dstReg, dstReg, offsetRegId);
            }

            if (type.isArrayTy()) {
                type = type.asArray().getElementType();
            }
        }
    }

    private String generateFixedGetElementPtrInstAddress(GetElementPtrInst inst) {
        var address = inst.getAddress();
        var type = address.getPointerType().getElementType();

        int offset = 0;
        for (var subscript : inst.getSubscripts()) {
            if (!(subscript instanceof ConstantData constant)) {
                throw new UnsupportedOperationException("Unsupported subscript type: " + subscript);
            }
            offset += constant.getValue() * type.getBytes();
            if (type.isArrayTy()) {
                type = type.asArray().getElementType();
            }
        }

        var bufferedOut = new VerboseMipsWriter(new TwioBufferWriter());
        if (address instanceof AllocaInst) {
            var add = memoryProfile.getStackProfile().getAddress(address);
            bufferedOut.push(String.valueOf(offset + add.offset()));
            bufferedOut.push('(');
            bufferedOut.pushRegister(add.base()).push(')');
        } else {
            if (offset != 0) {
                bufferedOut.push(String.valueOf(offset));
            }
            var reg = memoryProfile.getRegisterProfile().acquire(address);
            bufferedOut.push('(');
            bufferedOut.pushRegister(reg.getId()).push(')');
        }

        return bufferedOut.dumps();
    }

    private boolean isFixedGetElementPtrInst(GetElementPtrInst inst) {
        if (!(inst.getAddress() instanceof AllocaInst)) {
            return false;
        }
        if (!inst.hasFixedDimension()) {
            return false;
        }
        for (var user : inst.getUsers()) {
            if (!(user instanceof LoadInst || user instanceof StoreInst)) {
                return false;
            }
        }

        return true;
    }

    private void generateZEInst(ZExtInst inst) {
        var reg = memoryProfile.getRegisterProfile().acquire(inst);
        var operand = inst.getOperand();
        if (operand instanceof ConstantData constant) {
            generateLoadImmediate(reg.getId(), constant.getValue());
        } else {
            var operandReg = memoryProfile.getRegisterProfile().acquire(operand);
            printer.printMove(out, reg.getId(), operandReg.getId());
        }
    }


    /*
     * ==================== Utility Methods ====================
     */

    private void generateHeader() {
        out.pushComment("This file is generated by ToMiC4J");
        out.pushComment("MIPS Version: 1.3.2+").pushNewLine();
    }

    /**
     * li $v0, {service} <br />
     * syscall
     */
    private void generateSysCall(int service) {
        out.push("li").pushSpace();
        out.pushRegister(Registers.V0).pushComma();
        out.pushNext(String.valueOf(service)).pushNewLine();
        out.push("syscall").pushNewLine();
    }

    private String generateAddress(Value address) {
        var bufferedOut = new VerboseMipsWriter(new TwioBufferWriter());
        if (address instanceof GlobalVariable) {
            bufferedOut.push(address.getName());
        } else if (address instanceof AllocaInst) {
            var add = memoryProfile.getStackProfile().getAddress(address);
            if (add.offset() != 0) {
                bufferedOut.push(String.valueOf(add.offset()));
            }
            bufferedOut.push('(');
            bufferedOut.pushRegister(add.base()).push(')');
        } else {
            if (address instanceof GetElementPtrInst inst) {
                if (isFixedGetElementPtrInst(inst)) {
                    return generateFixedGetElementPtrInstAddress(inst);
                }
            }
            var reg = memoryProfile.getRegisterProfile().acquire(address);
            bufferedOut.push('(');
            bufferedOut.pushRegister(reg.getId());
            bufferedOut.push(')');
        }
        return bufferedOut.dumps();
    }

    private int acquireRegisterId(Value value) {
        boolean temporary = value.getUsers().size() < 2;
        if (value instanceof ConstantData constant) {
            if (constant.isAllZero()) {
                return Registers.ZERO;
            } else {
                generateLoadImmediate(constant, constant.getValue());
            }
        }

        return memoryProfile.getRegisterProfile().acquire(value, temporary).getId();
    }

    private boolean isPowerOfTwo(int n) {
        return (n & (n - 1)) == 0;
    }

    private int getPowerOfTwo(int n) {
        int ret = 0;
        while (n > 1) {
            n >>= 1;
            ret++;
        }
        return ret;
    }

    /**
     * Release unused registers.
     *
     * @param inst The instruction just generated.
     */
    private void postGenerateInstruction(Instruction inst) {
        for (var operand : inst.getOperands()) {
            int blockIndex = -1;
            int instIndex = -1;
            for (var user : operand.getUsers()) {
                if (user instanceof Instruction instruction) {
                    int temp = instruction.getParent().getIndex();
                    if (blockIndex == temp) {
                        instIndex = Math.max(instIndex, instruction.getIndex());
                    } else if (blockIndex < temp) {
                        blockIndex = temp;
                        instIndex = instruction.getIndex();
                    }
                }
            }
            if ((inst.getParent().getIndex() == blockIndex) && (inst.getIndex() == instIndex)) {
                memoryProfile.getRegisterProfile().release(operand);
            }
        }
    }

    private boolean isJumpToNeighboring(BasicBlock from, BasicBlock to) {
        if (from.getIndex() + 1 != to.getIndex()) {
            return false;
        }

        var preds = to.getPredecessors();
        return preds.size() == 1 && preds.get(0) == from;
    }

    public static final int SYS_EXIT = 10;
    public static final int SYS_EXIT2 = 17;
    public static final int SYS_READ_INT = 5;
    public static final int SYS_PRINT_INT = 1;
    public static final int SYS_PRINT_STRING = 4;
}
