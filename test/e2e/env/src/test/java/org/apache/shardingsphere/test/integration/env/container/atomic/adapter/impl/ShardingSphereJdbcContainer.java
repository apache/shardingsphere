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

package org.apache.shardingsphere.test.integration.env.container.atomic.adapter.impl;

import com.google.common.base.Strings;
import lombok.SneakyThrows;
import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.mode.YamlModeConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.mode.YamlPersistRepositoryConfiguration;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.test.integration.env.container.atomic.EmbeddedITContainer;
import org.apache.shardingsphere.test.integration.env.container.atomic.adapter.AdapterContainer;
import org.apache.shardingsphere.test.integration.env.container.atomic.storage.StorageContainer;
import org.apache.shardingsphere.test.integration.env.runtime.scenario.path.ScenarioCommonPath;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ShardingSphere JDBC container.
 */
public final class ShardingSphereJdbcContainer implements EmbeddedITContainer, AdapterContainer {
    
    private final StorageContainer storageContainer;
    
    private final ScenarioCommonPath scenarioCommonPath;
    
    private final AtomicReference<DataSource> targetDataSourceProvider = new AtomicReference<>();
    
    public ShardingSphereJdbcContainer(final StorageContainer storageContainer, final String scenario) {
        this.storageContainer = storageContainer;
        scenarioCommonPath = new ScenarioCommonPath(scenario);
    }
    
    @Override
    public void start() {
    }
    
    @Override
    public DataSource getTargetDataSource(final String serverLists) {
        DataSource dataSource = targetDataSourceProvider.get();
        if (Objects.isNull(dataSource)) {
            if (Strings.isNullOrEmpty(serverLists)) {
                try {
                    targetDataSourceProvider.set(
                            YamlShardingSphereDataSourceFactory.createDataSource(storageContainer.getActualDataSourceMap(), new File(scenarioCommonPath.getRuleConfigurationFile())));
                } catch (final SQLException | IOException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                targetDataSourceProvider.set(createGovernanceClientDataSource(serverLists));
            }
        }
        return targetDataSourceProvider.get();
    }
    
    @SneakyThrows({SQLException.class, IOException.class})
    private DataSource createGovernanceClientDataSource(final String serverLists) {
        YamlRootConfiguration rootConfig = YamlEngine.unmarshal(new File(scenarioCommonPath.getRuleConfigurationFile()), YamlRootConfiguration.class);
        rootConfig.setMode(createYamlModeConfiguration(serverLists));
        return YamlShardingSphereDataSourceFactory.createDataSource(storageContainer.getActualDataSourceMap(), YamlEngine.marshal(rootConfig).getBytes(StandardCharsets.UTF_8));
    }
    
    private YamlModeConfiguration createYamlModeConfiguration(final String serverLists) {
        YamlModeConfiguration result = new YamlModeConfiguration();
        result.setType("Cluster");
        YamlPersistRepositoryConfiguration repositoryConfig = new YamlPersistRepositoryConfiguration();
        // TODO process more types
        repositoryConfig.setType("ZooKeeper");
        repositoryConfig.getProps().setProperty("namespace", "it_db");
        repositoryConfig.getProps().setProperty("server-lists", serverLists);
        repositoryConfig.getProps().setProperty("timeToLiveSeconds", "60");
        repositoryConfig.getProps().setProperty("operationTimeoutMilliseconds", "500");
        repositoryConfig.getProps().setProperty("retryIntervalMilliseconds", "500");
        repositoryConfig.getProps().setProperty("maxRetries", "3");
        result.setRepository(repositoryConfig);
        return result;
    }
    
    @Override
    public String getAbbreviation() {
        return "jdbc";
    }
}
