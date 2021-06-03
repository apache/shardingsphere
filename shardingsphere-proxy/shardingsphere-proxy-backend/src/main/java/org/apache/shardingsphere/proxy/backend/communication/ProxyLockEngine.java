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

package org.apache.shardingsphere.proxy.backend.communication;

import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.lock.LockNameUtil;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;
import org.apache.shardingsphere.infra.metadata.engine.MetadataRefreshEngine;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.TableLockWaitTimeoutException;
import org.apache.shardingsphere.proxy.backend.exception.TableLockedException;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Proxy lock engine.
 */
public final class ProxyLockEngine {
    
    private final ProxySQLExecutor proxySQLExecutor;
    
    private final MetadataRefreshEngine metadataRefreshEngine;
    
    private final String schemaName;
    
    private final Collection<String> lockNames = new ArrayList<>();
    
    public ProxyLockEngine(final ProxySQLExecutor proxySQLExecutor, final MetadataRefreshEngine metadataRefreshEngine, final String schemaName) {
        this.proxySQLExecutor = proxySQLExecutor;
        this.metadataRefreshEngine = metadataRefreshEngine;
        this.schemaName = schemaName;
    }
    
    /**
     * Execute.
     * 
     * @param executionContext execution context
     * @return collection of execute result
     * @throws SQLException SQL exception
     */
    public Collection<ExecuteResult> execute(final ExecutionContext executionContext) throws SQLException {
        if (ProxyContext.getInstance().getLock().isPresent()) {
            ShardingSphereLock lock = ProxyContext.getInstance().getLock().get();
            try {
                SQLStatement sqlStatement = executionContext.getSqlStatementContext().getSqlStatement();
                if (sqlStatement instanceof DDLStatement) {
                    tryTableLock(lock, executionContext.getSqlStatementContext().getTablesContext().getTableNames());
                } else if (sqlStatement instanceof DMLStatement && !(sqlStatement instanceof SelectStatement)) {
                    checkTableLock(lock, executionContext.getSqlStatementContext().getTablesContext().getTableNames());
                }
                return doExecute(executionContext);
            } finally {
                if (!lockNames.isEmpty()) {
                    lockNames.forEach(lock::releaseLock);
                }
            }
        }
        return doExecute(executionContext);
    }
    
    private void tryTableLock(final ShardingSphereLock lock, final Collection<String> tableNames) {
        for (String tableName : tableNames) {
            String lockName = LockNameUtil.getTableLockName(schemaName, tableName);
            if (!lock.tryLock(lockName)) {
                throw new TableLockWaitTimeoutException(schemaName, tableName, lock.getDefaultTimeOut());
            }
            lockNames.add(lockName);
        }
    }
    
    private void checkTableLock(final ShardingSphereLock lock, final Collection<String> tableNames) {
        for (String tableName : tableNames) {
            if (lock.isLocked(LockNameUtil.getTableLockName(schemaName, tableName))) {
                throw new TableLockedException(schemaName, tableName);
            }
        }
    }
    
    private Collection<ExecuteResult> doExecute(final ExecutionContext executionContext) throws SQLException {
        Collection<ExecuteResult> result = proxySQLExecutor.execute(executionContext);
        refreshMetadata(executionContext);
        return result;
    }
    
    private void refreshMetadata(final ExecutionContext executionContext) throws SQLException {
        SQLStatement sqlStatement = executionContext.getSqlStatementContext().getSqlStatement();
        metadataRefreshEngine.refresh(sqlStatement, executionContext.getRouteContext().getRouteUnits().stream().map(each -> each.getDataSourceMapper().getLogicName()).collect(Collectors.toList()));
    }
}
