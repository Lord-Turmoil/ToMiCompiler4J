import java.util.Arrays;
import java.util.List;

public class SplitFormatTest {
    public static void main(String[] args) {
        String format = "     %dhello  %d % from  %%d %d\n";
        for (var s : splitFormat(format)) {
            System.out.println("'" + s + "'");
        }
    }

    // Split %d from format, and get an array like the following:
    // "hello  " "%d" " % from  " "%d" " "
    public static List<String> splitFormat(String format) {
        return Arrays.asList(format.split("(?<=%d)|(?=%d)"));
    }
}
