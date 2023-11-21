package tomic.parser;

import lib.twio.ITwioReader;
import tomic.parser.ast.SyntaxTree;

public interface ISyntacticParser {
    ISyntacticParser setReader(ITwioReader reader);
    SyntaxTree parse();
}
