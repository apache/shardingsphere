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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.execute;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.callback.orchestration.DataSourceCallback;
import org.apache.shardingsphere.infra.callback.orchestration.RuleCallback;
import org.apache.shardingsphere.infra.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.kernel.context.SchemaContexts;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.execute.generator.YamlDataSourceConfigurationGenerator;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.execute.generator.YamlShardingRuleConfigurationGenerator;
import org.apache.shardingsphere.proxy.backend.response.BackendResponse;
import org.apache.shardingsphere.proxy.backend.response.error.ErrorResponse;
import org.apache.shardingsphere.proxy.backend.response.update.UpdateResponse;
import org.apache.shardingsphere.proxy.backend.schema.ProxySchemaContexts;
import org.apache.shardingsphere.proxy.config.util.DataSourceConverter;
import org.apache.shardingsphere.proxy.config.yaml.YamlDataSourceParameter;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.sql.parser.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.ddl.CreateDataSourcesStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.ddl.CreateShardingRuleStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

/**
 * Registry center execute engine.
 */
@RequiredArgsConstructor
public class RegistryCenterExecuteEngine implements SQLExecuteEngine {
    
    private final String schemaName;
    
    private final SQLStatement sqlStatement;
    
    @Override
    public final ExecutionContext execute(final String sql) {
        return new ExecutionContext(new CommonSQLStatementContext(sqlStatement), new LinkedList<>());
    }
    
    @Override
    public final BackendResponse execute(final ExecutionContext executionContext) {
        if (executionContext.getSqlStatementContext() instanceof CreateDataSourcesStatementContext) {
            return execute((CreateDataSourcesStatementContext) executionContext.getSqlStatementContext());
        }
        return execute((CreateShardingRuleStatementContext) executionContext.getSqlStatementContext());
    }
    
    private BackendResponse execute(final CreateDataSourcesStatementContext context) {
        if (!isRegistryCenterExisted()) {
            return new ErrorResponse(new SQLException("No Registry center to execute `%s` SQL", context.getClass().getSimpleName()));
        }
        Map<String, YamlDataSourceParameter> parameters = new YamlDataSourceConfigurationGenerator().generate(context);
        Map<String, DataSourceConfiguration> dataSources = DataSourceConverter.getDataSourceConfigurationMap(DataSourceConverter.getDataSourceParameterMap2(parameters));
        // TODO Need to get the executed feedback from registry center for returning.
        DataSourceCallback.getInstance().run(schemaName, dataSources);
        UpdateResponse result = new UpdateResponse();
        result.setType("CREATE");
        return result;
    }
    
    private BackendResponse execute(final CreateShardingRuleStatementContext context) {
        if (!isRegistryCenterExisted()) {
            return new ErrorResponse(new SQLException("No Registry center to execute `%s` SQL", context.getClass().getSimpleName()));
        }
        YamlShardingRuleConfiguration configurations = new YamlShardingRuleConfigurationGenerator().generate(context);
        Collection<RuleConfiguration> rules = new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(Collections.singleton(configurations));
        // TODO Need to get the executed feedback from registry center for returning.
        RuleCallback.getInstance().run(schemaName, rules);
        UpdateResponse result = new UpdateResponse();
        result.setType("CREATE");
        return result;
    }
    
    private boolean isRegistryCenterExisted() {
        return !(ProxySchemaContexts.getInstance().getSchemaContexts() instanceof SchemaContexts);
    }
}
