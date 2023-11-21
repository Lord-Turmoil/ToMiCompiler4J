/*******************************************************************************
 * Copyright (C) 2023 Tony Skywalker. All Rights Reserved
 */

package lib.ioc;

public interface IContainer {
    IContainer addSingleton(Class<?> cls, Object instance);

    IContainer addSingleton(Class<?> cls, Class<?> impl, Class<?>... dependencies);

    IContainer addTransient(Class<?> cls, Class<?> impl, Class<?>... dependencies);

    <T> T resolve(Class<T> cls);

    <T> T resolveRequired(Class<T> cls) throws NoSuchItemException;
}
