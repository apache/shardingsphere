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

package org.apache.shardingsphere.test.integration.framework.compose.mode;

import lombok.Getter;
import org.apache.shardingsphere.test.integration.framework.compose.ComposedContainer;
import org.apache.shardingsphere.test.integration.framework.container.adapter.AdapterContainer;
import org.apache.shardingsphere.test.integration.framework.container.adapter.AdapterContainerFactory;
import org.apache.shardingsphere.test.integration.framework.container.storage.StorageContainer;
import org.apache.shardingsphere.test.integration.framework.container.storage.StorageContainerFactory;
import org.apache.shardingsphere.test.integration.framework.param.model.ParameterizedArray;

/**
 * Memory composed container.
 */
@Getter
public final class MemoryComposedContainer extends ComposedContainer {
    
    private final StorageContainer storageContainer;
    
    private final AdapterContainer adapterContainer;
    
    public MemoryComposedContainer(final String suiteName, final ParameterizedArray parameterizedArray) {
        super(suiteName);
        this.storageContainer = getContainers().registerContainer(
                StorageContainerFactory.newInstance(parameterizedArray), parameterizedArray.getDatabaseType().getName().toLowerCase() + "." + parameterizedArray.getScenario() + ".host");
        this.adapterContainer = getContainers().registerContainer(AdapterContainerFactory.newInstance(parameterizedArray), "adapter");
        adapterContainer.dependsOn(storageContainer);
    }
}
