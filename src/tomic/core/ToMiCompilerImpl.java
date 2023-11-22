package tomic.core;

import lib.ioc.Container;
import lib.ioc.IContainer;
import lib.twio.*;
import tomic.lexer.IPreprocessor;
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

        logErrors();
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
                outputSyntaxTree(config.astOutput, container.resolveRequired(IAstPrinter.class), ast);
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
            outputSyntaxTree(config.astOutput, container.resolveRequired(IAstPrinter.class), ast);
        }

        outTable[0] = table;

        var errorLogger = container.resolveRequired(IErrorLogger.class);
        if (table == null || errorLogger.count() > 0) {
            logger.fatal("Semantic analyzing failed, compilation aborted");
            return false;
        }

        return true;
    }
    private void outputSyntaxTree(String filename, IAstPrinter printer, SyntaxTree tree) {
        var writer = TwioExt.buildWriter(filename);
        printer.print(tree, writer);
    }

    private void logErrors() {
        if (!config.enableError) {
            return;
        }

        var logger = container.resolveRequired(IDebugLogger.class);
        var errorLogger = container.resolveRequired(IErrorLogger.class);
        var errorWriter = TwioExt.buildWriter(config.errorOutput);
        if (errorLogger.count() > 0) {
            errorLogger.dumps(errorWriter);
        }

        if (errorLogger.count() > 0) {
            logger.fatal("Compilation completed with " + errorLogger.count() + " errors");
        }
    }
}
