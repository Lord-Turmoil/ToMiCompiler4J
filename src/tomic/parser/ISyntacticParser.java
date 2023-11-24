/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.parser;

import lib.twio.ITwioReader;
import tomic.parser.ast.SyntaxTree;

public interface ISyntacticParser {
    ISyntacticParser setReader(ITwioReader reader);
    SyntaxTree parse();
}
