package tomic.parser.ast;

import tomic.lexer.token.Token;

import java.util.HashMap;
import java.util.Map;

public abstract class SyntaxNode {
    ///// AST links
    protected SyntaxTree tree;
    protected SyntaxNode parent;
    protected SyntaxNode prev;
    protected SyntaxNode next;
    protected SyntaxNode firstChild;
    protected SyntaxNode lastChild;
    //// AST properties
    protected SyntaxTypes type;
    protected Token token;
    protected final Map<String, String> attributes = new HashMap<>();
    ///// Type identification
    private final SyntaxNodeTypes nodeType;

    SyntaxNode(SyntaxNodeTypes nodeType, SyntaxTypes type) {
        this(nodeType, type, null);
    }

    SyntaxNode(SyntaxNodeTypes nodeType, SyntaxTypes type, Token token) {
        this.nodeType = nodeType;
        this.type = type;
        this.token = token;
    }


    /*
     * ==================== Simple Getter/Setters ====================
     */
    public boolean isNonTerminal() {
        return nodeType == SyntaxNodeTypes.NON_TERMINAL;
    }

    public boolean isTerminal() {
        return nodeType == SyntaxNodeTypes.TERMINAL;
    }

    public boolean isEpsilon() {
        return nodeType == SyntaxNodeTypes.EPSILON;
    }

    public Token getToken() {
        return token;
    }

    public SyntaxTypes getType() {
        return type;
    }

    public boolean is(SyntaxTypes type) {
        return this.type == type;
    }

    public boolean is(SyntaxTypes... types) {
        for (SyntaxTypes type : types) {
            if (this.is(type)) {
                return true;
            }
        }
        return false;
    }

    public SyntaxNode getRoot() {
        SyntaxNode node = this;
        while (node.parent != null) {
            node = node.parent;
        }
        return node;
    }

    public SyntaxNode getParent() {
        return parent;
    }

    public SyntaxNode getFirstChild() {
        return firstChild;
    }

    public SyntaxNode getLastChild() {
        return lastChild;
    }

    public SyntaxNode getPrevSibling() {
        return prev;
    }

    public SyntaxNode getNextSibling() {
        return next;
    }

    public SyntaxNode childAt(int index) {
        SyntaxNode node = firstChild;
        while (index > 0 && node != null) {
            node = node.next;
            index--;
        }
        return node;
    }

    public abstract boolean accept(IAstVisitor visitor);

    /*
     * ==================== Visitor Pattern ====================
     */

    /*
     * ==================== AST Links ====================
     */
    public SyntaxNode insertEndChild(SyntaxNode child) {
        insertChildPreamble(child);
        if (lastChild != null) {
            lastChild.next = child;
            child.prev = lastChild;
            lastChild = child;
            child.next = null;
        } else {
            firstChild = lastChild = child;
            child.prev = child.next = null;
        }
        child.parent = this;
        return child;
    }

    public SyntaxNode insertFirstChild(SyntaxNode child) {
        insertChildPreamble(child);
        if (firstChild != null) {
            firstChild.prev = child;
            child.next = firstChild;
            firstChild = child;
            child.prev = null;
        } else {
            firstChild = lastChild = child;
            child.prev = child.next = null;
        }
        child.parent = this;
        return child;
    }

    public SyntaxNode insertAfterChild(SyntaxNode child, SyntaxNode after) {
        insertChildPreamble(child);
        if (after == null) {
            return insertFirstChild(child);
        }
        if (after == lastChild) {
            return insertEndChild(child);
        }

        child.prev = after;
        child.next = after.next;
        after.next.prev = child;
        after.next = child;

        child.parent = this;
        return child;
    }

    public SyntaxNode removeChild(SyntaxNode child) {
        return unlink(child);
    }

    public boolean hasChildren() {
        return firstChild != null;
    }

    public boolean hasManyChildren() {
        return firstChild != null && (firstChild != lastChild);
    }

    private void insertChildPreamble(SyntaxNode child) {
        assert child != null;
        assert tree == child.tree;

        if (child.parent != null) {
            child.parent.unlink(child);
        }
    }

    private SyntaxNode unlink(SyntaxNode child) {
        assert child != null;
        assert tree == child.tree;
        assert child.parent == this;

        if (child == firstChild) {
            firstChild = child.next;
        }
        if (child == lastChild) {
            lastChild = child.prev;
        }

        if (child.prev != null) {
            child.prev.next = child.next;
        }
        if (child.next != null) {
            child.next.prev = child.prev;
        }

        SyntaxNode ret = child.prev;
        child.prev = child.next = child.parent = null;
        return ret;
    }

    /*
     * ==================== AST Attributes ====================
     */
    public boolean hasAttribute(String name) {
        return attributes.containsKey(name);
    }

    public String getAttribute(String name) {
        return getAttribute(name, null);
    }

    public String getAttribute(String name, String defaultValue) {
        return attributes.getOrDefault(name, defaultValue);
    }

    public int getIntAttribute(String name) {
        return getIntAttribute(name, 0);
    }

    public int getIntAttribute(String name, int defaultValue) {
        String attr = getAttribute(name, null);
        if (attr == null) {
            return defaultValue;
        }
        return Integer.parseInt(attr);
    }

    public boolean getBoolAttribute(String name) {
        return getBoolAttribute(name, false);
    }

    public boolean getBoolAttribute(String name, boolean defaultValue) {
        String attr = getAttribute(name, null);
        if (attr == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(attr);
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public SyntaxNode setAttribute(String name, String value) {
        attributes.put(name, value);
        return this;
    }

    public SyntaxNode setIntAttribute(String name, int value) {
        return setAttribute(name, String.valueOf(value));
    }

    public SyntaxNode setBoolAttribute(String name, boolean value) {
        return setAttribute(name, String.valueOf(value));
    }

    public SyntaxNode removeAttribute(String name) {
        attributes.remove(name);
        return this;
    }

    public enum SyntaxNodeTypes {
        NON_TERMINAL,
        TERMINAL,
        EPSILON
    }
}
