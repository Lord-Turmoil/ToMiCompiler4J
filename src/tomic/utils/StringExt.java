/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package tomic.utils;

public class StringExt {
    private StringExt() {}

    public static boolean contains(String source, int ch) {
        return source.indexOf(ch) != -1;
    }

    public static boolean isNullOrEmpty(String source) {
        return source == null || source.isEmpty();
    }
}
