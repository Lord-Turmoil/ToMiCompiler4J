import tomic.parser.ast.AstExt;
import tomic.parser.ast.SyntaxTypes;

public class AstExtTest {
    public static void main(String[] args) {
        var array = AstExt.deserializeArray("1,2,3;4,5,6;7,8,9;");
        System.out.println(array);
        String serialized = AstExt.serializeArray(array);
        System.out.println(serialized);
    }
}
