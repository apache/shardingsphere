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
import org.apache.shardingsphere.core.shard.SimpleQueryShardingEngine;
import org.apache.shardingsphere.encrypt.rewrite.context.EncryptSQLRewriteContextDecorator;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.masterslave.route.engine.MasterSlaveRouter;
import org.apache.shardingsphere.shadow.rewrite.context.ShadowSQLRewriteContextDecorator;
import org.apache.shardingsphere.shadow.rewrite.judgement.ShadowJudgementEngine;
import org.apache.shardingsphere.shadow.rewrite.judgement.impl.SimpleJudgementEngine;
import org.apache.shardingsphere.shardingproxy.backend.schema.LogicSchema;
import org.apache.shardingsphere.shardingproxy.backend.schema.impl.EncryptSchema;
import org.apache.shardingsphere.shardingproxy.backend.schema.impl.MasterSlaveSchema;
import org.apache.shardingsphere.shardingproxy.backend.schema.impl.ShadowSchema;
import org.apache.shardingsphere.shardingproxy.backend.schema.impl.ShardingSchema;
import org.apache.shardingsphere.shardingproxy.context.ShardingProxyContext;
import org.apache.shardingsphere.sql.parser.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetas;
import org.apache.shardingsphere.sql.parser.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.underlying.common.constant.properties.PropertiesConstant;
import org.apache.shardingsphere.underlying.common.rule.BaseRule;
import org.apache.shardingsphere.underlying.executor.context.ExecutionContext;
import org.apache.shardingsphere.underlying.executor.context.ExecutionUnit;
import org.apache.shardingsphere.underlying.executor.context.SQLUnit;
import org.apache.shardingsphere.underlying.rewrite.SQLRewriteEntry;
import org.apache.shardingsphere.underlying.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.underlying.rewrite.context.SQLRewriteContextDecorator;
import org.apache.shardingsphere.underlying.rewrite.engine.SQLRewriteResult;
import org.apache.shardingsphere.underlying.rewrite.engine.impl.DefaultSQLRewriteEngine;
import org.apache.shardingsphere.underlying.route.context.RouteUnit;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

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
        SimpleQueryShardingEngine shardingEngine = new SimpleQueryShardingEngine(
                logicSchema.getShardingRule(), ShardingProxyContext.getInstance().getProperties(), logicSchema.getMetaData(), logicSchema.getSqlParserEngine());
        return shardingEngine.shard(sql, Collections.emptyList());
    }
    
    @SuppressWarnings("unchecked")
    private ExecutionContext doMasterSlaveRoute(final String sql) {
        SQLStatement sqlStatement = logicSchema.getSqlParserEngine().parse(sql, false);
        CommonSQLStatementContext sqlStatementContext = new CommonSQLStatementContext(sqlStatement);
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(logicSchema.getMetaData().getTables(), sqlStatementContext, sql, Collections.emptyList());
        sqlRewriteContext.generateSQLTokens();
        String rewriteSQL = new DefaultSQLRewriteEngine().rewrite(sqlRewriteContext).getSql();
        ExecutionContext result = new ExecutionContext(sqlStatementContext);
        for (RouteUnit each : new MasterSlaveRouter(((MasterSlaveSchema) logicSchema).getMasterSlaveRule(), logicSchema.getSqlParserEngine(),
                SHARDING_PROXY_CONTEXT.getProperties().<Boolean>getValue(PropertiesConstant.SQL_SHOW)).route(rewriteSQL, Collections.emptyList(), false).getRouteResult().getRouteUnits()) {
            result.getExecutionUnits().add(new ExecutionUnit(each.getActualDataSourceName(), new SQLUnit(rewriteSQL, Collections.emptyList())));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private ExecutionContext doEncryptRoute(final String sql) {
        EncryptSchema encryptSchema = (EncryptSchema) logicSchema;
        SQLStatement sqlStatement = encryptSchema.getSqlParserEngine().parse(sql, false);
        TableMetas tableMetas = logicSchema.getMetaData().getTables();
        SQLStatementContext sqlStatementContext = SQLStatementContextFactory.newInstance(tableMetas, sql, new LinkedList<>(), sqlStatement);
        SQLRewriteContext sqlRewriteContext = new SQLRewriteEntry(logicSchema.getMetaData(), ShardingProxyContext.getInstance().getProperties())
                .createSQLRewriteContext(sql, Collections.emptyList(), sqlStatementContext, createSQLRewriteContextDecorator(encryptSchema.getEncryptRule()));
        SQLRewriteResult sqlRewriteResult = new DefaultSQLRewriteEngine().rewrite(sqlRewriteContext);
        ExecutionContext result = new ExecutionContext(sqlStatementContext);
        result.getExecutionUnits().add(
                new ExecutionUnit(logicSchema.getDataSources().keySet().iterator().next(), new SQLUnit(sqlRewriteResult.getSql(), sqlRewriteResult.getParameters())));
        return result;
    }
    
    private ExecutionContext doShadowRoute(final String sql) {
        ShadowSchema shadowSchema = (ShadowSchema) logicSchema;
        SQLStatement sqlStatement = shadowSchema.getSqlParserEngine().parse(sql, true);
        TableMetas tableMetas = logicSchema.getMetaData().getTables();
        SQLStatementContext sqlStatementContext = SQLStatementContextFactory.newInstance(tableMetas, sql, new LinkedList<>(), sqlStatement);
        ShadowJudgementEngine shadowJudgementEngine = new SimpleJudgementEngine(shadowSchema.getShadowRule(), sqlStatementContext);
        String dataSourceName = shadowJudgementEngine.isShadowSQL()
                ? shadowSchema.getShadowRule().getRuleConfiguration().getShadowMappings().get(logicSchema.getDataSources().keySet().iterator().next())
                : logicSchema.getDataSources().keySet().iterator().next();
        SQLRewriteContext sqlRewriteContext = new SQLRewriteEntry(logicSchema.getMetaData(), ShardingProxyContext.getInstance().getProperties())
                .createSQLRewriteContext(sql, Collections.emptyList(), sqlStatementContext, createSQLRewriteContextDecorator(shadowSchema.getShadowRule()));
        SQLRewriteResult sqlRewriteResult = new DefaultSQLRewriteEngine().rewrite(sqlRewriteContext);
        ExecutionContext result = new ExecutionContext(sqlStatementContext);
        result.getExecutionUnits().add(
                new ExecutionUnit(dataSourceName, new SQLUnit(sqlRewriteResult.getSql(), sqlRewriteResult.getParameters())));
        return result;
    }
    
    private Map<BaseRule, SQLRewriteContextDecorator> createSQLRewriteContextDecorator(final BaseRule baseRule) {
        Map<BaseRule, SQLRewriteContextDecorator> result = new HashMap<>(1, 1);
        if (baseRule instanceof EncryptRule) {
            result.put(baseRule, new EncryptSQLRewriteContextDecorator());
        } else if (baseRule instanceof ShadowRule) {
            result.put(baseRule, new ShadowSQLRewriteContextDecorator());
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private ExecutionContext doTransparentRoute(final String sql) {
        SQLStatement sqlStatement = logicSchema.getSqlParserEngine().parse(sql, false);
        ExecutionContext result = new ExecutionContext(new CommonSQLStatementContext(sqlStatement));
        result.getExecutionUnits().add(new ExecutionUnit(logicSchema.getDataSources().keySet().iterator().next(), new SQLUnit(sql, Collections.emptyList())));
        return result;
    }
    
    @Override
    public Statement createStatement(final Connection connection, final SQLUnit sqlUnit, final boolean isReturnGeneratedKeys) throws SQLException {
        return connection.createStatement();
    }
    
    @Override
    public boolean executeSQL(final Statement statement, final String sql, final boolean isReturnGeneratedKeys) throws SQLException {
        return statement.execute(sql, isReturnGeneratedKeys ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS);
    }
}
