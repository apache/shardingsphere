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
import org.apache.shardingsphere.infra.callback.governance.DataSourceCallback;
import org.apache.shardingsphere.infra.callback.governance.RuleCallback;
import org.apache.shardingsphere.infra.callback.governance.SchemaNameCallback;
import org.apache.shardingsphere.infra.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.infra.context.impl.StandardSchemaContexts;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.exception.DBCreateExistsException;
import org.apache.shardingsphere.proxy.backend.response.BackendResponse;
import org.apache.shardingsphere.proxy.backend.response.error.ErrorResponse;
import org.apache.shardingsphere.proxy.backend.response.query.QueryData;
import org.apache.shardingsphere.proxy.backend.response.update.UpdateResponse;
import org.apache.shardingsphere.proxy.backend.schema.ProxySchemaContexts;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.config.util.DataSourceConverter;
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
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateDatabaseStatement;

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
    public BackendResponse execute() {
        SQLStatementContext<?> context = getSQLStatementContext();
        if (!isRegistryCenterExisted()) {
            return new ErrorResponse(new SQLException("No Registry center to execute `%s` SQL", context.getClass().getSimpleName()));
        }
        return getBackendResponse(context);
    }
    
    private BackendResponse execute(final CreateDatabaseStatementContext context) {
        if (ProxySchemaContexts.getInstance().getSchemaNames().contains(context.getSqlStatement().getDatabaseName())) {
            return new ErrorResponse(new DBCreateExistsException(context.getSqlStatement().getDatabaseName()));
        }
        SchemaNameCallback.getInstance().run(context.getSqlStatement().getDatabaseName(), true);
        // TODO Need to get the executed feedback from registry center for returning.
        UpdateResponse result = new UpdateResponse();
        result.setType("CREATE");
        return result;
    }
    
    private BackendResponse execute(final CreateDataSourcesStatementContext context) {
        Map<String, YamlDataSourceParameter> parameters = new CreateDataSourcesStatementContextConverter().convert(context);
        Map<String, DataSourceConfiguration> dataSources = DataSourceConverter.getDataSourceConfigurationMap(DataSourceConverter.getDataSourceParameterMap2(parameters));
        // TODO Need to get the executed feedback from registry center for returning.
        DataSourceCallback.getInstance().run(backendConnection.getSchema(), dataSources);
        UpdateResponse result = new UpdateResponse();
        result.setType("CREATE");
        return result;
    }
    
    private BackendResponse execute(final CreateShardingRuleStatementContext context) {
        YamlShardingRuleConfiguration configurations = new CreateShardingRuleStatementContextConverter().convert(context);
        Collection<RuleConfiguration> rules = new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(Collections.singleton(configurations));
        // TODO Need to get the executed feedback from registry center for returning.
        RuleCallback.getInstance().run(backendConnection.getSchema(), rules);
        UpdateResponse result = new UpdateResponse();
        result.setType("CREATE");
        return result;
    }
    
    private SQLStatementContext<?> getSQLStatementContext() {
        DatabaseType databaseType = ProxySchemaContexts.getInstance().getSchemaContexts().getDatabaseType();
        if (sqlStatement instanceof CreateDataSourcesStatement) {
            return new CreateDataSourcesStatementContext((CreateDataSourcesStatement) sqlStatement, databaseType);
        } else if (sqlStatement instanceof CreateShardingRuleStatement) {
            return new CreateShardingRuleStatementContext((CreateShardingRuleStatement) sqlStatement);
        }
        return new CreateDatabaseStatementContext((CreateDatabaseStatement) sqlStatement);
    }
    
    private BackendResponse getBackendResponse(final SQLStatementContext<?> context) {
        if (context instanceof CreateDatabaseStatementContext) {
            return execute((CreateDatabaseStatementContext) context);
        }
        if (context instanceof CreateDataSourcesStatementContext) {
            return execute((CreateDataSourcesStatementContext) context);
        }
        return execute((CreateShardingRuleStatementContext) context);
    }
    
    private boolean isRegistryCenterExisted() {
        return !(ProxySchemaContexts.getInstance().getSchemaContexts() instanceof StandardSchemaContexts);
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
