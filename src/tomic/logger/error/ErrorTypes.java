package tomic.logger.error;

public enum ErrorTypes {
    UNKNOWN,

    UNEXPECTED_TOKEN,

    REDEFINED_SYMBOL,
    UNDEFINED_SYMBOL,

    ARGUMENT_COUNT_MISMATCH,
    ARGUMENT_TYPE_MISMATCH,

    RETURN_TYPE_MISMATCH,
    MISSING_RETURN_STATEMENT,

    ASSIGN_TO_CONST,

    MISSING_SEMICOLON,
    MISSING_RIGHT_PARENTHESIS,
    MISSING_RIGHT_BRACKET,
    MISSING_RIGHT_BRACE,

    PRINTF_EXTRA_ARGUMENTS,

    ILLEGAL_BREAK,
    ILLEGAL_CONTINUE,
}