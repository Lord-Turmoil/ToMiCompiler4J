package tomic.parser.ast.trans;

import tomic.parser.ast.IAstVisitor;
import tomic.parser.ast.SyntaxNode;
import tomic.parser.ast.SyntaxTree;
import tomic.parser.ast.SyntaxTypes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is a little tricky, for detailed comments, please
 * refer to the original ToMiC.
 */
public class RightRecursiveTransformer implements IAstTransformer, IAstVisitor {
    private static final Set<SyntaxTypes> RIGHT_RECURSIVE_TYPES = new HashSet<>() {
        {
            add(SyntaxTypes.ADD_EXP);
            add(SyntaxTypes.MUL_EXP);
            add(SyntaxTypes.OR_EXP);
            add(SyntaxTypes.AND_EXP);
            add(SyntaxTypes.EQ_EXP);
            add(SyntaxTypes.REL_EXP);
        }
    };

    @Override
    public SyntaxTree transform(SyntaxTree tree) {
        tree.accept(this);
        return tree;
    }

    @Override
    public boolean visitExit(SyntaxNode node) {
        ArrayList<SyntaxNode> children = new ArrayList<>();
        for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            children.add(child);
        }

        for (var child : children) {
            if (needTransform(child)) {
                transform(child);
            }
        }

        if (node.getParent() == null && needTransform(node)) {
            transform(node);
        }

        return true;
    }

    private boolean isRightRecursive(SyntaxNode node) {
        return RIGHT_RECURSIVE_TYPES.contains(node.getType());
    }

    private boolean needTransform(SyntaxNode node) {
        if (!isRightRecursive(node)) {
            return false;
        }
        var lastChild = node.getLastChild();
        return lastChild != null && lastChild.getType() == node.getType();
    }

    private void transform(SyntaxNode node) {
        if (!needTransform(node)) {
            return;
        }

        var parent = node.getParent();
        SyntaxNode mark = null;

        if (parent != null) {
            mark = parent.removeChild(node);
        }

        var lastChild = node.getLastChild();
        node.removeChild(lastChild);

        var candidate = lastChild;
        while (candidate.hasChildren() && (candidate.getFirstChild().getType() == lastChild.getType())) {
            candidate = candidate.getFirstChild();
        }

        candidate.insertFirstChild(node);

        if (parent != null) {
            parent.insertAfterChild(lastChild, mark);
        }
    }
}
