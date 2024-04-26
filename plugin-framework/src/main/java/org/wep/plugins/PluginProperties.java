package org.wep.plugins;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "plugin")
public class PluginProperties {

    /**
     * 插件的路径
     */
    private String pluginPath;
    /**
     * 插件文件的路径
     */
    private String pluginConfigFilePath;
    /**
     * context path
     */
    private String pluginRestPathPrefix;
    /**
     * 是否开启插件id作为二级path
     */
    private boolean enablePluginIdRestPathPrefix = true;

    public void setPluginPath(String pluginPath) {
        this.pluginPath = pluginPath;
    }

    public void setPluginConfigFilePath(String pluginConfigFilePath) {
        this.pluginConfigFilePath = pluginConfigFilePath;
    }

    public void setPluginRestPathPrefix(String pluginRestPathPrefix) {
        this.pluginRestPathPrefix = pluginRestPathPrefix;
    }

    public void setEnablePluginIdRestPathPrefix(boolean enablePluginIdRestPathPrefix) {
        this.enablePluginIdRestPathPrefix = enablePluginIdRestPathPrefix;
    }

    public String getPluginPath() {
        return pluginPath;
    }


    public String getPluginConfigFilePath() {
        return pluginConfigFilePath;
    }


    public String getPluginRestPathPrefix() {
        return pluginRestPathPrefix;
    }


    public boolean isEnablePluginIdRestPathPrefix() {
        return enablePluginIdRestPathPrefix;
    }

}
