package tomic.lexer.token;

import java.util.Collection;

public class Token {
    public TokenTypes type;  // The type of the token/
    public String lexeme;   // The actual string of the token.
    public int lineNo;  // The line number of the token.
    public int charNo;  // The character number of the token.

    public Token(TokenTypes type, String lexeme, int lineNo, int charNo) {
        this.type = type;
        this.lexeme = lexeme;
        this.lineNo = lineNo;
        this.charNo = charNo;
    }

    public Token(TokenTypes type) {
        this(type, "", 0, 0);
    }

    public static TokenTypes type(Token token) {
        return token != null ? token.type : TokenTypes.UNKNOWN;
    }

    @Override
    public String toString() {
        return "(" + lineNo + ":" + charNo + ") " + type + " " + lexeme.replace("\n", "\\n");
    }

    public boolean is(TokenTypes type) {
        return this.type == type;
    }

    public boolean is(TokenTypes... types) {
        for (var type : types) {
            if (this.type == type) {
                return true;
            }
        }
        return false;
    }

    public boolean is(Collection<TokenTypes> types) {
        return types.contains(this.type);
    }

    public boolean isNot(TokenTypes type) {
        return this.type != type;
    }

    public boolean isNot(TokenTypes... types) {
        for (var type : types) {
            if (this.type == type) {
                return false;
            }
        }
        return true;
    }
}
