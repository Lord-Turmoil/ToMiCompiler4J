/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.logger.error.impl;

import tomic.logger.error.ErrorTypes;
import tomic.logger.error.IErrorMapper;

import java.util.HashMap;
import java.util.Map;

public class StandardErrorMapper implements IErrorMapper {
    private final Map<ErrorTypes, String> descriptions = new HashMap<>();

    public StandardErrorMapper() {
        descriptions.put(ErrorTypes.UNKNOWN, "Unknown error");

        descriptions.put(ErrorTypes.UNEXPECTED_TOKEN, "a");

        descriptions.put(ErrorTypes.REDEFINED_SYMBOL, "b");
        descriptions.put(ErrorTypes.UNDEFINED_SYMBOL, "c");

        descriptions.put(ErrorTypes.ARGUMENT_COUNT_MISMATCH, "d");
        descriptions.put(ErrorTypes.ARGUMENT_TYPE_MISMATCH, "e");

        descriptions.put(ErrorTypes.RETURN_TYPE_MISMATCH, "f");
        descriptions.put(ErrorTypes.MISSING_RETURN_STATEMENT, "g");

        descriptions.put(ErrorTypes.ASSIGN_TO_CONST, "h");

        descriptions.put(ErrorTypes.MISSING_SEMICOLON, "i");
        descriptions.put(ErrorTypes.MISSING_RIGHT_PARENTHESIS, "j");
        descriptions.put(ErrorTypes.MISSING_RIGHT_BRACKET, "k");
        descriptions.put(ErrorTypes.MISSING_RIGHT_BRACE, "z");

        descriptions.put(ErrorTypes.PRINTF_EXTRA_ARGUMENTS, "l");

        descriptions.put(ErrorTypes.ILLEGAL_BREAK, "m");
        descriptions.put(ErrorTypes.ILLEGAL_CONTINUE, "m");
    }


    @Override
    public String description(ErrorTypes type) {
        return descriptions.getOrDefault(type, "Unknown");
    }
}
