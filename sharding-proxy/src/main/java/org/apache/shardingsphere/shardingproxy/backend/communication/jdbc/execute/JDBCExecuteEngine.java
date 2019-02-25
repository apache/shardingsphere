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

package org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute;

import lombok.Getter;
import org.apache.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import org.apache.shardingsphere.core.executor.ShardingExecuteGroup;
import org.apache.shardingsphere.core.executor.StatementExecuteUnit;
import org.apache.shardingsphere.core.executor.sql.execute.SQLExecuteTemplate;
import org.apache.shardingsphere.core.executor.sql.execute.threadlocal.ExecutorExceptionHandler;
import org.apache.shardingsphere.core.executor.sql.prepare.SQLExecutePrepareTemplate;
import org.apache.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import org.apache.shardingsphere.core.routing.SQLRouteResult;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute.callback.ProxyJDBCExecutePrepareCallback;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute.callback.ProxySQLExecuteCallback;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute.response.ExecuteQueryResponseUnit;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute.response.ExecuteResponseUnit;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.wrapper.JDBCExecutorWrapper;
import org.apache.shardingsphere.shardingproxy.backend.response.BackendResponse;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryHeader;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryResponse;
import org.apache.shardingsphere.shardingproxy.backend.response.update.UpdateResponse;
import org.apache.shardingsphere.shardingproxy.runtime.ExecutorContext;
import org.apache.shardingsphere.shardingproxy.runtime.GlobalRegistry;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * SQL Execute engine for JDBC.
 *
 * @author zhaojun
 * @author zhangliang
 * @author panjuan
 */
public final class JDBCExecuteEngine implements SQLExecuteEngine {
    
    @Getter
    private final BackendConnection backendConnection;
    
    @Getter
    private final JDBCExecutorWrapper jdbcExecutorWrapper;
    
    private final SQLExecutePrepareTemplate sqlExecutePrepareTemplate;
    
    private final SQLExecuteTemplate sqlExecuteTemplate;
    
    public JDBCExecuteEngine(final BackendConnection backendConnection, final JDBCExecutorWrapper jdbcExecutorWrapper) {
        this.backendConnection = backendConnection;
        this.jdbcExecutorWrapper = jdbcExecutorWrapper;
        int maxConnectionsSizePerQuery = GlobalRegistry.getInstance().getShardingProperties().<Integer>getValue(ShardingPropertiesConstant.MAX_CONNECTIONS_SIZE_PER_QUERY);
        sqlExecutePrepareTemplate = new SQLExecutePrepareTemplate(maxConnectionsSizePerQuery);
        sqlExecuteTemplate = new SQLExecuteTemplate(ExecutorContext.getInstance().getExecuteEngine(), backendConnection.isSerialExecute());
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public BackendResponse execute(final SQLRouteResult routeResult) throws SQLException {
        boolean isReturnGeneratedKeys = routeResult.getSqlStatement() instanceof InsertStatement;
        boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        Collection<ShardingExecuteGroup<StatementExecuteUnit>> sqlExecuteGroups = sqlExecutePrepareTemplate.getExecuteUnitGroups(
                routeResult.getRouteUnits(), new ProxyJDBCExecutePrepareCallback(backendConnection, jdbcExecutorWrapper, isReturnGeneratedKeys));
        Collection<ExecuteResponseUnit> executeResponseUnits = sqlExecuteTemplate.executeGroup((Collection) sqlExecuteGroups, 
                new ProxySQLExecuteCallback(backendConnection, jdbcExecutorWrapper, isExceptionThrown, isReturnGeneratedKeys, true), 
                new ProxySQLExecuteCallback(backendConnection, jdbcExecutorWrapper, isExceptionThrown, isReturnGeneratedKeys, false));
        ExecuteResponseUnit firstExecuteResponseUnit = executeResponseUnits.iterator().next();
        return firstExecuteResponseUnit instanceof ExecuteQueryResponseUnit
                ? getExecuteQueryResponse(((ExecuteQueryResponseUnit) firstExecuteResponseUnit).getQueryHeaders(), executeResponseUnits) : new UpdateResponse(executeResponseUnits);
    }
    
    private BackendResponse getExecuteQueryResponse(final List<QueryHeader> queryHeaders, final Collection<ExecuteResponseUnit> executeResponseUnits) {
        QueryResponse result = new QueryResponse(queryHeaders);
        for (ExecuteResponseUnit each : executeResponseUnits) {
            result.getQueryResults().add(((ExecuteQueryResponseUnit) each).getQueryResult());
        }
        return result;
    }
}
