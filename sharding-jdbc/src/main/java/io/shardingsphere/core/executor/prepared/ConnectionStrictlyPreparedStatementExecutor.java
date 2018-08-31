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

package io.shardingsphere.core.executor.prepared;

import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.executor.sql.SQLExecuteCallback;
import io.shardingsphere.core.executor.sql.SQLExecuteTemplate;
import io.shardingsphere.core.executor.sql.StatementExecuteUnit;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Prepared statement executor for connection strictly mode.
 * 
 * @author zhangliang
 */
public final class ConnectionStrictlyPreparedStatementExecutor extends PreparedStatementExecutor {
    
    private final SQLExecuteTemplate executeTemplate;
    
    private final Map<String, List<List<PreparedStatementUnit>>> preparedStatementUnitGroups;
    
    public ConnectionStrictlyPreparedStatementExecutor(
            final SQLType sqlType, final SQLExecuteTemplate executeTemplate, final Map<String, List<List<PreparedStatementUnit>>> preparedStatementUnitGroups) {
        super(sqlType);
        this.executeTemplate = executeTemplate;
        this.preparedStatementUnitGroups = preparedStatementUnitGroups;
    }
    
    @Override
    protected <T> List<T> executeCallback(final SQLExecuteCallback<T> executeCallback) throws SQLException {
        return executeTemplate.execute(transformPreparedStatementUnitGroups(), executeCallback);
    }
    
    private Map<String, List<List<? extends StatementExecuteUnit>>> transformPreparedStatementUnitGroups() {
        Map<String, List<List<? extends StatementExecuteUnit>>> result = new HashMap<>(preparedStatementUnitGroups.size(), 1);
        for (Entry<String, List<List<PreparedStatementUnit>>> entry : preparedStatementUnitGroups.entrySet()) {
            List<List<PreparedStatementUnit>> preparedStatementUnitGroups = entry.getValue();
            for (List<PreparedStatementUnit> each : preparedStatementUnitGroups) {
                if (!result.containsKey(entry.getKey())) {
                    result.put(entry.getKey(), new LinkedList<List<? extends StatementExecuteUnit>>());
                }
                result.get(entry.getKey()).add(new LinkedList<>(each));
            }
        }
        return result;
    }
}
