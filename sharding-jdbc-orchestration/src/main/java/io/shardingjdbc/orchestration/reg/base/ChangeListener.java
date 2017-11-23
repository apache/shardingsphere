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
     * @param changeEvent change event
     * @throws Exception
     */
    void onChange(ChangeEvent changeEvent) throws Exception;
}
