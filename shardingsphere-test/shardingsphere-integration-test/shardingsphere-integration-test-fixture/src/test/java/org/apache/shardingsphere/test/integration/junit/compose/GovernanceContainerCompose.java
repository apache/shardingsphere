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

package org.apache.shardingsphere.test.integration.junit.compose;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.driver.governance.internal.datasource.GovernanceShardingSphereDataSource;
import org.apache.shardingsphere.test.integration.env.EnvironmentType;
import org.apache.shardingsphere.test.integration.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.test.integration.junit.container.adapter.ShardingSphereAdapterContainer;
import org.apache.shardingsphere.test.integration.junit.container.adapter.impl.ShardingSphereJDBCContainer;
import org.apache.shardingsphere.test.integration.junit.container.adapter.impl.ShardingSphereProxyContainer;
import org.apache.shardingsphere.test.integration.junit.container.governance.ShardingSphereGovernanceContainer;
import org.apache.shardingsphere.test.integration.junit.container.governance.impl.EmbeddedZookeeperContainer;
import org.apache.shardingsphere.test.integration.junit.container.governance.impl.ZookeeperContainer;
import org.apache.shardingsphere.test.integration.junit.container.storage.ShardingSphereStorageContainer;
import org.apache.shardingsphere.test.integration.junit.param.model.ParameterizedArray;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public final class GovernanceContainerCompose extends ContainerCompose {
    
    @Getter
    private final ShardingSphereStorageContainer storageContainer;
    
    @Getter
    private final ShardingSphereAdapterContainer adapterContainer;

    private final ShardingSphereAdapterContainer adapterContainerForReader;
    
    private final Map<String, DataSource> dataSourceMap = new HashMap<>(2);
    
    public GovernanceContainerCompose(final String clusterName, final ParameterizedArray parameterizedArray) {
        super(clusterName, parameterizedArray);
        this.storageContainer = createStorageContainer();
        this.adapterContainer = createAdapterContainer();
        this.storageContainer.setNetworkAliases(Collections.singletonList("mysql.sharding-governance.host"));
        // TODO support other types of governance
        ShardingSphereGovernanceContainer governanceContainer = createZookeeperContainer();
        if ("proxy".equals(parameterizedArray.getAdapter())) {
            adapterContainerForReader = createContainer(() -> new ShardingSphereProxyContainer("ShardingSphere-Proxy-1", parameterizedArray), "ShardingSphere-Proxy-1");
        } else {
            adapterContainerForReader = createContainer(() -> new ShardingSphereJDBCContainer("ShardingSphere-JDBC-1", parameterizedArray), "ShardingSphere-JDBC-1");
        }
        adapterContainerForReader.dependsOn(storageContainer, governanceContainer);
        adapterContainer.dependsOn(storageContainer, governanceContainer);
    }
    
    private ShardingSphereGovernanceContainer createZookeeperContainer() {
        return createContainer(() -> {
            if (EnvironmentType.DOCKER == IntegrationTestEnvironment.getInstance().getEnvType()) {
                return new ZookeeperContainer(getParameterizedArray());
            }
            return new EmbeddedZookeeperContainer(getParameterizedArray());
        }, "zk");
    }
    
    @Override
    public void before() {
        if (EnvironmentType.DOCKER == IntegrationTestEnvironment.getInstance().getEnvType()) {
            super.before();
        } else {
            start();
            waitUntilReady();
        }
        dataSourceMap.put("adapterForWriter", adapterContainer.getDataSource());
        dataSourceMap.put("adapterForReader", adapterContainerForReader.getDataSource());
    }
    
    @Override
    public Map<String, DataSource> getDataSourceMap() {
        return dataSourceMap;
    }
    
    @Override
    public void closeDataSource() {
        dataSourceMap.forEach((key, value) -> {
            if (value instanceof GovernanceShardingSphereDataSource) {
                ((GovernanceShardingSphereDataSource) value).getMetaDataContexts().getExecutorEngine().close();
            }
        });
    }
}
