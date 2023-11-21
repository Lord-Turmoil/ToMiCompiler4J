package tomic.lexer.token;

public interface ITokenMapper {
    TokenTypes type(String lexeme);

    String lexeme(TokenTypes type);

    String description(TokenTypes type);

    boolean isKeyword(String lexeme);
}
