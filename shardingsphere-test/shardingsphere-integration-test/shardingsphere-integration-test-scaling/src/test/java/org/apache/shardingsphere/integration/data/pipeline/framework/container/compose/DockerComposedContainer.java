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

package org.apache.shardingsphere.integration.data.pipeline.framework.container.compose;

import lombok.Getter;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.integration.data.pipeline.factory.DatabaseContainerFactory;
import org.apache.shardingsphere.integration.data.pipeline.framework.container.cluster.ZookeeperContainer;
import org.apache.shardingsphere.integration.data.pipeline.framework.container.database.DatabaseContainer;
import org.apache.shardingsphere.integration.data.pipeline.framework.container.proxy.ShardingSphereProxyDockerContainer;
import org.apache.shardingsphere.test.integration.env.DataSourceEnvironment;
import org.apache.shardingsphere.test.integration.framework.container.atomic.governance.GovernanceContainer;
import org.apache.shardingsphere.test.integration.util.NetworkAliasUtil;

/**
 * Composed container, include governance container and database container.
 */
@Getter
public final class DockerComposedContainer extends BaseComposedContainer {
    
    private final DatabaseType databaseType;
    
    private final GovernanceContainer governanceContainer;
    
    private final ShardingSphereProxyDockerContainer proxyContainer;
    
    private final DatabaseContainer databaseContainer;
    
    public DockerComposedContainer(final DatabaseType databaseType, final String dockerImageName) {
        this.databaseType = databaseType;
        ShardingSphereProxyDockerContainer proxyContainer = new ShardingSphereProxyDockerContainer(databaseType);
        governanceContainer = getContainers().registerContainer(new ZookeeperContainer(), NetworkAliasUtil.getNetworkAlias("zk"));
        databaseContainer = getContainers().registerContainer(DatabaseContainerFactory.newInstance(databaseType, dockerImageName), NetworkAliasUtil.getNetworkAlias("db"));
        proxyContainer.dependsOn(governanceContainer, databaseContainer);
        this.proxyContainer = getContainers().registerContainer(proxyContainer, NetworkAliasUtil.getNetworkAlias("sharding-proxy"));
    }
    
    @Override
    public void stop() {
        super.stop();
        proxyContainer.stop();
    }
    
    @Override
    public String getProxyJdbcUrl(final String databaseName) {
        return DataSourceEnvironment.getURL(databaseType, proxyContainer.getHost(), proxyContainer.getFirstMappedPort(), databaseName);
    }
    
    @Override
    public void cleanUpDatabase(final String databaseName) {
    }
}
