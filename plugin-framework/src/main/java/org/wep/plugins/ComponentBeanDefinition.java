package org.wep.plugins;


import org.pf4j.PluginWrapper;

public class ComponentBeanDefinition extends AbstractPluginBeanDefinition implements PluginBeanDefinition {


    public ComponentBeanDefinition(Class<?> beanClass, PluginWrapper plugin) {
        super(beanClass, plugin);
    }
}
