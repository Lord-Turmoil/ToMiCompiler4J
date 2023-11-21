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
