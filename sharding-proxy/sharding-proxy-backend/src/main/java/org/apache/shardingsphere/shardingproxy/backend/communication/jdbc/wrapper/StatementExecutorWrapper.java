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
import org.apache.shardingsphere.core.rule.ShadowRule;
import org.apache.shardingsphere.shadow.rewrite.judgement.ShadowJudgementEngine;
import org.apache.shardingsphere.shadow.rewrite.judgement.impl.SimpleJudgementEngine;
import org.apache.shardingsphere.shardingproxy.backend.schema.LogicSchema;
import org.apache.shardingsphere.shardingproxy.backend.schema.impl.EncryptSchema;
import org.apache.shardingsphere.shardingproxy.backend.schema.impl.MasterSlaveSchema;
import org.apache.shardingsphere.shardingproxy.backend.schema.impl.ShadowSchema;
import org.apache.shardingsphere.shardingproxy.backend.schema.impl.ShardingSchema;
import org.apache.shardingsphere.shardingproxy.context.ShardingProxyContext;
import org.apache.shardingsphere.sql.parser.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.sql.parser.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.DMLStatement;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.underlying.common.metadata.schema.RuleSchemaMetaData;
import org.apache.shardingsphere.underlying.common.rule.ShardingSphereRule;
import org.apache.shardingsphere.underlying.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.underlying.executor.sql.context.ExecutionContextBuilder;
import org.apache.shardingsphere.underlying.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.underlying.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.underlying.executor.sql.execute.jdbc.group.StatementExecuteGroupEngine;
import org.apache.shardingsphere.underlying.executor.sql.group.ExecuteGroupEngine;
import org.apache.shardingsphere.underlying.rewrite.SQLRewriteEntry;
import org.apache.shardingsphere.underlying.rewrite.engine.result.GenericSQLRewriteResult;
import org.apache.shardingsphere.underlying.rewrite.engine.result.SQLRewriteResult;
import org.apache.shardingsphere.underlying.rewrite.engine.result.SQLRewriteUnit;
import org.apache.shardingsphere.underlying.route.DataNodeRouter;
import org.apache.shardingsphere.underlying.route.context.RouteContext;
import org.apache.shardingsphere.underlying.route.context.RouteResult;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Executor wrapper for statement.
 */
@RequiredArgsConstructor
public final class StatementExecutorWrapper implements JDBCExecutorWrapper {
    
    private static final ShardingProxyContext SHARDING_PROXY_CONTEXT = ShardingProxyContext.getInstance();
    
    private final LogicSchema logicSchema;
    
    @Override
    public ExecutionContext route(final String sql) {
        if (logicSchema instanceof ShardingSchema) {
            return doShardingRoute(sql);
        }
        if (logicSchema instanceof MasterSlaveSchema) {
            return doMasterSlaveRoute(sql);
        }
        if (logicSchema instanceof EncryptSchema) {
            return doEncryptRoute(sql);
        }
        if (logicSchema instanceof ShadowSchema) {
            return doShadowRoute(sql);
        }
        return doTransparentRoute(sql);
    }
    
    private ExecutionContext doShardingRoute(final String sql) {
        Collection<ShardingSphereRule> rules = logicSchema.getRules();
        SQLStatement sqlStatement = logicSchema.getSqlParserEngine().parse(sql, false);
        RouteContext routeContext = new DataNodeRouter(logicSchema.getMetaData(), SHARDING_PROXY_CONTEXT.getProperties(), rules).route(sqlStatement, sql, Collections.emptyList());
        SQLRewriteResult sqlRewriteResult = new SQLRewriteEntry(logicSchema.getMetaData().getSchema().getConfiguredSchemaMetaData(),
                SHARDING_PROXY_CONTEXT.getProperties(), rules).rewrite(sql, Collections.emptyList(), routeContext);
        return new ExecutionContext(routeContext.getSqlStatementContext(), ExecutionContextBuilder.build(logicSchema.getMetaData(), sqlRewriteResult));
    }
    
    @SuppressWarnings("unchecked")
    private ExecutionContext doMasterSlaveRoute(final String sql) {
        SQLStatement sqlStatement = logicSchema.getSqlParserEngine().parse(sql, false);
        RouteContext routeContext = new DataNodeRouter(logicSchema.getMetaData(), SHARDING_PROXY_CONTEXT.getProperties(), logicSchema.getRules()).route(sqlStatement, sql, Collections.emptyList());
        SQLRewriteResult sqlRewriteResult = new SQLRewriteEntry(logicSchema.getMetaData().getSchema().getConfiguredSchemaMetaData(),
                SHARDING_PROXY_CONTEXT.getProperties(), logicSchema.getRules()).rewrite(sql, Collections.emptyList(), routeContext);
        return new ExecutionContext(routeContext.getSqlStatementContext(), ExecutionContextBuilder.build(logicSchema.getMetaData(), sqlRewriteResult));
    }
    
    @SuppressWarnings("unchecked")
    private ExecutionContext doEncryptRoute(final String sql) {
        SQLStatement sqlStatement = logicSchema.getSqlParserEngine().parse(sql, false);
        RouteContext routeContext = new DataNodeRouter(logicSchema.getMetaData(), SHARDING_PROXY_CONTEXT.getProperties(), logicSchema.getRules()).route(sqlStatement, sql, Collections.emptyList());
        SQLRewriteResult sqlRewriteResult = new SQLRewriteEntry(logicSchema.getMetaData().getSchema().getConfiguredSchemaMetaData(),
                SHARDING_PROXY_CONTEXT.getProperties(), logicSchema.getRules()).rewrite(sql, Collections.emptyList(), routeContext);
        return new ExecutionContext(routeContext.getSqlStatementContext(), ExecutionContextBuilder.build(logicSchema.getMetaData(), sqlRewriteResult));
    }
    
    private ExecutionContext doShadowRoute(final String sql) {
        ShadowSchema shadowSchema = (ShadowSchema) logicSchema;
        SQLStatement sqlStatement = shadowSchema.getSqlParserEngine().parse(sql, true);
        RuleSchemaMetaData ruleSchemaMetaData = logicSchema.getMetaData().getSchema();
        SQLStatementContext sqlStatementContext = SQLStatementContextFactory.newInstance(ruleSchemaMetaData.getConfiguredSchemaMetaData(), sql, new LinkedList<>(), sqlStatement);
        Collection<ExecutionUnit> executionUnits = new ArrayList<>();
        if (sqlStatement instanceof DMLStatement) {
            ShadowJudgementEngine shadowJudgementEngine = new SimpleJudgementEngine((ShadowRule) shadowSchema.getRules().iterator().next(), sqlStatementContext);
            String dataSourceName = shadowJudgementEngine.isShadowSQL()
                    ? ((ShadowRule) shadowSchema.getRules().iterator().next()).getRuleConfiguration().getShadowMappings().get(logicSchema.getDataSources().keySet().iterator().next())
                    : logicSchema.getDataSources().keySet().iterator().next();
            SQLRewriteEntry sqlRewriteEntry = new SQLRewriteEntry(
                    logicSchema.getMetaData().getSchema().getConfiguredSchemaMetaData(), ShardingProxyContext.getInstance().getProperties(), shadowSchema.getRules());
            SQLRewriteUnit sqlRewriteResult = ((GenericSQLRewriteResult) sqlRewriteEntry.rewrite(sql, Collections.emptyList(),
                    new RouteContext(sqlStatementContext, Collections.emptyList(), new RouteResult()))).getSqlRewriteUnit();
            executionUnits.add(new ExecutionUnit(dataSourceName, new SQLUnit(sqlRewriteResult.getSql(), sqlRewriteResult.getParameters())));
        } else {
            logicSchema.getDataSources().keySet()
                    .forEach(s -> executionUnits.add(new ExecutionUnit(s, new SQLUnit(sql, Collections.emptyList()))));
        }
        return new ExecutionContext(sqlStatementContext, executionUnits);
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
        return new StatementExecuteGroupEngine(maxConnectionsSizePerQuery, logicSchema.getRules());
    }
    
    @Override
    public boolean executeSQL(final Statement statement, final String sql, final boolean isReturnGeneratedKeys) throws SQLException {
        return statement.execute(sql, isReturnGeneratedKeys ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS);
    }
}
