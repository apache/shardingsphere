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

package org.apache.shardingsphere.driver.orchestration.internal.datasource;

import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.cluster.configuration.config.ClusterConfiguration;
import org.apache.shardingsphere.control.panel.spi.FacadeConfiguration;
import org.apache.shardingsphere.control.panel.spi.engine.ControlPanelFacadeEngine;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.driver.jdbc.unsupported.AbstractUnsupportedOperationDataSource;
import org.apache.shardingsphere.driver.orchestration.internal.util.DataSourceConverter;
import org.apache.shardingsphere.infra.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.metadata.schema.RuleSchemaMetaData;
import org.apache.shardingsphere.orchestration.core.common.eventbus.ShardingOrchestrationEventBus;
import org.apache.shardingsphere.orchestration.core.facade.ShardingOrchestrationFacade;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Abstract orchestration data source.
 */
public abstract class AbstractOrchestrationDataSource extends AbstractUnsupportedOperationDataSource implements AutoCloseable {
    
    @Getter
    @Setter
    private PrintWriter logWriter = new PrintWriter(System.out);
    
    @Getter(AccessLevel.PROTECTED)
    private final ShardingOrchestrationFacade shardingOrchestrationFacade;
    
    @Getter(AccessLevel.PROTECTED)
    private final Map<String, DataSourceConfiguration> dataSourceConfigurations = new LinkedHashMap<>();
    
    public AbstractOrchestrationDataSource(final ShardingOrchestrationFacade shardingOrchestrationFacade) {
        this.shardingOrchestrationFacade = shardingOrchestrationFacade;
        ShardingOrchestrationEventBus.getInstance().register(this);
    }
    
    protected abstract DataSource getDataSource();
    
    @Override
    public final Connection getConnection(final String username, final String password) throws SQLException {
        return getConnection();
    }
    
    @Override
    public final Logger getParentLogger() {
        return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    }
    
    @Override
    public final void close() throws Exception {
        ((ShardingSphereDataSource) getDataSource()).close();
        shardingOrchestrationFacade.close();
    }
    
    protected final void initShardingOrchestrationFacade() {
        shardingOrchestrationFacade.init();
        dataSourceConfigurations.putAll(shardingOrchestrationFacade.getConfigCenter().loadDataSourceConfigurations(DefaultSchema.LOGIC_NAME));
    }
    
    protected final void initShardingOrchestrationFacade(final ClusterConfiguration clusterConfiguration) {
        shardingOrchestrationFacade.init();
        shardingOrchestrationFacade.initClusterConfiguration(clusterConfiguration);
        dataSourceConfigurations.putAll(shardingOrchestrationFacade.getConfigCenter().loadDataSourceConfigurations(DefaultSchema.LOGIC_NAME));
    }
    
    protected final void initShardingOrchestrationFacade(
            final Map<String, Map<String, DataSourceConfiguration>> dataSourceConfigurations,
            final Map<String, Collection<RuleConfiguration>> schemaRules, final Properties props) {
        shardingOrchestrationFacade.init(dataSourceConfigurations, schemaRules, null, props);
        this.dataSourceConfigurations.putAll(dataSourceConfigurations.get(DefaultSchema.LOGIC_NAME));
    }
    
    protected final void initShardingOrchestrationFacade(
            final Map<String, Map<String, DataSourceConfiguration>> dataSourceConfigurations,
            final Map<String, Collection<RuleConfiguration>> schemaRules, final Properties props, final ClusterConfiguration clusterConfiguration) {
        shardingOrchestrationFacade.init(dataSourceConfigurations, schemaRules, null, props);
        shardingOrchestrationFacade.initClusterConfiguration(clusterConfiguration);
        this.dataSourceConfigurations.putAll(dataSourceConfigurations.get(DefaultSchema.LOGIC_NAME));
    }
    
    protected final synchronized Map<String, DataSource> getChangedDataSources(final Map<String, DataSource> oldDataSources, final Map<String, DataSourceConfiguration> newDataSources) {
        Map<String, DataSource> result = new LinkedHashMap<>(oldDataSources);
        Map<String, DataSourceConfiguration> modifiedDataSources = getModifiedDataSources(newDataSources);
        result.keySet().removeAll(getDeletedDataSources(newDataSources));
        result.keySet().removeAll(modifiedDataSources.keySet());
        result.putAll(DataSourceConverter.getDataSourceMap(modifiedDataSources));
        result.putAll(DataSourceConverter.getDataSourceMap(getAddedDataSources(newDataSources)));
        return result;
    }
    
    protected final synchronized Map<String, DataSourceConfiguration> getModifiedDataSources(final Map<String, DataSourceConfiguration> dataSourceConfigurations) {
        return dataSourceConfigurations.entrySet().stream().filter(this::isModifiedDataSource).collect(Collectors.toMap(Entry::getKey, Entry::getValue, (key, repeatKey) -> key, LinkedHashMap::new));
    }
    
    private synchronized boolean isModifiedDataSource(final Entry<String, DataSourceConfiguration> dataSourceNameAndConfig) {
        return dataSourceConfigurations.containsKey(dataSourceNameAndConfig.getKey()) && !dataSourceConfigurations.get(dataSourceNameAndConfig.getKey()).equals(dataSourceNameAndConfig.getValue());
    }
    
    protected final synchronized List<String> getDeletedDataSources(final Map<String, DataSourceConfiguration> dataSourceConfigurations) {
        List<String> result = new LinkedList<>(this.dataSourceConfigurations.keySet());
        result.removeAll(dataSourceConfigurations.keySet());
        return result;
    }
    
    private synchronized Map<String, DataSourceConfiguration> getAddedDataSources(final Map<String, DataSourceConfiguration> dataSourceConfigurations) {
        return Maps.filterEntries(dataSourceConfigurations, input -> !AbstractOrchestrationDataSource.this.dataSourceConfigurations.containsKey(input.getKey()));
    }
    
    protected final void persistMetaData(final RuleSchemaMetaData ruleSchemaMetaData) {
        ShardingOrchestrationFacade.getInstance().getMetaDataCenter().persistMetaDataCenterNode(DefaultSchema.LOGIC_NAME, ruleSchemaMetaData);
    }
    
    protected final void initCluster() {
        ClusterConfiguration clusterConfiguration = shardingOrchestrationFacade.getConfigCenter().loadClusterConfiguration();
        if (null != clusterConfiguration && null != clusterConfiguration.getHeartbeat()) {
            List<FacadeConfiguration> facadeConfigurations = new LinkedList<>();
            facadeConfigurations.add(clusterConfiguration);
            new ControlPanelFacadeEngine().init(facadeConfigurations);
        }
    }
}
