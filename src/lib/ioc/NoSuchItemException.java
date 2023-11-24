/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package lib.ioc;

public class NoSuchItemException extends RuntimeException {
    public NoSuchItemException() {
    }

    public NoSuchItemException(String message) {
        super(message);
    }

    public NoSuchItemException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuchItemException(Throwable cause) {
        super(cause);
    }
}
