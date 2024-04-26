package org.wep.plugins;

import org.wep.plugins.state.PluginStateChangeListener;
import org.wep.plugins.state.StateEvent;
import org.wep.utils.AnnotationUtils;
import org.wep.utils.CollectionUtils;
import org.wep.utils.StringUtils;
import org.pf4j.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class PluginApplication implements PluginStateListener {
    Logger logger = LoggerFactory.getLogger("PluginApplication");
    private final ApplicationContext applicationContext;
    private final DefaultListableBeanFactory beanFactory;
    private final PluginProperties properties;
    private final PluginBeanDefinitionFactory definitionFactory = new PluginBeanDefinitionFactory();
    private final ConcurrentHashMap<String, List<PluginBeanDefinition>> beanDefinitions;
    private final ConcurrentHashMap<String, File> configs;
    private final Collection<PluginStateChangeListener> stateChangeListeners;
    private final ControllerProcessor controllerProcessor;
    private PluginManager pluginManager;

    public PluginApplication(ApplicationContext applicationContext, PluginProperties properties) {
        this.applicationContext = applicationContext;
        this.properties = properties;
        this.beanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        this.beanDefinitions = new ConcurrentHashMap<>();
        this.configs = new ConcurrentHashMap<>();
        this.stateChangeListeners = new ArrayList<>();
        this.controllerProcessor = new ControllerProcessor(this, properties);
    }

    public String getPluginPath() {
        return properties.getPluginPath();
    }

    public void addListener(PluginStateChangeListener listener) {
        stateChangeListeners.add(listener);
    }

    public void addListeners(Collection<PluginStateChangeListener> listeners) {
        stateChangeListeners.addAll(listeners);
    }

    public List<PluginInfo> getPluginDescriptors() {
        return pluginManager.getPlugins().stream().map(PluginInfo::new).collect(Collectors.toList());
    }

    public Environment getEnv() {
        return applicationContext.getEnvironment();
    }

    public byte[] getPluginEntry(String pluginId, String name) {
        return pluginManager.getPlugins().stream()
                .filter(p -> p.getPluginId().equals(pluginId))
                .findAny()
                .map(p -> {
                    try {
                        Path pluginPath = p.getPluginPath();
                        JarFile jar = new JarFile(pluginPath.toFile());
                        Enumeration<JarEntry> en = jar.entries();
                        while (en.hasMoreElements()) {
                            JarEntry je = en.nextElement();
                            String n = je.getName();
                            if (n.equals(name)) {
                                return readEntry(jar, je);
                            }

                        }
                    } catch (Exception e) {
                        logger.error("", e);
                    }
                    return null;
                }).orElse(new byte[]{});
    }

    public <T> Collection<T> getBeans(Class<T> clz) {
        return applicationContext.getBeansOfType(clz).values();
    }

    public <T> Map<String, T> getBeanMap(Class<T> clz) {
        return applicationContext.getBeansOfType(clz);
    }

    public <T> T getBean(Class<T> clz) {
        return applicationContext.getBean(clz);
    }

    public <T> T getBean(String name) {
        return (T) applicationContext.getBean(name);
    }

    public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> aClass) {
        return applicationContext.getBeansWithAnnotation(aClass);
    }

    public Collection<PluginEntry<?>> entries(EntryWrapper<?> wrapper) throws IOException, PluginException {
        List<PluginEntry<?>> entries = new ArrayList<>();
        for (PluginWrapper plugin : pluginManager.getPlugins()) {
            for (JarEntry entry : entries(plugin, wrapper)) {
                entries.add(wrapper.wrap(new PluginInfo(plugin), entry));
            }
        }
        return entries;
    }

    /**
     * zip/jar/libs(zip/libs), zip/yml
     *
     * @throws PluginException
     */
    public PluginInfo install(File file) throws PluginException {
        String pluginId = pluginManager.loadPlugin(file.toPath());
        return new PluginInfo(pluginManager.getPlugin(pluginId));
    }

    private PluginWrapper get(String id) throws PluginException {
        PluginWrapper plugin = pluginManager.getPlugin(id);
        if (plugin == null) {
            throw new PluginException(String.format("unknown plugin: %s", id));
        }
        return plugin;
    }

    public void uninstall(String id) throws PluginException {
        PluginWrapper pluginWrapper = get(id);
        pluginManager.unloadPlugin(id);
        File file = pluginWrapper.getPluginPath().toFile();
        if (file.exists()) {
            file.delete();
        }
    }

    public void start(String id) throws PluginException {
        pluginManager.startPlugin(id);
    }

    public void stop(String id) throws PluginException {
        pluginManager.stopPlugin(id);
    }

    public void init() {
        resolveConfigs();
        pluginManager = new DefaultPluginManager(new File(properties.getPluginPath()).toPath());
        pluginManager.addPluginStateListener(this);
        pluginManager.loadPlugins();
        pluginManager.startPlugins();
    }


    private void resolveConfigs() {
        String pluginConfigFilePath = this.properties.getPluginConfigFilePath();
        if (StringUtils.isEmpty(pluginConfigFilePath)) {
            return;
        }
        File file = new File(pluginConfigFilePath);
        File[] files = file.listFiles();
        if (files == null) {
            return;
        }
        for (File listFile : files) {
            configs.putIfAbsent(listFile.getName(), listFile);
        }
    }

    public Optional<PluginBeanDefinition> lookupBeanDefinition(Object bean) {
        return beanDefinitions.values().stream()
                .flatMap(Collection::stream)
                .filter(b -> b.cls().isAssignableFrom(bean.getClass()))
                .findAny();
    }

    public Optional<File> getConfig(String configFileName) {
        return Optional.ofNullable(configs.get(configFileName));
    }

    private byte[] readEntry(JarFile jarFile, JarEntry entry) throws IOException {

        try (InputStream input = jarFile.getInputStream(entry)) {
            int bufferSize = 4096;
            byte[] buffer = new byte[bufferSize];
            int bytesNumRead = 0;
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                while ((bytesNumRead = input.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesNumRead);
                }
                byte[] entryBytes = baos.toByteArray();
                baos.flush();
                return entryBytes;
            }
        }
    }

    protected void onStop(PluginStateEvent event) {
        try {
            List<PluginBeanDefinition> pluginBeanDefinitions = beanDefinitions.remove(event.getPlugin().getPluginId());
            if (CollectionUtils.isEmpty(pluginBeanDefinitions)) {
                return;
            }
            pluginBeanDefinitions.stream().filter(ControllerBeanDefinition.class::isInstance)
                    .forEach(p -> controllerProcessor.unregisterController((ControllerBeanDefinition) p));
            Set<String> beanNames = pluginBeanDefinitions.stream().map(p -> p.beanName(beanFactory)).collect(Collectors.toSet());

            Collection<Object> beans = beanNames.stream()
                    .map(beanFactory::getBean)
                    .collect(Collectors.toSet());
            beanNames.forEach(name -> {
                logger.info("destroy: {}", name);
                try {
                    beanFactory.removeBeanDefinition(name);
                    beanFactory.destroySingleton(name);
                } catch (BeanCreationException e) {
                    logger.error("error destroy bean: {}", name, e);
                }
            });
            stateChangeListeners
                    .forEach(l -> l.change(StateEvent.stop(new PluginInfo(event.getPlugin()), beans)));
        } catch (Exception e) {
            logger.error("error in stop plugin: {}", event.getPlugin().getPluginId(), e);
        }
    }

    private final EntryWrapper.ClassEntryWrapper classWrapper = new EntryWrapper.ClassEntryWrapper();

    protected void onStart(PluginStateEvent event) {
        try {
            entries(event.getPlugin(), classWrapper).forEach(je -> resolveBean(event.getPlugin(), je));
            resolveExtensions(event.getPlugin());
            stateChangeListeners.forEach(l -> l.change(StateEvent.start(new PluginInfo(event.getPlugin()))));
        } catch (Exception e) {
            logger.error("", e);
        }
    }


    private Collection<JarEntry> entries(PluginWrapper plugin, EntryWrapper<?> filter) throws IOException, PluginException {
        Set<JarEntry> entries = new HashSet<>();
        Path pluginPath = plugin.getPluginPath();
        try (JarFile jar = new JarFile(pluginPath.toFile())) {
            Enumeration<JarEntry> en = jar.entries();
            while (en.hasMoreElements()) {
                JarEntry je = en.nextElement();
                if (filter.filter(new PluginInfo(plugin), je)) {
                    entries.add(je);
                }
            }
        }
        return entries;
    }


    private void registerBean(PluginWrapper plugin, Class<?> clz) {
        PluginBeanDefinition build = definitionFactory.build(plugin, clz);
        String beanName = build.beanName(beanFactory);
        beanFactory.registerBeanDefinition(beanName, build.rawBeanDefinition());
        List<PluginBeanDefinition> list = beanDefinitions.getOrDefault(plugin.getPluginId(), new ArrayList<>());
        list.add(build);
        beanDefinitions.putIfAbsent(plugin.getPluginId(), list);
    }

    private void resolveExtensions(PluginWrapper plugin) {
        List extensions = pluginManager.getExtensions(plugin.getPluginId());
        for (Object extension : extensions) {
            registerBean(plugin, extension.getClass());
        }
    }

    private void resolveBean(PluginWrapper plugin, JarEntry je) {
        try {
            String className = je.getName().replace(".class", "").replace("/", ".");
            Class<?> aClass = plugin.getPluginClassLoader().loadClass(className);
            if (support(aClass)) {
                registerBean(plugin, aClass);
            }
        } catch (ClassNotFoundException e) {
            logger.error("error in resolveBeans", e);
        }
    }

    private boolean support(Class<?> aClass) {
        if (aClass.isInterface()) {
            return false;
        }
        //是否是抽象类
        if (Modifier.isAbstract(aClass.getModifiers())) {
            return false;
        }
        return AnnotationUtils.anyAnnotation(aClass,
                Component.class, Repository.class, Service.class,
                RestController.class, Config.class);
    }


    @Override
    public void pluginStateChanged(PluginStateEvent event) {

        logger.info("{} {}", event.getPlugin().getPluginId(), event.getPluginState().name());
        switch (event.getPluginState()) {
            case STARTED:
                onStart(event);
                break;
            case STOPPED:
                onStop(event);
                break;
            default:
                //don't care
        }
    }
}
