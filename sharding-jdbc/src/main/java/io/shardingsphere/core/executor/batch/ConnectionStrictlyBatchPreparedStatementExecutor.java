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

package io.shardingsphere.core.executor.batch;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.executor.sql.SQLExecuteCallback;
import io.shardingsphere.core.executor.sql.SQLExecuteTemplate;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Prepared statement executor to process add batch for connection strictly mode.
 * 
 * @author zhangliang
 */
public final class ConnectionStrictlyBatchPreparedStatementExecutor extends BatchPreparedStatementExecutor {
    
    private final SQLExecuteTemplate executeTemplate;
    
    private final Map<String, List<List<BatchPreparedStatementUnit>>> batchPreparedStatementUnitGroups;
    
    public ConnectionStrictlyBatchPreparedStatementExecutor(final DatabaseType dbType, final SQLType sqlType, final int batchCount,
                                                            final SQLExecuteTemplate executeTemplate, final Map<String, List<List<BatchPreparedStatementUnit>>> batchPreparedStatementUnitGroups) {
        super(dbType, sqlType, batchCount);
        this.executeTemplate = executeTemplate;
        this.batchPreparedStatementUnitGroups = batchPreparedStatementUnitGroups;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected <T> List<T> executeCallback(final SQLExecuteCallback<T> executeCallback) throws SQLException {
        return executeTemplate.execute((Map) batchPreparedStatementUnitGroups, executeCallback);
    }
    
    @Override
    protected Collection<BatchPreparedStatementUnit> getBatchPreparedStatementUnitGroups() {
        Collection<BatchPreparedStatementUnit> result = new LinkedList<>();
        for (Entry<String, List<List<BatchPreparedStatementUnit>>> entry : batchPreparedStatementUnitGroups.entrySet()) {
            for (List<BatchPreparedStatementUnit> each : entry.getValue()) {
                result.addAll(each);
            }
        }
        return result;
    }
}
