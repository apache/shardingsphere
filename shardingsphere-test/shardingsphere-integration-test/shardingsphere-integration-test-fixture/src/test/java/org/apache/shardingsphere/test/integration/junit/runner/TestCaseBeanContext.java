/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.test.integration.junit.runner;

import org.apache.curator.shaded.com.google.common.collect.Maps;

import java.util.Map;

public class TestCaseBeanContext {
    
    private final Map<Object, Object> classObjectMap = Maps.newConcurrentMap();
    
    /**
     * Register the bean into context.
     *
     * @param <T> type
     * @param identity key
     * @param instance value
     */
    public <T> void registerBean(final Class<T> identity, final T instance) {
        classObjectMap.putIfAbsent(identity, instance);
    }
    
    /**
     * Put the value with key into context.
     *
     * @param quality key
     * @param value   value
     */
    public void registerBeanByName(final String quality, final Object value) {
        classObjectMap.putIfAbsent(quality, value);
    }
    
    /**
     * Get the bean from context.
     *
     * @param <T> type
     * @param klass class
     * @return value
     */
    public <T> T getBean(final Class<T> klass) {
        return (T) classObjectMap.get(klass);
    }
    
    /**
     * Get the bean from context by name.
     *
     * @param <T> type
     * @param name name
     * @return value
     */
    public <T> T getBeanByName(final String name) {
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
