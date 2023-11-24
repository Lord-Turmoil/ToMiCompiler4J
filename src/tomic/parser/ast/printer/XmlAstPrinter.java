/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.parser.ast.printer;

import lib.twio.ITwioWriter;
import tomic.lexer.token.ITokenMapper;
import tomic.parser.ast.IAstVisitor;
import tomic.parser.ast.SyntaxNode;
import tomic.parser.ast.SyntaxTree;
import tomic.parser.ast.mapper.ISyntaxMapper;

import java.util.Map;

public class XmlAstPrinter implements IAstPrinter, IAstVisitor {
    private final ITokenMapper tokenMapper;
    private final ISyntaxMapper syntaxMapper;
    private ITwioWriter writer;
    private final int indent;
    private int depth;

    public XmlAstPrinter(ITokenMapper tokenMapper, ISyntaxMapper syntaxMapper) {
        this.tokenMapper = tokenMapper;
        this.syntaxMapper = syntaxMapper;
        depth = 0;
        indent = 2;
    }


    @Override
    public void print(SyntaxTree tree, ITwioWriter writer) {
        this.writer = writer;
        depth = -1;
        tree.accept(this);
    }

    @Override
    public boolean visitEnter(SyntaxNode node) {
        String descr = syntaxMapper.description(node.getType());
        if (descr != null) {
            depth++;
            printIndent(depth);
            writer.write("<" + descr);
            for (Map.Entry<String, String> attr : node.getAttributes().entrySet()) {
                writer.write(String.format(" %s='%s'", attr.getKey(), attr.getValue()));
            }
            writer.writeLine(">");
        }
        return true;
    }

    @Override
    public boolean visitExit(SyntaxNode node) {
        String descr = syntaxMapper.description(node.getType());

        if (descr != null) {
            printIndent(depth);
            writer.writeLine("</" + descr + ">");
            depth--;
        }

        return true;
    }

    @Override
    public boolean visit(SyntaxNode node) {
        depth++;
        if (node.isNonTerminal()) {
            visitNonTerminal(node);
        } else if (node.isTerminal()) {
            visitTerminal(node);
        } else {
            throw new IllegalStateException("What the?");
        }
        depth--;

        return true;
    }

    private void visitNonTerminal(SyntaxNode node) {
        String descr = syntaxMapper.description(node.getType());

        if (descr != null) {
            printIndent(depth);
            writer.write("<" + descr);
            for (var attr : node.getAttributes().entrySet()) {
                writer.write(String.format(" %s='%s'", attr.getKey(), attr.getValue()));
            }
            writer.writeLine(" />");
        }
    }

    private void visitTerminal(SyntaxNode node) {
        var syntacticDescr = syntaxMapper.description(node.getType());
        if (syntacticDescr == null) {
            return;
        }

        printIndent(depth);
        writer.write("<" + syntacticDescr);

        var tokenDescr = tokenMapper.description(node.getToken().type);
        writer.write(String.format(" token='%s'", tokenDescr == null ? "" : tokenDescr));
        writer.write(" lexeme='");
        String lexeme = node.getToken().lexeme;
        for (int i = 0; i < lexeme.length(); i++) {
            switch (lexeme.charAt(i)) {
                case '&' -> writer.write("&amp;");
                case '<' -> writer.write("&lt;");
                case '>' -> writer.write("&gt;");
                case '\n' -> writer.write("\\n");
                default -> writer.write(lexeme.charAt(i));
            }
        }
        writer.write('\'');
        writer.write(String.format(" line='%d'", node.getToken().lineNo));
        writer.write(String.format(" char='%d'", node.getToken().charNo));

        for (var attr : node.getAttributes().entrySet()) {
            writer.write(String.format(" %s='%s'", attr.getKey(), attr.getValue()));
        }
        writer.writeLine(" />");
    }

    void printIndent(int depth) {
        for (int i = 0; i < depth * indent; i++) {
            writer.write(' ');
        }
    }
}
