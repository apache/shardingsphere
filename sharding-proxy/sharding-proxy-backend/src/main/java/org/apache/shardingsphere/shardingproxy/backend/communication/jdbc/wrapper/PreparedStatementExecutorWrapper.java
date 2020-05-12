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

package org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.wrapper;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.shardingproxy.backend.schema.LogicSchema;
import org.apache.shardingsphere.shardingproxy.backend.schema.impl.ShardingSphereSchema;
import org.apache.shardingsphere.shardingproxy.context.ShardingProxyContext;
import org.apache.shardingsphere.sql.parser.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.underlying.common.rule.ShardingSphereRule;
import org.apache.shardingsphere.underlying.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.underlying.executor.sql.context.ExecutionContextBuilder;
import org.apache.shardingsphere.underlying.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.underlying.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.underlying.executor.sql.execute.jdbc.group.PreparedStatementExecuteGroupEngine;
import org.apache.shardingsphere.underlying.executor.sql.group.ExecuteGroupEngine;
import org.apache.shardingsphere.underlying.rewrite.SQLRewriteEntry;
import org.apache.shardingsphere.underlying.rewrite.engine.result.SQLRewriteResult;
import org.apache.shardingsphere.underlying.route.DataNodeRouter;
import org.apache.shardingsphere.underlying.route.context.RouteContext;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Executor wrapper for prepared statement.
 */
@RequiredArgsConstructor
public final class PreparedStatementExecutorWrapper implements JDBCExecutorWrapper {
    
    private static final ShardingProxyContext SHARDING_PROXY_CONTEXT = ShardingProxyContext.getInstance();
    
    private final LogicSchema logicSchema;
    
    private final List<Object> parameters;
    
    @Override
    public ExecutionContext route(final String sql) {
        if (logicSchema instanceof ShardingSphereSchema) {
            return doShardingRoute(sql);
        }
        return doTransparentRoute(sql);
    }
    
    private ExecutionContext doShardingRoute(final String sql) {
        Collection<ShardingSphereRule> rules = logicSchema.getRules();
        SQLStatement sqlStatement = logicSchema.getSqlParserEngine().parse(sql, true);
        RouteContext routeContext = new DataNodeRouter(logicSchema.getMetaData(), SHARDING_PROXY_CONTEXT.getProperties(), rules).route(sqlStatement, sql, parameters);
        SQLRewriteResult sqlRewriteResult = new SQLRewriteEntry(logicSchema.getMetaData().getSchema().getConfiguredSchemaMetaData(),
                SHARDING_PROXY_CONTEXT.getProperties(), rules).rewrite(sql, new ArrayList<>(parameters), routeContext);
        return new ExecutionContext(routeContext.getSqlStatementContext(), ExecutionContextBuilder.build(logicSchema.getMetaData(), sqlRewriteResult));
    }
    
    @SuppressWarnings("unchecked")
    private ExecutionContext doTransparentRoute(final String sql) {
        SQLStatement sqlStatement = logicSchema.getSqlParserEngine().parse(sql, false);
        return new ExecutionContext(
                new CommonSQLStatementContext(sqlStatement), new ExecutionUnit(logicSchema.getDataSources().keySet().iterator().next(), new SQLUnit(sql, Collections.emptyList())));
    }
    
    @Override
    public ExecuteGroupEngine getExecuteGroupEngine() {
        int maxConnectionsSizePerQuery = ShardingProxyContext.getInstance().getProperties().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        return new PreparedStatementExecuteGroupEngine(maxConnectionsSizePerQuery, logicSchema.getRules());
    }
    
    @Override
    public boolean executeSQL(final Statement statement, final String sql, final boolean isReturnGeneratedKeys) throws SQLException {
        return ((PreparedStatement) statement).execute();
    }
}
