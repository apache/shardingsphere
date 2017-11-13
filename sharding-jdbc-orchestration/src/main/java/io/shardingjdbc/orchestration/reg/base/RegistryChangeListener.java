package io.shardingjdbc.orchestration.reg.base;

/**
 * Registry change Listener
 *
 * @author junxiong
 */
public interface RegistryChangeListener {
    void onRegistryChange(RegistryChangeEvent registryChangeEvent) throws Exception;
}
