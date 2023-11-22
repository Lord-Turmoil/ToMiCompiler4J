package tomic.llvm.ir.value;

public enum ValueTypes {
    // === Value ===
    ArgumentTy,
    BasicBlockTy,

    // === Value.User.Instruction ===
    BinaryOperatorTy,
    CompareInstTy,
    BranchInstTy,
    IndirectBrInstTy,
    GetElementPtrInstTy,
    ReturnInstTy,
    StoreInstTy,
    CallInstTy,
    InputInstTy,
    OutputInstTy,

    // === Value.User.Instruction.UnaryInstruction ===
    AllocaInstTy,
    LoadInstTy,
    UnaryOperatorTy,

    // === Value.User.Constant ===
    ConstantTy,
    ConstantDataTy,

    // === Value.User.Constant.GlobalValue.GlobalObject ===
    GlobalValueTy,
    FunctionTy,
    GlobalVariableTy,
    GlobalStringTy,
}
