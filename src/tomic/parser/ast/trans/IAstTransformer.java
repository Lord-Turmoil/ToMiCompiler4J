/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.parser.ast.trans;

import tomic.parser.ast.SyntaxTree;

public interface IAstTransformer {
    SyntaxTree transform(SyntaxTree tree);
}
