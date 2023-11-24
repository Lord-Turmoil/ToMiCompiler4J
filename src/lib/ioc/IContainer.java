/*
 * Copyright (C) Tony's Studio 2018 - 2023. All rights reserved.
 *
 *   For BUAA 2023 Compiler Technology
 */

package lib.ioc;

public interface IContainer {
    IContainer addSingleton(Class<?> cls, Object instance);
    IContainer addSingleton(Class<?> cls, Class<?> impl);

    IContainer addSingleton(Class<?> cls, Class<?> impl, Class<?>... dependencies);

    IContainer addTransient(Class<?> cls, Class<?> impl, Class<?>... dependencies);

    <T> T resolve(Class<T> cls);

    <T> T resolveRequired(Class<T> cls) throws NoSuchItemException;
}
