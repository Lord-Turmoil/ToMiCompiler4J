/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.parser.ast;

public class EpsilonSyntaxNode extends SyntaxNode {
    EpsilonSyntaxNode() {
        super(SyntaxNodeTypes.EPSILON, SyntaxTypes.EPSILON);
    }

    @Override
    public boolean accept(IAstVisitor visitor) {
        return visitor.visit(this);
    }
}
