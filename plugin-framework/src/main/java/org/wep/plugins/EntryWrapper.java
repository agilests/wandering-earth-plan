package org.wep.plugins;

import java.util.jar.JarEntry;

public interface EntryWrapper<T extends PluginEntry<?>> {

    default boolean filter(PluginInfo info, JarEntry entry) throws PluginException {
        return true;
    }

    T wrap(PluginInfo info, JarEntry entry) throws PluginException;

    class ClassEntryWrapper implements EntryWrapper<ClassEntry> {

        @Override
        public boolean filter(PluginInfo info, JarEntry entry) throws PluginException {
            String name = entry.getName();
            return name.endsWith(ClassEntry.CLASS);
        }

        @Override
        public ClassEntry wrap(PluginInfo info, JarEntry entry) {
            return new ClassEntry(info, entry);
        }
    }
}
