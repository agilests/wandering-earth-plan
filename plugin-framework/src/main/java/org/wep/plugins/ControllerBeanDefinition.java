package org.wep.plugins;

import org.wep.utils.AnnotationUtils;
import org.wep.utils.ArrayUtils;
import org.wep.utils.StringUtils;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.util.pattern.PathPatternParser;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ControllerBeanDefinition extends AbstractPluginBeanDefinition implements PluginBeanDefinition {
    private static final Logger logger = LoggerFactory.getLogger(ControllerBeanDefinition.class);
    private final RequestMapping requestMapping;
    private final List<Method> mappingMethods;
    private RequestMappingInfo.BuilderConfiguration defaultBuilderConfiguration;

    public ControllerBeanDefinition(Class<?> beanClass, PluginWrapper plugin) {
        super(beanClass, plugin);
        RequestMapping requestMapping = beanClass.getAnnotation(RequestMapping.class);
        if (requestMapping == null) {
            throw new BeanCreationException("missing @RequestMapping on " + beanClass);
        }
        this.requestMapping = requestMapping;
        this.mappingMethods = AnnotationUtils.annotatedMethods(beanClass,
                        RequestMapping.class, GetMapping.class, PutMapping.class, PostMapping.class, DeleteMapping.class)
                .values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    public List<Method> getMappingMethods() {
        return mappingMethods;
    }

    public boolean match(Map.Entry<RequestMappingInfo, HandlerMethod> entry) {
        return mappingMethods.stream()
                .anyMatch(e -> e.equals(entry.getValue().getMethod()));
    }

    public Optional<RequestMappingInfo> resolveRequestMappingInfo(Map.Entry<RequestMappingInfo, HandlerMethod> entry, String prefix) {
        return mappingMethods.stream().filter(l -> l.equals(entry.getValue().getMethod()))
                .findAny()
                .map(ex -> resolveRequestMappingInfo(ex, prefix));
    }

    public RequestMappingInfo resolveRequestMappingInfo(Method method, String x) {
        Annotation[] annotations = method.getAnnotations();
        String[] path = null;
        String[] headers = null;
        String[] consumes = null;
        String[] params = null;
        String[] produces = null;
        RequestMethod[] methods = null;

        for (Annotation anno : annotations) {
            if (anno instanceof GetMapping) {
                GetMapping get = (GetMapping) anno;
                path = get.value();
                headers = get.headers();
                consumes = get.consumes();
                params = get.params();
                produces = get.produces();
                methods = new RequestMethod[]{RequestMethod.GET};
            }
            if (anno instanceof PostMapping) {
                PostMapping post = (PostMapping) anno;
                path = post.value();
                headers = post.headers();
                consumes = post.consumes();
                params = post.params();
                produces = post.produces();
                methods = new RequestMethod[]{RequestMethod.POST};
            }
            if (anno instanceof PutMapping) {
                PutMapping put = (PutMapping) anno;
                path = put.value();
                headers = put.headers();
                consumes = put.consumes();
                params = put.params();
                produces = put.produces();
                methods = new RequestMethod[]{RequestMethod.PUT};
            }
            if (anno instanceof DeleteMapping) {
                DeleteMapping delete = (DeleteMapping) anno;
                path = delete.value();
                headers = delete.headers();
                consumes = delete.consumes();
                params = delete.params();
                produces = delete.produces();
                methods = new RequestMethod[]{RequestMethod.DELETE};
            }
            if (anno instanceof RequestMapping) {
                RequestMapping request = (RequestMapping) anno;
                path = request.value();
                headers = request.headers();
                consumes = request.consumes();
                params = request.params();
                produces = request.produces();
                methods = ArrayUtils.isEmpty(request.method())
                        ? new RequestMethod[]{RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE}
                        : request.method();
            }
        }
        String[] finalPath = finalPath(path, x);
        logger.debug("method: {}.{} final path: {}",
                method.getDeclaringClass().getName(), method.getName(),
                Stream.of(finalPath).collect(Collectors.joining(",")));
        return RequestMappingInfo
                .paths(finalPath)
                .consumes(consumes)
                .headers(headers)
                .params(params)
                .produces(produces)
                .methods(methods)
                .options(defaultBuilderConfiguration())
                .build();
    }



    private RequestMappingInfo.BuilderConfiguration defaultBuilderConfiguration() {
        if (defaultBuilderConfiguration != null) {
            return defaultBuilderConfiguration;
        }
        defaultBuilderConfiguration = new RequestMappingInfo.BuilderConfiguration();
        defaultBuilderConfiguration.setPatternParser(new PathPatternParser());
        defaultBuilderConfiguration.setPathMatcher(new AntPathMatcher());
        return defaultBuilderConfiguration;
    }

    public String[] finalPath(String[] current, String x) {
        String[] onClass = notNull(ArrayUtils.isEmpty(requestMapping.value()) ? requestMapping.path() : requestMapping.value());
        String[] onMethod = notNull(current);
        List<String> ret = new ArrayList<>();
        for (String s : onClass) {
            for (String s1 : onMethod) {
                ret.add(joint(x, s, s1));
            }
        }
        return ret.toArray(new String[]{});
    }

    private String[] notNull(String[] origin) {
        if (ArrayUtils.isEmpty(origin)) {
            return new String[]{""};
        }
        return origin;
    }

    private String joint(String x, String s, String s1) {
        return Stream.of(x, s, s1).filter(StringUtils::isNotEmpty).map(ele -> StringUtils.removeStart(StringUtils.removeEnd(ele, "/"), "/")).collect(Collectors.joining("/"));
    }

}
