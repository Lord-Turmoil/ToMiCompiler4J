package tomic.core;

import lib.ioc.Container;
import lib.ioc.IContainer;
import lib.twio.ITwioReader;
import lib.twio.ITwioWriter;
import lib.twio.TwioBufferWriter;
import tomic.lexer.IPreprocessor;
import tomic.logger.debug.IDebugLogger;

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
        if (config.target.ordinal() < Config.TargetTypes.Preprocess.ordinal()) {
            return;
        }
        ITwioWriter writer = preprocess();
    }

    ITwioWriter preprocess() {
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

        return writer;
    }
}