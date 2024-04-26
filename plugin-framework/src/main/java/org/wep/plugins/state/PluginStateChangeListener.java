package org.wep.plugins.state;

public interface PluginStateChangeListener {
    void change(StateEvent event);


    enum State {
        START,
        STOP,
        INSTALL,
        UNINSTALL
    }
}
