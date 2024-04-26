package org.wep.utils;

import sun.reflect.ConstantPool;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReflectionUtils {
    private ReflectionUtils() {
    }

    private static final Map<Class<?>, Method[]> declaredMethodsCache = new ConcurrentHashMap<>(256);
    private static final Method[] EMPTY_METHOD_ARRAY = new Method[0];

    public static List<Object> staticFieldValues(Class<?> cls) {
        return staticFields(cls)
                .stream()
                .map(field -> {
                    try {
                        field.setAccessible(true);
                        return field.get(null);
                    } catch (IllegalAccessException e) {
                        return null;
                    }
                })
                .collect(Collectors.toList());
    }

    public static List<Field> staticFields(Class<?> cls) {
        return getFields(cls)
                .stream()
                .filter(isStatic)
                .collect(Collectors.toList());
    }

    private static final Predicate<Field> isStatic = field -> Modifier.isStatic(field.getModifiers());

    public static List<Field> getFields(Class<?> cls) {
        Objects.requireNonNull(cls);
        List<Field> fields = new ArrayList<>(cls.getDeclaredFields().length);
        fields.addAll(Arrays.stream(cls.getDeclaredFields()).collect(Collectors.toList()));
        return getFields(cls.getSuperclass(), fields);
    }


    private static List<Field> getFields(Class<?> parent, List<Field> fields) {
        if (parent == null) {
            return fields;
        }
        fields.addAll(Arrays.stream(parent.getDeclaredFields()).collect(Collectors.toList()));
        return getFields(parent.getSuperclass(), fields);
    }


    public static boolean isString(Class<?> cls) {
        return cls == String.class;
    }

    public static boolean isString(Type type) {
        return Class.class.isAssignableFrom(type.getClass()) && isString((Class<?>) type);
    }

    public static boolean isNumber(Type type) {
        return Class.class.isAssignableFrom(type.getClass()) && isNumber((Class<?>) type);
    }

    public static boolean isNumber(Class<?> cls) {
        return int.class.isAssignableFrom(cls) || Integer.class.isAssignableFrom(cls)
                || double.class.isAssignableFrom(cls) || Double.class.isAssignableFrom(cls)
                || float.class.isAssignableFrom(cls) || Float.class.isAssignableFrom(cls)
                || Number.class.isAssignableFrom(cls);
    }

    public static boolean isDate(Type type) {
        return Class.class.isAssignableFrom(type.getClass()) && isDate((Class<?>) type);
    }

    public static boolean isDate(Class<?> cls) {
        return Date.class.isAssignableFrom(cls);
    }

    public static boolean isBool(Type type) {
        return Class.class.isAssignableFrom(type.getClass()) && isBool((Class<?>) type);
    }

    public static boolean isBool(Class<?> cls) {
        return boolean.class.isAssignableFrom(cls) || Boolean.class.isAssignableFrom(cls);
    }

    public static boolean isBasic(Class<?> cls) {
        return cls.isPrimitive() || cls == Boolean.class || cls == Character.class
                || Number.class.isAssignableFrom(cls) || Date.class.isAssignableFrom(cls);
    }

    public static boolean isBasic(Type type) {
        return Class.class.isAssignableFrom(type.getClass()) && isBasic((Class<?>) type);
    }

    public static boolean isStringOrBasic(Class<?> cls) {
        return isBasic(cls) || isString(cls);
    }

    public static boolean isStringOrBasic(Type type) {
        return Class.class.isAssignableFrom(type.getClass()) && (isBasic((Class<?>) type) || isString((Class<?>) type));
    }

    public static boolean isCollection(Class<?> cls) {
        return Collection.class.isAssignableFrom(cls);
    }

    public static boolean isCollection(Type type) {
        return Class.class.isAssignableFrom(type.getClass()) && Collection.class.isAssignableFrom((Class<?>) type);
    }

    public static boolean isMap(Class<?> cls) {
        return Map.class.isAssignableFrom(cls);
    }

    public static boolean isMap(Type type) {
        return Class.class.isAssignableFrom(type.getClass()) && Map.class.isAssignableFrom((Class<?>) type);
    }

    public static boolean isArray(Class<?> cls) {
        return cls.isArray();
    }

    public static boolean isArray(Type type) {
        return Class.class.isAssignableFrom(type.getClass()) && ((Class<?>) type).isArray();
    }

    public static Type getGeneric(Class<?> self) {
        Objects.requireNonNull(self);

        Type genericSuperclass = self.getGenericSuperclass();
        if (genericSuperclass != Object.class) {
            return genericSuper(genericSuperclass);
        }
        Type[] genericInterfaces = self.getGenericInterfaces();
        if (!ArrayUtils.isEmpty(genericInterfaces)) {
            return genericInterfaces(genericInterfaces);
        }
        return self.getComponentType();
    }


    private static Type genericSuper(Type genericSuperclass) {
        if (genericSuperclass instanceof ParameterizedType) {
            ParameterizedType genericSuper = (ParameterizedType) genericSuperclass;
            if (genericSuper.getActualTypeArguments() == null || genericSuper.getActualTypeArguments().length == 0) {
                return Object.class;
            }
            return genericSuper.getActualTypeArguments()[0];
        }
        if (genericSuperclass == Object.class) {
            return Object.class;
        }
        Type type = ((Class) genericSuperclass).getGenericSuperclass();
        if (type != Object.class) {
            return genericSuper(type);
        }
        return Object.class;
    }

    private static Type genericInterfaces(Type[] genericInterfaces) {
        return Stream.of(genericInterfaces)
                .filter(ParameterizedType.class::isInstance)
                .findAny()
                .map(ParameterizedType.class::cast)
                .filter(c -> !ArrayUtils.isEmpty(c.getActualTypeArguments()))
                .map(c -> c.getActualTypeArguments()[0])
                .orElse(Object.class);
    }


    /**
     * 找到 Lambda 表达式对应的 Class 的常量池
     * 依次遍历常量池中对方法的符号引用
     * 排除无关方法的符号引用
     * 从第一个满足条件的方法中取出参数类型
     *
     * @param self
     * @return
     */

    private static Boolean isLambdaGeneric(Class<?> genericClass, Class<?> self) {
        try {
            ConstantPool constantPool = (ConstantPool) invoke(self, "getConstantPool");
            for (int i = constantPool.getSize(); i >= 0; --i) {
                try {
                    Member methodAt = constantPool.getMethodAt(i);
                    if (methodAt instanceof Method
                            && methodAt.getDeclaringClass() != Object.class) {
                        Method method = (Method) methodAt;
                        return Stream.of(method.getParameters())
                                .anyMatch(p -> p.getType().isAssignableFrom(genericClass));
                    }
                } catch (Exception ignored) {
                    // ignored
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static Method getMethod(Class<?> objClass, String methodName) throws NoSuchMethodException {
        for (Method method : objClass.getDeclaredMethods()) {
            if (methodName.equals(method.getName())) {
                return method;
            }
        }
        throw new NoSuchMethodException();
    }

    private static Object invoke(Object obj, String methodName, Object... args) throws ReflectiveOperationException {
        Field overrideField = AccessibleObject.class.getDeclaredField("override");
        overrideField.setAccessible(true);
        Method targetMethod = getMethod(obj.getClass(), methodName);
        overrideField.set(targetMethod, true);
        return targetMethod.invoke(obj, args);
    }

    public static boolean isImplementationGeneric(Class<?> genericClass, Class<?> self) {
        Objects.requireNonNull(genericClass);
        Objects.requireNonNull(self);

        Objects.requireNonNull(self);
        if (self.getName().contains("Lambda") && self.isSynthetic()) {
            return isLambdaGeneric(genericClass, self);
        }


        Type genericSuperclass = self.getGenericSuperclass();
        if (genericSuperclass != Object.class) {
            return predicateSuper(genericSuperclass, genericClass);
        }

        Type[] genericInterfaces = self.getGenericInterfaces();
        if (!ArrayUtils.isEmpty(genericInterfaces)) {
            return predicateInterfaces(genericInterfaces, genericClass);
        }
        return false;
    }

    private static boolean predicateInterfaces(Type[] genericInterfaces, Class<?> genericClass) {
        return Stream.of(genericInterfaces)
                .filter(ParameterizedType.class::isInstance)
                .findAny()
                .map(ParameterizedType.class::cast)
                .filter(c -> !ArrayUtils.isEmpty(c.getActualTypeArguments()))
                .map(c -> c.getActualTypeArguments()[0])
                .map(c -> ((Class) c).isAssignableFrom(genericClass))
                .orElse(false);
    }

    private static boolean predicateSuper(Type genericSuperclass, Class<?> genericClass) {
        if (genericSuperclass instanceof ParameterizedType) {
            ParameterizedType genericSuper = (ParameterizedType) genericSuperclass;
            if (genericSuper.getActualTypeArguments() == null || genericSuper.getActualTypeArguments().length == 0) {
                return false;
            }
            Type actualTypeArgument = genericSuper.getActualTypeArguments()[0];
            return ((Class) actualTypeArgument).isAssignableFrom(genericClass);
        }
        if (genericSuperclass == Object.class) {
            return false;
        }
        Type type = ((Class) genericSuperclass).getGenericSuperclass();
        if (type != Object.class) {
            return predicateSuper(type, genericClass);
        }
        return false;
    }

    public static Field getField(Class<?> cls, String fieldName) throws NoSuchFieldException {
        if (cls == Object.class) {
            throw new NoSuchFieldException(fieldName);
        }
        try {
            return cls.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return getField(cls.getSuperclass(), fieldName);
        }
    }

    public static Object getter(String fieldName, Object bean) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        return getter(getField(bean.getClass(), fieldName), bean);
    }

    public static Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
        Class<?> searchType = clazz;
        while (searchType != null) {
            Method[] methods = (searchType.isInterface() ? searchType.getMethods() :
                    getDeclaredMethods(searchType, false));
            for (Method method : methods) {
                if (name.equals(method.getName()) && (paramTypes == null || hasSameParams(method, paramTypes))) {
                    return method;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }

    private static boolean hasSameParams(Method method, Class<?>[] paramTypes) {
        return (paramTypes.length == method.getParameterCount() &&
                Arrays.equals(paramTypes, method.getParameterTypes()));
    }

    private static Method[] getDeclaredMethods(Class<?> clazz, boolean defensive) {
        Method[] result = declaredMethodsCache.get(clazz);
        if (result == null) {
            try {
                Method[] declaredMethods = clazz.getDeclaredMethods();
                List<Method> defaultMethods = findConcreteMethodsOnInterfaces(clazz);
                if (defaultMethods != null) {
                    result = new Method[declaredMethods.length + defaultMethods.size()];
                    System.arraycopy(declaredMethods, 0, result, 0, declaredMethods.length);
                    int index = declaredMethods.length;
                    for (Method defaultMethod : defaultMethods) {
                        result[index] = defaultMethod;
                        index++;
                    }
                } else {
                    result = declaredMethods;
                }
                declaredMethodsCache.put(clazz, (result.length == 0 ? EMPTY_METHOD_ARRAY : result));
            } catch (Throwable ex) {
                throw new IllegalStateException("Failed to introspect Class [" + clazz.getName() +
                        "] from ClassLoader [" + clazz.getClassLoader() + "]", ex);
            }
        }
        return (result.length == 0 || !defensive) ? result : result.clone();
    }

    private static List<Method> findConcreteMethodsOnInterfaces(Class<?> clazz) {
        List<Method> result = null;
        for (Class<?> ifc : clazz.getInterfaces()) {
            for (Method ifcMethod : ifc.getMethods()) {
                if (!Modifier.isAbstract(ifcMethod.getModifiers())) {
                    if (result == null) {
                        result = new ArrayList<>();
                    }
                    result.add(ifcMethod);
                }
            }
        }
        return result;
    }

    private static final String GETTER_PREFIX = "get";
    private static final String BOOL_GETTER_PREFIX = "is";

    public static Object getter(Field field, Object bean) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Objects.requireNonNull(field);
        Objects.requireNonNull(bean);
        String prefix = GETTER_PREFIX;
        if (isBool(field.getType())) {
            prefix = BOOL_GETTER_PREFIX;
        }
        String mName = String.format("%s%s", prefix, StringUtils.capitalize(field.getName()));
        Method getter = ReflectionUtils.findMethod(bean.getClass(), mName);
        if (getter == null) {
            throw new NoSuchMethodException(String.format("method %s not found, class: %s", mName, bean.getClass().getName()));
        }
        return getter.invoke(bean, null);
    }

    public static Object setter(String fieldName, Object value, Object bean) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        return setter(bean.getClass().getDeclaredField(fieldName), value, bean);
    }

    public static Object setter(Field field, Object value, Object bean) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        Objects.requireNonNull(field);
        Objects.requireNonNull(bean);
        Method setter = bean.getClass().getDeclaredMethod(String.format("set%s", StringUtils.capitalize(field.getName())), field.getType());
        setter.invoke(bean, value);
        return bean;
    }
}
