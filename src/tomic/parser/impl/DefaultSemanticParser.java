/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

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
