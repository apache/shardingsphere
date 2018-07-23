/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.proxy.backend.common.jdbc.wrapper;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.parsing.SQLJudgeEngine;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.routing.PreparedStatementRoutingEngine;
import io.shardingsphere.core.routing.SQLExecutionUnit;
import io.shardingsphere.core.routing.SQLRouteResult;
import io.shardingsphere.core.routing.SQLUnit;
import io.shardingsphere.core.routing.router.masterslave.MasterSlaveRouter;
import io.shardingsphere.proxy.config.RuleRegistry;
import io.shardingsphere.proxy.transport.mysql.packet.command.statement.execute.PreparedStatementParameter;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;

/**
 * Executor wrapper for prepared statement.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class PreparedStatementExecutorWrapper implements JDBCExecutorWrapper {
    
    private final RuleRegistry ruleRegistry = RuleRegistry.getInstance();
    
    private final List<PreparedStatementParameter> preparedStatementParameters;
    
    @Override
    public SQLRouteResult route(final String sql, final DatabaseType databaseType) {
        return ruleRegistry.isMasterSlaveOnly() ? doMasterSlaveRoute(sql) : doShardingRoute(sql, databaseType);
    }
    
    private SQLRouteResult doMasterSlaveRoute(final String sql) {
        SQLStatement sqlStatement = new SQLJudgeEngine(sql).judge();
        SQLRouteResult result = new SQLRouteResult(sqlStatement);
        for (String each : new MasterSlaveRouter(ruleRegistry.getMasterSlaveRule(), ruleRegistry.isShowSQL()).route(sql)) {
            result.getExecutionUnits().add(new SQLExecutionUnit(each, new SQLUnit(sql, Collections.<List<Object>>emptyList())));
        }
        return result;
    }
    
    private SQLRouteResult doShardingRoute(final String sql, final DatabaseType databaseType) {
        PreparedStatementRoutingEngine routingEngine = new PreparedStatementRoutingEngine(
                sql, ruleRegistry.getShardingRule(), ruleRegistry.getShardingMetaData(), databaseType, ruleRegistry.isShowSQL(), ruleRegistry.getShardingDataSourceMetaData());
        return routingEngine.route(Lists.transform(preparedStatementParameters, new Function<PreparedStatementParameter, Object>() {
            
            @Override
            public Object apply(final PreparedStatementParameter input) {
                return input.getValue();
            }
        }));
    }
    
    @Override
    public Statement createStatement(final Connection connection, final String sql, final boolean isReturnGeneratedKeys) throws SQLException {
        PreparedStatement result = isReturnGeneratedKeys ? connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS) : connection.prepareStatement(sql);
        for (int i = 0; i < preparedStatementParameters.size(); i++) {
            result.setObject(i + 1, preparedStatementParameters.get(i).getValue());
        }
        return result;
    }
    
    @Override
    public boolean executeSQL(final Statement statement, final String sql, final boolean isReturnGeneratedKeys) throws SQLException {
        return ((PreparedStatement) statement).execute();
    }
}
