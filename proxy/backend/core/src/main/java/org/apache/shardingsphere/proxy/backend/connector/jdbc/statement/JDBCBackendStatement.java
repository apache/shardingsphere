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

package org.apache.shardingsphere.proxy.backend.connector.jdbc.statement;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.protocol.parameter.TypeUnspecifiedSQLParameter;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.ExecutorJDBCStatementManager;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.PreparedStatementCacheContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * JDBC backend statement.
 */
public final class JDBCBackendStatement implements ExecutorJDBCStatementManager {
    
    private final ConnectionSession connectionSession;
    
    public JDBCBackendStatement() {
        this(null);
    }
    
    public JDBCBackendStatement(final ConnectionSession connectionSession) {
        this.connectionSession = connectionSession;
    }
    
    @Override
    public Statement createStorageResource(final Connection connection, final ConnectionMode connectionMode, final StatementOption option, final DatabaseType databaseType) throws SQLException {
        Statement result = connection.createStatement();
        if (ConnectionMode.MEMORY_STRICTLY == connectionMode) {
            setFetchSize(result, databaseType);
        }
        return result;
    }
    
    @Override
    public Statement createStorageResource(final ExecutionUnit executionUnit, final Connection connection, final int connectionOffset,
                                           final ConnectionMode connectionMode, final StatementOption option, final DatabaseType databaseType) throws SQLException {
        String sql = executionUnit.getSqlUnit().getSql();
        List<Object> params = executionUnit.getSqlUnit().getParameters();
        PreparedStatement result = getPreparedStatement(connection, sql, option.isReturnGeneratedKeys());
        result.clearParameters();
        Iterator<Object> paramIterator = params.iterator();
        int index = 0;
        while (paramIterator.hasNext()) {
            Object param = paramIterator.next();
            if (param instanceof TypeUnspecifiedSQLParameter) {
                result.setObject(index + 1, param, Types.OTHER);
            } else {
                result.setObject(index + 1, param);
            }
            index++;
        }
        if (ConnectionMode.MEMORY_STRICTLY == connectionMode) {
            setFetchSize(result, databaseType);
        }
        return result;
    }
    
    private PreparedStatement getPreparedStatement(final Connection connection, final String sql, final boolean returnGeneratedKeys) throws SQLException {
        if (null == connectionSession) {
            return createPreparedStatement(connection, sql, returnGeneratedKeys);
        }
        PreparedStatementCacheContext cacheContext = connectionSession.getPreparedStatementCacheContext();
        return cacheContext.getOrCreate(connection, sql, returnGeneratedKeys, () -> createPreparedStatement(connection, sql, returnGeneratedKeys));
    }
    
    private PreparedStatement createPreparedStatement(final Connection connection, final String sql, final boolean returnGeneratedKeys) throws SQLException {
        return returnGeneratedKeys ? connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS) : connection.prepareStatement(sql);
    }
    
    private void setFetchSize(final Statement statement, final DatabaseType databaseType) throws SQLException {
        Optional<StatementMemoryStrictlyFetchSizeSetter> fetchSizeSetter = DatabaseTypedSPILoader.findService(StatementMemoryStrictlyFetchSizeSetter.class, databaseType);
        if (fetchSizeSetter.isPresent()) {
            fetchSizeSetter.get().setFetchSize(statement);
        }
    }
}
