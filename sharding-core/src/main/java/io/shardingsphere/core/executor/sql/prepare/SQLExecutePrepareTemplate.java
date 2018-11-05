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

package io.shardingsphere.core.executor.sql.prepare;

import com.google.common.collect.Lists;
import io.shardingsphere.core.constant.ConnectionMode;
import io.shardingsphere.core.executor.ShardingExecuteCallback;
import io.shardingsphere.core.executor.ShardingExecuteEngine;
import io.shardingsphere.core.executor.ShardingExecuteGroup;
import io.shardingsphere.core.executor.StatementExecuteUnit;
import io.shardingsphere.core.routing.RouteUnit;
import io.shardingsphere.core.routing.SQLUnit;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * SQL execute prepare template.
 *
 * @author zhaojun
 * @author zhangliang
 * @author panjuan
 */
@RequiredArgsConstructor
@AllArgsConstructor
public final class SQLExecutePrepareTemplate {
    
    private final int maxConnectionsSizePerQuery;
    
    private ShardingExecuteEngine shardingExecuteEngine;
    
    /**
     * Get execute unit groups.
     *
     * @param routeUnits route units
     * @param callback SQL execute prepare callback
     * @return statement execute unit groups
     * @throws SQLException SQL exception
     */
    public Collection<ShardingExecuteGroup<StatementExecuteUnit>> getExecuteUnitGroups(final Collection<RouteUnit> routeUnits, final SQLExecutePrepareCallback callback) throws SQLException {
        return null == shardingExecuteEngine ? getSynchronizedExecuteUnitGroups(routeUnits, callback) : getAsynchronizedExecuteUnitGroups(routeUnits, callback);
    }
    
    private Collection<ShardingExecuteGroup<StatementExecuteUnit>> getSynchronizedExecuteUnitGroups(
            final Collection<RouteUnit> routeUnits, final SQLExecutePrepareCallback callback) throws SQLException {
        Map<String, List<SQLUnit>> sqlUnitGroups = getSQLUnitGroups(routeUnits);
        Collection<ShardingExecuteGroup<StatementExecuteUnit>> result = new LinkedList<>();
        for (Entry<String, List<SQLUnit>> entry : sqlUnitGroups.entrySet()) {
            result.addAll(getSQLExecuteGroups(entry.getKey(), entry.getValue(), callback));
        }
        return result;
    }
    
    private Collection<ShardingExecuteGroup<StatementExecuteUnit>> getAsynchronizedExecuteUnitGroups(
            final Collection<RouteUnit> routeUnits, final SQLExecutePrepareCallback callback) throws SQLException {
        Map<String, List<SQLUnit>> sqlUnitGroups = getSQLUnitGroups(routeUnits);
        List<Collection<ShardingExecuteGroup<StatementExecuteUnit>>> results = shardingExecuteEngine.execute(sqlUnitGroups.entrySet(),
                new ShardingExecuteCallback<Entry<String, List<SQLUnit>>, Collection<ShardingExecuteGroup<StatementExecuteUnit>>>() {
                    
                    @Override
                    public Collection<ShardingExecuteGroup<StatementExecuteUnit>> execute(final Entry<String, List<SQLUnit>> input, final boolean isTrunkThread,
                                                                                          final Map<String, Object> shardingExecuteDataMap) throws SQLException {
                        return getSQLExecuteGroups(input.getKey(), input.getValue(), callback);
                    }
                });
        Collection<ShardingExecuteGroup<StatementExecuteUnit>> result = new LinkedList<>();
        for (Collection<ShardingExecuteGroup<StatementExecuteUnit>> each : results) {
            result.addAll(each);
        }
        return result;
    }
    
    private Map<String, List<SQLUnit>> getSQLUnitGroups(final Collection<RouteUnit> routeUnits) {
        Map<String, List<SQLUnit>> result = new LinkedHashMap<>(routeUnits.size(), 1);
        for (RouteUnit each : routeUnits) {
            if (!result.containsKey(each.getDataSourceName())) {
                result.put(each.getDataSourceName(), new LinkedList<SQLUnit>());
            }
            result.get(each.getDataSourceName()).add(each.getSqlUnit());
        }
        return result;
    }
    
    private List<ShardingExecuteGroup<StatementExecuteUnit>> getSQLExecuteGroups(
            final String dataSourceName, final List<SQLUnit> sqlUnits, final SQLExecutePrepareCallback callback) throws SQLException {
        List<ShardingExecuteGroup<StatementExecuteUnit>> result = new LinkedList<>();
        int desiredPartitionSize = Math.max(sqlUnits.size() / maxConnectionsSizePerQuery, 1);
        List<List<SQLUnit>> sqlUnitGroups = Lists.partition(sqlUnits, desiredPartitionSize);
        ConnectionMode connectionMode = maxConnectionsSizePerQuery < sqlUnits.size() ? ConnectionMode.CONNECTION_STRICTLY : ConnectionMode.MEMORY_STRICTLY;
        List<Connection> connections = callback.getConnections(connectionMode, dataSourceName, sqlUnitGroups.size());
        int count = 0;
        for (List<SQLUnit> each : sqlUnitGroups) {
            result.add(getSQLExecuteGroup(connectionMode, connections.get(count++), dataSourceName, each, callback));
        }
        return result;
    }
    
    private ShardingExecuteGroup<StatementExecuteUnit> getSQLExecuteGroup(final ConnectionMode connectionMode, final Connection connection, 
                                                                          final String dataSourceName, final List<SQLUnit> sqlUnitGroup, final SQLExecutePrepareCallback callback) throws SQLException {
        List<StatementExecuteUnit> result = new LinkedList<>();
        for (SQLUnit each : sqlUnitGroup) {
            result.add(callback.createStatementExecuteUnit(connection, new RouteUnit(dataSourceName, each), connectionMode));
        }
        return new ShardingExecuteGroup<>(result);
    }
}


