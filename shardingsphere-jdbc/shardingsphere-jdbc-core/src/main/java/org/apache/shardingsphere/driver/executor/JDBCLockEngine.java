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

package org.apache.shardingsphere.driver.executor;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.lock.LockNameUtil;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;
import org.apache.shardingsphere.infra.context.refresher.MetaDataRefreshEngine;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JDBC lock engine.
 */
public final class JDBCLockEngine {
    
    private final String schemaName;
    
    private final MetaDataContexts metaDataContexts;
    
    private final JDBCExecutor jdbcExecutor;
    
    private final MetaDataRefreshEngine metadataRefreshEngine;
    
    private final Collection<String> lockNames = new ArrayList<>();
    
    public JDBCLockEngine(final String schemaName, final MetaDataContexts metaDataContexts, final JDBCExecutor jdbcExecutor) {
        this.schemaName = schemaName;
        this.metaDataContexts = metaDataContexts;
        this.jdbcExecutor = jdbcExecutor;
        metadataRefreshEngine = new MetaDataRefreshEngine(metaDataContexts.getMetaData(schemaName),
                metaDataContexts.getOptimizerContext().getMetaData().getSchemas().get(schemaName), metaDataContexts.getProps());
    }
    
    /**
     * Execute.
     * 
     * @param executionGroupContext execution group context
     * @param sqlStatementContext sql statement context
     * @param routeUnits route units
     * @param callback callback
     * @param <T> the type of return value
     * @return result
     * @throws SQLException SQL exception
     */
    public <T> List<T> execute(final ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext, final SQLStatementContext<?> sqlStatementContext,
                               final Collection<RouteUnit> routeUnits, final JDBCExecutorCallback<T> callback) throws SQLException {
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        if (metaDataContexts.getLock().isPresent()) {
            ShardingSphereLock lock = metaDataContexts.getLock().get();
            try {
                if (sqlStatement instanceof DDLStatement) {
                    tryTableLock(lock, sqlStatementContext.getTablesContext().getTableNames());
                } else if (sqlStatement instanceof DMLStatement && !(sqlStatement instanceof SelectStatement)) {
                    checkTableLock(lock, sqlStatementContext.getTablesContext().getTableNames());
                }
                return doExecute(executionGroupContext, routeUnits, callback, sqlStatement);
            } finally {
                if (!lockNames.isEmpty()) {
                    lockNames.forEach(lock::releaseLock);
                }
            }
        }
        return doExecute(executionGroupContext, routeUnits, callback, sqlStatement);
    }
    
    private void tryTableLock(final ShardingSphereLock lock, final Collection<String> tableNames) throws SQLException {
        for (String each : tableNames) {
            String lockName = LockNameUtil.getTableLockName(schemaName, each);
            if (!lock.tryLock(lockName)) {
                throw new SQLException(String.format("Table %s lock wait timeout of %s ms exceeded", each, lock.getDefaultTimeOut()));
            }
            lockNames.add(lockName);
        }
    }
    
    private void checkTableLock(final ShardingSphereLock lock, final Collection<String> tableNames) throws SQLException {
        for (String each : tableNames) {
            if (lock.isLocked(LockNameUtil.getTableLockName(schemaName, each))) {
                throw new SQLException(String.format("Table %s is locked", each));
            }
        }
    }
    
    private <T> List<T> doExecute(final ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext, final Collection<RouteUnit> routeUnits,
                                  final JDBCExecutorCallback<T> callback, final SQLStatement sqlStatement) throws SQLException {
        List<T> results = jdbcExecutor.execute(executionGroupContext, callback);
        refreshMetaData(sqlStatement, routeUnits);
        return results;
    }
    
    private void refreshMetaData(final SQLStatement sqlStatement, final Collection<RouteUnit> routeUnits) throws SQLException {
        metadataRefreshEngine.refresh(sqlStatement, routeUnits.stream().map(each -> each.getDataSourceMapper().getLogicName()).collect(Collectors.toList()));
    }
}
