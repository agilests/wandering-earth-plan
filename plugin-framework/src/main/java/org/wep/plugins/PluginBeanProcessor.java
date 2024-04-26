package org.wep.plugins;

import org.wep.utils.ReflectionUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.Collection;
import java.util.Optional;

/**
 * controller bean和Config bean注册后处理
 * controller bean: refresh url
 * config bean: load config file
 */
public class PluginBeanProcessor implements BeanPostProcessor {
    private final PluginApplication pluginApplication;
    private final Collection<BeanProcessor> processors;

    public PluginBeanProcessor(PluginApplication pluginApplication, Collection<BeanProcessor> processors) {
        this.pluginApplication = pluginApplication;
        this.processors = processors;
    }

    protected Optional<BeanProcessor> findBeanProcessor(PluginBeanDefinition definition) {
        return processors.stream()
                .filter(l -> ReflectionUtils.isImplementationGeneric(definition.getClass(), l.getClass()))
                .findAny();
    }

    protected Object resolveBean(Object bean) {
        return pluginApplication.lookupBeanDefinition(bean)
                .map(definition ->
                        findBeanProcessor(definition)
                                .map(processor -> processor.resolveBean(definition, bean))
                                .orElse(bean)
                ).orElse(bean);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return resolveBean(bean);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return resolveBean(bean);
    }
}
