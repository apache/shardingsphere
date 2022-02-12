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
import org.apache.shardingsphere.test.integration.framework.container.atomic.ShardingSphereContainers;
import org.apache.shardingsphere.test.integration.framework.container.atomic.adapter.AdapterContainer;
import org.apache.shardingsphere.test.integration.framework.container.atomic.adapter.AdapterContainerFactory;
import org.apache.shardingsphere.test.integration.framework.container.atomic.adapter.impl.ShardingSphereProxyContainer;
import org.apache.shardingsphere.test.integration.framework.container.atomic.governance.ZookeeperContainer;
import org.apache.shardingsphere.test.integration.framework.container.atomic.storage.StorageContainer;
import org.apache.shardingsphere.test.integration.framework.container.atomic.storage.StorageContainerFactory;
import org.apache.shardingsphere.test.integration.framework.container.compose.ComposedContainer;
import org.apache.shardingsphere.test.integration.framework.param.model.ParameterizedArray;

import javax.sql.DataSource;

/**
 * Cluster composed container.
 */
public final class ClusterComposedContainer implements ComposedContainer {
    
    @Getter
    private final ShardingSphereContainers containers;
    
    @Getter
    private final StorageContainer storageContainer;
    
    private final AdapterContainer adapterContainer;
    
    private final AdapterContainer adapterContainerForReader;
    
    private final ZookeeperContainer zookeeperContainer;
    
    public ClusterComposedContainer(final String testSuiteName, final ParameterizedArray parameterizedArray) {
        containers = new ShardingSphereContainers(testSuiteName, parameterizedArray.getScenario());
        storageContainer = containers.registerContainer(
                StorageContainerFactory.newInstance(parameterizedArray.getDatabaseType(), parameterizedArray.getScenario()), parameterizedArray.getDatabaseType().getName());
        adapterContainer = containers.registerContainer(AdapterContainerFactory.newInstance(
                parameterizedArray.getAdapter(), parameterizedArray.getDatabaseType(), parameterizedArray.getScenario()), parameterizedArray.getAdapter());
        // TODO support other types of governance
        zookeeperContainer = containers.registerContainer(new ZookeeperContainer(), "zk");
        if ("proxy".equals(parameterizedArray.getAdapter())) {
            adapterContainerForReader = containers.registerContainer(
                    new ShardingSphereProxyContainer("ShardingSphere-Proxy-1", parameterizedArray.getDatabaseType(), parameterizedArray.getScenario()), "ShardingSphere-Proxy-1");
            adapterContainerForReader.dependsOn(storageContainer, zookeeperContainer);
        } else {
            adapterContainerForReader = containers.registerContainer(
                    AdapterContainerFactory.newInstance(parameterizedArray.getAdapter(), parameterizedArray.getDatabaseType(), parameterizedArray.getScenario()), parameterizedArray.getAdapter());
            adapterContainerForReader.dependsOn(storageContainer, zookeeperContainer);
        }
        adapterContainer.dependsOn(storageContainer, zookeeperContainer);
    }
    
    @Override
    public DataSource getClientDataSource() {
        return adapterContainer.getClientDataSource(zookeeperContainer.getServerLists());
    }
    
    /**
     * Get another client data source.
     * 
     * @return another client data source
     */
    public DataSource getAnotherClientDataSource() {
        return adapterContainerForReader.getAnotherClientDataSource(zookeeperContainer.getServerLists());
    }
}
