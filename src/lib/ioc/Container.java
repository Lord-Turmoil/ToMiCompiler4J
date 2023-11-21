/*******************************************************************************
 * Copyright (C) 2023 Tony Skywalker. All Rights Reserved
 */

package lib.ioc;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class Container implements IContainer {
    private static Container globalContainer = null;

    private final Dictionary<Class<?>, Entry> pool = new Hashtable<>();

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
        pool.put(cls, new Entry(instance));
        return this;
    }

    @Override
    public IContainer addSingleton(Class<?> cls, Class<?> impl) {
        pool.put(cls, new Entry(impl, null, null, true));
        return this;
    }

    @Override
    public IContainer addSingleton(Class<?> cls, Class<?> impl, Class<?>... dependencies) {
        pool.put(cls, new Entry(impl, dependencies));
        return this;
    }


    @Override
    public IContainer addTransient(Class<?> cls, Class<?> impl, Class<?>... dependencies) {
        pool.put(cls, new Entry(impl, dependencies));
        return this;
    }

    @Override
    public <T> T resolve(Class<T> cls) {
        var entry = pool.get(cls);
        if (entry == null) {
            return null;
        }
        try {
            return (T) entry.getInstance();
        } catch (NoSuchItemException e) {
            return null;
        }
    }

    @Override
    public <T> T resolveRequired(Class<T> cls) throws NoSuchItemException {
        var entry = pool.get(cls);
        if (entry == null) {
            throw new NoSuchItemException("Class not registered");
        }
        return (T) entry.getInstance();
    }

    private class Entry {
        public final Class<?> cls;
        public final List<Class<?>> dependencies;
        public Object instance;
        public final boolean isSingleton;

        public Entry(Object instance) {
            this(instance.getClass(), null, instance, true);
        }

        public Entry(Class<?> cls, Class<?>... dependencies) {
            this(cls, Arrays.stream(dependencies).toList(), null, false);
        }

        public Entry(Class<?> cls, boolean isSingleton, Class<?>... dependencies) {
            this(cls, Arrays.stream(dependencies).toList(), null, isSingleton);
        }

        public Entry(Class<?> cls) {
            this(cls, null, null, false);
        }

        public Entry(Class<?> cls, List<Class<?>> dependencies, Object instance, boolean isSingleton) {
            this.cls = cls;
            this.dependencies = dependencies;
            this.instance = instance;
            this.isSingleton = isSingleton;
        }

        public Object getInstance() {
            try {
                if (isSingleton && instance != null) {
                    return instance;
                }

                if (dependencies == null || dependencies.isEmpty()) {
                    return cls.getConstructor().newInstance();
                }

                List<Object> objects = new ArrayList<>();
                Class<?>[] depCls = new Class[dependencies.size()];
                int i = 0;
                for (Class<?> dep : dependencies) {
                    objects.add(resolveRequired(dep));
                    depCls[i++] = dep;
                }
                var ctor = cls.getConstructor(depCls);
                var newInstance = ctor.newInstance(objects.toArray());
                if (isSingleton) {
                    instance = newInstance;
                }
                return newInstance;
            } catch (NoSuchMethodException e) {
                throw new NoSuchItemException(e);
            } catch (InvocationTargetException e) {
                throw new NoSuchItemException(e);
            } catch (InstantiationException e) {
                throw new NoSuchItemException(e);
            } catch (IllegalAccessException e) {
                throw new NoSuchItemException(e);
            }
        }
    }
}
