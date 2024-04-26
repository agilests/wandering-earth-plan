package org.wep.plugins;

import org.pf4j.PluginWrapper;

public class ExtensionBeanDefinition extends AbstractPluginBeanDefinition implements PluginBeanDefinition {
    public ExtensionBeanDefinition(Class<?> beanClass, PluginWrapper plugin) {
        super(beanClass, plugin);
    }

}
