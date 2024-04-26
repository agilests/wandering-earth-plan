package org.wep.utils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CollectionUtils {
    private CollectionUtils() {
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static <T> Collection<T> from(Iterable<T> it) {
        Collection<T> collection = new ArrayList<>();
        for (T s : it) {
            collection.add(s);
        }
        return collection;
    }

    public static <T, R> Stream<R> map(Collection<T> collection, Function<? super T, ? extends R> mapper) {
        if (CollectionUtils.isEmpty(collection)) {
            return Stream.empty();
        }
        return collection.stream().map(mapper);
    }

    public static <T, R> Set<R> toSet(Collection<T> collection, Function<? super T, ? extends R> mapper) {
        if (CollectionUtils.isEmpty(collection)) {
            return Collections.emptySet();
        }
        return collection.stream().map(mapper).collect(Collectors.toSet());
    }


}
