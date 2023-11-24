/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package lib.twio;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public interface ITwioReader {
    boolean hasNext();
    int read();
    int rewind();

    int getLineNo();
    int getCharNo();
}
