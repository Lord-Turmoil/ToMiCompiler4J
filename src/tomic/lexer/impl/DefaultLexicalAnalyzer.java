package tomic.lexer.impl;

import lib.twio.ITwioReader;
import tomic.lexer.ILexicalAnalyzer;
import tomic.lexer.impl.task.*;
import tomic.lexer.token.ITokenMapper;
import tomic.lexer.token.Token;
import tomic.lexer.token.TokenTypes;
import tomic.utils.Constants;
import tomic.utils.StringExt;

import java.util.ArrayList;

public class DefaultLexicalAnalyzer implements ILexicalAnalyzer {
    private ITwioReader reader;
    private ArrayList<LexicalTask> tasks;
    private final ITokenMapper mapper;

    public DefaultLexicalAnalyzer(ITokenMapper mapper) {
        this.mapper = mapper;
        initTasks();
    }

    @Override
    public void setReader(ITwioReader reader) {
        this.reader = reader;
    }

    @Override
    public Token next() {
        Token token;

        do {
            token = nextImpl();
        } while (token == null);

        return token;
    }

    private void initTasks() {
        tasks = new ArrayList<>();

        tasks.add(new NumberLexicalTask(mapper));
        tasks.add(new IdentifierLexicalTask(mapper));
        tasks.add(new StringLexicalTask(mapper));
        tasks.add(new SingleOpLexicalTask(mapper));
        tasks.add(new DoubleOpLexicalTask(mapper));
        tasks.add(new DelimiterLexicalTask(mapper));
        tasks.add(new UnknownLexicalTask(mapper));
    }

    private Token nextImpl() {
        int lookahead;

        do {
            lookahead = reader.read();
        } while (StringExt.contains(Constants.WHITESPACES, lookahead));

        if (lookahead == Constants.EOF) {
            return new Token(TokenTypes.TERMINATOR, "", reader.getLineNo(), reader.getCharNo());
        }

        Token token = null;
        for (LexicalTask task : tasks) {
            if (task.beginsWith(lookahead)) {
                reader.rewind();
                token = task.analyze(reader);
                break;
            }
        }

        if (token == null) {
            throw new IllegalStateException("No lexical task found for character " + (char) lookahead);
        }

        return token;
    }
}
