package org.quuux.headspace.events;

import com.squareup.otto.Bus;

public class EventBus extends Bus {
    private static EventBus instance;

    public static EventBus getInstance() {
        if (instance == null)
            instance = new EventBus();
        return instance;
    }
}
