import tomic.core.Config;
import tomic.core.ToMiCompiler;

public class Compiler {
    private static boolean showHelp = false;

    public static void main(String[] args) {
        Config config = new Config();

        // Override default config
        config.target = Config.TargetTypes.IR;
        config.input = "testfile.txt";
        config.emitLlvm = true;
        config.llvmOutput = "llvm_ir.txt";

        new ToMiCompiler().configure(config).compile();
    }
}