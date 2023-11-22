package tomic.parser.impl;

import tomic.parser.ISemanticAnalyzer;
import tomic.parser.ISemanticParser;
import tomic.parser.ast.SyntaxTree;
import tomic.parser.table.SymbolTable;

public class DefaultSemanticParser implements ISemanticParser {
    private final ISemanticAnalyzer analyzer;

    public DefaultSemanticParser(ISemanticAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    @Override
    public SymbolTable parse(SyntaxTree tree) {
        return analyzer.analyze(tree);
    }
}
