package tomic.core;

import lib.twio.TwioExt;
import tomic.lexer.ILexicalAnalyzer;
import tomic.lexer.ILexicalParser;
import tomic.lexer.IPreprocessor;
import tomic.lexer.impl.DefaultLexicalAnalyzer;
import tomic.lexer.impl.DefaultLexicalParser;
import tomic.lexer.impl.DefaultPreprocessor;
import tomic.lexer.token.ITokenMapper;
import tomic.lexer.token.impl.DefaultTokenMapper;
import tomic.logger.debug.IDebugLogger;
import tomic.logger.debug.LogLevel;
import tomic.logger.debug.impl.DefaultLogger;
import tomic.logger.debug.impl.DumbLogger;
import tomic.logger.error.IErrorLogger;
import tomic.logger.error.IErrorMapper;
import tomic.logger.error.impl.*;
import tomic.utils.StringExt;

public class ToMiCompiler {
    private final ToMiCompilerImpl impl = new ToMiCompilerImpl();

    public ToMiCompiler configure(Config config) {
        impl.configure(config);

        // prepare dependency injection!
        //////////////////// Logger
        impl.configure(service -> {
            if (config.enableLog) {
                if (StringExt.isNullOrEmpty(config.logOutput)) {
                    service.addSingleton(IDebugLogger.class, DumbLogger.class);
                } else {
                    var logWriter = TwioExt.buildWriter(config.logOutput);
                    var logger = new DefaultLogger().setLevel(LogLevel.DEBUG).setWriter(logWriter);
                    service.addSingleton(IDebugLogger.class, logger);
                }
            } else {
                service.addSingleton(IDebugLogger.class, DumbLogger.class);
            }
        });

        //////////////////// Error logger
        impl.configure(service -> {
            if (config.enableError) {
                if (config.enableVerboseError) {
                    service.addSingleton(IErrorMapper.class, VerboseErrorMapper.class)
                            .addSingleton(IErrorLogger.class, VerboseErrorLogger.class, IErrorMapper.class);
                } else {
                    service.addSingleton(IErrorMapper.class, StandardErrorMapper.class)
                            .addSingleton(IErrorLogger.class, StandardErrorLogger.class, IErrorMapper.class);
                }
            } else {
                service.addSingleton(IErrorLogger.class, DumbErrorLogger.class);
            }
        });

        //////////////////// Preprocess
        impl.configure(service -> {
            service.addTransient(IPreprocessor.class, DefaultPreprocessor.class);
        });

        //////////////////// Lexical
        impl.configure(service -> {
            service.addTransient(ITokenMapper.class, DefaultTokenMapper.class)
                    .addTransient(ILexicalAnalyzer.class, DefaultLexicalAnalyzer.class, ITokenMapper.class)
                    .addTransient(ILexicalParser.class, DefaultLexicalParser.class, ILexicalAnalyzer.class, IErrorLogger.class, IDebugLogger.class);
        });

        return this;
    }

    public void compile() {
        impl.compile();
    }
}
