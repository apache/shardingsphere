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
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import org.apache.shardingsphere.core.parse.SQLJudgeEngine;
import org.apache.shardingsphere.core.parse.parser.sql.SQLStatement;
import org.apache.shardingsphere.core.rewrite.MasterSlaveSQLRewriteEngine;
import org.apache.shardingsphere.core.rewrite.SQLBuilder;
import org.apache.shardingsphere.core.rewrite.SQLRewriteEngine;
import org.apache.shardingsphere.core.routing.RouteUnit;
import org.apache.shardingsphere.core.routing.SQLLogger;
import org.apache.shardingsphere.core.routing.SQLRouteResult;
import org.apache.shardingsphere.core.routing.SQLUnit;
import org.apache.shardingsphere.core.routing.StatementRoutingEngine;
import org.apache.shardingsphere.core.routing.router.masterslave.MasterSlaveRouter;
import org.apache.shardingsphere.core.routing.type.TableUnit;
import org.apache.shardingsphere.shardingproxy.backend.schema.LogicSchema;
import org.apache.shardingsphere.shardingproxy.backend.schema.MasterSlaveSchema;
import org.apache.shardingsphere.shardingproxy.backend.schema.ShardingSchema;
import org.apache.shardingsphere.shardingproxy.context.ShardingProxyContext;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

/**
 * Executor wrapper for statement.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class StatementExecutorWrapper implements JDBCExecutorWrapper {
    
    private static final ShardingProxyContext SHARDING_PROXY_CONTEXT = ShardingProxyContext.getInstance();
    
    private final LogicSchema logicSchema;
    
    @Override
    public SQLRouteResult route(final String sql, final DatabaseType databaseType) {
        return logicSchema instanceof MasterSlaveSchema ? doMasterSlaveRoute(sql) : doShardingRoute(sql, databaseType);
    }
    
    private SQLRouteResult doMasterSlaveRoute(final String sql) {
        SQLStatement sqlStatement = new SQLJudgeEngine(sql).judge();
        String rewriteSQL = new MasterSlaveSQLRewriteEngine(((MasterSlaveSchema) logicSchema).getMasterSlaveRule(), sql, sqlStatement, logicSchema.getMetaData()).rewrite();
        SQLRouteResult result = new SQLRouteResult(sqlStatement);
        for (String each : new MasterSlaveRouter(
                ((MasterSlaveSchema) logicSchema).getMasterSlaveRule(), SHARDING_PROXY_CONTEXT.getShardingProperties().<Boolean>getValue(ShardingPropertiesConstant.SQL_SHOW)).route(rewriteSQL)) {
            result.getRouteUnits().add(new RouteUnit(each, new SQLUnit(rewriteSQL, Collections.emptyList())));
        }
        return result;
    }
    
    private SQLRouteResult doShardingRoute(final String sql, final DatabaseType databaseType) {
        StatementRoutingEngine routingEngine = new StatementRoutingEngine(
                ((ShardingSchema) logicSchema).getShardingRule(), logicSchema.getMetaData(), databaseType, logicSchema.getParsingResultCache());
        SQLRouteResult result = routingEngine.route(sql);
        result.getRouteUnits().addAll(getRouteUnits(sql, databaseType, (ShardingSchema) logicSchema, result));
        return result;
    }
    
    private Collection<RouteUnit> getRouteUnits(final String logicSQL, final DatabaseType databaseType, final ShardingSchema logicSchema, final SQLRouteResult routeResult) {
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(
                logicSchema.getShardingRule(), logicSQL, databaseType, routeResult.getSqlStatement(), Collections.emptyList(), routeResult.getOptimizeResult());
        SQLBuilder sqlBuilder = rewriteEngine.rewrite(routeResult.getRoutingResult().isSingleRouting());
        Collection<RouteUnit> result = new LinkedHashSet<>();
        for (TableUnit each : routeResult.getRoutingResult().getTableUnits().getTableUnits()) {
            result.add(new RouteUnit(each.getDataSourceName(), rewriteEngine.generateSQL(each, sqlBuilder, logicSchema.getMetaData().getDataSource())));
        }
        if (ShardingProxyContext.getInstance().getShardingProperties().getValue(ShardingPropertiesConstant.SQL_SHOW)) {
            SQLLogger.logSQL(logicSQL, routeResult.getSqlStatement(), routeResult.getRouteUnits());
        }
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
