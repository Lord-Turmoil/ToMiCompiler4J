import tomic.core.Config;
import tomic.core.ToMiCompiler;

public class Compiler {
    private static boolean showHelp = false;

    public static void main(String[] args) {
        Config config = new Config();

        // Override default config

        new ToMiCompiler().configure(config).compile();
    }
}