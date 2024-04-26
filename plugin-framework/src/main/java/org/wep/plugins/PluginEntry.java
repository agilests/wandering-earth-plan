package org.wep.plugins;

public interface PluginEntry<T> {
    PluginInfo getPluginInfo();

    T getEntry() throws PluginException;
}
