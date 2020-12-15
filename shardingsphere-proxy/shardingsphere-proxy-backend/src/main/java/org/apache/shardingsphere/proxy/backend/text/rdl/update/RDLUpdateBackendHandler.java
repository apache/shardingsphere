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

package org.apache.shardingsphere.proxy.backend.text.rdl.update;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateDataSourcesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateShardingRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.impl.DropRuleStatement;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.governance.core.event.model.datasource.DataSourcePersistEvent;
import org.apache.shardingsphere.governance.core.event.model.rule.RuleConfigurationsChangedEvent;
import org.apache.shardingsphere.governance.core.event.model.rule.RuleConfigurationsPersistEvent;
import org.apache.shardingsphere.governance.core.event.model.schema.SchemaNamePersistEvent;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CreateDatabaseStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.DropDatabaseStatementContext;
import org.apache.shardingsphere.infra.binder.statement.rdl.CreateDataSourcesStatementContext;
import org.apache.shardingsphere.infra.binder.statement.rdl.CreateShardingRuleStatementContext;
import org.apache.shardingsphere.infra.binder.statement.rdl.DropRuleStatementContext;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.context.metadata.impl.StandardMetaDataContexts;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.DBCreateExistsException;
import org.apache.shardingsphere.proxy.backend.exception.NoDatabaseSelectedException;
import org.apache.shardingsphere.proxy.backend.exception.TablesInUsedException;
import org.apache.shardingsphere.proxy.backend.exception.UnknownDatabaseException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.config.util.DataSourceParameterConverter;
import org.apache.shardingsphere.proxy.config.yaml.YamlDataSourceParameter;
import org.apache.shardingsphere.proxy.converter.CreateDataSourcesStatementContextConverter;
import org.apache.shardingsphere.replicaquery.api.config.ReplicaQueryRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.converter.CreateShardingRuleStatementContextConverter;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropDatabaseStatement;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Backend handler for RDL update.
 */
@RequiredArgsConstructor
public final class RDLUpdateBackendHandler implements TextProtocolBackendHandler {
    
    private final SQLStatement sqlStatement;
    
    private final BackendConnection backendConnection;
    
    @Override
    public ResponseHeader execute() throws SQLException {
        if (!isRegistryCenterExisted()) {
            throw new SQLException(String.format("No Registry center to execute `%s` SQL", sqlStatement.getClass().getSimpleName()));
        }
        return getResponseHeader(getSQLStatementContext());
    }
    
    private ResponseHeader execute(final CreateDatabaseStatementContext context) {
        if (ProxyContext.getInstance().getAllSchemaNames().contains(context.getSqlStatement().getDatabaseName())) {
            throw new DBCreateExistsException(context.getSqlStatement().getDatabaseName());
        }
        // TODO Need to get the executed feedback from registry center for returning.
        ShardingSphereEventBus.getInstance().post(new SchemaNamePersistEvent(context.getSqlStatement().getDatabaseName(), false));
        return new UpdateResponseHeader(context.getSqlStatement());
    }
    
    private ResponseHeader execute(final CreateDataSourcesStatementContext context) {
        Map<String, YamlDataSourceParameter> parameters = CreateDataSourcesStatementContextConverter.convert(context);
        Map<String, DataSourceConfiguration> dataSources = DataSourceParameterConverter.getDataSourceConfigurationMap(
                DataSourceParameterConverter.getDataSourceParameterMapFromYamlConfiguration(parameters));
        // TODO Need to get the executed feedback from registry center for returning.
        ShardingSphereEventBus.getInstance().post(new DataSourcePersistEvent(backendConnection.getSchemaName(), dataSources));
        return new UpdateResponseHeader(context.getSqlStatement());
    }
    
    private ResponseHeader execute(final CreateShardingRuleStatementContext context) {
        YamlShardingRuleConfiguration config = CreateShardingRuleStatementContextConverter.convert(context);
        Collection<RuleConfiguration> rules = new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(Collections.singleton(config));
        // TODO Need to get the executed feedback from registry center for returning.
        ShardingSphereEventBus.getInstance().post(new RuleConfigurationsPersistEvent(backendConnection.getSchemaName(), rules));
        return new UpdateResponseHeader(context.getSqlStatement());
    }
    
    private ResponseHeader execute(final DropDatabaseStatementContext context) {
        if (!ProxyContext.getInstance().getAllSchemaNames().contains(context.getSqlStatement().getDatabaseName())) {
            throw new DBCreateExistsException(context.getSqlStatement().getDatabaseName());
        }
        // TODO Need to get the executed feedback from registry center for returning.
        ShardingSphereEventBus.getInstance().post(new SchemaNamePersistEvent(context.getSqlStatement().getDatabaseName(), true));
        return new UpdateResponseHeader(context.getSqlStatement());
    }
    
    private ResponseHeader execute(final DropRuleStatementContext context) {
        String schemaName = getSchemaName(context);
        String ruleType = context.getSqlStatement().getRuleType();
        Class<? extends RuleConfiguration> ruleConfigurationClass = getRuleConfigurationClass(ruleType);
        switch (ruleType.toUpperCase()) {
            case "SHARDING":
                checkShardingTables(schemaName);
                break;
            case "REPLICA_QUERY":
                // TODO
            case "ENCRYPT":
                // TODO
                break;
            case "SHADOW":
                // TODO
                break;
            default:
                throw new UnsupportedOperationException(ruleType);
        }
        Collection<RuleConfiguration> configs = ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations();
        ShardingSphereEventBus.getInstance().post(new RuleConfigurationsChangedEvent(schemaName, 
                configs.stream().filter(each -> !ruleConfigurationClass.isAssignableFrom(each.getClass())).collect(Collectors.toList())));
        // TODO Need to get the executed feedback from registry center for returning.
        return new UpdateResponseHeader(context.getSqlStatement());
    }
    
    private void checkShardingTables(final String schemaName) {
        ShardingSphereMetaData metaData = ProxyContext.getInstance().getMetaData(schemaName);
        Optional<ShardingRule> shardingRule = metaData.getRuleMetaData()
                .getRules().stream().filter(each -> each instanceof ShardingRule).map(each -> (ShardingRule) each).findFirst();
        if (!shardingRule.isPresent()) {
            return;
        }
        Collection<String> inUsedTableNames = new LinkedList<>();
        Collection<String> shardingTables = new LinkedList<>(shardingRule.get().getTables());
        shardingTables.addAll(shardingRule.get().getBroadcastTables());
        shardingTables.addAll(shardingRule.get().getSingleTableRules().keySet());
        for (String each : shardingTables) {
            if (metaData.getSchema().containsTable(each)) {
                inUsedTableNames.add(each);
            }
        }
        if (!inUsedTableNames.isEmpty()) {
            throw new TablesInUsedException(inUsedTableNames);
        }
    }
    
    private Class<? extends RuleConfiguration> getRuleConfigurationClass(final String ruleType) {
        switch (ruleType.toUpperCase()) {
            case "SHARDING":
                return ShardingRuleConfiguration.class;
            case "REPLICA_QUERY":
                return ReplicaQueryRuleConfiguration.class;
            case "ENCRYPT":
                return EncryptRuleConfiguration.class;
            case "SHADOW":
                return ShadowRuleConfiguration.class;
            default:
                throw new UnsupportedOperationException(ruleType);
        }
    }
    
    private String getSchemaName(final DropRuleStatementContext context) {
        String result = null == context.getSqlStatement().getSchemaName() ? backendConnection.getSchemaName() : context.getSqlStatement().getSchemaName().getIdentifier().getValue();
        if (null == result) {
            throw new NoDatabaseSelectedException();
        }
        if (!ProxyContext.getInstance().schemaExists(result)) {
            throw new UnknownDatabaseException(result);
        }
        return result;
    }
    
    private boolean isRegistryCenterExisted() {
        return !(ProxyContext.getInstance().getMetaDataContexts() instanceof StandardMetaDataContexts);
    }
    
    private SQLStatementContext<?> getSQLStatementContext() {
        DatabaseType databaseType = ProxyContext.getInstance().getMetaDataContexts().getMetaData(backendConnection.getSchemaName()).getResource().getDatabaseType();
        if (sqlStatement instanceof CreateDataSourcesStatement) {
            return new CreateDataSourcesStatementContext((CreateDataSourcesStatement) sqlStatement, databaseType);
        }
        if (sqlStatement instanceof CreateShardingRuleStatement) {
            return new CreateShardingRuleStatementContext((CreateShardingRuleStatement) sqlStatement);
        }
        if (sqlStatement instanceof CreateDatabaseStatement) {
            return new CreateDatabaseStatementContext((CreateDatabaseStatement) sqlStatement);
        }
        if (sqlStatement instanceof DropDatabaseStatement) {
            return new DropDatabaseStatementContext((DropDatabaseStatement) sqlStatement);
        }
        if (sqlStatement instanceof DropRuleStatement) {
            return new DropRuleStatementContext((DropRuleStatement) sqlStatement);
        }
        throw new UnsupportedOperationException(sqlStatement.getClass().getName());
    }
    
    private ResponseHeader getResponseHeader(final SQLStatementContext<?> context) {
        if (context instanceof CreateDatabaseStatementContext) {
            return execute((CreateDatabaseStatementContext) context);
        }
        if (context instanceof CreateDataSourcesStatementContext) {
            return execute((CreateDataSourcesStatementContext) context);
        }
        if (context instanceof CreateShardingRuleStatementContext) {
            return execute((CreateShardingRuleStatementContext) context);
        }
        if (context instanceof DropDatabaseStatementContext) {
            return execute((DropDatabaseStatementContext) context);
        }
        if (context instanceof DropRuleStatementContext) {
            return execute((DropRuleStatementContext) context);
        }
        throw new UnsupportedOperationException(context.getClass().getName());
    }
}
