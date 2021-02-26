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

package org.apache.shardingsphere.test.integration.junit.container;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.test.integration.env.EnvironmentPath;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.lifecycle.Startable;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class ShardingSphereJDBCContainer extends ShardingSphereAdapterContainer {
    
    public ShardingSphereJDBCContainer() {
        super("ShardingSphere-JDBC");
    }
    
    @Override
    public void start() {
        fake = true;
        super.start();
        // do not start because it is a fake container.
        List<Startable> startables = getDependencies().stream()
                .filter(e -> e instanceof StorageContainer)
                .collect(Collectors.toList());
        StorageContainer storage = (StorageContainer) startables.get(0);
        try {
            DataSource dataSource = YamlShardingSphereDataSourceFactory.createDataSource(
                    storage.getDataSourceMap(),
                    new File(EnvironmentPath.getRulesConfigurationFile(getDescription().getScenario()))
            );
            dataSourceProvider.set(dataSource);
        } catch (SQLException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public DataSource getDataSource() {
        return dataSourceProvider.get();
    }
    
    @Override
    public boolean isHealthy() {
        return true;
    }
    
    @Override
    public ShardingSphereContainer waitingFor(@NonNull WaitStrategy waitStrategy) {
        return super.waitingFor(waitStrategy);
    }
    
}
