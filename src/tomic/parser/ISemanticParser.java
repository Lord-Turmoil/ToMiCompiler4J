/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.parser;

import tomic.parser.ast.SyntaxTree;
import tomic.parser.table.SymbolTable;

public interface ISemanticParser {

    SymbolTable parse(SyntaxTree tree);
}
