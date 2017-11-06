package io.shardingjdbc.orchestration.reg.base;

/**
 * state change listener
 *
 * @author junxiong
 */
public interface StateChangeListener {
    /**
     * Notify when state change event fired
     *
     * @param stateChangeEvent state change event
     */
    void onStateChange(StateChangeEvent stateChangeEvent);
}
