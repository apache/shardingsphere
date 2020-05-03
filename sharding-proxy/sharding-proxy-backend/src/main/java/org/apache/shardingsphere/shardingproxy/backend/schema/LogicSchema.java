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

package org.apache.shardingsphere.shardingproxy.backend.schema;

import com.google.common.eventbus.Subscribe;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.orchestration.core.common.event.DataSourceChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.eventbus.ShardingOrchestrationEventBus;
import org.apache.shardingsphere.orchestration.core.facade.ShardingOrchestrationFacade;
import org.apache.shardingsphere.orchestration.core.metadatacenter.event.MetaDataChangedEvent;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.datasource.JDBCBackendDataSource;
import org.apache.shardingsphere.shardingproxy.config.yaml.YamlDataSourceParameter;
import org.apache.shardingsphere.shardingproxy.context.ShardingProxyContext;
import org.apache.shardingsphere.shardingproxy.util.DataSourceConverter;
import org.apache.shardingsphere.sql.parser.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.SQLParserEngineFactory;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.underlying.common.config.DatabaseAccessConfiguration;
import org.apache.shardingsphere.underlying.common.database.type.DatabaseType;
import org.apache.shardingsphere.underlying.common.database.type.DatabaseTypes;
import org.apache.shardingsphere.underlying.common.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.underlying.common.metadata.datasource.DataSourceMetas;
import org.apache.shardingsphere.underlying.common.metadata.schema.RuleSchemaMetaData;
import org.apache.shardingsphere.underlying.common.metadata.schema.RuleSchemaMetaDataLoader;
import org.apache.shardingsphere.underlying.common.rule.ShardingSphereRule;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Logic schema.
 */
@Getter
public abstract class LogicSchema {
    
    private final String name;
    
    private final SQLParserEngine sqlParserEngine;
    
    @Setter
    private Collection<ShardingSphereRule> rules;
    
    private JDBCBackendDataSource backendDataSource;
    
    private ShardingSphereMetaData metaData;
    
    public LogicSchema(final String name, final Map<String, YamlDataSourceParameter> dataSources, final Collection<ShardingSphereRule> rules) throws SQLException {
        this.name = name;
        this.rules = rules;
        sqlParserEngine = SQLParserEngineFactory.getSQLParserEngine(DatabaseTypes.getTrunkDatabaseTypeName(LogicSchemas.getInstance().getDatabaseType()));
        backendDataSource = new JDBCBackendDataSource(dataSources);
        metaData = loadOrCreateMetaData(name, rules);
        ShardingOrchestrationEventBus.getInstance().register(this);
    }
    
    private ShardingSphereMetaData loadOrCreateMetaData(final String name, final Collection<ShardingSphereRule> rules) throws SQLException {
        boolean isOverwrite = null != ShardingOrchestrationFacade.getInstance() && ShardingOrchestrationFacade.getInstance().isOverwrite();
        DatabaseType databaseType = LogicSchemas.getInstance().getDatabaseType();
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
        return new RuleSchemaMetaDataLoader(rules).load(databaseType, getBackendDataSource().getDataSources(), ShardingProxyContext.getInstance().getProperties());
    }
    
    private Map<String, DatabaseAccessConfiguration> getDatabaseAccessConfigurationMap() {
        return backendDataSource.getDataSourceParameters().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> new DatabaseAccessConfiguration(entry.getValue().getUrl(), null, null)));
    }
    
    /**
     * Get data source parameters.
     * 
     * @return data source parameters
     */
    public Map<String, YamlDataSourceParameter> getDataSources() {
        return backendDataSource.getDataSourceParameters();
    }
    
    /**
     * Renew meta data of the schema.
     *
     * @param event meta data changed event.
     */
    @Subscribe
    public final synchronized void renew(final MetaDataChangedEvent event) {
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
    public final synchronized void renew(final DataSourceChangedEvent dataSourceChangedEvent) throws Exception {
        if (name.equals(dataSourceChangedEvent.getShardingSchemaName())) {
            backendDataSource.renew(DataSourceConverter.getDataSourceParameterMap(dataSourceChangedEvent.getDataSourceConfigurations()));
        }
    }
    
    /**
     * Refresh table meta data.
     * 
     * @param sqlStatementContext SQL statement context
     * @throws SQLException SQL exception
     */
    public void refreshTableMetaData(final SQLStatementContext sqlStatementContext) throws SQLException {
    }
}
