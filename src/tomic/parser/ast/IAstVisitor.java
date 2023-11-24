/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.parser.ast;

public interface IAstVisitor {
    default boolean visitEnter(SyntaxNode node) {
        return true;
    }

    default boolean visitExit(SyntaxNode node) {
        return true;
    }

    default boolean visit(SyntaxNode node) {
        return true;
    }
}
