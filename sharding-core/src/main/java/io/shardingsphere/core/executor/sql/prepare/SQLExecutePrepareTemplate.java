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
import io.shardingsphere.core.executor.sql.StatementExecuteUnit;
import io.shardingsphere.core.routing.SQLExecutionUnit;
import io.shardingsphere.core.routing.SQLUnit;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * SQL execute prepare template.
 *
 * @author zhaojun
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class SQLExecutePrepareTemplate {
    
    private final int maxConnectionsSizePerQuery;
    
    /**
     * Get statement execute units.
     * 
     * @param sqlUnitGroups SQL unit groups
     * @param callback SQL execute prepare callback
     * @return key is data source name, value is statement execute unit groups
     * @throws SQLException SQL exception
     */
    public Map<String, List<List<StatementExecuteUnit>>> getStatementExecuteUnits(final Map<String, List<SQLUnit>> sqlUnitGroups, final SQLExecutePrepareCallback callback) throws SQLException {
        Map<String, List<List<StatementExecuteUnit>>> result = new HashMap<>(sqlUnitGroups.size(), 1);
        for (Entry<String, List<SQLUnit>> entry : sqlUnitGroups.entrySet()) {
            result.put(entry.getKey(), partitionSQLUnits(entry.getKey(), entry.getValue(), callback));
        }
        return result;
    }
    
    private List<List<StatementExecuteUnit>> partitionSQLUnits(final String dataSourceName, final List<SQLUnit> sqlUnits, final SQLExecutePrepareCallback callback) throws SQLException {
        List<List<StatementExecuteUnit>> result = new LinkedList<>();
        int desiredPartitionSize = Math.max(sqlUnits.size() / maxConnectionsSizePerQuery, 1);
        for (List<SQLUnit> each : Lists.partition(sqlUnits, desiredPartitionSize)) {
            // TODO get connection sync to prevent dead lock
            result.add(getStatementExecuteUnitGroup(callback.getConnection(dataSourceName), dataSourceName, each, callback));
        }
        return result;
    }
    
    private List<StatementExecuteUnit> getStatementExecuteUnitGroup(
            final Connection connection, final String dataSourceName, final List<SQLUnit> sqlUnitGroup, final SQLExecutePrepareCallback callback) throws SQLException {
        List<StatementExecuteUnit> result = new LinkedList<>();
        for (SQLUnit each : sqlUnitGroup) {
            result.add(callback.createStatementExecuteUnit(connection, new SQLExecutionUnit(dataSourceName, each)));
        }
        return result;
    }
}
