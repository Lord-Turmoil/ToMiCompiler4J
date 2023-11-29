/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.parser.ast;

import tomic.lexer.token.TokenTypes;
import tomic.parser.table.ConstantEntry;
import tomic.parser.table.SymbolTableBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AstExt {
    private AstExt() {}

    public static int countDirectChildNode(SyntaxNode node, SyntaxTypes type) {
        int count = 0;
        for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.is(type)) {
                count++;
            }
        }
        return count;
    }

    public static int countDirectTerminalNode(SyntaxNode node, TokenTypes type) {
        int count = 0;
        for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.isTerminal() && child.getToken().type == type) {
                count++;
            }
        }
        return count;
    }

    public static SyntaxNode getDirectChildNode(SyntaxNode node, SyntaxTypes type) {
        return getDirectChildNode(node, type, 1);
    }

    public static SyntaxNode getDirectChildNode(SyntaxNode node, SyntaxTypes type, int index) {
        assert index != 0;
        if (node == null) {
            return null;
        }

        if (index > 0) {
            return frontGetDirectChildNode(node, type, index);
        } else {
            return rearGetDirectChildNode(node, type, -index);
        }
    }

    private static SyntaxNode frontGetDirectChildNode(SyntaxNode node, SyntaxTypes type, int index) {
        int count = 0;
        for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.is(type)) {
                count++;
                if (count == index) {
                    return child;
                }
            }
        }
        return null;
    }

    private static SyntaxNode rearGetDirectChildNode(SyntaxNode node, SyntaxTypes type, int index) {
        int count = 0;
        for (var child = node.getLastChild(); child != null; child = child.getPrevSibling()) {
            if (child.is(type)) {
                count++;
                if (count == index) {
                    return child;
                }
            }
        }
        return null;
    }

    public static List<SyntaxNode> getDirectChildNodes(SyntaxNode node, SyntaxTypes type) {
        List<SyntaxNode> nodes = new ArrayList<>();
        for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.is(type)) {
                nodes.add(child);
            }
        }
        return nodes;
    }

    private static int currentCount;

    public static SyntaxNode getChildNode(SyntaxNode node, SyntaxTypes type) {
        return getChildNode(node, type, 1);
    }

    public static SyntaxNode getChildNode(SyntaxNode node, SyntaxTypes type, int index) {
        assert index != 0;

        currentCount = 0;
        if (index > 0) {
            return frontGetChildNode(node, type, index);
        } else {
            return rearGetChildNode(node, type, -index);
        }
    }

    private static SyntaxNode frontGetChildNode(SyntaxNode node, SyntaxTypes type, int index) {
        if (node.is(type)) {
            currentCount++;
            if (currentCount == index) {
                return node;
            }
            return null;
        }

        for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            var result = frontGetChildNode(child, type, index);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    private static SyntaxNode rearGetChildNode(SyntaxNode node, SyntaxTypes type, int index) {
        if (node.is(type)) {
            currentCount++;
            if (currentCount == index) {
                return node;
            }
            return null;
        }

        for (var child = node.getLastChild(); child != null; child = child.getPrevSibling()) {
            var result = rearGetChildNode(child, type, index);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    public static List<SyntaxNode> getChildNodes(SyntaxNode node, SyntaxTypes type) {
        List<SyntaxNode> nodes = new ArrayList<>();
        getChildNodes(node, type, nodes);
        return nodes;
    }

    public static List<SyntaxNode> getChildNodes(SyntaxNode node, SyntaxTypes... types) {
        List<SyntaxNode> nodes = new ArrayList<>();
        getChildNodes(node, nodes, types);
        return nodes;
    }

    private static void getChildNodes(SyntaxNode node, SyntaxTypes type, List<SyntaxNode> nodes) {
        if (node.is(type)) {
            nodes.add(node);
            return;
        }

        for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            getChildNodes(child, type, nodes);
        }
    }

    private static void getChildNodes(SyntaxNode node, List<SyntaxNode> nodes, SyntaxTypes... types) {
        if (node.is(types)) {
            nodes.add(node);
            return;
        }

        for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            getChildNodes(child, nodes, types);
        }
    }

    public static boolean hasParent(SyntaxNode node, SyntaxTypes type) {
        var parent = node.getParent();
        while (parent != null) {
            if (parent.is(type)) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    public static boolean hasAttribute(SyntaxNode node, String name) {
        if (node.hasAttribute(name)) {
            return true;
        }

        for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (hasAttribute(child, name)) {
                return true;
            }
        }

        return false;
    }

    public static boolean hasInheritedAttribute(SyntaxNode node, String name) {
        if (node.hasAttribute(name)) {
            return true;
        }

        var parent = node.getParent();
        while (parent != null) {
            if (parent.hasAttribute(name)) {
                return true;
            }
            parent = parent.getParent();
        }

        return false;
    }

    public static String getInheritedAttribute(SyntaxNode node, String name) {
        return getInheritedAttribute(node, name, null);
    }

    public static String getInheritedAttribute(SyntaxNode node, String name, String defaultValue) {
        if (node.hasAttribute(name)) {
            return node.getAttribute(name, defaultValue);
        }

        var parent = node.getParent();
        while (parent != null) {
            if (parent.hasAttribute(name)) {
                return parent.getAttribute(name, defaultValue);
            }
            parent = parent.getParent();
        }

        return null;
    }

    public static int getInheritedIntAttribute(SyntaxNode node, String name) {
        return getInheritedIntAttribute(node, name, 0);
    }

    public static int getInheritedIntAttribute(SyntaxNode node, String name, int defaultValue) {
        if (node.hasAttribute(name)) {
            return node.getIntAttribute(name, defaultValue);
        }

        var parent = node.getParent();
        while (parent != null) {
            if (parent.hasAttribute(name)) {
                return parent.getIntAttribute(name, defaultValue);
            }
            parent = parent.getParent();
        }

        return defaultValue;
    }

    public static boolean getInheritedBoolAttribute(SyntaxNode node, String name) {
        return getInheritedBoolAttribute(node, name, false);
    }

    public static boolean getInheritedBoolAttribute(SyntaxNode node, String name, boolean defaultValue) {
        if (node.hasAttribute(name)) {
            return node.getBoolAttribute(name, defaultValue);
        }

        var parent = node.getParent();
        while (parent != null) {
            if (parent.hasAttribute(name)) {
                return parent.getBoolAttribute(name, defaultValue);
            }
            parent = parent.getParent();
        }

        return defaultValue;
    }

    public static boolean hasSynthesizedAttribute(SyntaxNode node, String name) {
        if (hasAttribute(node, name)) {
            return true;
        }

        if (node.getPrevSibling() != null) {
            return hasSynthesizedAttribute(node.getPrevSibling(), name);
        }

        return false;
    }

    public static String getSynthesizedAttribute(SyntaxNode node, String name) {
        return getSynthesizedAttribute(node, name, null);
    }

    public static String getSynthesizedAttribute(SyntaxNode node, String name, String defaultValue) {
        String[] value = { defaultValue };

        if (querySynthesizedAttribute(node, name, value, defaultValue)) {
            return value[0];
        }

        return defaultValue;
    }

    public static int getSynthesizedIntAttribute(SyntaxNode node, String name) {
        return getSynthesizedIntAttribute(node, name, 0);
    }

    public static int getSynthesizedIntAttribute(SyntaxNode node, String name, int defaultValue) {
        int[] value = { defaultValue };

        if (querySynthesizedIntAttribute(node, name, value, defaultValue)) {
            return value[0];
        }

        return defaultValue;
    }

    public static boolean getSynthesizedBoolAttribute(SyntaxNode node, String name) {
        return getSynthesizedBoolAttribute(node, name, false);
    }

    public static boolean getSynthesizedBoolAttribute(SyntaxNode node, String name, boolean defaultValue) {
        boolean[] value = { defaultValue };

        if (querySynthesizedBoolAttribute(node, name, value, defaultValue)) {
            return value[0];
        }

        return defaultValue;
    }

    private static boolean querySynthesizedAttribute(SyntaxNode node, String name, String[] value, String defaultValue) {
        if (node.hasAttribute(name)) {
            value[0] = node.getAttribute(name, defaultValue);
            return true;
        }

        for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (querySynthesizedAttribute(child, name, value, defaultValue)) {
                return true;
            }
        }

        return false;
    }

    private static boolean querySynthesizedIntAttribute(SyntaxNode node, String name, int[] value, int defaultValue) {
        String[] attr = { null };
        if (querySynthesizedAttribute(node, name, attr, null)) {
            value[0] = Integer.parseInt(attr[0]);
            return true;
        }

        value[0] = defaultValue;

        return false;
    }

    private static boolean querySynthesizedBoolAttribute(SyntaxNode node, String name, boolean[] value, boolean defaultValue) {
        String[] attr = { null };
        if (querySynthesizedAttribute(node, name, attr, null)) {
            value[0] = Boolean.parseBoolean(attr[0]);
            return true;
        }

        value[0] = defaultValue;

        return false;
    }

    /******************************************************************/
    // Array serialization
    // The format is like this:
    // 1 2 3 4 5
    public static String serializeArray(List<Integer> array) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < array.size(); i++) {
            builder.append(array.get(i));
            if (i != array.size() - 1) {
                builder.append(' ');
            }
        }
        return builder.toString();
    }

    public static ArrayList<Integer> deserializeArray(String str) {
        ArrayList<Integer> array = new ArrayList<>();
        Scanner scanner = new Scanner(str);
        while (scanner.hasNextInt()) {
            array.add(scanner.nextInt());
        }
        return array;
    }

    public static int getFormatStringArgCount(String format) {
        int pos = format.indexOf("%d");
        int count = 0;
        while (pos != -1) {
            count++;
            pos = format.indexOf("%d", pos + 1);
        }
        return count;
    }

    /******************************************************************/
    // Compile time Evaluation
    public static int evaluateBinary(String op, int left, int right) {
        return switch (op) {
            case "+" -> left + right;
            case "-" -> left - right;
            case "*" -> left * right;
            case "/" -> left / right;
            case "%" -> left % right;
            case "&&" -> left != 0 && right != 0 ? 1 : 0;
            case "||" -> left != 0 || right != 0 ? 1 : 0;
            case "<" -> left < right ? 1 : 0;
            case ">" -> left > right ? 1 : 0;
            case "<=" -> left <= right ? 1 : 0;
            case ">=" -> left >= right ? 1 : 0;
            case "==" -> left == right ? 1 : 0;
            case "!=" -> left != right ? 1 : 0;
            default -> throw new IllegalStateException("Unexpected value: " + op);
        };
    }

    public static int evaluateUnary(String op, int value) {
        return switch (op) {
            case "+" -> value;
            case "-" -> -value;
            case "!" -> value == 0 ? 1 : 0;
            default -> throw new IllegalStateException("Unexpected value: " + op);
        };
    }


    public static int evaluateNumber(SyntaxNode node) {
        return Integer.parseInt(node.getFirstChild().getToken().lexeme);
    }

    // I hate Java. So clumsy.
    public static boolean tryEvaluate(SyntaxNode node, SymbolTableBlock block, int[] value) {
        if (node.getIntAttribute("dim", -1) != 0) {
            return false;
        }

        String name = node.getFirstChild().getToken().lexeme;
        var rawEntry = block.findEntry(name);
        if ((rawEntry == null) || !rawEntry.isConstant()) {
            return false;
        }
        ConstantEntry entry = (ConstantEntry) rawEntry;
        if (entry.isInteger()) {
            value[0] = entry.getValue();
            return true;
        }

        var indexNodes = getDirectChildNodes(node, SyntaxTypes.EXP);
        if (indexNodes.size() != entry.getDimension()) {
            return false;
        }

        int size = 1;
        ArrayList<Integer> indices = new ArrayList<>();
        for (int i = 0; i < indexNodes.size(); i++) {
            if (!indexNodes.get(i).getBoolAttribute("det")) {
                return false;
            }
            indices.add(indexNodes.get(i).getIntAttribute("value"));
        }

        value[0] = entry.getValue(indices);
        return true;
    }
}
