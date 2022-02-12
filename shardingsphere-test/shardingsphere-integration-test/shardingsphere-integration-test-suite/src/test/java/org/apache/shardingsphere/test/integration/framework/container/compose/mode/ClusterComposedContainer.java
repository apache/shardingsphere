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
import org.apache.shardingsphere.test.integration.framework.container.atomic.governance.GovernanceContainer;
import org.apache.shardingsphere.test.integration.framework.container.atomic.governance.GovernanceContainerFactory;
import org.apache.shardingsphere.test.integration.framework.container.atomic.storage.StorageContainer;
import org.apache.shardingsphere.test.integration.framework.container.atomic.storage.StorageContainerFactory;
import org.apache.shardingsphere.test.integration.framework.container.compose.ComposedContainer;
import org.apache.shardingsphere.test.integration.framework.param.model.ParameterizedArray;

import javax.sql.DataSource;

/**
 * Cluster composed container.
 */
public final class ClusterComposedContainer implements ComposedContainer {
    
    private final GovernanceContainer governanceContainer;
    
    @Getter
    private final AtomicContainers containers;
    
    @Getter
    private final StorageContainer storageContainer;
    
    private final AdapterContainer adapterContainer;
    
    public ClusterComposedContainer(final String testSuiteName, final ParameterizedArray parameterizedArray) {
        containers = new AtomicContainers(testSuiteName, parameterizedArray.getScenario());
        // TODO support other types of governance
        governanceContainer = containers.registerContainer(GovernanceContainerFactory.newInstance("ZooKeeper"), "zk");
        storageContainer = containers.registerContainer(
                StorageContainerFactory.newInstance(parameterizedArray.getDatabaseType(), parameterizedArray.getScenario()), parameterizedArray.getDatabaseType().getName());
        adapterContainer = containers.registerContainer(
                AdapterContainerFactory.newInstance(parameterizedArray.getAdapter(), parameterizedArray.getDatabaseType(), parameterizedArray.getScenario()), parameterizedArray.getAdapter());
        adapterContainer.dependsOn(governanceContainer, storageContainer);
    }
    
    @Override
    public DataSource getTargetDataSource() {
        return adapterContainer.getTargetDataSource(governanceContainer.getServerLists());
    }
}
