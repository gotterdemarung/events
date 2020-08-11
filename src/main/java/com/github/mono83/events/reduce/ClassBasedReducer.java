package com.github.mono83.events.reduce;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

/**
 * Groups events by class and reduces everything that supports it.
 */
public class ClassBasedReducer implements Function<Collection<Object>, Collection<Object>> {
    /**
     * Groups events by class and reduces everything that supports it.
     *
     * @param events Event to reduce.
     * @return Reduced collection.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static List<Object> reduce(final Collection<Object> events) {
        ArrayList<Object> response = new ArrayList<>(events.size());
        HashMap<Class<?>, ArrayList<Reducible<?>>> buffer = new HashMap<>();
        for (Object candidate : events) {
            if (candidate instanceof Reducible) {
                Reducible<?> red = (Reducible<?>) candidate;
                buffer.computeIfAbsent(red.getClass(), $ -> new ArrayList<>()).add(red);
            } else {
                response.add(candidate);
            }
        }
        for (ArrayList<Reducible<?>> list : buffer.values()) {
            response.add(Reducible.reduceCollection((List) list));
        }
        return response;
    }

    @Override
    public Collection<Object> apply(final Collection<Object> events) {
        return reduce(events);
    }
}
