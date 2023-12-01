/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.core;

public class Config {
    public enum TargetTypes {
        Initial,
        Preprocess,
        Syntactic,
        Semantic,
        IR,
        ASM,
    }

    public TargetTypes target;

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

    // Optimization
    public int optimizationLevel;

    public Config() {
        target = TargetTypes.Initial;
        enableCompleteAst = false;
        emitAst = false;
        emitLlvm = false;
        enableVerboseLlvm = false;
        enableLog = false;
        enableError = false;
        enableVerboseError = false;
        optimizationLevel = 0;
    }

    @Override
    public String toString() {
        return "Config {" +
                "\n\ttarget = " + target +
                ",\n\tinput = '" + input + '\'' +
                ",\n\toutput = '" + output + '\'' +
                ",\n\tenableCompleteAst = " + enableCompleteAst +
                ",\n\temitAst = " + emitAst +
                ",\n\tastOutput = '" + astOutput + '\'' +
                ",\n\temitLlvm = " + emitLlvm +
                ",\n\tenableVerboseLlvm = " + enableVerboseLlvm +
                ",\n\tllvmOutput = '" + llvmOutput + '\'' +
                ",\n\tenableLog = " + enableLog +
                ",\n\tlogOutput = '" + logOutput + '\'' +
                ",\n\tenableError = " + enableError +
                ",\n\tenableVerboseError = " + enableVerboseError +
                ",\n\terrorOutput = '" + errorOutput + '\'' +
                ",\n\toptimizationLevel = " + optimizationLevel +
                "\n}";
    }
}
