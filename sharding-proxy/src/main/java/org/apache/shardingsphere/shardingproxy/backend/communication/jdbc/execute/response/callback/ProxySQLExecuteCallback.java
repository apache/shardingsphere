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

package org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute.response.callback;

import org.apache.shardingsphere.core.constant.ConnectionMode;
import org.apache.shardingsphere.core.executor.sql.execute.SQLExecuteCallback;
import org.apache.shardingsphere.core.executor.sql.execute.result.MemoryQueryResult;
import org.apache.shardingsphere.core.executor.sql.execute.result.StreamQueryResult;
import org.apache.shardingsphere.core.merger.QueryResult;
import org.apache.shardingsphere.core.routing.RouteUnit;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute.response.unit.ExecuteQueryResponseUnit;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute.response.unit.ExecuteResponseUnit;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute.response.unit.ExecuteUpdateResponseUnit;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.wrapper.JDBCExecutorWrapper;
import org.apache.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import org.apache.shardingsphere.shardingproxy.runtime.schema.LogicSchema;
import org.apache.shardingsphere.shardingproxy.runtime.schema.MasterSlaveSchema;
import org.apache.shardingsphere.shardingproxy.runtime.schema.ShardingSchema;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.query.DataHeaderPacket;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.query.QueryResponsePackets;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * SQL execute callback for Sharding-Proxy.
 *
 * @author zhangliang
 */
public final class ProxySQLExecuteCallback extends SQLExecuteCallback<ExecuteResponseUnit> {
    
    private final BackendConnection backendConnection;
    
    private final JDBCExecutorWrapper jdbcExecutorWrapper;
    
    private final boolean isReturnGeneratedKeys;
    
    private final boolean fetchMetaData;
    
    private boolean hasMetaData;
    
    public ProxySQLExecuteCallback(final BackendConnection backendConnection, final JDBCExecutorWrapper jdbcExecutorWrapper, 
                                    final boolean isExceptionThrown, final boolean isReturnGeneratedKeys, final boolean fetchMetaData) {
        super(GlobalRegistry.getInstance().getDatabaseType(), isExceptionThrown);
        this.backendConnection = backendConnection;
        this.jdbcExecutorWrapper = jdbcExecutorWrapper;
        this.isReturnGeneratedKeys = isReturnGeneratedKeys;
        this.fetchMetaData = fetchMetaData;
    }
    
    @Override
    public ExecuteResponseUnit executeSQL(final RouteUnit routeUnit, final Statement statement, final ConnectionMode connectionMode) throws SQLException {
        boolean withMetaData = false;
        if (fetchMetaData && !hasMetaData) {
            hasMetaData = true;
            withMetaData = true;
        }
        return createExecuteResponseUnit(statement, routeUnit.getSqlUnit().getSql(), connectionMode, isReturnGeneratedKeys, withMetaData);
    }
    
    private ExecuteResponseUnit createExecuteResponseUnit(
            final Statement statement, final String sql, final ConnectionMode connectionMode, final boolean isReturnGeneratedKeys, final boolean withMetadata) throws SQLException {
        backendConnection.add(statement);
        if (!jdbcExecutorWrapper.executeSQL(statement, sql, isReturnGeneratedKeys)) {
            return new ExecuteUpdateResponseUnit(statement.getUpdateCount(), isReturnGeneratedKeys ? getGeneratedKey(statement) : 0L);
        }
        ResultSet resultSet = statement.getResultSet();
        backendConnection.add(resultSet);
        return new ExecuteQueryResponseUnit(withMetadata ? getHeaderPackets(resultSet.getMetaData()) : null, createQueryResult(resultSet, connectionMode));
    }
    
    private long getGeneratedKey(final Statement statement) throws SQLException {
        ResultSet resultSet = statement.getGeneratedKeys();
        return resultSet.next() ? resultSet.getLong(1) : 0L;
    }
    
    private QueryResponsePackets getHeaderPackets(final ResultSetMetaData resultSetMetaData) throws SQLException {
        int currentSequenceId = 1;
        int columnCount = resultSetMetaData.getColumnCount();
        Collection<DataHeaderPacket> dataHeaderPackets = new LinkedList<>();
        List<Integer> columnTypes = new ArrayList<>(128);
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            dataHeaderPackets.add(new DataHeaderPacket(++currentSequenceId, resultSetMetaData, backendConnection.getLogicSchema(), columnIndex));
            columnTypes.add(resultSetMetaData.getColumnType(columnIndex));
        }
        return new QueryResponsePackets(columnTypes, columnCount, dataHeaderPackets, ++currentSequenceId);
    }
    
    private QueryResult createQueryResult(final ResultSet resultSet, final ConnectionMode connectionMode) {
        LogicSchema logicSchema = backendConnection.getLogicSchema();
        if (logicSchema instanceof MasterSlaveSchema) {
            return connectionMode == ConnectionMode.MEMORY_STRICTLY ? new StreamQueryResult(resultSet) : new MemoryQueryResult(resultSet);
        }
        ShardingRule shardingRule = ((ShardingSchema) logicSchema).getShardingRule();
        return connectionMode == ConnectionMode.MEMORY_STRICTLY ? new StreamQueryResult(resultSet, shardingRule) : new MemoryQueryResult(resultSet, shardingRule);
    }
}
