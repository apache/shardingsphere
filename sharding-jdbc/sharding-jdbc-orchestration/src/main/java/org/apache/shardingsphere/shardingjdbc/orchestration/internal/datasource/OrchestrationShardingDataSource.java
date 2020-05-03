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

package org.apache.shardingsphere.shardingjdbc.orchestration.internal.datasource;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.core.rule.MasterSlaveRule;
import org.apache.shardingsphere.core.rule.builder.ConfigurationBuilder;
import org.apache.shardingsphere.core.rule.builder.RuleBuilder;
import org.apache.shardingsphere.orchestration.center.config.OrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.core.common.event.DataSourceChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.PropertiesChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.ShardingRuleChangedEvent;
import org.apache.shardingsphere.orchestration.core.configcenter.ConfigCenter;
import org.apache.shardingsphere.orchestration.core.facade.ShardingOrchestrationFacade;
import org.apache.shardingsphere.orchestration.core.metadatacenter.event.MetaDataChangedEvent;
import org.apache.shardingsphere.orchestration.core.registrycenter.event.DisabledStateChangedEvent;
import org.apache.shardingsphere.orchestration.core.registrycenter.schema.OrchestrationShardingSchema;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource;
import org.apache.shardingsphere.shardingjdbc.orchestration.internal.util.DataSourceConverter;
import org.apache.shardingsphere.underlying.common.config.DataSourceConfiguration;
import org.apache.shardingsphere.underlying.common.config.RuleConfiguration;
import org.apache.shardingsphere.underlying.common.database.DefaultSchema;
import org.apache.shardingsphere.underlying.common.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.underlying.common.rule.ShardingSphereRule;

import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Orchestration sharding data source.
 */
@Getter(AccessLevel.PROTECTED)
public class OrchestrationShardingDataSource extends AbstractOrchestrationDataSource {
    
    private ShardingDataSource dataSource;
    
    public OrchestrationShardingDataSource(final OrchestrationConfiguration orchestrationConfig) throws SQLException {
        super(new ShardingOrchestrationFacade(orchestrationConfig, Collections.singletonList(DefaultSchema.LOGIC_NAME)));
        ConfigCenter configService = getShardingOrchestrationFacade().getConfigCenter();
        ShardingRuleConfiguration shardingRuleConfig = configService.loadShardingRuleConfiguration(DefaultSchema.LOGIC_NAME);
        Preconditions.checkState(null != shardingRuleConfig && !shardingRuleConfig.getTableRuleConfigs().isEmpty(), "Missing the sharding rule configuration on registry center");
        Map<String, DataSourceConfiguration> dataSourceConfigurations = configService.loadDataSourceConfigurations(DefaultSchema.LOGIC_NAME);
        dataSource = new ShardingDataSource(DataSourceConverter.getDataSourceMap(dataSourceConfigurations),
                RuleBuilder.build(dataSourceConfigurations.keySet(), ConfigurationBuilder.buildSharding(shardingRuleConfig)), configService.loadProperties());
        initShardingOrchestrationFacade();
        persistMetaData(dataSource.getRuntimeContext().getMetaData().getSchema());
    }
    
    public OrchestrationShardingDataSource(final ShardingDataSource shardingDataSource, final OrchestrationConfiguration orchestrationConfig) {
        super(new ShardingOrchestrationFacade(orchestrationConfig, Collections.singletonList(DefaultSchema.LOGIC_NAME)));
        dataSource = shardingDataSource;
        initShardingOrchestrationFacade(Collections.singletonMap(DefaultSchema.LOGIC_NAME, DataSourceConverter.getDataSourceConfigurationMap(dataSource.getDataSourceMap())),
                getRuleConfigurationMap(), dataSource.getRuntimeContext().getProperties().getProps());
        persistMetaData(dataSource.getRuntimeContext().getMetaData().getSchema());
    }
    
    private Map<String, RuleConfiguration> getRuleConfigurationMap() {
        Map<String, RuleConfiguration> result = new LinkedHashMap<>(1, 1);
        result.put(DefaultSchema.LOGIC_NAME, dataSource.getRuntimeContext().getRules().iterator().next().getRuleConfiguration());
        return result;
    }
    
    /**
     * Renew meta data of the schema.
     *
     * @param event meta data changed event.
     */
    @Subscribe
    public final synchronized void renew(final MetaDataChangedEvent event) {
        for (String each : event.getSchemaNames()) {
            if (DefaultSchema.LOGIC_NAME.equals(each)) {
                dataSource.getRuntimeContext().setMetaData(new ShardingSphereMetaData(dataSource.getRuntimeContext().getMetaData().getDataSources(), event.getRuleSchemaMetaData()));
            }
        }
    }
    
    /**
     * Renew sharding rule.
     *
     * @param shardingRuleChangedEvent sharding rule changed event
     */
    @Subscribe
    @SneakyThrows
    public final synchronized void renew(final ShardingRuleChangedEvent shardingRuleChangedEvent) {
        dataSource = new ShardingDataSource(dataSource.getDataSourceMap(), 
                RuleBuilder.build(dataSource.getDataSourceMap().keySet(), shardingRuleChangedEvent.getRuleConfigurations()), dataSource.getRuntimeContext().getProperties().getProps());
    }
    
    /**
     * Renew sharding data source.
     *
     * @param dataSourceChangedEvent data source changed event
     */
    @Subscribe
    @SneakyThrows
    public final synchronized void renew(final DataSourceChangedEvent dataSourceChangedEvent) {
        Map<String, DataSourceConfiguration> dataSourceConfigurations = dataSourceChangedEvent.getDataSourceConfigurations();
        dataSource.close(getDeletedDataSources(dataSourceConfigurations));
        dataSource.close(getModifiedDataSources(dataSourceConfigurations).keySet());
        dataSource = new ShardingDataSource(getChangedDataSources(dataSource.getDataSourceMap(), dataSourceConfigurations), 
                dataSource.getRuntimeContext().getRules(), dataSource.getRuntimeContext().getProperties().getProps());
        getDataSourceConfigurations().clear();
        getDataSourceConfigurations().putAll(dataSourceConfigurations);
    }
    
    /**
     * Renew properties.
     *
     * @param propertiesChangedEvent properties changed event
     */
    @SneakyThrows
    @Subscribe
    public final synchronized void renew(final PropertiesChangedEvent propertiesChangedEvent) {
        dataSource = new ShardingDataSource(dataSource.getDataSourceMap(), dataSource.getRuntimeContext().getRules(), propertiesChangedEvent.getProps());
    }
    
    /**
     * Renew disabled data source names.
     *
     * @param disabledStateChangedEvent disabled state changed event
     */
    @Subscribe
    public synchronized void renew(final DisabledStateChangedEvent disabledStateChangedEvent) {
        OrchestrationShardingSchema shardingSchema = disabledStateChangedEvent.getShardingSchema();
        if (DefaultSchema.LOGIC_NAME.equals(shardingSchema.getSchemaName())) {
            for (ShardingSphereRule each : dataSource.getRuntimeContext().getRules()) {
                if (each instanceof MasterSlaveRule) {
                    ((MasterSlaveRule) each).updateDisabledDataSourceNames(shardingSchema.getDataSourceName(), disabledStateChangedEvent.isDisabled());
                }
            }
        }
    }
}
