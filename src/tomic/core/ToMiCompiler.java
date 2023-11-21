package tomic.core;

public class ToMiCompiler {
    private final ToMiCompilerImpl impl = new ToMiCompilerImpl();

    ToMiCompiler() {
    }

    public void compile(Config config) {
        impl.compile();
    }
}
