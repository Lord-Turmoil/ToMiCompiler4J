/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

import tomic.core.Config;
import tomic.core.ToMiCompiler;

import static tomic.utils.StringExt.isNullOrEmpty;

public class Main {
    private static boolean showHelp = false;
    public static void main(String[] args) {
        Config config = new Config();
        if (!parseArgs(args.length, args, config)) {
            System.exit(1);
        }

        if (showHelp) {
            printHelp();
        } else {
            new ToMiCompiler().configure(config).compile();
        }
    }

    private static boolean parseArgs(int argc, String[] argv, Config config) {
        ArgumentParser parser = new ArgumentParser();
        boolean error = false;
        int argCnt = 0;

        while (parser.getopt(argc, argv, "o:t:l:e:va:ci:h") != 0) {
            if (parser.opterr != 0) {
                System.err.println(parser.optmsg);
                parser.reset();
                error = true;
                break;
            }

            switch (parser.optopt) {
                case 'o' -> config.output = parser.optarg;
                case 't' -> handleLongOpt("target", parser.optarg, config);
                case 'l' -> handleLongOpt("enable-logger", parser.optarg, config);
                case 'e' -> handleLongOpt("enable-error", parser.optarg, config);
                case 'v' -> handleLongOpt("verbose-error", parser.optarg, config);
                case 'a' -> handleLongOpt("emit-ast", parser.optarg, config);
                case 'c' -> handleLongOpt("complete-ast", parser.optarg, config);
                case 'i' -> handleLongOpt("emit-llvm", parser.optarg, config);
                case 'h' -> handleLongOpt("help", parser.optarg, config);
                case '@' -> {
                    if (!handleLongOpt(parser.longopt, parser.optarg, config)) {
                        error = true;
                    }
                }
                case '!' -> {
                    argCnt++;
                    if (argCnt == 1) {
                        config.input = parser.optarg;
                    } else {
                        System.err.println("Too many input files");
                        error = true;
                    }
                }
                case '?' -> {
                    System.err.println("Unknown parameter: " + parser.optopt);
                    error = true;
                }
                default -> {
                }
            }
        }

        return !error;
    }

    private static boolean handleLongOpt(String opt, String arg, Config config) {
        switch (opt) {
            case "target" -> {
                if (isNullOrEmpty(arg)) {
                    System.err.println("Missing argument for: " + opt);
                    return false;
                }
                switch (arg) {
                    case "preprocess" -> config.target = Config.TargetTypes.Preprocess;
                    case "syntactic" -> config.target = Config.TargetTypes.Syntactic;
                    case "semantic" -> config.target = Config.TargetTypes.Semantic;
                    case "ir" -> config.target = Config.TargetTypes.IR;
                    case "asm" -> config.target = Config.TargetTypes.ASM;
                    default -> {
                        System.err.println("Unknown target: " + opt);
                        return false;
                    }
                }
            }
            case "enable-logger" -> {
                config.enableLog = true;
                config.logOutput = isNullOrEmpty(arg) ? "stdout" : arg;
            }
            case "enable-error" -> {
                config.enableError = true;
                config.errorOutput = isNullOrEmpty(arg) ? "stderr" : arg;
            }
            case "verbose-error" -> config.enableVerboseError = true;
            case "complete-ast" -> config.enableCompleteAst = true;
            case "emit-ast" -> {
                config.emitAst = true;
                config.astOutput = isNullOrEmpty(arg) ? "ast.xml" : arg;
            }
            case "emit-llvm" -> {
                config.emitLlvm = true;
                config.llvmOutput = isNullOrEmpty(arg) ? "llvm.ll" : arg;
            }
            case "verbose-llvm" -> config.enableVerboseLlvm = true;
            case "help" -> showHelp = true;
            default -> {
                System.err.println("Unknown parameter: --" + opt);
                return false;
            }
        }
        return true;
    }

    private static final String HELP = """
            Usage: ToMiCompiler <input> [-o output]
                      --target=(syntactic | semantic | ir | asm)
                      --enable-logger[=filename]
                      --enable-error[=filename] --verbose-error
                      --emit-ast[=filename] --complete-ast
                      --emit-llvm[=filename]
                
              --target, -t:         specify the target type
              --enable-logger, -l:  enable logger
              --enable-error, -e:   enable error
              --verbose-error, -v:  verbose error
              --emit-ast, -a:       emit ast
              --complete-ast, -c:   complete ast
              --emit-llvm, -i:      emit llvm ir
              --help, -h:           show help
                )";
            """;

    private static void printHelp() {
        System.out.println(HELP);
    }
}