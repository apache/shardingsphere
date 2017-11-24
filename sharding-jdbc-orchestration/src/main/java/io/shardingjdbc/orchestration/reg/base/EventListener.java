package io.shardingjdbc.orchestration.reg.base;

/**
 * Coordinator registry change event.
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
