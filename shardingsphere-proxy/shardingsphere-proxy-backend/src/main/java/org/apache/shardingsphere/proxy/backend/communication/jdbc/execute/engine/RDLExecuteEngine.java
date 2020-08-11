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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.execute.engine;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.callback.orchestration.DataSourceCallback;
import org.apache.shardingsphere.infra.callback.orchestration.RuleCallback;
import org.apache.shardingsphere.infra.callback.orchestration.SchemaNameCallback;
import org.apache.shardingsphere.infra.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.kernel.context.SchemaContext;
import org.apache.shardingsphere.kernel.context.StandardSchemaContexts;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.execute.SQLExecuteEngine;
import org.apache.shardingsphere.proxy.backend.response.BackendResponse;
import org.apache.shardingsphere.proxy.backend.response.error.ErrorResponse;
import org.apache.shardingsphere.proxy.backend.response.update.UpdateResponse;
import org.apache.shardingsphere.proxy.backend.schema.ProxySchemaContexts;
import org.apache.shardingsphere.proxy.config.util.DataSourceConverter;
import org.apache.shardingsphere.proxy.config.yaml.YamlDataSourceParameter;
import org.apache.shardingsphere.proxy.convert.CreateDataSourcesStatementContextConverter;
import org.apache.shardingsphere.rdl.parser.binder.context.CreateDataSourcesStatementContext;
import org.apache.shardingsphere.rdl.parser.binder.context.CreateSchemaStatementContext;
import org.apache.shardingsphere.rdl.parser.binder.context.CreateShardingRuleStatementContext;
import org.apache.shardingsphere.rdl.parser.statement.rdl.CreateDataSourcesStatement;
import org.apache.shardingsphere.rdl.parser.statement.rdl.CreateSchemaStatement;
import org.apache.shardingsphere.rdl.parser.statement.rdl.CreateShardingRuleStatement;
import org.apache.shardingsphere.sharding.convert.CreateShardingRuleStatementContextConverter;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

/**
 * RDL execute engine.
 */
@RequiredArgsConstructor
public final class RDLExecuteEngine implements SQLExecuteEngine {
    
    private final SchemaContext schema;
    
    private final SQLStatement sqlStatement;
    
    @Override
    public ExecutionContext execute(final String sql) {
        return new ExecutionContext(getSqlStatementContext(), Collections.singleton(getExecutionUnit(sql)));
    }
    
    @Override
    public BackendResponse execute(final ExecutionContext executionContext) {
        if (!isRegistryCenterExisted()) {
            return new ErrorResponse(new SQLException("No Registry center to execute `%s` SQL", executionContext.getSqlStatementContext().getClass().getSimpleName()));
        }
        return getBackendResponse(executionContext.getSqlStatementContext());
    }
    
    private BackendResponse execute(final CreateSchemaStatementContext context) {
        SchemaNameCallback.getInstance().run(context.getSqlStatement().getSchemaName(), true);
        // TODO Need to get the executed feedback from registry center for returning.
        UpdateResponse result = new UpdateResponse();
        result.setType("CREATE");
        return result;
    }
    
    private BackendResponse execute(final CreateDataSourcesStatementContext context) {
        Map<String, YamlDataSourceParameter> parameters = new CreateDataSourcesStatementContextConverter().convert(context);
        Map<String, DataSourceConfiguration> dataSources = DataSourceConverter.getDataSourceConfigurationMap(DataSourceConverter.getDataSourceParameterMap2(parameters));
        // TODO Need to get the executed feedback from registry center for returning.
        DataSourceCallback.getInstance().run(schema.getName(), dataSources);
        UpdateResponse result = new UpdateResponse();
        result.setType("CREATE");
        return result;
    }
    
    private BackendResponse execute(final CreateShardingRuleStatementContext context) {
        YamlShardingRuleConfiguration configurations = new CreateShardingRuleStatementContextConverter().convert(context);
        Collection<RuleConfiguration> rules = new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(Collections.singleton(configurations));
        // TODO Need to get the executed feedback from registry center for returning.
        RuleCallback.getInstance().run(schema.getName(), rules);
        UpdateResponse result = new UpdateResponse();
        result.setType("CREATE");
        return result;
    }
    
    private SQLStatementContext<?> getSqlStatementContext() {
        DatabaseType databaseType = ProxySchemaContexts.getInstance().getSchemaContexts().getDatabaseType();
        if (sqlStatement instanceof CreateDataSourcesStatement) {
            return new CreateDataSourcesStatementContext((CreateDataSourcesStatement) sqlStatement, databaseType);
        } else if (sqlStatement instanceof CreateShardingRuleStatement) {
            return new CreateShardingRuleStatementContext((CreateShardingRuleStatement) sqlStatement);
        }
        return new CreateSchemaStatementContext((CreateSchemaStatement) sqlStatement);
    }
    
    private ExecutionUnit getExecutionUnit(final String sql) {
        return new ExecutionUnit("", new SQLUnit(sql, new LinkedList<>()));
    }
    
    private BackendResponse getBackendResponse(final SQLStatementContext context) {
        if (context instanceof CreateSchemaStatementContext) {
            return execute((CreateSchemaStatementContext) context);
        }
        if (context instanceof CreateDataSourcesStatementContext) {
            return execute((CreateDataSourcesStatementContext) context);
        }
        return execute((CreateShardingRuleStatementContext) context);
    }
    
    private boolean isRegistryCenterExisted() {
        return !(ProxySchemaContexts.getInstance().getSchemaContexts() instanceof StandardSchemaContexts);
    }
}
