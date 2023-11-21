package tomic.core;

import lib.twio.TwioExt;
import tomic.lexer.IPreprocessor;
import tomic.lexer.impl.DefaultPreprocessor;
import tomic.logger.debug.IDebugLogger;
import tomic.logger.debug.LogLevel;
import tomic.logger.debug.impl.DefaultLogger;
import tomic.logger.debug.impl.DumbLogger;
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

        //////////////////// Preprocess
        impl.configure(service -> {
            service.addTransient(IPreprocessor.class, DefaultPreprocessor.class);
        });

        return this;
    }

    public void compile() {
        impl.compile();
    }
}
