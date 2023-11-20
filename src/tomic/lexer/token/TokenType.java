package tomic.lexer.token;

public enum TokenType {
    UNKNOWN,        // unknown token
    TERMINATOR,     // terminator

    IDENTIFIER,     // identifier
    INTEGER,         // integer constant
    FORMAT,         // format string, e.g. "a = %d"
    MAIN,           // main
    RETURN,         // return
    GETINT,         // getint
    PRINTF,         // printf

    IF,             // if
    ELSE,           // else

    FOR,            // for
    BREAK,          // break
    CONTINUE,       // continue

    CONST,          // const
    INT,            // int
    VOID,           // void

    NOT,            // !
    AND,            // &&
    OR,             // ||
    PLUS,           // +
    MINUS,          // -
    MULTIPLY,       // *
    DIVIDE,         // /
    MOD,            // %
    LESS,           // <
    LESS_EQUAL,     // <=
    GREATER,        // >
    GREATER_EQUAL,  // >=
    EQUAL,          // ==
    NOT_EQUAL,      // !=
    ASSIGN,         // =

    SEMICOLON,      // ;
    COMMA,          // ,

    LEFT_PARENTHESIS,    // (
    RIGHT_PARENTHESIS,   // )
    LEFT_BRACE,          // {
    RIGHT_BRACE,         // }
    LEFT_BRACKET,        // [
    RIGHT_BRACKET,       // ]
}
