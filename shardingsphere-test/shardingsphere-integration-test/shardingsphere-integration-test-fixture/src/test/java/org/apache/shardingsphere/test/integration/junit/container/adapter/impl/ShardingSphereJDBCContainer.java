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

import com.google.common.base.Strings;
import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.mode.YamlModeConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.infra.yaml.config.swapper.mode.ModeConfigurationYamlSwapper;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.test.integration.env.EnvironmentPath;
import org.apache.shardingsphere.test.integration.junit.container.ShardingSphereContainer;
import org.apache.shardingsphere.test.integration.junit.container.adapter.ShardingSphereAdapterContainer;
import org.apache.shardingsphere.test.integration.junit.container.storage.ShardingSphereStorageContainer;
import org.apache.shardingsphere.test.integration.junit.param.model.ParameterizedArray;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.lifecycle.Startable;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * ShardingSphere JDBC container.
 */
public final class ShardingSphereJDBCContainer extends ShardingSphereAdapterContainer {
    
    private final AtomicBoolean isHealthy = new AtomicBoolean();
    
    private Map<String, DataSource> dataSourceMap;

    private final AtomicReference<DataSource> dataSourceProvider = new AtomicReference<>();

    private final AtomicReference<DataSource> dataSourceForReaderProvider = new AtomicReference<>();
    
    public ShardingSphereJDBCContainer(final ParameterizedArray parameterizedArray) {
        super("ShardingSphere-JDBC", "ShardingSphere-JDBC", true, parameterizedArray);
    }
    
    @Override
    public void start() {
        super.start();
        List<Startable> startables = getDependencies().stream()
                .filter(e -> e instanceof ShardingSphereStorageContainer)
                .collect(Collectors.toList());
        dataSourceMap = ((ShardingSphereStorageContainer) startables.get(0)).getDataSourceMap();
        isHealthy.set(true);
    }

    /**
     * Get data source.
     *
     * @param serverLists server list
     * @return data source
     */
    public DataSource getDataSource(final String serverLists) {
        DataSource dataSource = dataSourceProvider.get();
        if (Objects.isNull(dataSource)) {
            if (null != serverLists && !"".equals(serverLists)) {
                dataSourceProvider.lazySet(createGovernanceDataSource(serverLists));
            } else {
                try {
                    dataSourceProvider.lazySet(YamlShardingSphereDataSourceFactory.createDataSource(dataSourceMap,
                            new File(EnvironmentPath.getRulesConfigurationFile(getParameterizedArray().getScenario()))));
                } catch (SQLException | IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        return dataSourceProvider.get();
    }

    /**
     * Get governance data source for reader.
     *
     * @param serverLists server list
     * @return data source
     */
    public DataSource getDataSourceForReader(final String serverLists) {
        DataSource dataSource = dataSourceForReaderProvider.get();
        if (Objects.isNull(dataSource)) {
            dataSourceForReaderProvider.lazySet(createGovernanceDataSource(serverLists));
        }
        return dataSourceForReaderProvider.get();
    }

    /**
     * Create governance data source.
     *
     * @param serverLists server list
     * @return data source
     */
    private DataSource createGovernanceDataSource(final String serverLists) {
        try {
            File yamlFile = new File(EnvironmentPath.getRulesConfigurationFile(getParameterizedArray().getScenario()));
            YamlRootConfiguration rootConfig = YamlEngine.unmarshal(yamlFile, YamlRootConfiguration.class);
            String schemaName = Strings.isNullOrEmpty(rootConfig.getSchemaName()) ? DefaultSchema.LOGIC_NAME : rootConfig.getSchemaName();
            YamlModeConfiguration yamlModeConfig = rootConfig.getMode();
            yamlModeConfig.getRepository().getProps().setProperty("server-lists", serverLists);
            ModeConfiguration modeConfig = new ModeConfigurationYamlSwapper().swapToObject(rootConfig.getMode());
            if (rootConfig.getRules().isEmpty() || dataSourceMap.isEmpty()) {
                return new ShardingSphereDataSource(schemaName, modeConfig);
            } else {
                Properties properties = rootConfig.getProps();
                return new ShardingSphereDataSource(schemaName, modeConfig, dataSourceMap, new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(rootConfig.getRules()), properties);
            }
        } catch (final SQLException | IOException ex) {
            throw new RuntimeException(ex);
        }
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
