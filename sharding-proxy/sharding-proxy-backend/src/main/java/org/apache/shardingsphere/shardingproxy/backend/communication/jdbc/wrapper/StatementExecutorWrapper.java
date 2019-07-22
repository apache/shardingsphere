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
import org.apache.shardingsphere.core.SimpleQueryShardingEngine;
import org.apache.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import org.apache.shardingsphere.core.optimize.api.statement.OptimizedStatement;
import org.apache.shardingsphere.core.optimize.encrypt.EncryptOptimizeEngineFactory;
import org.apache.shardingsphere.core.optimize.transparent.statement.TransparentOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.rewrite.SQLRewriteEngine;
import org.apache.shardingsphere.core.route.RouteUnit;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.route.SQLUnit;
import org.apache.shardingsphere.core.route.router.masterslave.MasterSlaveRouter;
import org.apache.shardingsphere.shardingproxy.backend.schema.EncryptSchema;
import org.apache.shardingsphere.shardingproxy.backend.schema.LogicSchema;
import org.apache.shardingsphere.shardingproxy.backend.schema.MasterSlaveSchema;
import org.apache.shardingsphere.shardingproxy.backend.schema.ShardingSchema;
import org.apache.shardingsphere.shardingproxy.context.ShardingProxyContext;
import org.apache.shardingsphere.spi.database.DatabaseType;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Executor wrapper for statement.
 *
 * @author zhangliang
 * @author pannjuan
 */
@RequiredArgsConstructor
public final class StatementExecutorWrapper implements JDBCExecutorWrapper {
    
    private static final ShardingProxyContext SHARDING_PROXY_CONTEXT = ShardingProxyContext.getInstance();
    
    private final LogicSchema logicSchema;
    
    @Override
    public SQLRouteResult route(final String sql, final DatabaseType databaseType) {
        if (logicSchema instanceof ShardingSchema) {
            return doShardingRoute(sql, databaseType);
        }
        if (logicSchema instanceof MasterSlaveSchema) {
            return doMasterSlaveRoute(sql);
        }
        if (logicSchema instanceof EncryptSchema) {
            return doEncryptRoute(sql);
        }
        return doTransparentRoute(sql);
    }
    
    private SQLRouteResult doShardingRoute(final String sql, final DatabaseType databaseType) {
        SimpleQueryShardingEngine shardingEngine = new SimpleQueryShardingEngine(logicSchema.getShardingRule(), 
                ShardingProxyContext.getInstance().getShardingProperties(), logicSchema.getMetaData(), databaseType, logicSchema.getParseEngine());
        return shardingEngine.shard(sql, Collections.emptyList());
    }
    
    private SQLRouteResult doMasterSlaveRoute(final String sql) {
        SQLStatement sqlStatement = logicSchema.getParseEngine().parse(sql, false);
        OptimizedStatement optimizedStatement = new TransparentOptimizedStatement(sqlStatement);
        SQLRewriteEngine sqlRewriteEngine = new SQLRewriteEngine(((MasterSlaveSchema) logicSchema).getMasterSlaveRule(), optimizedStatement, sql);
        String rewriteSQL = sqlRewriteEngine.generateSQL().getSql();
        SQLRouteResult result = new SQLRouteResult(optimizedStatement);
        for (String each : new MasterSlaveRouter(((MasterSlaveSchema) logicSchema).getMasterSlaveRule(), logicSchema.getParseEngine(),
                SHARDING_PROXY_CONTEXT.getShardingProperties().<Boolean>getValue(ShardingPropertiesConstant.SQL_SHOW)).route(rewriteSQL, false)) {
            result.getRouteUnits().add(new RouteUnit(each, new SQLUnit(rewriteSQL, Collections.emptyList())));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private SQLRouteResult doEncryptRoute(final String sql) {
        EncryptSchema encryptSchema = (EncryptSchema) logicSchema;
        SQLStatement sqlStatement = encryptSchema.getParseEngine().parse(sql, false);
        OptimizedStatement optimizedStatement = EncryptOptimizeEngineFactory.newInstance(
                sqlStatement).optimize(encryptSchema.getEncryptRule(), logicSchema.getMetaData().getTable(), sql, new LinkedList<>(), sqlStatement);
        SQLRewriteEngine sqlRewriteEngine = new SQLRewriteEngine(encryptSchema.getEncryptRule(), 
                optimizedStatement, sql, Collections.emptyList(), ShardingProxyContext.getInstance().getShardingProperties().<Boolean>getValue(ShardingPropertiesConstant.QUERY_WITH_CIPHER_COLUMN));
        SQLRouteResult result = new SQLRouteResult(optimizedStatement);
        result.getRouteUnits().add(new RouteUnit(logicSchema.getDataSources().keySet().iterator().next(), new SQLUnit(sqlRewriteEngine.generateSQL().getSql(), Collections.emptyList())));
        return result;
    }
    
    private SQLRouteResult doTransparentRoute(final String sql) {
        SQLRouteResult result = new SQLRouteResult(new TransparentOptimizedStatement(logicSchema.getParseEngine().parse(sql, false)));
        result.getRouteUnits().add(new RouteUnit(logicSchema.getDataSources().keySet().iterator().next(), new SQLUnit(sql, Collections.emptyList())));
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
