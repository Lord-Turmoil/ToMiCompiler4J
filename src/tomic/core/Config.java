package tomic.core;

public class Config {
    public static class TargetTypes {
        public static final int Initial = 0;
        public static final int Preprocess = 1;
        public static final int Syntactic = 2;
        public static final int Semantic = 3;
        public static final int IR = 4;
        public static final int ASM = 5;
    }

    public int target;

    // basic
    public String input;
    public String output;

    // AST
    public boolean enableCompleteAst;
    public boolean emitAst;
    public String astOutput;

    // LLVM IR
    public boolean emitLlvm;
    public boolean enableVerboseLlvm;
    public String llvmOutput;

    // logger
    public boolean enableLog;
    public String logOutput;

    // error
    public boolean enableError;
    public boolean enableVerboseError;
    public String errorOutput;

    public Config() {
        target = TargetTypes.Initial;
        enableCompleteAst = false;
        emitAst = false;
        emitLlvm = false;
        enableVerboseLlvm = false;
        enableLog = false;
        enableError = false;
        enableVerboseError = false;
    }

    @Override
    public String toString() {
        return "Config {" +
                "\n\ttarget=" + target +
                ",\n\tinput='" + input + '\'' +
                ",\n\toutput='" + output + '\'' +
                ",\n\tenableCompleteAst=" + enableCompleteAst +
                ",\n\temitAst=" + emitAst +
                ",\n\tastOutput='" + astOutput + '\'' +
                ",\n\temitLlvm=" + emitLlvm +
                ",\n\tenableVerboseLlvm=" + enableVerboseLlvm +
                ",\n\tllvmOutput='" + llvmOutput + '\'' +
                ",\n\tenableLog=" + enableLog +
                ",\n\tlogOutput='" + logOutput + '\'' +
                ",\n\tenableError=" + enableError +
                ",\n\tenableVerboseError=" + enableVerboseError +
                ",\n\terrorOutput='" + errorOutput + '\'' +
                "\n}";
    }
}
