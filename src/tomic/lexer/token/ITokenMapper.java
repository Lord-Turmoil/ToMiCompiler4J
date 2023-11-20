package tomic.lexer.token;

public interface ITokenMapper {
    TokenType type(String lexeme);

    String lexeme(TokenType type);

    String description(TokenType type);

    boolean isKeyword(String lexeme);
}
