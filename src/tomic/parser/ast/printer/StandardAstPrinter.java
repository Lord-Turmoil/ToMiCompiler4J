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

public class StandardAstPrinter implements IAstPrinter, IAstVisitor {
    private final ITokenMapper tokenMapper;
    private final ISyntaxMapper syntaxMapper;
    private ITwioWriter writer;

    public StandardAstPrinter(ITokenMapper tokenMapper, ISyntaxMapper syntaxMapper) {
        this.tokenMapper = tokenMapper;
        this.syntaxMapper = syntaxMapper;
    }

    @Override
    public void print(SyntaxTree tree, ITwioWriter writer) {
        this.writer = writer;
        tree.accept(this);
    }

    @Override
    public boolean visitExit(SyntaxNode node) {
        if (node.isNonTerminal()) {
            visitNonTerminal(node);
        }
        return true;
    }

    @Override
    public boolean visit(SyntaxNode node) {
        if (node.isTerminal()) {
            visitTerminal(node);
        }
        return true;
    }

    public void visitNonTerminal(SyntaxNode node) {
        String descr = syntaxMapper.description(node.getType());
        if (descr != null) {
            writer.writeLine("<" + descr + ">");
        }
    }

    public void visitTerminal(SyntaxNode node) {
        var token = node.getToken();
        String descr = tokenMapper.description(token.type);

        writer.write(descr);
        writer.write(" ");
        writer.writeLine(token.lexeme.replace("\n", "\\n"));
    }
}
