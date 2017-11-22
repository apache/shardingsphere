package io.shardingjdbc.orchestration.reg.base;

/**
 * Coordinator registry change event.
 *
 * @author junxiong
 */
public interface ChangeListener {
    void onChange(ChangeEvent changeEvent) throws Exception;
}
