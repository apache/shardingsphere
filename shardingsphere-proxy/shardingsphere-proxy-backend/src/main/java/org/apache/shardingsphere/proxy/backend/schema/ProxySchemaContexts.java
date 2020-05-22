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

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import lombok.Getter;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypes;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.executor.sql.ConnectionMode;
import org.apache.shardingsphere.infra.log.ConfigurationLogger;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.StatusContainedRule;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceNameDisabledEvent;
import org.apache.shardingsphere.kernal.context.SchemaContext;
import org.apache.shardingsphere.kernal.context.SchemaContexts;
import org.apache.shardingsphere.kernal.context.SchemaContextsBuilder;
import org.apache.shardingsphere.kernal.context.schema.DataSourceParameter;
import org.apache.shardingsphere.orchestration.core.common.event.AuthenticationChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.DataSourceChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.PropertiesChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.RuleConfigurationsChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.SchemaAddedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.SchemaDeletedEvent;
import org.apache.shardingsphere.orchestration.core.common.eventbus.ShardingOrchestrationEventBus;
import org.apache.shardingsphere.orchestration.core.metadatacenter.event.MetaDataChangedEvent;
import org.apache.shardingsphere.orchestration.core.registrycenter.event.CircuitStateChangedEvent;
import org.apache.shardingsphere.orchestration.core.registrycenter.event.DisabledStateChangedEvent;
import org.apache.shardingsphere.orchestration.core.registrycenter.schema.OrchestrationSchema;
import org.apache.shardingsphere.proxy.backend.BackendDataSource;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.datasource.JDBCBackendDataSourceFactory;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.datasource.JDBCRawBackendDataSourceFactory;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.recognizer.JDBCDriverURLRecognizerEngine;
import org.apache.shardingsphere.proxy.backend.util.DataSourceConverter;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.spi.ShardingTransactionManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

@Getter
public final class ProxySchemaContexts {
    
    private static final ProxySchemaContexts INSTANCE = new ProxySchemaContexts();
    
    private final JDBCBackendDataSourceFactory dataSourceFactory = JDBCRawBackendDataSourceFactory.getInstance();
    
    private SchemaContexts schemaContexts = new SchemaContexts();
    
    private boolean isCircuitBreak;
    
    private ProxySchemaContexts() {
        ShardingOrchestrationEventBus.getInstance().register(this);
    }
    
    /**
     * Get instance of proxy schema schemas.
     *
     * @return instance of ShardingSphere schemas.
     */
    public static ProxySchemaContexts getInstance() {
        return INSTANCE;
    }
    
    /**
     * Initialize proxy schema contexts.
     *
     * @param schemaDataSources data source map
     * @param schemaRules schema rule map
     * @param authentication authentication
     * @param properties properties
     * @throws SQLException SQL exception
     */
    public void init(final Map<String, Map<String, DataSourceParameter>> schemaDataSources, final Map<String, Collection<RuleConfiguration>> schemaRules, 
                     final Authentication authentication, final Properties properties) throws SQLException {
        DatabaseType databaseType = DatabaseTypes.getActualDatabaseType(
                JDBCDriverURLRecognizerEngine.getJDBCDriverURLRecognizer(schemaDataSources.values().iterator().next().values().iterator().next().getUrl()).getDatabaseType());
        SchemaContextsBuilder schemaContextsBuilder = 
                new SchemaContextsBuilder(createDataSourcesMap(schemaDataSources), schemaDataSources, authentication, databaseType, schemaRules, properties);
        schemaContexts = schemaContextsBuilder.build();
    }
    
    private Map<String, Map<String, DataSource>> createDataSourcesMap(final Map<String, Map<String, DataSourceParameter>> schemaDataSources) {
        Map<String, Map<String, DataSource>> result = new LinkedHashMap<>();
        for (Entry<String, Map<String, DataSourceParameter>> entry : schemaDataSources.entrySet()) {
            result.put(entry.getKey(), createDataSources(entry.getValue()));
        }
        return result;
    }
    
    private Map<String, DataSource> createDataSources(final Map<String, DataSourceParameter> dataSourceParameters) {
        Map<String, DataSource> result = new LinkedHashMap<>(dataSourceParameters.size(), 1);
        for (Entry<String, DataSourceParameter> entry : dataSourceParameters.entrySet()) {
            try {
                result.put(entry.getKey(), dataSourceFactory.build(entry.getKey(), entry.getValue()));
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                throw new ShardingSphereException(String.format("Can not build data source, name is `%s`.", entry.getKey()), ex);
            }
        }
        return result;
    }
    
    /**
     * Check schema exists.
     *
     * @param schema schema
     * @return schema exists or not
     */
    public boolean schemaExists(final String schema) {
        return null != schemaContexts && schemaContexts.getSchemaContexts().containsKey(schema);
    }
    
    /**
     * Get ShardingSphere schema.
     *
     * @param schemaName schema name
     * @return ShardingSphere schema
     */
    public SchemaContext getSchema(final String schemaName) {
        return Strings.isNullOrEmpty(schemaName) ? null : schemaContexts.getSchemaContexts().get(schemaName);
    }
    
    /**
     * Get schema names.
     *
     * @return schema names
     */
    public List<String> getSchemaNames() {
        return new LinkedList<>(schemaContexts.getSchemaContexts().keySet());
    }
    
    /**
     * Renew to add new schema.
     *
     * @param schemaAddedEvent schema add changed event
     * @throws SQLException SQL exception
     */
    @Subscribe
    public synchronized void renew(final SchemaAddedEvent schemaAddedEvent) throws SQLException {
        String schemaName = schemaAddedEvent.getShardingSchemaName();
        Map<String, DataSourceParameter> dataSourceParameters = DataSourceConverter.getDataSourceParameterMap(schemaAddedEvent.getDataSourceConfigurations());
        Map<String, Map<String, DataSourceParameter>> dataSourceParametersMap = Collections.singletonMap(schemaName, dataSourceParameters);
        DatabaseType databaseType = schemaContexts.getSchemaContexts().values().iterator().next().getSchema().getDatabaseType();
        SchemaContextsBuilder schemaContextsBuilder = new SchemaContextsBuilder(createDataSourcesMap(dataSourceParametersMap), dataSourceParametersMap, 
                schemaContexts.getAuthentication(), databaseType, Collections.singletonMap(schemaName, schemaAddedEvent.getRuleConfigurations()), 
                schemaContexts.getProperties().getProps());
        schemaContexts.getSchemaContexts().put(schemaName, schemaContextsBuilder.build().getSchemaContexts().get(schemaName));
    }
    
    /**
     * Renew to delete new schema.
     *
     * @param schemaDeletedEvent schema delete changed event
     */
    @Subscribe
    public synchronized void renew(final SchemaDeletedEvent schemaDeletedEvent) {
        schemaContexts.getSchemaContexts().remove(schemaDeletedEvent.getShardingSchemaName());
    }
    
    /**
     * Renew properties.
     *
     * @param event properties changed event
     */
    @Subscribe
    public synchronized void renew(final PropertiesChangedEvent event) {
        ConfigurationLogger.log(event.getProps());
        schemaContexts.setProperties(new ConfigurationProperties(event.getProps()));
    }
    
    /**
     * Renew authentication.
     *
     * @param event authentication changed event
     */
    @Subscribe
    public synchronized void renew(final AuthenticationChangedEvent event) {
        ConfigurationLogger.log(event.getAuthentication());
        schemaContexts.setAuthentication(event.getAuthentication());
    }
    
    /**
     * Renew circuit breaker state.
     *
     * @param event circuit state changed event
     */
    @Subscribe
    public synchronized void renew(final CircuitStateChangedEvent event) {
        isCircuitBreak = event.isCircuitBreak();
    }
    
    /**
     * Renew meta data of the schema.
     *
     * @param event meta data changed event.
     */
    @Subscribe
    public synchronized void renew(final MetaDataChangedEvent event) {
        for (String each : event.getSchemaNames()) {
            ShardingSphereMetaData oldMetaData = schemaContexts.getSchemaContexts().get(each).getSchema().getMetaData();
            schemaContexts.getSchemaContexts().get(each).getSchema().setMetaData(new ShardingSphereMetaData(oldMetaData.getDataSources(), event.getRuleSchemaMetaData()));
        }
    }
    
    /**
     * Renew rule configurations.
     *
     * @param ruleConfigurationsChangedEvent rule configurations changed event.
     */
    @Subscribe
    public synchronized void renew(final RuleConfigurationsChangedEvent ruleConfigurationsChangedEvent) {
        schemaContexts.getSchemaContexts().get(ruleConfigurationsChangedEvent.getShardingSchemaName()).getSchema().renew(ruleConfigurationsChangedEvent.getRuleConfigurations());
    }
    
    /**
     * Renew disabled data source names.
     *
     * @param disabledStateChangedEvent disabled state changed event
     */
    @Subscribe
    public synchronized void renew(final DisabledStateChangedEvent disabledStateChangedEvent) {
        OrchestrationSchema orchestrationSchema = disabledStateChangedEvent.getOrchestrationSchema();
        Collection<ShardingSphereRule> rules = schemaContexts.getSchemaContexts().get(orchestrationSchema.getSchemaName()).getSchema().getRules();
        for (ShardingSphereRule each : rules) {
            if (each instanceof StatusContainedRule) {
                ((StatusContainedRule) each).updateRuleStatus(new DataSourceNameDisabledEvent(orchestrationSchema.getDataSourceName(), disabledStateChangedEvent.isDisabled()));
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
        Map<String, DataSourceParameter> newDataSourceParameters = DataSourceConverter.getDataSourceParameterMap(dataSourceChangedEvent.getDataSourceConfigurations());
        renew(dataSourceChangedEvent.getShardingSchemaName(), newDataSourceParameters);
    }
    
    private void renew(final String schemaName, final Map<String, DataSourceParameter> newDataSourceParameters) throws Exception {
        SchemaContext schemaContext = schemaContexts.getSchemaContexts().get(schemaName);
        Map<String, DataSourceParameter> oldDataSourceParameters = schemaContext.getSchema().getDataSourceParameters();
        List<String> deletedDataSourceParameters = getDeletedDataSources(oldDataSourceParameters, newDataSourceParameters);
        Map<String, DataSourceParameter> modifiedDataSourceParameters = getModifiedDataSources(oldDataSourceParameters, newDataSourceParameters);
        Map<String, DataSource> newDataSources = getNewDataSources(schemaContext.getSchema().getDataSources(), 
                        deletedDataSourceParameters, getAddedDataSourceParameters(oldDataSourceParameters, newDataSourceParameters), modifiedDataSourceParameters);
        schemaContext.getSchema().closeDataSources(deletedDataSourceParameters);
        schemaContext.getSchema().closeDataSources(modifiedDataSourceParameters.keySet());
        schemaContext.renew(newDataSourceParameters, newDataSources);
    }
    
    private synchronized List<String> getDeletedDataSources(final Map<String, DataSourceParameter> oldDataSourceParameters, final Map<String, DataSourceParameter> newDataSourceParameters) {
        List<String> result = new LinkedList<>(oldDataSourceParameters.keySet());
        result.removeAll(newDataSourceParameters.keySet());
        return result;
    }
    
    private synchronized Map<String, DataSourceParameter> getAddedDataSourceParameters(final Map<String, DataSourceParameter> oldDataSourceParameters, 
                                                                                       final Map<String, DataSourceParameter> newDataSourceParameters) {
        return Maps.filterEntries(newDataSourceParameters, input -> !oldDataSourceParameters.containsKey(input.getKey()));
    }
    
    private synchronized Map<String, DataSourceParameter> getModifiedDataSources(final Map<String, DataSourceParameter> oldDataSourceParameters, 
                                                                                 final Map<String, DataSourceParameter> newDataSourceParameters) {
        Map<String, DataSourceParameter> result = new LinkedHashMap<>();
        for (Entry<String, DataSourceParameter> entry : newDataSourceParameters.entrySet()) {
            if (isModifiedDataSource(oldDataSourceParameters, entry)) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
    
    private synchronized boolean isModifiedDataSource(final Map<String, DataSourceParameter> oldDataSourceParameters, final Entry<String, DataSourceParameter> target) {
        return oldDataSourceParameters.containsKey(target.getKey()) && !oldDataSourceParameters.get(target.getKey()).equals(target.getValue());
    }
    
    private synchronized Map<String, DataSource> getNewDataSources(final Map<String, DataSource> oldDataSources, final List<String> deletedDataSources,
                                                                   final Map<String, DataSourceParameter> addedDataSources, final Map<String, DataSourceParameter> modifiedDataSources) {
        Map<String, DataSource> result = new LinkedHashMap<>(oldDataSources);
        result.keySet().removeAll(deletedDataSources);
        result.keySet().removeAll(modifiedDataSources.keySet());
        result.putAll(createDataSources(modifiedDataSources));
        result.putAll(createDataSources(addedDataSources));
        return result;
    }
    
    private final class JDBCBackendDataSource implements BackendDataSource {
    
        /**
         * Get connection.
         *
         * @param dataSourceName data source name
         * @return connection
         * @throws SQLException SQL exception
         */
        public Connection getConnection(final String schemaName, final String dataSourceName) throws SQLException {
            return getConnections(schemaName, dataSourceName, 1, ConnectionMode.MEMORY_STRICTLY).get(0);
        }
    
        /**
         * Get connections.
         *
         * @param dataSourceName data source name
         * @param connectionSize size of connections to get
         * @param connectionMode connection mode
         * @return connections
         * @throws SQLException SQL exception
         */
        public List<Connection> getConnections(final String schemaName, final String dataSourceName, final int connectionSize, final ConnectionMode connectionMode) throws SQLException {
            return getConnections(schemaName, dataSourceName, connectionSize, connectionMode, TransactionType.LOCAL);
        }
    
        /**
         * Get connections.
         *
         * @param dataSourceName data source name
         * @param connectionSize size of connections to be get
         * @param connectionMode connection mode
         * @param transactionType transaction type
         * @return connections
         * @throws SQLException SQL exception
         */
        @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
        public List<Connection> getConnections(final String schemaName, final String dataSourceName, 
                                               final int connectionSize, final ConnectionMode connectionMode, final TransactionType transactionType) throws SQLException {
            DataSource dataSource = schemaContexts.getSchemaContexts().get(schemaName).getSchema().getDataSources().get(dataSourceName);
            if (1 == connectionSize) {
                return Collections.singletonList(createConnection(schemaName, dataSourceName, dataSource, transactionType));
            }
            if (ConnectionMode.CONNECTION_STRICTLY == connectionMode) {
                return createConnections(schemaName, dataSourceName, dataSource, connectionSize, transactionType);
            }
            synchronized (dataSource) {
                return createConnections(schemaName, dataSourceName, dataSource, connectionSize, transactionType);
            }
        }
    
        private List<Connection> createConnections(final String schemaName, final String dataSourceName, 
                                                   final DataSource dataSource, final int connectionSize, final TransactionType transactionType) throws SQLException {
            List<Connection> result = new ArrayList<>(connectionSize);
            for (int i = 0; i < connectionSize; i++) {
                try {
                    result.add(createConnection(schemaName, dataSourceName, dataSource, transactionType));
                } catch (final SQLException ex) {
                    for (Connection each : result) {
                        each.close();
                    }
                    throw new SQLException(String.format("Could't get %d connections one time, partition succeed connection(%d) have released!", connectionSize, result.size()), ex);
                }
            }
            return result;
        }
    
        private Connection createConnection(final String schemaName, final String dataSourceName, final DataSource dataSource, final TransactionType transactionType) throws SQLException {
            ShardingTransactionManager shardingTransactionManager = 
                    schemaContexts.getSchemaContexts().get(schemaName).getRuntimeContext().getTransactionManagerEngine().getTransactionManager(transactionType);
            return isInShardingTransaction(shardingTransactionManager) ? shardingTransactionManager.getConnection(dataSourceName) : dataSource.getConnection();
        }
    
        private boolean isInShardingTransaction(final ShardingTransactionManager shardingTransactionManager) {
            return null != shardingTransactionManager && shardingTransactionManager.isInTransaction();
        }
    
        /**
         * Renew data source.
         *
         * @param dataSourceParameters data source parameters
         * @throws Exception exception
         */
        public void renew(final String schemaName, final Map<String, DataSourceParameter> dataSourceParameters) throws Exception {
            ProxySchemaContexts.this.renew(schemaName, dataSourceParameters);
        }
    }
}
