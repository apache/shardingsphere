package io.shardingjdbc.orchestration.reg.base;

/**
 * Coordinator registry change event.
 *
 * @author junxiong
 */
public interface ChangeListener {
    
    /**
     * Fire when event changed.
     * 
     * @param event data changed event
     * @throws Exception
     */
    void onChange(DataChangedEvent event) throws Exception;
}
