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

package org.apache.shardingsphere.driver.jdbc.core.datasource;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.driver.jdbc.adapter.AbstractDataSourceAdapter;
import org.apache.shardingsphere.driver.state.DriverStateContext;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceProvidedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.scope.GlobalRuleConfiguration;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaDataBuilder;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.builder.ContextManagerBuilder;
import org.apache.shardingsphere.mode.manager.builder.ContextManagerBuilderParameter;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * ShardingSphere data source.
 */
@Slf4j
public final class ShardingSphereDataSource extends AbstractDataSourceAdapter implements AutoCloseable {
    
    private final String databaseName;
    
    private final ContextManager contextManager;
    
    public ShardingSphereDataSource(final String databaseName, final ModeConfiguration modeConfig) throws SQLException {
        this(databaseName, modeConfig, new LinkedHashMap<>(), new LinkedList<>(), new Properties());
    }
    
    public ShardingSphereDataSource(final String databaseName, final ModeConfiguration modeConfig, final Map<String, DataSource> dataSourceMap,
                                    final Collection<RuleConfiguration> ruleConfigs, final Properties props) throws SQLException {
        this.databaseName = databaseName;
        contextManager = createContextManager(modeConfig, dataSourceMap, ruleConfigs, null == props ? new Properties() : props);
        printDriverInstanceId(contextManager);
    }
    
    private ContextManager createContextManager(final ModeConfiguration modeConfig, final Map<String, DataSource> dataSourceMap,
                                                final Collection<RuleConfiguration> ruleConfigs, final Properties props) throws SQLException {
        InstanceMetaData instanceMetaData = TypedSPILoader.getService(InstanceMetaDataBuilder.class, "JDBC").build(-1, databaseName);
        Collection<RuleConfiguration> globalRuleConfigs = ruleConfigs.stream().filter(GlobalRuleConfiguration.class::isInstance).collect(Collectors.toList());
        Collection<RuleConfiguration> databaseRuleConfigs = new LinkedList<>(ruleConfigs);
        databaseRuleConfigs.removeAll(globalRuleConfigs);
        ContextManagerBuilderParameter param = new ContextManagerBuilderParameter(modeConfig, Collections.singletonMap(databaseName,
                new DataSourceProvidedDatabaseConfiguration(dataSourceMap, databaseRuleConfigs)), Collections.emptyMap(), globalRuleConfigs, props, Collections.emptyList(), instanceMetaData);
        return TypedSPILoader.getService(ContextManagerBuilder.class, null == modeConfig ? null : modeConfig.getType()).build(param, new EventBusContext());
    }
    
    private void printDriverInstanceId(final ContextManager contextManager) {
        log.info("ShardingSphere-JDBC {} mode started successfully.", contextManager.getComputeNodeInstanceContext().getModeConfiguration().getType());
        InstanceMetaData instanceMetaData = contextManager.getComputeNodeInstanceContext().getInstance().getMetaData();
        log.info("Instance id: {}, IP: {}", instanceMetaData.getId(), instanceMetaData.getIp());
    }
    
    @HighFrequencyInvocation(canBeCached = true)
    @Override
    public Connection getConnection() {
        return DriverStateContext.getConnection(databaseName, contextManager);
    }
    
    @HighFrequencyInvocation(canBeCached = true)
    @Override
    public Connection getConnection(final String username, final String password) {
        return getConnection();
    }
    
    @Override
    public int getLoginTimeout() throws SQLException {
        Map<String, StorageUnit> storageUnits = contextManager.getStorageUnits(databaseName);
        return storageUnits.isEmpty() ? 0 : storageUnits.values().iterator().next().getDataSource().getLoginTimeout();
    }
    
    @Override
    public void setLoginTimeout(final int seconds) throws SQLException {
        for (StorageUnit each : contextManager.getStorageUnits(databaseName).values()) {
            each.getDataSource().setLoginTimeout(seconds);
        }
    }
    
    @Override
    public void close() throws SQLException {
        for (StorageUnit each : contextManager.getStorageUnits(databaseName).values()) {
            close(each.getDataSource());
        }
        contextManager.close();
    }
    
    private void close(final DataSource dataSource) throws SQLException {
        if (dataSource instanceof AutoCloseable) {
            try {
                ((AutoCloseable) dataSource).close();
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                throw new SQLException(ex);
            }
        }
    }
}
