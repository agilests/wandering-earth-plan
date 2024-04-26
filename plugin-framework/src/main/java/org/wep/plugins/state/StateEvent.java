package org.wep.plugins.state;

import org.wep.plugins.PluginInfo;

import java.util.Collection;

public class StateEvent {
    private PluginStateChangeListener.State state;
    private PluginInfo plugin;
    private Collection<Object> beans;

    private StateEvent() {
    }

    public static StateEvent start(PluginInfo plugin) {
        StateEvent event = new StateEvent();
        event.state = PluginStateChangeListener.State.START;
        event.plugin = plugin;
        return event;
    }

    public static StateEvent stop(PluginInfo plugin, Collection<Object> beans) {
        StateEvent event = new StateEvent();
        event.state = PluginStateChangeListener.State.STOP;
        event.plugin = plugin;
        event.beans = beans;
        return event;
    }

    public static StateEvent install(PluginInfo plugin) {
        StateEvent event = new StateEvent();
        event.state = PluginStateChangeListener.State.INSTALL;
        event.plugin = plugin;
        return event;
    }

    public static StateEvent uninstall(PluginInfo plugin) {
        StateEvent event = new StateEvent();
        event.state = PluginStateChangeListener.State.UNINSTALL;
        event.plugin = plugin;
        return event;
    }

    public PluginStateChangeListener.State getState() {
        return state;
    }

    public PluginInfo getPlugin() {
        return plugin;
    }

    public Collection<Object> getBeans() {
        return beans;
    }
}
