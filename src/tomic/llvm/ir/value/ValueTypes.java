/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.ir.value;

public enum ValueTypes {
    // === Value ===
    ArgumentTy,
    BasicBlockTy,

    // === Value.User.Instruction ===
    BranchInstTy,
    JumpInstTy,
    GetElementPtrInstTy,
    ReturnInstTy,
    StoreInstTy,
    CallInstTy,
    InputInstTy,
    OutputInstTy,

    // === Value.User.Instruction.BinaryInstruction ===
    BinaryOperatorTy,
    CompareInstTy,

    // === Value.User.Instruction.UnaryInstruction ===
    AllocaInstTy,
    LoadInstTy,
    UnaryOperatorTy,
    ZExtInstTy,

    // === Value.User.Constant ===
    ConstantTy,
    ConstantDataTy,

    // === Value.User.Constant.GlobalValue.GlobalObject ===
    GlobalValueTy,
    FunctionTy,
    GlobalVariableTy,
    GlobalStringTy,
}
