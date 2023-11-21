package tomic.lexer.token;

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
}
