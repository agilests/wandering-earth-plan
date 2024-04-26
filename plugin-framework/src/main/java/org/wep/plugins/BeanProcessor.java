package org.wep.plugins;

public interface BeanProcessor<T extends PluginBeanDefinition> {

    Object resolveBean(T definition, Object bean);
}
