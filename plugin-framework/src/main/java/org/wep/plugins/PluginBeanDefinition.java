package org.wep.plugins;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

public interface PluginBeanDefinition {
    Class<?> cls();

    String pluginId();

    BeanDefinition rawBeanDefinition();

    String beanName(BeanDefinitionRegistry registry);
}
