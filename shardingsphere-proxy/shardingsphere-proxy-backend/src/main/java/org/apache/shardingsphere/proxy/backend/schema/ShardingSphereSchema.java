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

package org.apache.shardingsphere.proxy.backend.schema;

import com.google.common.eventbus.Subscribe;
import lombok.Getter;
import org.apache.shardingsphere.infra.config.DatabaseAccessConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypes;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorKernel;
import org.apache.shardingsphere.infra.log.ConfigurationLogger;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.datasource.DataSourceMetas;
import org.apache.shardingsphere.infra.metadata.refresh.MetaDataRefreshStrategy;
import org.apache.shardingsphere.infra.metadata.refresh.MetaDataRefreshStrategyFactory;
import org.apache.shardingsphere.infra.metadata.schema.RuleSchemaMetaData;
import org.apache.shardingsphere.infra.metadata.schema.RuleSchemaMetaDataLoader;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.ShardingSphereRulesBuilder;
import org.apache.shardingsphere.infra.rule.StatusContainedRule;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceNameDisabledEvent;
import org.apache.shardingsphere.kernal.context.schema.DataSourceParameter;
import org.apache.shardingsphere.orchestration.core.common.event.DataSourceChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.RuleConfigurationsChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.eventbus.ShardingOrchestrationEventBus;
import org.apache.shardingsphere.orchestration.core.facade.ShardingOrchestrationFacade;
import org.apache.shardingsphere.orchestration.core.metadatacenter.event.MetaDataChangedEvent;
import org.apache.shardingsphere.orchestration.core.registrycenter.event.DisabledStateChangedEvent;
import org.apache.shardingsphere.orchestration.core.registrycenter.schema.OrchestrationSchema;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.datasource.JDBCBackendDataSource;
import org.apache.shardingsphere.proxy.backend.executor.BackendExecutorContext;
import org.apache.shardingsphere.proxy.backend.util.DataSourceConverter;
import org.apache.shardingsphere.proxy.context.ShardingSphereProxyContext;
import org.apache.shardingsphere.sql.parser.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.SQLParserEngineFactory;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetaData;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ShardingSphere schema.
 */
@Getter
public final class ShardingSphereSchema {
    
    private final String name;
    
    private final SQLParserEngine sqlParserEngine;
    
    private Collection<RuleConfiguration> configurations;
    
    private Collection<ShardingSphereRule> rules;
    
    private JDBCBackendDataSource backendDataSource;
    
    private ShardingSphereMetaData metaData;
    
    public ShardingSphereSchema(final String name, final Map<String, DataSourceParameter> dataSources, final Collection<RuleConfiguration> configurations) throws SQLException {
        this.name = name;
        this.configurations = configurations;
        this.rules = ShardingSphereRulesBuilder.build(configurations, dataSources.keySet());
        sqlParserEngine = SQLParserEngineFactory.getSQLParserEngine(DatabaseTypes.getTrunkDatabaseTypeName(ShardingSphereSchemas.getInstance().getDatabaseType()));
        backendDataSource = new JDBCBackendDataSource(dataSources);
        metaData = loadOrCreateMetaData(name, rules);
        ShardingOrchestrationEventBus.getInstance().register(this);
    }
    
    private ShardingSphereMetaData loadOrCreateMetaData(final String name, final Collection<ShardingSphereRule> rules) throws SQLException {
        boolean isOverwrite = null != ShardingOrchestrationFacade.getInstance() && ShardingOrchestrationFacade.getInstance().isOverwrite();
        DatabaseType databaseType = ShardingSphereSchemas.getInstance().getDatabaseType();
        DataSourceMetas dataSourceMetas = new DataSourceMetas(databaseType, getDatabaseAccessConfigurationMap());
        RuleSchemaMetaData ruleSchemaMetaData;
        if (null == ShardingOrchestrationFacade.getInstance()) {
            return new ShardingSphereMetaData(dataSourceMetas, loadRuleSchemaMetaData(databaseType, rules));
        }
        if (isOverwrite) {
            ruleSchemaMetaData = loadRuleSchemaMetaData(databaseType, rules);
            ShardingOrchestrationFacade.getInstance().getMetaDataCenter().persistMetaDataCenterNode(name, ruleSchemaMetaData);
        } else {
            ruleSchemaMetaData = ShardingOrchestrationFacade.getInstance().getMetaDataCenter().loadRuleSchemaMetaData(name).orElse(loadRuleSchemaMetaData(databaseType, rules));
        }
        return new ShardingSphereMetaData(dataSourceMetas, ruleSchemaMetaData);
    }
    
    private RuleSchemaMetaData loadRuleSchemaMetaData(final DatabaseType databaseType, final Collection<ShardingSphereRule> rules) throws SQLException {
        ExecutorKernel executorKernel = BackendExecutorContext.getInstance().getExecutorKernel();
        return new RuleSchemaMetaDataLoader(rules).load(databaseType, getBackendDataSource().getDataSources(), ShardingSphereProxyContext.getInstance().getProperties(),
                executorKernel.getExecutorService().getExecutorService());
    }
    
    private Map<String, DatabaseAccessConfiguration> getDatabaseAccessConfigurationMap() {
        return backendDataSource.getDataSourceParameters().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> new DatabaseAccessConfiguration(entry.getValue().getUrl(), null, null)));
    }
    
    /**
     * Set configurations.
     * 
     * @param configurations rule configurations
     */
    public void setConfigurations(final Collection<RuleConfiguration> configurations) {
        this.configurations = configurations;
        rules = ShardingSphereRulesBuilder.build(configurations, backendDataSource.getDataSourceParameters().keySet());
    }
    
    /**
     * Get data source parameters.
     * 
     * @return data source parameters
     */
    public Map<String, DataSourceParameter> getDataSources() {
        return backendDataSource.getDataSourceParameters();
    }
    
    /**
     * Renew meta data of the schema.
     *
     * @param event meta data changed event.
     */
    @Subscribe
    public synchronized void renew(final MetaDataChangedEvent event) {
        for (String each : event.getSchemaNames()) {
            if (name.equals(each)) {
                metaData = new ShardingSphereMetaData(metaData.getDataSources(), event.getRuleSchemaMetaData());
            }
        }
    }
    
    /**
     * Renew data source configuration.
     *
     * @param dataSourceChangedEvent data source changed event.
     * @throws Exception exception
     */
    @Subscribe
    public synchronized void renew(final DataSourceChangedEvent dataSourceChangedEvent) throws Exception {
        if (name.equals(dataSourceChangedEvent.getShardingSchemaName())) {
            backendDataSource.renew(DataSourceConverter.getDataSourceParameterMap(dataSourceChangedEvent.getDataSourceConfigurations()));
        }
    }
    
    /**
     * Renew rule configurations.
     *
     * @param ruleConfigurationsChangedEvent rule configurations changed event.
     */
    @Subscribe
    public synchronized void renew(final RuleConfigurationsChangedEvent ruleConfigurationsChangedEvent) {
        if (getName().equals(ruleConfigurationsChangedEvent.getShardingSchemaName())) {
            ConfigurationLogger.log(ruleConfigurationsChangedEvent.getRuleConfigurations());
            setConfigurations(ruleConfigurationsChangedEvent.getRuleConfigurations());
        }
    }
    
    /**
     * Renew disabled data source names.
     *
     * @param disabledStateChangedEvent disabled state changed event
     */
    @Subscribe
    public synchronized void renew(final DisabledStateChangedEvent disabledStateChangedEvent) {
        OrchestrationSchema orchestrationSchema = disabledStateChangedEvent.getOrchestrationSchema();
        if (getName().equals(orchestrationSchema.getSchemaName())) {
            for (ShardingSphereRule each : getRules()) {
                if (each instanceof StatusContainedRule) {
                    ((StatusContainedRule) each).updateRuleStatus(new DataSourceNameDisabledEvent(orchestrationSchema.getDataSourceName(), disabledStateChangedEvent.isDisabled()));
                }
            }
        }
    }
    
    /**
     * Refresh table meta data.
     * 
     * @param sqlStatementContext SQL statement context
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("unchecked")
    public void refreshTableMetaData(final SQLStatementContext sqlStatementContext) throws SQLException {
        if (null == sqlStatementContext) {
            return;
        }
        Optional<MetaDataRefreshStrategy> refreshStrategy = MetaDataRefreshStrategyFactory.newInstance(sqlStatementContext);
        if (refreshStrategy.isPresent()) {
            refreshStrategy.get().refreshMetaData(
                    getMetaData(), ShardingSphereSchemas.getInstance().getDatabaseType(), getBackendDataSource().getDataSources(), sqlStatementContext, this::loadTableMetaData);
            if (null != ShardingOrchestrationFacade.getInstance()) {
                ShardingOrchestrationFacade.getInstance().getMetaDataCenter().persistMetaDataCenterNode(getName(), getMetaData().getSchema());
            }
        }
    }
    
    private Optional<TableMetaData> loadTableMetaData(final String tableName) throws SQLException {
        RuleSchemaMetaDataLoader loader = new RuleSchemaMetaDataLoader(getRules());
        return loader.load(ShardingSphereSchemas.getInstance().getDatabaseType(), getBackendDataSource().getDataSources(), tableName, ShardingSphereProxyContext.getInstance().getProperties());
    }
}
