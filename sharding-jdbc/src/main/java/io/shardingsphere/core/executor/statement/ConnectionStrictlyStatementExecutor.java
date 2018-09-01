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

package io.shardingsphere.core.executor.statement;

import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.executor.ShardingExecuteGroup;
import io.shardingsphere.core.executor.sql.execute.SQLExecuteCallback;
import io.shardingsphere.core.executor.sql.execute.SQLExecuteTemplate;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Statement executor for connection strictly mode.
 * 
 * @author zhangliang
 */
public final class ConnectionStrictlyStatementExecutor extends StatementExecutor {
    
    private final SQLExecuteTemplate executeTemplate;
    
    private final Map<String, List<ShardingExecuteGroup<StatementUnit>>> statementUnitGroups;
    
    public ConnectionStrictlyStatementExecutor(final SQLType sqlType, final SQLExecuteTemplate executeTemplate, final Map<String, List<ShardingExecuteGroup<StatementUnit>>> statementUnitGroups) {
        super(sqlType);
        this.executeTemplate = executeTemplate;
        this.statementUnitGroups = statementUnitGroups;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected <T> List<T> executeCallback(final SQLExecuteCallback<T> executeCallback) throws SQLException {
        return executeTemplate.execute((Map) statementUnitGroups, executeCallback);
    }
}
