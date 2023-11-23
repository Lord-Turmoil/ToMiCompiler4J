package tomic.llvm.ir.value;

public enum ValueTypes {
    // === Value ===
    ArgumentTy,
    BasicBlockTy,

    // === Value.User.Instruction ===
    BranchInstTy,
    IndirectBrInstTy,
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
