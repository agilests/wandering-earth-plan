package org.wep.plugins;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.pf4j.PluginWrapper;

import java.nio.file.Path;

public class PluginInfo {
    @JsonIgnore
    private Path path;
    private String pluginId;
    private String pluginDescription;
    private String version;
    private String provider;
    private String license;
    private ClassLoader classLoader;
    private String state;

    public PluginInfo(PluginWrapper pluginWrapper) {
        this.path = pluginWrapper.getPluginPath();
        this.pluginId = pluginWrapper.getPluginId();
        this.pluginDescription = pluginWrapper.getDescriptor().getPluginDescription();
        this.version = pluginWrapper.getDescriptor().getVersion();
        this.provider = pluginWrapper.getDescriptor().getProvider();
        this.license = pluginWrapper.getDescriptor().getLicense();
        this.classLoader = pluginWrapper.getPluginClassLoader();
        this.state = pluginWrapper.getPluginState().name();
    }

    public String getPluginId() {
        return pluginId;
    }

    public String getPluginDescription() {
        return pluginDescription;
    }

    public String getVersion() {
        return version;
    }

    public String getProvider() {
        return provider;
    }

    public String getLicense() {
        return license;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public Path getPath() {
        return path;
    }

    public String getState() {
        return state;
    }

    @JsonIgnore
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        return classLoader.loadClass(className);
    }
}
