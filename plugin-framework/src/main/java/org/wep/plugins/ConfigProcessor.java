package org.wep.plugins;

import org.wep.utils.BeanUtils;
import org.wep.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.core.env.Environment;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigProcessor implements BeanProcessor<ConfigBeanDefinition> {
    private static final Logger logger = LoggerFactory.getLogger(ConfigProcessor.class);

    private final String regex = "\\$\\{(.*?)\\}";
    private final Pattern pattern = Pattern.compile(regex);
    private final PluginApplication pluginApplication;

    public ConfigProcessor(PluginApplication pluginApplication) {
        this.pluginApplication = pluginApplication;
    }

    @Override
    public Object resolveBean(ConfigBeanDefinition definition, Object bean) {
        return pluginApplication.getConfig(definition.getConfigFileName())
                .map(f -> config(bean, f))
                .orElseGet(() -> {
                    logger.warn("{} not found, definition on {}", definition.getConfigFileName(), definition.rawBeanDefinition().getBeanClassName());
                    return bean;
                });
    }


    protected Object config(Object bean, File configFile) {
        Objects.requireNonNull(bean);
        Objects.requireNonNull(configFile);
        String absolutePath = configFile.getAbsolutePath();
        if (!configFile.exists()) {
            throw new BeanInitializationException(String.format("config file [%s] not exists", absolutePath));
        }
        Map<String, Object> properties = resolveEnv(configFile);
        return BeanUtils.toBean(BeanUtils.toJson(properties), bean.getClass());
    }

    public Map<String, Object> resolveEnv(File configFile) {
        Map<String, Object> map = null;

        String absolutePath = configFile.getAbsolutePath();
        String suffix = absolutePath.substring(absolutePath.lastIndexOf(".") + 1);
        if (StringUtils.isEmpty(suffix)) {
            throw new BeanInitializationException(String.format("unknown config file [%s] type", absolutePath));
        }
        switch (suffix) {
            case "yml":
            case "YML":
            case "yaml":
            case "YAML":
                map = BeanUtils.toYamlMap(configFile, String.class, Object.class);
                break;
            case "json":
                map = BeanUtils.toMap(configFile, String.class, Object.class);
                break;
            case ".properties":
                throw new BeanInitializationException("properties todo");
            default:
                throw new BeanInitializationException(String.format("unknown config file [%s] type: %s", absolutePath, suffix));
        }

        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (e.getValue() instanceof String) {
                map.put(e.getKey(), resolveProperty(e.getValue().toString()));
            } else if (e.getValue() instanceof Map) {
                map.put(e.getKey(), resolveEnv((Map<String, Object>) e.getValue()));
            }
        }
        return map;
    }


    public String resolveProperty(final String property) {
        if (org.wep.utils.StringUtils.isEmpty(property)) {
            return property;
        }
        String replace = property;
        Matcher matcher = pattern.matcher(property);
        int count = 0;
        while (matcher.find(count)) {
            String group = matcher.group(1);
            replace = replacePlaceholder(replace, group);
            count = matcher.end();
        }
        return replace;
    }

    protected Environment env() {
        return pluginApplication.getEnv();
    }

    protected String replacePlaceholder(final String property, final String placeHolder) {
        String real = env().getProperty(placeHolder);
        if (StringUtils.isNotEmpty(real)) {
            return property.replace(String.format("${%s}", placeHolder), real);
        }
        return placeHolder;
    }

    private Map<String, Object> resolveEnv(Map<String, Object> map) {
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (e.getValue() instanceof String) {
                map.put(e.getKey(), resolveProperty(e.getValue().toString()));
            } else if (e.getValue() instanceof Map) {
                map.put(e.getKey(), resolveEnv((Map<String, Object>) e.getValue()));
            }
        }
        return map;
    }

}
