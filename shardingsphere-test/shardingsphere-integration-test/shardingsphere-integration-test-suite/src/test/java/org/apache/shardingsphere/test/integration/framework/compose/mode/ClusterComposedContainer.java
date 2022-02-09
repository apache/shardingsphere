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
import org.apache.shardingsphere.test.integration.framework.container.adapter.impl.ShardingSphereProxyContainer;
import org.apache.shardingsphere.test.integration.framework.container.governance.ZookeeperContainer;
import org.apache.shardingsphere.test.integration.framework.container.storage.StorageContainer;
import org.apache.shardingsphere.test.integration.framework.container.storage.StorageContainerFactory;
import org.apache.shardingsphere.test.integration.framework.param.model.ParameterizedArray;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Cluster composed container.
 */
public final class ClusterComposedContainer extends ComposedContainer {
    
    @Getter
    private final StorageContainer storageContainer;
    
    @Getter
    private final AdapterContainer adapterContainer;
    
    private final AdapterContainer adapterContainerForReader;
    
    private final ZookeeperContainer zookeeperContainer;
    
    public ClusterComposedContainer(final String testSuiteName, final ParameterizedArray parameterizedArray) {
        super(testSuiteName);
        storageContainer = getContainers().registerContainer(
                StorageContainerFactory.newInstance(parameterizedArray), parameterizedArray.getDatabaseType().getName().toLowerCase() + "." + parameterizedArray.getScenario() + ".host");
        adapterContainer = getContainers().registerContainer(AdapterContainerFactory.newInstance(parameterizedArray), "adapter");
        storageContainer.setNetworkAliases(Collections.singletonList(parameterizedArray.getDatabaseType().getName().toLowerCase() + ".sharding_governance.host"));
        // TODO support other types of governance
        zookeeperContainer = getContainers().registerContainer(new ZookeeperContainer(parameterizedArray), "zk");
        if ("proxy".equals(parameterizedArray.getAdapter())) {
            adapterContainerForReader = getContainers().registerContainer(new ShardingSphereProxyContainer("ShardingSphere-Proxy-1", parameterizedArray), "ShardingSphere-Proxy-1");
            adapterContainerForReader.dependsOn(storageContainer, zookeeperContainer);
        } else {
            adapterContainerForReader = getContainers().registerContainer(AdapterContainerFactory.newInstance(parameterizedArray), "adapter");
            adapterContainerForReader.dependsOn(storageContainer, zookeeperContainer);
        }
        adapterContainer.dependsOn(storageContainer, zookeeperContainer);
    }
    
    @Override
    public Map<String, DataSource> getDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(2, 1);
        String serverLists = zookeeperContainer.getServerLists();
        result.put("adapterForWriter", adapterContainer.getDataSource(serverLists));
        result.put("adapterForReader", adapterContainerForReader.getDataSourceForReader(serverLists));
        return result;
    }
}
