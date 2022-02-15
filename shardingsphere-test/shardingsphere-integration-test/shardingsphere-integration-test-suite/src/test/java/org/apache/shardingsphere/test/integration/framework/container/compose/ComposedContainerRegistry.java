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

package org.apache.shardingsphere.test.integration.framework.container.compose;

import org.apache.shardingsphere.test.integration.framework.container.compose.mode.ClusterComposedContainer;
import org.apache.shardingsphere.test.integration.framework.container.compose.mode.MemoryComposedContainer;
import org.apache.shardingsphere.test.integration.framework.param.model.ParameterizedArray;
import org.testcontainers.lifecycle.Startable;

import java.util.HashMap;
import java.util.Map;

/**
 * Composed container registry.
 */
public final class ComposedContainerRegistry implements AutoCloseable {
    
    private final Map<String, ComposedContainer> composedContainers = new HashMap<>();
    
    /**
     * Get composed container.
     *
     * @param testSuiteName test suite name
     * @param parameterizedArray parameterized array
     * @return composed container
     */
    public ComposedContainer getComposedContainer(final String testSuiteName, final ParameterizedArray parameterizedArray) {
        String key = generateKey(testSuiteName, parameterizedArray);
        if (composedContainers.containsKey(key)) {
            return composedContainers.get(key);
        }
        synchronized (composedContainers) {
            if (!composedContainers.containsKey(key)) {
                composedContainers.put(key, createComposedContainer(testSuiteName, parameterizedArray));
            }
            return composedContainers.get(key);
        }
    }
    
    private String generateKey(final String testSuiteName, final ParameterizedArray parameterizedArray) {
        return String.join("-", testSuiteName, parameterizedArray.getScenario(), parameterizedArray.getAdapter(), parameterizedArray.getDatabaseType().getName());
    }
    
    private ComposedContainer createComposedContainer(final String testSuiteName, final ParameterizedArray parameterizedArray) {
        // TODO fix sharding_governance
        return "sharding_governance".equals(parameterizedArray.getScenario())
                ? new ClusterComposedContainer(testSuiteName, parameterizedArray) : new MemoryComposedContainer(testSuiteName, parameterizedArray);
    }
    
    @Override
    public void close() {
        composedContainers.values().forEach(Startable::close);
    }
}
