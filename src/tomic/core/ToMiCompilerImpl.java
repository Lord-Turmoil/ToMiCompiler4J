package tomic.core;

import lib.ioc.Container;
import lib.twio.*;
import tomic.lexer.IPreprocessor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.function.Consumer;

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

    ToMiCompilerImpl configure(Config config) {
        this.config = config;
        return this;
    }

    ToMiCompilerImpl configure(Consumer<Config> action) {
        action.accept(this.config);
        return this;
    }

    void compile() {
        // Preprocess
        if (config.target.ordinal() < Config.TargetTypes.Preprocess.ordinal()) {
            return;
        }
        ITwioWriter writer = preprocess();
    }

    ITwioWriter preprocess() {
        ITwioReader reader = buildReader(config.input);
        ITwioWriter writer;
        if (config.target == Config.TargetTypes.Preprocess) {
            writer = buildWriter(config.output);
        } else {
            writer = new TwioBufferWriter();
        }

        container.resolveRequired(IPreprocessor.class)
                .setReader(reader)
                .setWriter(writer)
                .process();

        return writer;
    }

    private ITwioReader buildReader(String input) {
        if (input == null || input.equals("null")) {
            throw new IllegalArgumentException("input cannot be null");
        }

        if (input.equals("stdin")) {
            return new TwioReader(System.in);
        }

        try {
            return new TwioReader(new FileInputStream(input));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private ITwioWriter buildWriter(String output) {
        if (output == null || output.equals("null")) {
            return new TwioBufferWriter();
        }

        if (output.equals("stdout")) {
            return new TwioFileWriter(System.out);
        } else if (output.equals("stderr")) {
            return new TwioFileWriter(System.err);
        }

        try {
            return new TwioFileWriter(new FileOutputStream(output));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
