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
import io.shardingsphere.core.executor.sql.execute.SQLExecuteCallback;
import io.shardingsphere.core.executor.sql.execute.SQLExecuteTemplate;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * Prepared statement executor to process add batch for memory strictly mode.
 * 
 * @author zhangliang
 */
public final class MemoryStrictlyBatchPreparedStatementExecutor extends BatchPreparedStatementExecutor {
    
    private final SQLExecuteTemplate executeTemplate;
    
    private final Collection<BatchPreparedStatementExecuteUnit> batchPreparedStatementUnits;
    
    public MemoryStrictlyBatchPreparedStatementExecutor(final DatabaseType dbType, final SQLType sqlType, final int batchCount, 
                                                        final SQLExecuteTemplate executeTemplate, final Collection<BatchPreparedStatementExecuteUnit> batchPreparedStatementUnits) {
        super(dbType, sqlType, batchCount);
        this.executeTemplate = executeTemplate;
        this.batchPreparedStatementUnits = batchPreparedStatementUnits;
    }
    
    @Override
    protected <T> List<T> executeCallback(final SQLExecuteCallback<T> executeCallback) throws SQLException {
        return executeTemplate.execute(batchPreparedStatementUnits, executeCallback);
    }
    
    @Override
    protected Collection<BatchPreparedStatementExecuteUnit> getBatchPreparedStatementUnitGroups() {
        return batchPreparedStatementUnits;
    }
}
