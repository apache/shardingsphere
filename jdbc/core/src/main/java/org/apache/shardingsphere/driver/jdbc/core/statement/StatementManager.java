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

package org.apache.shardingsphere.driver.jdbc.core.statement;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.driver.jdbc.adapter.executor.ForceExecuteTemplate;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.ExecutorJDBCStatementManager;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Statement manager.
 */
public final class StatementManager implements ExecutorJDBCStatementManager, AutoCloseable {
    
    private final Map<CacheKey, Statement> cachedStatements = new ConcurrentHashMap<>();
    
    private final ForceExecuteTemplate<Statement> forceExecuteTemplate = new ForceExecuteTemplate<>();
    
    @Override
    public Statement createStorageResource(final Connection connection, final ConnectionMode connectionMode, final StatementOption option, final DatabaseType databaseType) throws SQLException {
        return createStatement(connection, option);
    }
    
    @Override
    public Statement createStorageResource(final ExecutionUnit executionUnit, final Connection connection, final ConnectionMode connectionMode, final StatementOption option,
                                           final DatabaseType databaseType) throws SQLException {
        Statement result = cachedStatements.get(new CacheKey(executionUnit, connectionMode));
        if (null == result || result.isClosed() || result.getConnection().isClosed()) {
            String sql = executionUnit.getSqlUnit().getSql();
            if (option.isReturnGeneratedKeys()) {
                result = null == option.getColumns() || 0 == option.getColumns().length
                        ? connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
                        : connection.prepareStatement(sql, option.getColumns());
            } else {
                result = prepareStatement(connection, option, sql);
            }
            cachedStatements.put(new CacheKey(executionUnit, connectionMode), result);
        }
        return result;
    }
    
    @SuppressWarnings("MagicConstant")
    private Statement createStatement(final Connection connection, final StatementOption option) throws SQLException {
        Statement result;
        try {
            result = connection.createStatement(option.getResultSetType(), option.getResultSetConcurrency(), option.getResultSetHoldability());
        } catch (final SQLFeatureNotSupportedException ignore) {
            result = connection.createStatement();
        }
        return result;
    }
    
    @SuppressWarnings("MagicConstant")
    private PreparedStatement prepareStatement(final Connection connection, final StatementOption option, final String sql) throws SQLException {
        PreparedStatement result;
        try {
            result = connection.prepareStatement(sql, option.getResultSetType(), option.getResultSetConcurrency(), option.getResultSetHoldability());
        } catch (final SQLFeatureNotSupportedException ignore) {
            result = connection.prepareStatement(sql);
        }
        return result;
    }
    
    @Override
    public void close() throws SQLException {
        try {
            forceExecuteTemplate.execute(cachedStatements.values(), Statement::close);
        } finally {
            cachedStatements.clear();
        }
    }
    
    @RequiredArgsConstructor
    @EqualsAndHashCode
    private static final class CacheKey {
        
        private final ExecutionUnit executionUnit;
        
        private final ConnectionMode connectionMode;
    }
}
