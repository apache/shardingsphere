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
import io.shardingsphere.core.executor.ShardingExecuteGroup;
import io.shardingsphere.core.executor.sql.execute.SQLExecuteCallback;
import io.shardingsphere.core.executor.sql.execute.SQLExecuteTemplate;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * Prepared statement executor for connection strictly mode.
 * 
 * @author zhangliang
 */
public final class ConnectionStrictlyPreparedStatementExecutor extends PreparedStatementExecutor {
    
    private final SQLExecuteTemplate executeTemplate;
    
    private final Collection<ShardingExecuteGroup<PreparedStatementExecuteUnit>> executeGroups;
    
    public ConnectionStrictlyPreparedStatementExecutor(
            final SQLType sqlType, final SQLExecuteTemplate executeTemplate, final Collection<ShardingExecuteGroup<PreparedStatementExecuteUnit>> executeGroups) {
        super(sqlType);
        this.executeTemplate = executeTemplate;
        this.executeGroups = executeGroups;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected <T> List<T> executeCallback(final SQLExecuteCallback<T> executeCallback) throws SQLException {
        return executeTemplate.executeGroup((Collection) executeGroups, executeCallback);
    }
}
