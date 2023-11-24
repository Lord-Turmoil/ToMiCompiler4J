/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.parser.ast.printer;

import lib.twio.ITwioWriter;
import tomic.parser.ast.SyntaxTree;

public interface IAstPrinter {
    void print(SyntaxTree tree, ITwioWriter writer);
}
