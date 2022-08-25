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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.statement;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.db.protocol.parameter.TypeUnspecifiedSQLParameter;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.DatabaseTypeAware;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.ExecutorJDBCStatementManager;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.Optional;

/**
 * JDBC backend statement.
 */
@Getter
@Setter
public final class JDBCBackendStatement implements ExecutorJDBCStatementManager, DatabaseTypeAware {
    
    private DatabaseType databaseType;
    
    @Override
    public Statement createStorageResource(final Connection connection, final ConnectionMode connectionMode, final StatementOption option) throws SQLException {
        Statement result = connection.createStatement();
        if (ConnectionMode.MEMORY_STRICTLY == connectionMode) {
            setFetchSize(result);
        }
        return result;
    }
    
    @Override
    public Statement createStorageResource(final ExecutionUnit executionUnit, final Connection connection, final ConnectionMode connectionMode, final StatementOption option) throws SQLException {
        String sql = executionUnit.getSqlUnit().getSql();
        List<Object> parameters = executionUnit.getSqlUnit().getParameters();
        PreparedStatement result = option.isReturnGeneratedKeys()
                ? connection.prepareStatement(executionUnit.getSqlUnit().getSql(), Statement.RETURN_GENERATED_KEYS)
                : connection.prepareStatement(sql);
        for (int i = 0; i < parameters.size(); i++) {
            Object parameter = parameters.get(i);
            if (parameter instanceof TypeUnspecifiedSQLParameter) {
                result.setObject(i + 1, parameter, Types.OTHER);
            } else {
                result.setObject(i + 1, parameter);
            }
        }
        if (ConnectionMode.MEMORY_STRICTLY == connectionMode) {
            setFetchSize(result);
        }
        return result;
    }
    
    private void setFetchSize(final Statement statement) throws SQLException {
        Optional<StatementMemoryStrictlyFetchSizeSetter> fetchSizeSetter = StatementMemoryStrictlyFetchSizeSetterFactory.findInstance(databaseType.getType());
        if (fetchSizeSetter.isPresent()) {
            fetchSizeSetter.get().setFetchSize(statement);
        }
    }
}
