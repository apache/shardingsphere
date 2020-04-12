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

package org.apache.shardingsphere.sharding.execute.sql.prepare;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sharding.execute.sql.StatementExecuteUnit;
import org.apache.shardingsphere.underlying.executor.connection.ExecutionConnection;
import org.apache.shardingsphere.underlying.executor.connection.StatementOption;
import org.apache.shardingsphere.underlying.executor.constant.ConnectionMode;
import org.apache.shardingsphere.underlying.executor.context.ExecutionUnit;
import org.apache.shardingsphere.underlying.executor.context.SQLUnit;
import org.apache.shardingsphere.underlying.executor.kernel.InputGroup;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * SQL execute group engine.
 */
@RequiredArgsConstructor
public final class SQLExecuteGroupEngine {
    
    private final boolean isPreparedStatement;
    
    private final int maxConnectionsSizePerQuery;
    
    /**
     * Get execute unit groups.
     *
     * @param executionConnection execution connection
     * @param executionUnits execution units
     * @param statementOption statement option
     * @return statement execute unit groups
     * @throws SQLException SQL exception
     */
    public Collection<InputGroup<StatementExecuteUnit>> getExecuteUnitGroups(final ExecutionConnection executionConnection, final Collection<ExecutionUnit> executionUnits, 
                                                                             final StatementOption statementOption) throws SQLException {
        return getSynchronizedExecuteUnitGroups(executionConnection, executionUnits, statementOption);
    }
    
    private Collection<InputGroup<StatementExecuteUnit>> getSynchronizedExecuteUnitGroups(final ExecutionConnection executionConnection, final Collection<ExecutionUnit> executionUnits, 
            final StatementOption statementOption) throws SQLException {
        Map<String, List<SQLUnit>> sqlUnitGroups = getSQLUnitGroups(executionUnits);
        Collection<InputGroup<StatementExecuteUnit>> result = new LinkedList<>();
        for (Entry<String, List<SQLUnit>> entry : sqlUnitGroups.entrySet()) {
            result.addAll(getSQLExecuteGroups(executionConnection, entry.getKey(), entry.getValue(), statementOption));
        }
        return result;
    }
    
    private Map<String, List<SQLUnit>> getSQLUnitGroups(final Collection<ExecutionUnit> executionUnits) {
        Map<String, List<SQLUnit>> result = new LinkedHashMap<>(executionUnits.size(), 1);
        for (ExecutionUnit each : executionUnits) {
            if (!result.containsKey(each.getDataSourceName())) {
                result.put(each.getDataSourceName(), new LinkedList<>());
            }
            result.get(each.getDataSourceName()).add(each.getSqlUnit());
        }
        return result;
    }
    
    private List<InputGroup<StatementExecuteUnit>> getSQLExecuteGroups(final ExecutionConnection executionConnection, final String dataSourceName, final List<SQLUnit> sqlUnits, 
                                                                       final StatementOption statementOption) throws SQLException {
        List<InputGroup<StatementExecuteUnit>> result = new LinkedList<>();
        int desiredPartitionSize = Math.max(0 == sqlUnits.size() % maxConnectionsSizePerQuery ? sqlUnits.size() / maxConnectionsSizePerQuery : sqlUnits.size() / maxConnectionsSizePerQuery + 1, 1);
        List<List<SQLUnit>> sqlUnitPartitions = Lists.partition(sqlUnits, desiredPartitionSize);
        ConnectionMode connectionMode = maxConnectionsSizePerQuery < sqlUnits.size() ? ConnectionMode.CONNECTION_STRICTLY : ConnectionMode.MEMORY_STRICTLY;
        List<Connection> connections = executionConnection.getConnections(dataSourceName, sqlUnitPartitions.size(), connectionMode);
        int count = 0;
        for (List<SQLUnit> each : sqlUnitPartitions) {
            result.add(getSQLExecuteGroup(executionConnection, connections.get(count++), dataSourceName, each, connectionMode, statementOption));
        }
        return result;
    }
    
    private InputGroup<StatementExecuteUnit> getSQLExecuteGroup(final ExecutionConnection executionConnection, final Connection connection, 
                                                                final String dataSourceName, final List<SQLUnit> sqlUnitGroup,
                                                                final ConnectionMode connectionMode, final StatementOption statementOption) throws SQLException {
        List<StatementExecuteUnit> result = new LinkedList<>();
        for (SQLUnit each : sqlUnitGroup) {
            ExecutionUnit executionUnit = new ExecutionUnit(dataSourceName, each);
            Statement statement = isPreparedStatement ? executionConnection.createPreparedStatement(each.getSql(), each.getParameters(), connection, connectionMode, statementOption)
                    : executionConnection.createStatement(connection, connectionMode, statementOption);
            result.add(new StatementExecuteUnit(executionUnit, statement, connectionMode));
        }
        return new InputGroup<>(result);
    }
}
