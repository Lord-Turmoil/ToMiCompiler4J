/*******************************************************************************
 * Copyright (C) 2023 Tony Skywalker. All Rights Reserved
 */

package lib.ioc;

public interface IContainer {
    IContainer addSingleton(Class<?> cls, Object instance);

    IContainer addTransient(Class<?> cls, Class<?>... dependencies);

    <T> T resolve(Class<T> cls);

    <T> T resolveRequired(Class<T> cls) throws NoSuchItemException;
}
