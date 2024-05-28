package de.werwolf2303.tldwr.utils;

import java.util.ArrayList;

public class AutoThrow {
    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> classReference, Object... args) {
        try {
            ArrayList<Class<?>> paramsTypes = new ArrayList<>();
            for(Object o : args) {
                paramsTypes.add(o.getClass());
            }
            Object instance = classReference.getConstructor(paramsTypes.toArray(new Class<?>[0])).newInstance(args);
            return (T) instance;
        }catch (Exception e) {
            throw new RuntimeException();
        }
    }
}
