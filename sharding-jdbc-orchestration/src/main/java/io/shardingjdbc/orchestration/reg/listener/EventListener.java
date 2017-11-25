package io.shardingjdbc.orchestration.reg.listener;

/**
 * Coordinator registry event change listener.
 *
 * @author junxiong
 */
public interface EventListener {
    
    /**
     * Fire when event changed.
     * 
     * @param event data changed event
     */
    void onChange(DataChangedEvent event);
}
