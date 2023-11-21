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
