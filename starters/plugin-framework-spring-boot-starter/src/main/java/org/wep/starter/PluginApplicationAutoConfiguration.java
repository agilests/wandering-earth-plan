package org.wep.starter;

import org.wep.plugins.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(PluginProperties.class)
public class PluginApplicationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(PluginBeanProcessor.class)
    public PluginBeanProcessor processor(PluginApplication pluginApplication, PluginProperties configuration) {
        Set<BeanProcessor> processorSet = new HashSet<>();
        processorSet.add(new ControllerProcessor(pluginApplication, configuration));
        processorSet.add(new ConfigProcessor(pluginApplication));
        return new PluginBeanProcessor(pluginApplication, processorSet);
    }

    @Bean
    @ConditionalOnMissingBean(PluginApplication.class)
    public PluginApplication pluginApplication(ApplicationContext applicationContext, PluginProperties configuration) {
        PluginApplication pluginApplication = new PluginApplication(applicationContext, configuration);
        pluginApplication.init();
        return pluginApplication;
    }
}
