package tomic.parser.ast;

import tomic.lexer.token.Token;

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
    protected Map<String, String> attributes;
    ///// Type identification
    private SyntaxNodeTypes nodeType;

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

    public Token getToken() {
        return token;
    }

    public SyntaxTypes getType() {
        return type;
    }

    public boolean is(SyntaxTypes type) {
        return this.type == type;
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

    public SyntaxNode prevSibling() {
        return prev;
    }

    public SyntaxNode nextSibling() {
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
    boolean hasAttribute(String name) {
        return attributes.containsKey(name);
    }

    String attribute(String name) {
        return attribute(name, null);
    }

    String attribute(String name, String defaultValue) {
        return attributes.getOrDefault(name, defaultValue);
    }

    int intAttribute(String name) {
        return intAttribute(name, 0);
    }

    int intAttribute(String name, int defaultValue) {
        return Integer.getInteger(attribute(name, String.valueOf(defaultValue)));
    }

    boolean boolAttribute(String name) {
        return boolAttribute(name, false);
    }

    boolean boolAttribute(String name, boolean defaultValue) {
        return Boolean.getBoolean(attribute(name, String.valueOf(defaultValue)));
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    SyntaxNode setAttribute(String name, String value) {
        attributes.put(name, value);
        return this;
    }

    SyntaxNode setIntAttribute(String name, int value) {
        return setAttribute(name, String.valueOf(value));
    }

    SyntaxNode setBoolAttribute(String name, boolean value) {
        return setAttribute(name, String.valueOf(value));
    }

    SyntaxNode removeAttribute(String name) {
        attributes.remove(name);
        return this;
    }

    enum SyntaxNodeTypes {
        NON_TERMINAL,
        TERMINAL
    }
}
