package tomic.parser.ast.trans;

import tomic.parser.ast.SyntaxTree;

public interface IAstTransformer {
    SyntaxTree transform(SyntaxTree tree);
}
