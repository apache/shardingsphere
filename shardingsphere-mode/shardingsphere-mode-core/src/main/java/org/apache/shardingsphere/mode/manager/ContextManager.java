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

package org.apache.shardingsphere.mode.manager;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConverter;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.loader.SchemaLoader;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.schema.SchemaRulesBuilder;
import org.apache.shardingsphere.infra.state.StateContext;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.MetaDataContextsBuilder;
import org.apache.shardingsphere.transaction.ShardingSphereTransactionManagerEngine;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.apache.shardingsphere.transaction.rule.builder.DefaultTransactionRuleConfigurationBuilder;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Context manager.
 */
@Slf4j
@Getter
public final class ContextManager implements AutoCloseable {
    
    private volatile MetaDataContexts metaDataContexts = new MetaDataContexts(null);
    
    private volatile TransactionContexts transactionContexts = new TransactionContexts();
    
    private final StateContext stateContext = new StateContext();
    
    /**
     * Initialize context manager.
     *
     * @param metaDataContexts meta data contexts
     * @param transactionContexts transaction contexts
     */
    public void init(final MetaDataContexts metaDataContexts, final TransactionContexts transactionContexts) {
        this.metaDataContexts = metaDataContexts;
        this.transactionContexts = transactionContexts;
    }
    
    /**
     * Get data source map.
     * 
     * @param schemaName schema name
     * @return data source map
     */
    public Map<String, DataSource> getDataSourceMap(final String schemaName) {
        return metaDataContexts.getMetaData(schemaName).getResource().getDataSources();
    }
    
    /**
     * Renew meta data contexts.
     *
     * @param metaDataContexts meta data contexts
     */
    public synchronized void renewMetaDataContexts(final MetaDataContexts metaDataContexts) {
        this.metaDataContexts = metaDataContexts;
    }
    
    /**
     * Renew transaction contexts.
     *
     * @param transactionContexts transaction contexts
     */
    public synchronized void renewTransactionContexts(final TransactionContexts transactionContexts) {
        this.transactionContexts = transactionContexts;
    }
    
    /**
     * Add schema.
     * 
     * @param schemaName schema name
     * @throws SQLException SQL exception                  
     */
    public void addSchema(final String schemaName) throws SQLException {
        if (metaDataContexts.getMetaDataMap().containsKey(schemaName)) {
            return;
        }
        MetaDataContexts newMetaDataContexts = buildNewMetaDataContext(schemaName);
        metaDataContexts.getOptimizerContext().getMetaData().getSchemas().put(schemaName,
                newMetaDataContexts.getOptimizerContext().getMetaData().getSchemas().get(schemaName));
        metaDataContexts.getMetaDataMap().put(schemaName, newMetaDataContexts.getMetaData(schemaName));
    }
    
    /**
     * Delete schema.
     * 
     * @param schemaName schema name
     */
    public void deleteSchema(final String schemaName) {
        if (metaDataContexts.getMetaDataMap().containsKey(schemaName)) {
            metaDataContexts.getOptimizerContext().getMetaData().getSchemas().remove(schemaName);
            metaDataContexts.getOptimizerContext().getParserContexts().remove(schemaName);
            metaDataContexts.getOptimizerContext().getPlannerContexts().remove(schemaName);
            ShardingSphereMetaData removeMetaData = metaDataContexts.getMetaDataMap().remove(schemaName);
            closeDataSources(removeMetaData);
            closeTransactionEngine(schemaName);
        }
    }
    
    /**
     * Add resource.
     *
     * @param schemaName schema name
     * @param dataSourceConfigs data source configs
     * @throws SQLException SQL exception                         
     */
    public void addResource(final String schemaName, final Map<String, DataSourceConfiguration> dataSourceConfigs) throws SQLException {
        refreshMetaDataContext(schemaName, dataSourceConfigs);
    }
    
    /**
     * Alter resource.
     *
     * @param schemaName schema name
     * @param dataSourceConfigs data source configs
     * @throws SQLException SQL exception                         
     */
    public void alterResource(final String schemaName, final Map<String, DataSourceConfiguration> dataSourceConfigs) throws SQLException {
        refreshMetaDataContext(schemaName, dataSourceConfigs);
    }
    
    private void refreshMetaDataContext(final String schemaName, final Map<String, DataSourceConfiguration> dataSourceConfigs) throws SQLException {
        MetaDataContexts changedMetaDataContext = buildChangedMetaDataContext(metaDataContexts.getMetaDataMap().get(schemaName), dataSourceConfigs);
        metaDataContexts.getMetaDataMap().putAll(changedMetaDataContext.getMetaDataMap());
        metaDataContexts.getOptimizerContext().getMetaData().getSchemas().putAll(changedMetaDataContext.getOptimizerContext().getMetaData().getSchemas());
        metaDataContexts.getOptimizerContext().getParserContexts().putAll(changedMetaDataContext.getOptimizerContext().getParserContexts());
        metaDataContexts.getOptimizerContext().getPlannerContexts().putAll(changedMetaDataContext.getOptimizerContext().getPlannerContexts());
        renewTransactionContext(schemaName, metaDataContexts.getMetaData(schemaName).getResource());
    }
    
    private MetaDataContexts buildChangedMetaDataContext(final ShardingSphereMetaData originalMetaData, final Map<String, DataSourceConfiguration> addedDataSourceConfigs) throws SQLException {
        Map<String, DataSource> dataSourceMap = new HashMap<>(originalMetaData.getResource().getDataSources());
        dataSourceMap.putAll(DataSourceConverter.getDataSourceMap(addedDataSourceConfigs));
        Map<String, Map<String, DataSource>> dataSourcesMap = Collections.singletonMap(originalMetaData.getName(), dataSourceMap);
        Map<String, Collection<RuleConfiguration>> schemaRuleConfigs = Collections.singletonMap(originalMetaData.getName(), originalMetaData.getRuleMetaData().getConfigurations());
        Properties props = metaDataContexts.getProps().getProps();
        Map<String, Collection<ShardingSphereRule>> rules = SchemaRulesBuilder.buildRules(dataSourcesMap, schemaRuleConfigs, props);
        Map<String, ShardingSphereSchema> schemas = new SchemaLoader(dataSourcesMap, schemaRuleConfigs, rules, props).load();
        return new MetaDataContextsBuilder(dataSourcesMap, schemaRuleConfigs, metaDataContexts.getGlobalRuleMetaData().getConfigurations(), schemas, rules, props)
                .build(metaDataContexts.getMetaDataPersistService().orElse(null));
    }
    
    private void renewTransactionContext(final String schemaName, final ShardingSphereResource resource) {
        closeTransactionEngine(schemaName);
        transactionContexts.getEngines().put(schemaName, createNewEngine(resource.getDatabaseType(), resource.getDataSources()));
    }
    
    private ShardingSphereTransactionManagerEngine createNewEngine(final DatabaseType databaseType, final Map<String, DataSource> dataSources) {
        ShardingSphereTransactionManagerEngine result = new ShardingSphereTransactionManagerEngine();
        result.init(databaseType, dataSources, getTransactionRule());
        return result;
    }
    
    private TransactionRule getTransactionRule() {
        Optional<TransactionRule> transactionRule = metaDataContexts.getGlobalRuleMetaData().getRules().stream()
                .filter(each -> each instanceof TransactionRule).map(each -> (TransactionRule) each).findFirst();
        return transactionRule.orElseGet(() -> new TransactionRule(new DefaultTransactionRuleConfigurationBuilder().build()));
    }
    
    private MetaDataContexts buildNewMetaDataContext(final String schemaName) throws SQLException {
        Map<String, Map<String, DataSource>> dataSourcesMap = Collections.singletonMap(schemaName, new HashMap<>());
        Map<String, Collection<RuleConfiguration>> schemaRuleConfigs = Collections.singletonMap(schemaName, new LinkedList<>());
        Properties props = metaDataContexts.getProps().getProps();
        Map<String, ShardingSphereSchema> schemas = Collections.singletonMap(schemaName, new ShardingSphereSchema());
        Map<String, Collection<ShardingSphereRule>> rules = SchemaRulesBuilder.buildRules(dataSourcesMap, schemaRuleConfigs, props);
        return new MetaDataContextsBuilder(dataSourcesMap, schemaRuleConfigs, metaDataContexts.getGlobalRuleMetaData().getConfigurations(), schemas, rules, props)
                .build(metaDataContexts.getMetaDataPersistService().orElse(null));
    }
    
    private void closeDataSources(final ShardingSphereMetaData removeMetaData) {
        if (null != removeMetaData.getResource()) {
            removeMetaData.getResource().getDataSources().values().forEach(each -> closeDataSource(removeMetaData.getResource(), each));
        }
    }
    
    private void closeDataSource(final ShardingSphereResource resource, final DataSource dataSource) {
        try {
            resource.close(dataSource);
        } catch (final SQLException ex) {
            log.error("Close data source failed", ex);
        }
    }
    
    private void closeTransactionEngine(final String schemaName) {
        ShardingSphereTransactionManagerEngine staleEngine = transactionContexts.getEngines().remove(schemaName);
        if (null != staleEngine) {
            try {
                staleEngine.close();
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                log.error("Close transaction engine failed", ex);
            }
        }
    }
    
    @Override
    public void close() throws Exception {
        metaDataContexts.close();
    }
}
