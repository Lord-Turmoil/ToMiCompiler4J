package tomic.parser.ast;

import tomic.lexer.token.Token;

public class TerminalSyntaxNode extends SyntaxNode {
    TerminalSyntaxNode(Token token) {
        super(SyntaxNodeTypes.TERMINAL, SyntaxTypes.TERMINATOR, token);
    }

    @Override
    public boolean accept(IAstVisitor visitor) {
        return visitor.visit(this);
    }
}
