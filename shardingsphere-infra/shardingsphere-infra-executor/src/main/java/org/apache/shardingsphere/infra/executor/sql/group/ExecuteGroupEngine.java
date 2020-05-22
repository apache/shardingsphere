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

package org.apache.shardingsphere.infra.executor.sql.group;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.infra.executor.kernel.InputGroup;
import org.apache.shardingsphere.infra.executor.sql.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.ExecutionConnection;
import org.apache.shardingsphere.infra.executor.sql.StorageResourceExecuteUnit;
import org.apache.shardingsphere.infra.executor.sql.StorageResourceOption;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.order.OrderedSPIRegistry;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Execute group engine.
 * 
 * @param <U> type of storage resource execute unit
 * @param <E> type of execution connection
 * @param <C> type of resource connection
 * @param <O> type of storage resource option
 */
public abstract class ExecuteGroupEngine<U extends StorageResourceExecuteUnit, E extends ExecutionConnection<C, ?, O>, C, O extends StorageResourceOption> {
    
    static {
        ShardingSphereServiceLoader.register(ExecuteGroupDecorator.class);
    }
    
    private final int maxConnectionsSizePerQuery;
    
    private final Map<ShardingSphereRule, ExecuteGroupDecorator> decorators;
    
    public ExecuteGroupEngine(final int maxConnectionsSizePerQuery, final Collection<ShardingSphereRule> rules) {
        this.maxConnectionsSizePerQuery = maxConnectionsSizePerQuery;
        decorators = OrderedSPIRegistry.getRegisteredServices(rules, ExecuteGroupDecorator.class);
    }
    
    /**
     * Generate storage resource execute unit groups.
     *
     * @param executionUnits execution units
     * @param executionConnection execution connection
     * @param option storage resource option
     * @return storage resource execute unit groups
     * @throws SQLException SQL exception
     */
    public Collection<InputGroup<U>> generate(final Collection<ExecutionUnit> executionUnits, final E executionConnection, final O option) throws SQLException {
        Collection<InputGroup<U>> inputGroups = new LinkedList<>();
        for (Entry<String, List<SQLUnit>> entry : generateSQLUnitGroups(executionUnits).entrySet()) {
            inputGroups.addAll(generateSQLExecuteGroups(entry.getKey(), entry.getValue(), executionConnection, option));
        }
        return decorate(inputGroups);
    }
    
    @SuppressWarnings("unchecked")
    private Collection<InputGroup<U>> decorate(final Collection<InputGroup<U>> inputGroups) {
        Collection<InputGroup<U>> result = inputGroups;
        for (Entry<ShardingSphereRule, ExecuteGroupDecorator> each : decorators.entrySet()) {
            result = each.getValue().decorate(result);
        }
        return result;
    }
    
    private Map<String, List<SQLUnit>> generateSQLUnitGroups(final Collection<ExecutionUnit> executionUnits) {
        Map<String, List<SQLUnit>> result = new LinkedHashMap<>(executionUnits.size(), 1);
        for (ExecutionUnit each : executionUnits) {
            if (!result.containsKey(each.getDataSourceName())) {
                result.put(each.getDataSourceName(), new LinkedList<>());
            }
            result.get(each.getDataSourceName()).add(each.getSqlUnit());
        }
        return result;
    }
    
    private List<InputGroup<U>> generateSQLExecuteGroups(final String dataSourceName, final List<SQLUnit> sqlUnits, final E executionConnection, final O option) throws SQLException {
        List<InputGroup<U>> result = new LinkedList<>();
        int desiredPartitionSize = Math.max(0 == sqlUnits.size() % maxConnectionsSizePerQuery ? sqlUnits.size() / maxConnectionsSizePerQuery : sqlUnits.size() / maxConnectionsSizePerQuery + 1, 1);
        List<List<SQLUnit>> sqlUnitPartitions = Lists.partition(sqlUnits, desiredPartitionSize);
        ConnectionMode connectionMode = maxConnectionsSizePerQuery < sqlUnits.size() ? ConnectionMode.CONNECTION_STRICTLY : ConnectionMode.MEMORY_STRICTLY;
        List<C> connections = executionConnection.getConnections(dataSourceName, sqlUnitPartitions.size(), connectionMode);
        int count = 0;
        for (List<SQLUnit> each : sqlUnitPartitions) {
            result.add(generateSQLExecuteGroup(dataSourceName, each, executionConnection, connections.get(count++), connectionMode, option));
        }
        return result;
    }
    
    private InputGroup<U> generateSQLExecuteGroup(final String dataSourceName, final List<SQLUnit> sqlUnitGroup, 
                                                  final E executionConnection, final C connection, final ConnectionMode connectionMode, final O option) throws SQLException {
        List<U> result = new LinkedList<>();
        for (SQLUnit each : sqlUnitGroup) {
            result.add(createStorageResourceExecuteUnit(new ExecutionUnit(dataSourceName, each), executionConnection, connection, connectionMode, option));
        }
        return new InputGroup<>(result);
    }
    
    protected abstract U createStorageResourceExecuteUnit(ExecutionUnit executionUnit, E executionConnection, C connection, ConnectionMode connectionMode, O option) throws SQLException;
}
