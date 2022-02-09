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

package org.apache.shardingsphere.test.integration.framework.compose;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.test.integration.env.EnvironmentType;
import org.apache.shardingsphere.test.integration.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.test.integration.framework.compose.mode.ClusterComposedContainer;
import org.apache.shardingsphere.test.integration.framework.compose.mode.MemoryComposedContainer;
import org.apache.shardingsphere.test.integration.framework.param.model.ParameterizedArray;
import org.junit.rules.ExternalResource;

import java.util.HashMap;
import java.util.Map;

/**
 * Composed container manager.
 */
@RequiredArgsConstructor
public final class ComposedContainerManager extends ExternalResource {
    
    private final String testSuiteName;
    
    private final Map<String, ComposedContainer> composedContainers = new HashMap<>();
    
    /**
     * Create or get container compose.
     *
     * @param parameterizedArray parameterized array
     * @return container compose
     */
    public ComposedContainer getOrCreateCompose(final ParameterizedArray parameterizedArray) {
        String key = generateKey(parameterizedArray);
        if (composedContainers.containsKey(key)) {
            return composedContainers.get(key);
        }
        ComposedContainer result;
        // TODO fix sharding_governance
        if ("sharding_governance".equals(parameterizedArray.getScenario())) {
            result = new ClusterComposedContainer(testSuiteName, parameterizedArray);
        } else {
            result = new MemoryComposedContainer(testSuiteName, parameterizedArray);
        }
        composedContainers.put(key, result);
        return result;
    }
    
    private String generateKey(final ParameterizedArray parameter) {
        return String.join("-", testSuiteName, parameter.getScenario(), parameter.getAdapter(), parameter.getDatabaseType().getName());
    }
    
    @Override
    protected void before() {
        if (EnvironmentType.DOCKER == IntegrationTestEnvironment.getInstance().getEnvType()) {
            composedContainers.values().forEach(each -> {
                each.getContainers().start();
                each.getContainers().waitUntilReady();
            });
        }
    }
    
    @Override
    protected void after() {
        composedContainers.values().forEach(ComposedContainer::close);
    }
}
