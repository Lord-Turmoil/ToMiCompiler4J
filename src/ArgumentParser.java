/**
 * ... :(
 * Forgive me, I'm not a Java programmer.
 * You are not expected to understand this. :(
 */
public class ArgumentParser {
    public String optarg;
    public String optmsg;
    public int opterr;
    public int optopt;
    public String longopt;

    private int optind;

    public void reset() {
        optind = 0;
        opterr = 0;
        optarg = null;
        optmsg = null;
    }

    public int getopt(int argc, String[] argv, String pattern) {
        init_opt();

        if (optind >= argc) {
            reset();
            return 0;
        }

        int opt = parse_opt(argv[optind]);
        if (opt == 0) {
            String tmp = parse_long_opt(argv[optind]);
            if (tmp != null) {
                opt = optopt = '@';
                StringBuilder builder = new StringBuilder();
                int i = 0;
                while (i < tmp.length() && tmp.charAt(i) != '=') {
                    builder.append(tmp.charAt(i));
                    i++;
                }
                longopt = builder.toString();
                if (i < tmp.length() - 1) {
                    optarg = tmp.substring(i + 1);
                } else {
                    optarg = null;
                }
            } else {
                opt = optopt = '!';
                optarg = argv[optind];
            }
        } else {
            optopt = opt;
            int pos = pattern.indexOf(opt);
            if (pos == -1) {
                opt = '?';
                opterr = 1;
                optmsg = "Unknown option: " + opt;
            } else {
                if (pos + 1 < pattern.length() && pattern.charAt(pos + 1) == ':') {
                    optarg = parse_arg(argv[optind]);
                    if (optarg == null) {
                        // argument not compact
                        if (((optind < argc - 1) && (parse_opt(argv[optind + 1]) != 0)) ||
                                (optind == argc - 1)) {
                            opterr = 2;
                            optmsg = "Missing argument for option: " + opt;
                        } else {
                            optarg = argv[++optind];
                        }
                    }
                }
            }
        }

        return opt;
    }

    private int parse_opt(String arg) {
        if (arg.length() < 2) {
            return 0;
        }
        if (arg.charAt(0) == '-' && arg.charAt(1) != '-') {
            return arg.charAt(1);
        }
        return 0;
    }

    private String parse_long_opt(String arg) {
        if (arg.length() < 3) {
            return null;
        }
        if (arg.charAt(0) == '-' && arg.charAt(1) == '-') {
            return arg.substring(2);
        }
        return null;
    }

    private String parse_arg(String arg) {
        if (arg.length() < 3) {
            return null;
        }

        return arg.substring(2);
    }

    private void init_opt() {
        optarg = null;
        optmsg = null;
        opterr = 0;
        optopt = '?';
        optind++;
    }
}
