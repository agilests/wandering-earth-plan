package org.wep.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.sf.cglib.beans.BeanCopier;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.*;

public class BeanUtils {

    public static final String ERROR_IN_TO_BEAN = "error in toBean";
    public static final String ERROR_IN_TO_MAP = "error in toMap";
    private static final String ERROR_IN_TO_COLLECTION = "error in toCollection";
    private static final String ERROR_IN_TO_JSON = "error in toJson";

    private static final Objenesis objenesis = new ObjenesisStd();

    private BeanUtils() {
    }

    static Logger logger = LoggerFactory.getLogger(BeanUtils.class);
    static ObjectMapper objectMapper = new ObjectMapper();
    static ObjectMapper yml = new ObjectMapper(new YAMLFactory());

    static {
        objectMapper.registerModule(new JavaTimeModule());
        yml.registerModule(new JavaTimeModule());
    }

    public static String toJson(Object bean) {
        if (bean == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(bean);
        } catch (JsonProcessingException e) {
            logger.error(ERROR_IN_TO_JSON, e);
        }
        return null;
    }

    public static String toJson2(Object bean) throws JsonProcessingException {
        return objectMapper.writeValueAsString(bean);
    }

    public static String prettyJson(Object bean) {
        if (bean == null) {
            return null;
        }
        try {
            return objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(bean);
        } catch (JsonProcessingException e) {
            logger.error(ERROR_IN_TO_JSON, e);
        }
        return null;
    }

    private static <T> Collection<T> toCollection(String json, Class<T> clz, Class<? extends Collection> collectionType) {
        CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(collectionType, clz);
        try {
            return objectMapper.readValue(json, listType);
        } catch (JsonProcessingException e) {
            logger.error(ERROR_IN_TO_COLLECTION, e);
        }
        return Collections.emptyList();
    }

    private static <T> Collection<T> toCollection(String json, JavaType javaType, Class<? extends Collection> collectionType) {
        CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(collectionType, javaType);
        try {
            return objectMapper.readValue(json, listType);
        } catch (JsonProcessingException e) {
            logger.error(ERROR_IN_TO_COLLECTION, e);
        }
        return Collections.emptyList();
    }

    public static <T> Collection<T> toCollection(String json, Class<T> clz) {
        return toCollection(json, clz, ArrayList.class);
    }

    public static <T> Collection<T> toCollection(String json, JavaType javaType) {
        return toCollection(json, javaType, ArrayList.class);
    }

    public static <T> Collection<T> toList(String json, Class<T> clz) {
        return toCollection(json, clz, ArrayList.class);
    }

    public static <T> Collection<T> toList(String json, JavaType javaType) {
        return toCollection(json, javaType, ArrayList.class);
    }

    public static <T> Collection<T> toSet(String json, Class<T> clz) {
        return toCollection(json, clz, HashSet.class);
    }

    public static <T> Collection<T> toSet(String json, JavaType javaType) {
        return toCollection(json, javaType, HashSet.class);
    }

    public static Map<String, Object> toMap(String json) {
        MapType mapType = objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class);
        try {
            return objectMapper.readValue(json, mapType);
        } catch (JsonProcessingException e) {
            logger.error(ERROR_IN_TO_MAP, e);
        }
        return Collections.emptyMap();
    }

    public static <K, V> Map<K, V> toMap(String json, Class<K> key, Class<V> value) {
        MapType mapType = objectMapper.getTypeFactory().constructMapType(HashMap.class, key, value);
        try {
            return objectMapper.readValue(json, mapType);
        } catch (JsonProcessingException e) {
            logger.error(ERROR_IN_TO_MAP, e);
        }
        return Collections.emptyMap();
    }

    public static Map<String, Object> toYamlMap(String yaml) {
        MapType mapType = objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class);
        try {
            return yml.readValue(yaml, mapType);
        } catch (JsonProcessingException e) {
            logger.error(ERROR_IN_TO_MAP, e);
        }
        return Collections.emptyMap();
    }

    public static <K, V> Map<K, V> toYamlMap(String yaml, Class<K> key, Class<V> value) {
        MapType mapType = objectMapper.getTypeFactory().constructMapType(HashMap.class, key, value);
        try {
            return yml.readValue(yaml, mapType);
        } catch (JsonProcessingException e) {
            logger.error(ERROR_IN_TO_MAP, e);
        }
        return Collections.emptyMap();
    }

    public static Map<String, Object> toMap(File file) {
        MapType mapType = objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class);
        try {
            return objectMapper.readValue(file, mapType);
        } catch (IOException e) {
            logger.error(ERROR_IN_TO_MAP, e);
        }
        return Collections.emptyMap();
    }

    public static <K, V> Map<K, V> toMap(File file, Class<K> key, Class<V> value) {
        MapType mapType = objectMapper.getTypeFactory().constructMapType(HashMap.class, key, value);
        try {
            return objectMapper.readValue(file, mapType);
        } catch (IOException e) {
            logger.error(ERROR_IN_TO_MAP, e);
        }
        return Collections.emptyMap();
    }

    public static <K, V> Map<K, V> toMap(String json, JavaType key, JavaType value) {
        MapType mapType = objectMapper.getTypeFactory().constructMapType(HashMap.class, key, value);
        try {
            return objectMapper.readValue(json, mapType);
        } catch (IOException e) {
            logger.error(ERROR_IN_TO_BEAN, e);
        }
        return Collections.emptyMap();
    }

    public static Map<String, Object> toYamlMap(File yaml) {
        MapType mapType = objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class);
        try {
            return yml.readValue(yaml, mapType);
        } catch (IOException e) {
            logger.error(ERROR_IN_TO_MAP, e);
        }
        return Collections.emptyMap();
    }

    public static <K, V> Map<K, V> toYamlMap(File yaml, Class<K> key, Class<V> value) {
        MapType mapType = objectMapper.getTypeFactory().constructMapType(HashMap.class, key, value);
        try {
            return yml.readValue(yaml, mapType);
        } catch (IOException e) {
            logger.error(ERROR_IN_TO_MAP, e);
        }
        return Collections.emptyMap();
    }

    public static Map<String, Object> toMap(URL url) {
        MapType mapType = objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class);
        try {
            return objectMapper.readValue(url, mapType);
        } catch (IOException e) {
            logger.error(ERROR_IN_TO_MAP, e);
        }
        return Collections.emptyMap();
    }

    public static <K, V> Map<K, V> toMap(URL url, Class<K> key, Class<V> value) {
        MapType mapType = objectMapper.getTypeFactory().constructMapType(HashMap.class, key, value);
        try {
            return objectMapper.readValue(url, mapType);
        } catch (IOException e) {
            logger.error(ERROR_IN_TO_MAP, e);
        }
        return Collections.emptyMap();
    }

    public static <T> T toBean2(URL url, Class<T> clz) throws IOException {
        return objectMapper.readValue(url, clz);
    }

    public static <T> T toBean(URL url, Class<T> clz) {
        try {
            return objectMapper.readValue(url, clz);
        } catch (IOException e) {
            logger.error(ERROR_IN_TO_BEAN, e);
        }
        return null;
    }

    public static <T> T toBean2(String json, JavaType type) throws JsonProcessingException {
        return objectMapper.readValue(json, type);
    }

    public static <T> T toBean(String json, JavaType type) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            logger.error(ERROR_IN_TO_BEAN, e);
        }
        return null;
    }

    public static <T> T toBean2(String json, Class<T> clz) throws JsonProcessingException {
        return objectMapper.readValue(json, clz);
    }

    public static <T> T toBean2(InputStream in, Class<T> clz) throws IOException {
        return objectMapper.readValue(in, clz);
    }

    public static <T> T toBean(String json, Class<T> clz) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clz);
        } catch (JsonProcessingException e) {
            logger.error(ERROR_IN_TO_BEAN, e);
        }
        return null;
    }

    public static <T> T toBean2(File json, Class<T> clz) throws IOException {
        return objectMapper.readValue(json, clz);
    }

    public static <T> T toBean(File json, Class<T> clz) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clz);
        } catch (IOException e) {
            logger.error(ERROR_IN_TO_BEAN, e);
        }
        return null;
    }

    public static <T> T toBean2(String json, TypeReference<T> type) throws JsonProcessingException {
        return objectMapper.readValue(json, type);
    }

    public static <T> T toBean(String json, TypeReference<T> type) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            logger.error(ERROR_IN_TO_BEAN, e);
        }
        return null;
    }

    public static <T> T yml(URL url, Class<T> clz) throws IOException {
        return yml.readValue(url, clz);
    }

    public static <T> T yml(File ymlFile, Class<T> clz) throws IOException {
        return yml.readValue(ymlFile, clz);
    }

    public static <T> T yml(InputStream in, Class<T> clz) throws IOException {
        return yml.readValue(in, clz);
    }

    public static <T> T yml(String ymlContent, Class<T> clz) throws IOException {
        return yml.readValue(ymlContent, clz);
    }

    public static JavaType getJavaType(Class<?> clz) {
        return objectMapper.getTypeFactory().constructType(clz);
    }

    public static JavaType getJavaType(Type type) {
        return objectMapper.getTypeFactory().constructType(type);
    }

    public static JavaType getCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {
        return objectMapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }

    /**
     * 使用asm拷贝对象属性
     *
     * @param source
     * @param target
     * @param <T>
     * @return
     */
    public static <T> T copy(T source, T target) {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(target, "target must not be null");
        BeanCopier copier = BeanCopier.create(source.getClass(), target.getClass(), false);
        copier.copy(source, target, null);
        return target;
    }

    public static <T> T instance(Class<T> clz) {
        return objenesis.newInstance(clz);
    }
}
