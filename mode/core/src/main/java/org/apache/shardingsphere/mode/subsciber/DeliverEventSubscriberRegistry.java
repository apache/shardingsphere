package org.apache.shardingsphere.mode.subsciber;

import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.infra.util.eventbus.EventSubscriber;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.Arrays;
import java.util.Collection;

/**
 * Deliver event subscriber registry.
 */
public abstract class DeliverEventSubscriberRegistry implements EventSubscriberRegistry {
    
    private final EventBusContext eventBusContext;
    
    private final Collection<EventSubscriber> subscribers;
    
    protected DeliverEventSubscriberRegistry(final ContextManager contextManager, final EventSubscriber... subscribers) {
        eventBusContext = contextManager.getComputeNodeInstanceContext().getEventBusContext();
        this.subscribers = Arrays.asList(subscribers);
    }
    
    /**
     * Register subscribers.
     */
    public void register() {
        subscribers.forEach(eventBusContext::register);
    }
}
