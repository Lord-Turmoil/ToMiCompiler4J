/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.parser.ast;

public class NonTerminalSyntaxNode extends SyntaxNode {
    NonTerminalSyntaxNode(SyntaxTypes type) {
        super(SyntaxNodeTypes.NON_TERMINAL, type);
    }

    @Override
    public boolean accept(IAstVisitor visitor) {
        if (visitor.visitEnter(this)) {
            for (var node = getFirstChild(); node != null; node = node.getNextSibling()) {
                if (!node.accept(visitor)) {
                    break;
                }
            }
        }
        return visitor.visitExit(this);
    }
}
