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

public class VerboseErrorMapper implements IErrorMapper {
    private final Map<ErrorTypes, String> descriptions = new HashMap<>();

    public VerboseErrorMapper() {
        descriptions.put(ErrorTypes.UNKNOWN, "Unknown error");
        descriptions.put(ErrorTypes.UNEXPECTED_TOKEN, "Unexpected token");

        descriptions.put(ErrorTypes.REDEFINED_SYMBOL, "Redefined symbol");
        descriptions.put(ErrorTypes.UNDEFINED_SYMBOL, "Undefined symbol");

        descriptions.put(ErrorTypes.ARGUMENT_COUNT_MISMATCH, "Argument count mismatch");
        descriptions.put(ErrorTypes.ARGUMENT_TYPE_MISMATCH, "Argument type mismatch");

        descriptions.put(ErrorTypes.RETURN_TYPE_MISMATCH, "Return type mismatch");
        descriptions.put(ErrorTypes.MISSING_RETURN_STATEMENT, "Missing return statement");

        descriptions.put(ErrorTypes.ASSIGN_TO_CONST, "Assign to const");

        descriptions.put(ErrorTypes.MISSING_SEMICOLON, "Missing ;");
        descriptions.put(ErrorTypes.MISSING_RIGHT_PARENTHESIS, "Missing )");
        descriptions.put(ErrorTypes.MISSING_RIGHT_BRACKET, "Missing ]");
        descriptions.put(ErrorTypes.MISSING_RIGHT_BRACE, "Missing }");

        descriptions.put(ErrorTypes.PRINTF_EXTRA_ARGUMENTS, "Extra arguments for printf");

        descriptions.put(ErrorTypes.ILLEGAL_BREAK, "Illegal break");
        descriptions.put(ErrorTypes.ILLEGAL_CONTINUE, "Illegal continue");
    }

    @Override
    public String description(ErrorTypes type) {
        return descriptions.getOrDefault(type, "Unknown");
    }
}
