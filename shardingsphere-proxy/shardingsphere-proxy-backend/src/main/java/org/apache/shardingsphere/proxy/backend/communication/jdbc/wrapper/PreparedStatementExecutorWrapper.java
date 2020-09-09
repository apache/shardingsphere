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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.wrapper;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.context.SchemaContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContextBuilder;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.group.ExecuteGroupEngine;
import org.apache.shardingsphere.infra.executor.sql.resourced.jdbc.group.PreparedStatementExecuteGroupEngine;
import org.apache.shardingsphere.infra.executor.sql.resourced.jdbc.group.StatementOption;
import org.apache.shardingsphere.infra.rewrite.SQLRewriteEntry;
import org.apache.shardingsphere.infra.rewrite.engine.result.SQLRewriteResult;
import org.apache.shardingsphere.infra.route.DataNodeRouter;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.schema.ProxyContext;
import org.apache.shardingsphere.sql.parser.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Executor wrapper for prepared statement.
 */
@RequiredArgsConstructor
public final class PreparedStatementExecutorWrapper implements JDBCExecutorWrapper {
    
    private static final ProxyContext PROXY_SCHEMA_CONTEXTS = ProxyContext.getInstance();
    
    private final SchemaContext schema;
    
    private final SQLStatement sqlStatement;
    
    private final List<Object> parameters;
    
    @SuppressWarnings("unchecked")
    @Override
    public ExecutionContext generateExecutionContext(final String sql) {
        Collection<ShardingSphereRule> rules = schema.getSchema().getRules();
        if (rules.isEmpty()) {
            return new ExecutionContext(
                    new CommonSQLStatementContext(sqlStatement), new ExecutionUnit(schema.getSchema().getDataSources().keySet().iterator().next(), new SQLUnit(sql, parameters)));
        }
        RouteContext routeContext = new DataNodeRouter(schema.getSchema().getMetaData(), PROXY_SCHEMA_CONTEXTS.getSchemaContexts().getProps(), rules).route(sqlStatement, sql, parameters);
        SQLRewriteEntry sqlRewriteEntry = new SQLRewriteEntry(schema.getSchema().getMetaData().getRuleSchemaMetaData().getConfiguredSchemaMetaData(), 
                PROXY_SCHEMA_CONTEXTS.getSchemaContexts().getProps(), rules);
        SQLRewriteResult sqlRewriteResult = sqlRewriteEntry.rewrite(sql, new ArrayList<>(parameters), routeContext);
        return new ExecutionContext(routeContext.getSqlStatementContext(), ExecutionContextBuilder.build(schema.getSchema().getMetaData(), sqlRewriteResult));
    }
    
    @Override
    public ExecuteGroupEngine<?> getExecuteGroupEngine(final BackendConnection backendConnection, final int maxConnectionsSizePerQuery, final StatementOption option) {
        return new PreparedStatementExecuteGroupEngine(maxConnectionsSizePerQuery, backendConnection, option, schema.getSchema().getRules());
    }
    
    @Override
    public boolean execute(final Statement statement, final String sql, final boolean isReturnGeneratedKeys) throws SQLException {
        return ((PreparedStatement) statement).execute();
    }
}
