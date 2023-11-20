/*******************************************************************************
 * Copyright (C) 2023 Tony Skywalker. All Rights Reserved
 */

package lib.ioc;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.function.Supplier;

public class Container implements IContainer {
    private static Container globalContainer = null;

    private final Dictionary<Class<?>, Item> pool = new Hashtable<>();

    public static Container getGlobal() {
        if (globalContainer == null) {
            globalContainer = new Container();
        }
        return globalContainer;
    }

    /**
     * Will overwrite old value.
     */
    @Override
    public IContainer addSingleton(Class<?> cls, Object instance) {
        pool.put(cls, new Item(true, instance, null));
        return this;
    }

    @Override
    public IContainer addSingleton(Class<?> cls, Supplier<?> supplier) {
        pool.put(cls, new Item(true, null, supplier));
        return this;
    }

    @Override
    public IContainer addTransient(Class<?> cls, Supplier<?> supplier) {
        pool.put(cls, new Item(false, null, supplier));
        return null;
    }

    @Override
    public <T> T resolve(Class<T> cls) {
        return mapResolve(cls);
    }

    @Override
    public <T> T mapResolve(Class<?> cls) {
        var item = pool.get(cls);
        if (item == null) {
            return null;
        }

        try {
            if (item.isSingleton) {
                if (item.instance == null) {
                    item.instance = item.supplier.get();
                }
                return (T) item.instance;
            } else {
                return (T) item.supplier.get();
            }
        } catch (ClassCastException e) {
            return null;
        }
    }

    @Override
    public <T> T resolveRequired(Class<T> cls) throws NoSuchItemException {
        return mapResolveRequired(cls);
    }

    @Override
    public <T> T mapResolveRequired(Class<?> cls) {
        var obj = this.<T>mapResolve(cls);
        if (obj == null) {
            throw new NoSuchItemException(cls + " not registered");
        }
        return obj;
    }

    private static class Item {
        public final boolean isSingleton;
        public final Supplier<?> supplier;
        public Object instance;

        public Item(boolean isSingleton, Object instance, Supplier<?> supplier) {
            this.isSingleton = isSingleton;
            this.instance = instance;
            this.supplier = supplier;
        }
    }
}
