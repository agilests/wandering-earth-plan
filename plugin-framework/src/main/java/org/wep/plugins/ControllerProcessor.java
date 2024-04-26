package org.wep.plugins;

import org.wep.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.Map;

public class ControllerProcessor implements BeanProcessor<ControllerBeanDefinition> {
    private static final Logger logger = LoggerFactory.getLogger(ControllerProcessor.class);
    private final PluginApplication pluginApplication;
    private final PluginProperties properties;
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    public ControllerProcessor(PluginApplication pluginApplication, PluginProperties properties) {
        this.pluginApplication = pluginApplication;
        this.properties = properties;
    }

    protected RequestMappingHandlerMapping initMappingHandler() {
        if (this.requestMappingHandlerMapping != null) {
            return requestMappingHandlerMapping;
        }
        this.requestMappingHandlerMapping = pluginApplication.getBean("requestMappingHandlerMapping");
        return this.requestMappingHandlerMapping;
    }


    @Override
    public Object resolveBean(ControllerBeanDefinition definition, Object bean) {
        initMappingHandler();
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = this.requestMappingHandlerMapping.getHandlerMethods();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            refreshController(bean, definition, entry);
        }
        return bean;
    }

    private void refreshController(Object bean, ControllerBeanDefinition definition, Map.Entry<RequestMappingInfo, HandlerMethod> entry) {
        String prefix = refreshMappingPath(definition);
        if (definition.match(entry)) {
            Method method = entry.getValue().getMethod();
            unregisterController(entry.getKey());
            definition.resolveRequestMappingInfo(entry, prefix)
                    .ifPresent(mapping -> registerController(mapping, bean, method));
        }
    }

    private void registerController(RequestMappingInfo mapping, Object bean, Method method) {
        logger.info("register controller: {}", mapping);
        this.requestMappingHandlerMapping.registerMapping(mapping, bean, method);
    }

    private void unregisterController(RequestMappingInfo mapping) {
        logger.info("unregister controller: {}", mapping);
        this.requestMappingHandlerMapping.unregisterMapping(mapping);
    }

    public void registerController(ControllerBeanDefinition definition, Object bean) {
        initMappingHandler();
        String prefix = refreshMappingPath(definition);
        definition.getMappingMethods().stream()
                .forEach(m -> {
                    RequestMappingInfo mapping = definition.resolveRequestMappingInfo(m, prefix);
                    logger.info("register controller: {}", mapping);
                    this.requestMappingHandlerMapping.registerMapping(mapping, bean, m);
                });
    }

    public void unregisterController(ControllerBeanDefinition definition) {
        initMappingHandler();
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = this.requestMappingHandlerMapping.getHandlerMethods();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            if (definition.match(entry)) {
                unregisterController(entry.getKey());
            }
        }
    }


    public String refreshMappingPath(PluginBeanDefinition definition) {
        String contextPath = properties.getPluginRestPathPrefix();
        String pluginId = definition.pluginId();
        PathPrefix annotation = definition.cls().getAnnotation(PathPrefix.class);
        if (annotation != null) {
            pluginId = annotation.value();
            if (StringUtils.isEmpty(pluginId)) {
                throw new IllegalArgumentException("empty rest PathPrefix: " + definition.cls().getName());
            }
        }
        if (properties.isEnablePluginIdRestPathPrefix()) {
            if (contextPath != null && !"".equals(contextPath)) {
                contextPath = joiningPath(contextPath, pluginId);
            } else {
                contextPath = pluginId;
            }
        } else {
            if (contextPath == null || "".equals(contextPath)) {
                // 不启用插件id作为路径前缀, 并且路径前缀为空, 则直接返回。
                return "";
            }
        }
        return contextPath;
    }

    private String joiningPath(String path1, String path2) {
        if (path1 != null && path2 != null) {
            if (path1.endsWith("/") && path2.startsWith("/")) {
                return path1 + path2.substring(1);
            } else if (!path1.endsWith("/") && !path2.startsWith("/")) {
                return path1 + "/" + path2;
            } else {
                return path1 + path2;
            }
        } else if (path1 != null) {
            return path1;
        } else if (path2 != null) {
            return path2;
        } else {
            return "";
        }
    }

}
