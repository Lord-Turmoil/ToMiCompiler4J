/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.lexer.token;

public interface ITokenMapper {
    TokenTypes type(String lexeme);

    String lexeme(TokenTypes type);

    String description(TokenTypes type);

    boolean isKeyword(String lexeme);
}
