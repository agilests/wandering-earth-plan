package org.wep.plugins;

import org.wep.utils.StringUtils;
import org.pf4j.PluginWrapper;

public class ConfigBeanDefinition extends AbstractPluginBeanDefinition implements PluginBeanDefinition {
    private final String configFileName;

    public ConfigBeanDefinition(Class<?> beanClass, PluginWrapper plugin) {
        super(beanClass, plugin);
        Config config = beanClass.getAnnotation(Config.class);
        this.configFileName = StringUtils.choose(config.value(), plugin.getPluginId() + ".yml");
    }

    public String getConfigFileName() {
        return configFileName;
    }
}
