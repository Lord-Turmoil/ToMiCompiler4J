package tomic.lexer.token;

public class Token {
    public TokenType type;  // The type of the token/
    public String lexeme;   // The actual string of the token.
    public int lineNo;  // The line number of the token.
    public int charNo;  // The character number of the token.

    public Token(TokenType type, String lexeme, int lineNo, int charNo) {
        this.type = type;
        this.lexeme = lexeme;
        this.lineNo = lineNo;
        this.charNo = charNo;
    }

    public Token(TokenType type) {
        this(type, "", 0, 0);
    }

    public static TokenType type(Token token) {
        return token != null ? token.type : TokenType.UNKNOWN;
    }
}
