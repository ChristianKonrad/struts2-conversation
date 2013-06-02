package com.github.overengineer.container.key;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * @author rees.byars
 */
public class DefaultKeyRepository implements KeyRepository {

    private final Map<Object, SerializableKey> keys = new HashMap<Object, SerializableKey>();

    @Override
    public void addKey(SerializableKey key) {
        keys.put(key, key);
    }

    @Override
    public SerializableKey retrieveKey(Class cls) {
        return new ClassKey(cls);
    }

    @Override
    public SerializableKey retrieveKey(Class cls, String name) {
        return new ClassKey(cls, name);
    }

    @Override
    public SerializableKey retrieveKey(final Type type) {
        return retrieveKey(type, null);
    }

    @Override
    public SerializableKey retrieveKey(Type type, String name) {
        if (type instanceof Class) {
            return retrieveKey((Class) type, name);
        }
        SerializableKey key = keys.get(new TempKey(type, name));
        if (key != null) {
            return key;
        }
        if (type instanceof ParameterizedType) {
            return new LazyDelegatingKey(type, name);
        }
        throw new UnsupportedOperationException("Unsupported injection type [" + type + "]");
    }

    public class LazyDelegatingKey extends DelegatingSerializableKey {

        private volatile SerializableKey key;
        private transient Type type;
        private String name;

        private LazyDelegatingKey(Type type, String name) {
            this.type = type;
            this.name = name;
        }

        @Override
        protected SerializableKey getDelegateKey() {
            if (key == null) {
                synchronized (this) {
                    if (key == null) {
                        key = keys.get(new TempKey(type, name));
                    }
                }
            }
            return key;
        }

        private void writeObject(ObjectOutputStream out) throws IOException, ClassNotFoundException {
            getDelegateKey();
            out.defaultWriteObject();
        }

    }


}