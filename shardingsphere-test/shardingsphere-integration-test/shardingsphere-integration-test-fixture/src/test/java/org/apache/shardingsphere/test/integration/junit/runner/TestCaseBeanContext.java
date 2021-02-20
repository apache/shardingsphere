package org.apache.shardingsphere.test.integration.junit.runner;

import org.apache.curator.shaded.com.google.common.collect.Maps;

import java.util.Map;

public class TestCaseBeanContext {
    
    private final Map<Object, Object> classObjectMap = Maps.newConcurrentMap();
    
    /**
     * Register the bean into context.
     *
     * @param identity key
     * @param instance value
     */
    public <T> void registerBean(Class<T> identity, T instance) {
        classObjectMap.putIfAbsent(identity, instance);
    }
    
    /**
     * Put the value with key into context.
     *
     * @param quality key
     * @param value   value
     */
    public void registerBeanByName(String quality, Object value) {
        classObjectMap.putIfAbsent(quality, value);
    }
    
    /**
     * Get the bean from context.
     *
     * @param klass class
     * @return value
     */
    public <T> T getBean(Class<T> klass) {
        return (T) classObjectMap.get(klass);
    }
    
    /**
     * Get the bean from context by name.
     *
     * @param name name
     * @return value
     */
    public <T> T getBeanByName(String name) {
        return (T) classObjectMap.get(name);
    }
    
    /**
     * Create sub-context.
     *
     * @return Test case bean context
     */
    public TestCaseBeanContext subContext() {
        TestCaseBeanContext subContext = new TestCaseBeanContext();
        subContext.classObjectMap.putAll(classObjectMap);
        return subContext;
    }
    
    /**
     * Clean up.
     */
    public void cleanup() {
        classObjectMap.clear();
    }
}
