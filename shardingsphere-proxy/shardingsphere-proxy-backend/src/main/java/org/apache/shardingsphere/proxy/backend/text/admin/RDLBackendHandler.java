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

package org.apache.shardingsphere.proxy.backend.text.admin;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.governance.core.event.persist.DataSourcePersistEvent;
import org.apache.shardingsphere.governance.core.event.persist.RulePersistEvent;
import org.apache.shardingsphere.governance.core.event.persist.SchemaNamePersistEvent;
import org.apache.shardingsphere.governance.core.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.context.impl.StandardSchemaContexts;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.DBCreateExistsException;
import org.apache.shardingsphere.proxy.backend.response.BackendResponse;
import org.apache.shardingsphere.proxy.backend.response.query.QueryData;
import org.apache.shardingsphere.proxy.backend.response.update.UpdateResponse;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.config.util.DataSourceParameterConverter;
import org.apache.shardingsphere.proxy.config.yaml.YamlDataSourceParameter;
import org.apache.shardingsphere.proxy.convert.CreateDataSourcesStatementContextConverter;
import org.apache.shardingsphere.rdl.parser.binder.context.CreateDataSourcesStatementContext;
import org.apache.shardingsphere.rdl.parser.binder.context.CreateShardingRuleStatementContext;
import org.apache.shardingsphere.rdl.parser.statement.rdl.CreateDataSourcesStatement;
import org.apache.shardingsphere.rdl.parser.statement.rdl.CreateShardingRuleStatement;
import org.apache.shardingsphere.sharding.convert.CreateShardingRuleStatementContextConverter;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.ddl.CreateDatabaseStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.ddl.DropDatabaseStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropDatabaseStatement;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Backer handler for RDL.
 */
@RequiredArgsConstructor
public final class RDLBackendHandler implements TextProtocolBackendHandler {
    
    private final BackendConnection backendConnection;
    
    private final SQLStatement sqlStatement;
    
    @Override
    public BackendResponse execute() throws SQLException {
        SQLStatementContext<?> context = getSQLStatementContext();
        if (!isRegistryCenterExisted()) {
            throw new SQLException(String.format("No Registry center to execute `%s` SQL", context.getClass().getSimpleName()));
        }
        return getBackendResponse(context);
    }
    
    private BackendResponse execute(final CreateDatabaseStatementContext context) {
        if (ProxyContext.getInstance().getAllSchemaNames().contains(context.getSqlStatement().getDatabaseName())) {
            throw new DBCreateExistsException(context.getSqlStatement().getDatabaseName());
        }
        // TODO Need to get the executed feedback from registry center for returning.
        ShardingSphereEventBus.getInstance().post(new SchemaNamePersistEvent(context.getSqlStatement().getDatabaseName(), false));
        UpdateResponse result = new UpdateResponse();
        result.setType("CREATE");
        return result;
    }
    
    private BackendResponse execute(final DropDatabaseStatementContext context) {
        if (!ProxyContext.getInstance().getAllSchemaNames().contains(context.getSqlStatement().getDatabaseName())) {
            throw new DBCreateExistsException(context.getSqlStatement().getDatabaseName());
        }
        // TODO Need to get the executed feedback from registry center for returning.
        ShardingSphereEventBus.getInstance().post(new SchemaNamePersistEvent(context.getSqlStatement().getDatabaseName(), true));
        UpdateResponse result = new UpdateResponse();
        result.setType("DROP");
        return result;
    }
    
    private BackendResponse execute(final CreateDataSourcesStatementContext context) {
        Map<String, YamlDataSourceParameter> parameters = new CreateDataSourcesStatementContextConverter().convert(context);
        Map<String, DataSourceConfiguration> dataSources = DataSourceParameterConverter.getDataSourceConfigurationMap(
                DataSourceParameterConverter.getDataSourceParameterMapFromYamlConfiguration(parameters));
        // TODO Need to get the executed feedback from registry center for returning.
        ShardingSphereEventBus.getInstance().post(new DataSourcePersistEvent(backendConnection.getSchemaName(), dataSources));
        UpdateResponse result = new UpdateResponse();
        result.setType("CREATE");
        return result;
    }
    
    private BackendResponse execute(final CreateShardingRuleStatementContext context) {
        YamlShardingRuleConfiguration configs = new CreateShardingRuleStatementContextConverter().convert(context);
        Collection<RuleConfiguration> rules = new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(Collections.singleton(configs));
        // TODO Need to get the executed feedback from registry center for returning.
        ShardingSphereEventBus.getInstance().post(new RulePersistEvent(backendConnection.getSchemaName(), rules));
        UpdateResponse result = new UpdateResponse();
        result.setType("CREATE");
        return result;
    }
    
    private SQLStatementContext<?> getSQLStatementContext() {
        DatabaseType databaseType = ProxyContext.getInstance().getSchemaContexts().getDatabaseType();
        if (sqlStatement instanceof CreateDataSourcesStatement) {
            return new CreateDataSourcesStatementContext((CreateDataSourcesStatement) sqlStatement, databaseType);
        }
        if (sqlStatement instanceof CreateShardingRuleStatement) {
            return new CreateShardingRuleStatementContext((CreateShardingRuleStatement) sqlStatement);
        }
        if (sqlStatement instanceof CreateDatabaseStatement) {
            return new CreateDatabaseStatementContext((CreateDatabaseStatement) sqlStatement);
        }
        return new DropDatabaseStatementContext((DropDatabaseStatement) sqlStatement);
    }
    
    private BackendResponse getBackendResponse(final SQLStatementContext<?> context) {
        if (context instanceof CreateDatabaseStatementContext) {
            return execute((CreateDatabaseStatementContext) context);
        }
        if (context instanceof CreateDataSourcesStatementContext) {
            return execute((CreateDataSourcesStatementContext) context);
        }
        if (context instanceof DropDatabaseStatementContext) {
            return execute((DropDatabaseStatementContext) context);
        }
        return execute((CreateShardingRuleStatementContext) context);
    }
    
    private boolean isRegistryCenterExisted() {
        return !(ProxyContext.getInstance().getSchemaContexts() instanceof StandardSchemaContexts);
    }
    
    @Override
    public boolean next() {
        return false;
    }
    
    @Override
    public QueryData getQueryData() {
        return new QueryData(Collections.emptyList(), Collections.emptyList());
    }
}
