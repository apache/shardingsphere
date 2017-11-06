package io.shardingjdbc.orchestration.reg.base;

/**
 * Sharding-jdbc configuration change listener
 *
 * @author junxiong
 */
public interface ConfigChangeListener {
    /**
     * Notify when config change event fired
     *
     * @param configChangeEvent config change event
     */
    void onConfigChange(ConfigChangeEvent configChangeEvent);
}
