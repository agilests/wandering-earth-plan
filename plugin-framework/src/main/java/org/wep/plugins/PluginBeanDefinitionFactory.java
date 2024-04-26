package org.wep.plugins;

import org.pf4j.Extension;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

//todo 重构
public class PluginBeanDefinitionFactory {
    public PluginBeanDefinition build(PluginWrapper plugin, Class<?> clz) {
        Component annotation = clz.getAnnotation(Component.class);
        if (annotation != null) {
            return new ComponentBeanDefinition(clz, plugin);
        }
        RestController controller = clz.getAnnotation(RestController.class);
        if (controller != null) {
            return new ControllerBeanDefinition(clz, plugin);
        }
        Extension extension = clz.getAnnotation(Extension.class);
        if (extension != null) {
            return new ExtensionBeanDefinition(clz, plugin);
        }
        Config config = clz.getAnnotation(Config.class);
        if (config != null) {
            return new ConfigBeanDefinition(clz, plugin);
        }
        throw new BeanCreationException("unknown bean: " + clz);
    }
}
