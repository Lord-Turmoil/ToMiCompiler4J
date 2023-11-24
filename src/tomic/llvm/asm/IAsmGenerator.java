/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.llvm.asm;

import tomic.llvm.ir.Module;
import tomic.parser.ast.SyntaxTree;
import tomic.parser.table.SymbolTable;

public interface IAsmGenerator {
    Module generate(SyntaxTree syntaxTree, SymbolTable symbolTable, String name);
}
