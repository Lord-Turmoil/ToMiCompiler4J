package tomic.parser.ast;

import tomic.lexer.token.Token;

public class SyntaxTree {
    private SyntaxNode root;

    public SyntaxNode newTerminalNode(Token token) {
        var node = new TerminalSyntaxNode(token);
        node.tree = this;
        return node;
    }

    public SyntaxNode newNonTerminalNode(SyntaxTypes type) {
        var node = new NonTerminalSyntaxNode(type);
        node.tree = this;
        return node;
    }

    public void deleteNode(SyntaxNode node) {
        if (node == root) {
            root = null;
        }

        if (node.parent != null) {
            node.parent.removeChild(node);
        }
    }

    public SyntaxNode getRoot() {
        return root;
    }

    public void setRoot(SyntaxNode root) {
        this.root = root;
    }

    public boolean accept(IAstVisitor visitor) {
        return root.accept(visitor);
    }
}
