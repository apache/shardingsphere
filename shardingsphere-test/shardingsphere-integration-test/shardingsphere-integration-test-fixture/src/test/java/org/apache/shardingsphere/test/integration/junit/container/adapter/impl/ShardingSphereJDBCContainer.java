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

package org.apache.shardingsphere.test.integration.junit.container.adapter.impl;

import lombok.SneakyThrows;
import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.apache.shardingsphere.driver.governance.api.yaml.YamlGovernanceShardingSphereDataSourceFactory;
import org.apache.shardingsphere.test.integration.env.EnvironmentPath;
import org.apache.shardingsphere.test.integration.junit.container.ShardingSphereContainer;
import org.apache.shardingsphere.test.integration.junit.container.adapter.ShardingSphereAdapterContainer;
import org.apache.shardingsphere.test.integration.junit.container.governance.ZookeeperContainer;
import org.apache.shardingsphere.test.integration.junit.container.storage.ShardingSphereStorageContainer;
import org.apache.shardingsphere.test.integration.junit.param.model.ParameterizedArray;
import org.testcontainers.containers.wait.strategy.WaitStrategy;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ShardingSphere JDBC container.
 */
public final class ShardingSphereJDBCContainer extends ShardingSphereAdapterContainer {
    
    private final AtomicBoolean isHealthy = new AtomicBoolean();
    
    private DataSource dataSource;
    
    public ShardingSphereJDBCContainer(final ParameterizedArray parameterizedArray) {
        super("ShardingSphere-JDBC", "ShardingSphere-JDBC", true, parameterizedArray);
    }
    
    @SneakyThrows
    @Override
    public void start() {
        super.start();
        Optional<ZookeeperContainer> governance = getDependencies().stream()
                .filter(x -> x instanceof ZookeeperContainer)
                .map(x -> (ZookeeperContainer) x)
                .findFirst();
        ShardingSphereStorageContainer storageContainer = getDependencies().stream()
                .filter(x -> x instanceof ShardingSphereStorageContainer)
                .map(x -> (ShardingSphereStorageContainer) x)
                .findFirst()
                .orElseThrow(Exception::new);
        Map<String, DataSource> dataSourceMap = storageContainer.getDataSourceMap();
        try {
            if ("sharding_governance".equals(getParameterizedArray().getScenario())) {
                dataSource = createDataSource(dataSourceMap, governance, 0);
            } else {
                dataSource = YamlShardingSphereDataSourceFactory.createDataSource(dataSourceMap, new File(EnvironmentPath.getRulesConfigurationFile(getParameterizedArray().getScenario())));
            }
        } catch (SQLException | IOException ex) {
            throw new RuntimeException(ex);
        }
        isHealthy.set(true);
    }
    
    @SneakyThrows
    private DataSource createDataSource(final Map<String, DataSource> dataSourceMap, final Optional<ZookeeperContainer> governance, final int retry) {
        try {
            return YamlGovernanceShardingSphereDataSourceFactory.createDataSource(
                    dataSourceMap,
                    governance.orElseThrow(() -> new NullPointerException("Governance Container cannot be null.")).getGovernanceConfiguration(),
                    new File(EnvironmentPath.getRulesConfigurationFile(getParameterizedArray().getScenario()))
            );
        } catch (NullPointerException ex) {
            if (retry == 0) {
                return createDataSource(dataSourceMap, governance, 1);
            }
            throw ex;
        }
    }
    
    /**
     * Get data source.
     *
     * @return data source
     */
    public DataSource getDataSource() {
        return dataSource;
    }
    
    @Override
    public boolean isHealthy() {
        return isHealthy.get();
    }
    
    @Override
    public ShardingSphereContainer waitingFor(final WaitStrategy waitStrategy) {
        return super.waitingFor(waitStrategy);
    }
    
}
