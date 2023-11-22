import tomic.core.Config;
import tomic.core.ToMiCompiler;

public class Compiler {
    private static boolean showHelp = false;

    public static void main(String[] args) {
        Config config = new Config();

        // Override default config
        config.target = Config.TargetTypes.Semantic;
        config.input = "testfile.txt";
        config.enableError = true;
        config.errorOutput = "error.txt";

        new ToMiCompiler().configure(config).compile();
    }
}