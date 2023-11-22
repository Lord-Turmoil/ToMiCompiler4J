package tomic.parser;

import tomic.parser.ast.SyntaxTree;
import tomic.parser.table.SymbolTable;

public interface ISemanticAnalyzer {
    SymbolTable analyze(SyntaxTree tree);
}
