package com.player.foreground.events;

public class ConnectivityChangedEvent {

    private boolean isNetworkEnabled;

    public ConnectivityChangedEvent(boolean isNetworkEnabled) {
        this.isNetworkEnabled = isNetworkEnabled;
    }

    public boolean isNetworkEnabled() {
        return isNetworkEnabled;
    }

}
