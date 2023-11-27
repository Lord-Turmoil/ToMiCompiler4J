/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

import tomic.core.Config;
import tomic.core.ToMiCompiler;

public class Compiler {
    private static boolean showHelp = false;

    public static void main(String[] args) {
        Config config = new Config();

        // Override default config
        config.target = Config.TargetTypes.ASM;
        config.input = "testfile.txt";
        config.output = "mips.txt";

        new ToMiCompiler().configure(config).compile();
    }
}