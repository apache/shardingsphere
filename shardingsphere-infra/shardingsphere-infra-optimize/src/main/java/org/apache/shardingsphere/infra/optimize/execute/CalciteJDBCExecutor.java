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

package org.apache.shardingsphere.infra.optimize.execute;

import lombok.RequiredArgsConstructor;
import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.validate.SqlConformanceEnum;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Calcite JDBC executor.
 */
@RequiredArgsConstructor
public final class CalciteJDBCExecutor {
    
    public static final String CONNECTION_URL = "jdbc:calcite:";
    
    public static final String DRIVER_NAME = "org.apache.calcite.jdbc.Driver";
    
    public static final Properties PROPERTIES = new Properties();
    
    private final JDBCExecutor jdbcExecutor;
    
    static {
        try {
            Class.forName(DRIVER_NAME);
        } catch (final ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
        PROPERTIES.setProperty("lex", Lex.MYSQL.name());
        PROPERTIES.setProperty("conformance", SqlConformanceEnum.MYSQL_5.name());
    }
    
    /**
     * Execute query.
     *
     * @param executionContext execution context
     * @param callback JDBC execute callback
     * @param <T> class type of return value
     * @return execute result
     * @throws SQLException SQL exception
     */
    public <T> List<T> executeQuery(final ExecutionContext executionContext, final JDBCExecutorCallback<T> callback) throws SQLException {
        return Collections.emptyList();
    }
    
    private ResultSet execute(final String sql, final List<Object> parameters) throws SQLException {
        PreparedStatement statement = DriverManager.getConnection(CONNECTION_URL, PROPERTIES).prepareStatement(sql);
        setParameters(statement, parameters);
        return statement.executeQuery();
    }
    
    private void setParameters(final PreparedStatement preparedStatement, final List<Object> parameters) throws SQLException {
        int count = 1;
        for (Object each : parameters) {
            preparedStatement.setObject(count, each);
            count++;
        }
    }
}
