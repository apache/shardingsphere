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

package org.apache.shardingsphere.integration.data.pipline.container;

import lombok.Getter;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.integration.data.pipline.factory.DatabaseContainerFactory;
import org.apache.shardingsphere.test.integration.framework.container.atomic.ITContainers;
import org.apache.shardingsphere.test.integration.framework.container.atomic.governance.GovernanceContainer;
import org.apache.shardingsphere.test.integration.framework.container.atomic.governance.GovernanceContainerFactory;
import org.apache.shardingsphere.test.integration.util.NetworkAliasUtil;
import org.testcontainers.lifecycle.Startable;

import javax.sql.DataSource;
import java.util.List;

public final class ComposedContainer implements Startable {
    
    private final ITContainers containers;
    
    @Getter
    private final GovernanceContainer governanceContainer;
    
    private final DockerDatabaseContainer databaseContainer;
    
    private final ShardingSphereProxyContainer proxyContainer;
    
    @Getter
    private final String databaseNetworkAlias = NetworkAliasUtil.getNetworkAlias("db");
    
    public ComposedContainer(final DatabaseType databaseType) {
        this.containers = new ITContainers("");
        this.governanceContainer = containers.registerContainer(GovernanceContainerFactory.newInstance("ZooKeeper"), NetworkAliasUtil.getNetworkAlias("zk"));
        this.databaseContainer = containers.registerContainer(DatabaseContainerFactory.newInstance(databaseType), databaseNetworkAlias);
        ShardingSphereProxyContainer proxyContainer = new ShardingSphereProxyContainer(databaseType, "");
        proxyContainer.dependsOn(governanceContainer, databaseContainer);
        this.proxyContainer = containers.registerContainer(proxyContainer, NetworkAliasUtil.getNetworkAlias("sharding-proxy"));
    }
    
    @Override
    public void start() {
        containers.start();
    }
    
    @Override
    public void stop() {
        containers.stop();
    }
    
    /**
     * Get target data source.
     *
     * @return target data source.
     */
    public DataSource getTargetDataSource() {
        return proxyContainer.getTargetDataSource();
    }
    
    /**
     * Get actual data source map.
     *
     * @return actual data source map
     */
    public List<String> listSourceDatabaseName() {
        return databaseContainer.getSourceDatabaseNames();
    }
    
    /**
     * Get expected data source map.
     *
     * @return expected data source map
     */
    public List<String> listTargetDatabaseName() {
        return databaseContainer.getTargetDatabaseNames();
    }
}
