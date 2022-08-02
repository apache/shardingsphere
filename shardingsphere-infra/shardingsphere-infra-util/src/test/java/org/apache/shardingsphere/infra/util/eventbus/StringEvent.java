package org.apache.shardingsphere.infra.util.eventbus;

import com.google.common.eventbus.Subscribe;
import java.util.ArrayList;
import java.util.List;

public class StringEvent {
    List<String> events = new ArrayList<>();

    @Subscribe
    public void lister(String ev) {
        events.add(ev);
    }

    public List<String> getEvents() {
        return events;
    }
}
