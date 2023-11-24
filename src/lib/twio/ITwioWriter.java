/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package lib.twio;

import java.io.InputStream;

public interface ITwioWriter {
    void write(int ch);
    void write(char ch);
    void write(String str);
    void writeLine(String str);
    void writeLine();

    /**
     * This is different from ToMiC, which terminates the stream.
     * It does not terminate the stream.
     * @return InputStream
     */
    InputStream yield();
}
