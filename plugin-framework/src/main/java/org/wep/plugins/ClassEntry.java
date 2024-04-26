package org.wep.plugins;

import java.util.jar.JarEntry;

public class ClassEntry implements PluginEntry<Class<?>> {
    public static final String CLASS = ".class";
    private final PluginInfo pluginInfo;
    private final JarEntry entry;

    public ClassEntry(PluginInfo pluginInfo, JarEntry entry) {
        this.pluginInfo = pluginInfo;
        this.entry = entry;
    }

    @Override
    public PluginInfo getPluginInfo() {
        return pluginInfo;
    }

    @Override
    public Class<?> getEntry() throws PluginException {
        if (!entry.getName().endsWith(CLASS)) {
            throw new PluginException("not a class entry");
        }
        try {
            String className = entry.getName().replace(CLASS, "").replace("/", ".");
            return pluginInfo.getClassLoader().loadClass(className);
        } catch (Exception e) {
            throw new PluginException(e);
        }
    }
}
