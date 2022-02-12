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

package org.apache.shardingsphere.test.integration.framework.container.compose.mode;

import lombok.Getter;
import org.apache.shardingsphere.test.integration.framework.container.atomic.AtomicContainers;
import org.apache.shardingsphere.test.integration.framework.container.atomic.adapter.AdapterContainer;
import org.apache.shardingsphere.test.integration.framework.container.atomic.adapter.AdapterContainerFactory;
import org.apache.shardingsphere.test.integration.framework.container.atomic.storage.StorageContainer;
import org.apache.shardingsphere.test.integration.framework.container.atomic.storage.StorageContainerFactory;
import org.apache.shardingsphere.test.integration.framework.container.compose.ComposedContainer;
import org.apache.shardingsphere.test.integration.framework.param.model.ParameterizedArray;

import javax.sql.DataSource;

/**
 * Memory composed container.
 */
@Getter
public final class MemoryComposedContainer implements ComposedContainer {
    
    private final AtomicContainers containers;
    
    private final StorageContainer storageContainer;
    
    private final AdapterContainer adapterContainer;
    
    public MemoryComposedContainer(final String testSuiteName, final ParameterizedArray parameterizedArray) {
        containers = new AtomicContainers(testSuiteName, parameterizedArray.getScenario());
        storageContainer = containers.registerContainer(
                StorageContainerFactory.newInstance(parameterizedArray.getDatabaseType(), parameterizedArray.getScenario()), parameterizedArray.getDatabaseType().getName());
        adapterContainer = containers.registerContainer(
                AdapterContainerFactory.newInstance(parameterizedArray.getAdapter(), parameterizedArray.getDatabaseType(), parameterizedArray.getScenario()), parameterizedArray.getAdapter());
        adapterContainer.dependsOn(storageContainer);
    }
    
    @Override
    public DataSource getClientDataSource() {
        return adapterContainer.getClientDataSource(null);
    }
}
