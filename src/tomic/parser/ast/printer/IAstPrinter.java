package tomic.parser.ast.printer;

import lib.twio.ITwioWriter;
import tomic.parser.ast.SyntaxTree;

public interface IAstPrinter {
    void print(SyntaxTree tree, ITwioWriter writer);
}
