package org.wep.plugins;

import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

public abstract class AbstractPluginBeanDefinition implements PluginBeanDefinition {
    private final Class<?> beanClass;
    private final PluginWrapper plugin;

    protected AbstractPluginBeanDefinition(Class<?> beanClass, PluginWrapper plugin) {
        this.beanClass = beanClass;
        this.plugin = plugin;
    }

    @Override
    public Class<?> cls() {
        return beanClass;
    }

    @Override
    public String pluginId() {
        return plugin.getPluginId();
    }

    @Override
    public BeanDefinition rawBeanDefinition() {
        if (beanClass.getAnnotation(Component.class) != null
                || beanClass.getAnnotation(Service.class) != null) {
            return new AnnotatedGenericBeanDefinition(beanClass);
        }
        return BeanDefinitionBuilder.genericBeanDefinition(beanClass).getRawBeanDefinition();
    }

    @Override
    public String beanName(BeanDefinitionRegistry registry) {
        String componentName = new AnnotationBeanNameGenerator().generateBeanName(rawBeanDefinition(), registry);
        return String.format("%s@%s", pluginId(), componentName);
    }
}
