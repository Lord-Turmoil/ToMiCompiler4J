/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.core;

import lib.ioc.Container;
import lib.ioc.IContainer;
import lib.twio.*;
import tomic.lexer.IPreprocessor;
import tomic.llvm.asm.IAsmGenerator;
import tomic.llvm.asm.IAsmPrinter;
import tomic.llvm.ir.Module;
import tomic.llvm.mips.IMipsGenerator;
import tomic.llvm.pass.PassManager;
import tomic.logger.debug.IDebugLogger;
import tomic.logger.debug.LogLevel;
import tomic.logger.error.IErrorLogger;
import tomic.parser.ISemanticParser;
import tomic.parser.ISyntacticParser;
import tomic.parser.ast.SyntaxTree;
import tomic.parser.ast.printer.IAstPrinter;
import tomic.parser.table.SymbolTable;

import java.util.function.Consumer;

import static lib.twio.TwioExt.buildReader;
import static lib.twio.TwioExt.buildWriter;

/**
 * Restrict access to the implementation of the ToMiCompiler class.
 */
class ToMiCompilerImpl {
    private final Container container;
    private Config config;

    ToMiCompilerImpl() {
        container = new Container();
        config = null;
    }

    void configure(Config config) {
        this.config = config;
    }

    void configure(Consumer<IContainer> config) {
        config.accept(container);
    }

    void compile() {
        // Preprocess
        ITwioWriter[] writer = { null };
        if (!preprocess(writer)) {
            logErrors();
            return;
        }

        // Syntactic
        SyntaxTree[] ast = { null };
        if (!syntacticParse(new TwioReader(writer[0].yield()), ast)) {
            logErrors();
            return;
        }

        // Semantic
        SymbolTable[] table = { null };
        if (!semanticParse(ast[0], table)) {
            logErrors();
            return;
        }

        // LLVM IR
        Module[] module = { null };
        if (!generateLlvmAsm(ast[0], table[0], module)) {
            logErrors();
            return;
        }

        // MIPS
        if (!generateMips(module[0])) {
            logErrors();
            return;
        }

        if (logErrors()) {
            var logger = container.resolveRequired(IDebugLogger.class);
            logger.info("Compilation completed!");
        }
    }

    private boolean preprocess(ITwioWriter[] outWriter) {
        if (config.target.ordinal() < Config.TargetTypes.Preprocess.ordinal()) {
            return false;
        }

        var logger = container.resolveRequired(IDebugLogger.class);

        ITwioReader reader = buildReader(config.input);
        ITwioWriter writer;
        if (config.target == Config.TargetTypes.Preprocess) {
            writer = buildWriter(config.output);
        } else {
            writer = new TwioBufferWriter();
        }

        logger.debug("Preprocessing " + config.input + "...");
        container.resolveRequired(IPreprocessor.class)
                .setReader(reader)
                .setWriter(writer)
                .process();
        logger.debug("Preprocess done");

        outWriter[0] = writer;

        return true;
    }

    private boolean syntacticParse(ITwioReader reader, SyntaxTree[] outAst) {
        if (config.target.ordinal() < Config.TargetTypes.Syntactic.ordinal()) {
            return false;
        }

        var logger = container.resolveRequired(IDebugLogger.class);

        logger.debug("Parsing " + config.input + "...");
        var ast = container.resolveRequired(ISyntacticParser.class).setReader(reader).parse();
        if (ast == null) {
            logger.fatal("Syntactic parse failed, compilation aborted");
            return false;
        }

        if (logger.count(LogLevel.ERROR) > 0) {
            logger.error("Syntactic parse completed with errors");
        }

        if (config.target == Config.TargetTypes.Syntactic) {
            if (config.emitAst) {
                outputSyntaxTree(config.astOutput, ast);
            }
        }

        outAst[0] = ast;

        return true;
    }

    private boolean semanticParse(SyntaxTree ast, SymbolTable[] outTable) {
        if (config.target.ordinal() < Config.TargetTypes.Semantic.ordinal()) {
            return false;
        }

        var logger = container.resolveRequired(IDebugLogger.class);
        logger.debug("Performing semantic analyzing on " + config.input + "...");

        var table = container.resolveRequired(ISemanticParser.class).parse(ast);

        if (config.emitAst) {
            outputSyntaxTree(config.astOutput, ast);
        }

        outTable[0] = table;

        var errorLogger = container.resolveRequired(IErrorLogger.class);
        if (table == null || errorLogger.count() > 0) {
            logger.fatal("Semantic analyzing failed, compilation aborted");
            return false;
        }

        return true;
    }

    private void outputSyntaxTree(String filename, SyntaxTree tree) {
        var printer = container.resolveRequired(IAstPrinter.class);
        printer.print(tree, TwioExt.buildWriter(filename));
    }

    private void outputLlvmAsm(String filename, Module module) {
        var printer = container.resolveRequired(IAsmPrinter.class);
        printer.print(module, TwioExt.buildWriter(filename));
    }

    private boolean generateLlvmAsm(SyntaxTree ast, SymbolTable table, Module[] outModule) {
        if (config.target.ordinal() < Config.TargetTypes.IR.ordinal()) {
            return false;
        }

        var logger = container.resolveRequired(IDebugLogger.class);
        logger.log(LogLevel.DEBUG, "Generating LLVM IR...");

        var module = container.resolveRequired(IAsmGenerator.class).generate(ast, table, config.input);
        if (module == null) {
            logger.fatal("Generating LLVM IR failed, compilation aborted");
            return false;
        }


        // Run passes.
        var manager = container.resolve(PassManager.class);
        if (manager != null) {
            manager.run(module);
        }

        // Output LLVM IR.
        if (config.emitLlvm) {
            outputLlvmAsm(config.llvmOutput, module);
        }

        outModule[0] = module;

        return true;
    }

    private boolean generateMips(Module module) {
        if (config.target.ordinal() < Config.TargetTypes.ASM.ordinal()) {
            return false;
        }

        var logger = container.resolveRequired(IDebugLogger.class);
        logger.log(LogLevel.DEBUG, "Generating MIPS...");

        var out = TwioExt.buildWriter(config.output);
        container.resolveRequired(IMipsGenerator.class).generate(module, out);

        return true;
    }

    private boolean logErrors() {
        if (!config.enableError) {
            return false;
        }

        var logger = container.resolveRequired(IDebugLogger.class);
        var errorLogger = container.resolveRequired(IErrorLogger.class);
        var errorWriter = TwioExt.buildWriter(config.errorOutput);
        if (errorLogger.count() > 0) {
            errorLogger.dumps(errorWriter);
            logger.fatal("Compilation completed with " + errorLogger.count() + " errors");
            return false;
        }

        return true;
    }
}
