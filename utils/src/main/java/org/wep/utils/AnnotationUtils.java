package org.wep.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class AnnotationUtils {
    private AnnotationUtils() {
    }

    public static Map<? extends Class<? extends Annotation>, List<Field>> annotatedFields(Class<?> beanClass,
                                                                                          Class<? extends Annotation>... annoClz) {
        return Stream.of(annoClz).map(c ->
                        new AbstractMap.SimpleEntry<Class<? extends Annotation>, List<Field>>(c,
                                ReflectionUtils.getFields(beanClass).stream()
                                        .filter(m -> m.getAnnotation(c) != null)
                                        .collect(Collectors.toList())))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    public static Map<? extends Class<? extends Annotation>, List<Method>> annotatedMethods(Class<?> beanClass,
                                                                                            Class<? extends Annotation>... annoClz) {
        return Stream.of(annoClz).map(c ->
                        new AbstractMap.SimpleEntry<Class<? extends Annotation>, List<Method>>(c,
                                Arrays.stream(beanClass.getDeclaredMethods())
                                        .filter(m -> m.getAnnotation(c) != null)
                                        .collect(Collectors.toList())))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    public static boolean hasAnnotation(Class<?> beanClass, Class<? extends Annotation> annoClz) {
        return beanClass.getAnnotation(annoClz) != null;
    }

    public static boolean anyAnnotation(Class<?> beanClass, Class<? extends Annotation>... annoClz) {
        return Stream.of(annoClz).anyMatch(c -> hasAnnotation(beanClass, c));
    }

    public static <T extends Annotation> Optional<T> getAnnotation(Field field, Predicate<Class<? extends Annotation>> predicate) {
        Annotation[] annotations = field.getAnnotations();
        if (ArrayUtils.isEmpty(annotations)) {
            return Optional.empty();
        }
        return (Optional<T>) Arrays.stream(annotations).filter(a -> predicate.test(a.annotationType())).findAny();
    }

}
