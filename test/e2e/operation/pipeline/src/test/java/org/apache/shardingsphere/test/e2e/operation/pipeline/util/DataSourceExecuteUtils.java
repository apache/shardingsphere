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

package org.apache.shardingsphere.test.e2e.operation.pipeline.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.exception.external.sql.type.wrapper.SQLWrapperException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Data source execute utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourceExecuteUtils {
    
    /**
     * Execute SQL.
     *
     * @param dataSource data source
     * @param sql SQL
     * @throws SQLException SQL exception
     */
    public static void execute(final DataSource dataSource, final String sql) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
    
    /**
     * Execute SQL.
     *
     * @param dataSource data source
     * @param sql SQL
     * @param params parameters
     * @throws SQLWrapperException SQL wrapper exception
     */
    // TODO Throw SQLException
    public static void execute(final DataSource dataSource, final String sql, final Object[] params) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }
            preparedStatement.execute();
        } catch (final SQLException ex) {
            throw new SQLWrapperException(ex);
        }
    }
    
    /**
     * Execute SQL with batch mode.
     *
     * @param dataSource data source
     * @param sql SQL
     * @param paramsList parameters
     * @throws SQLException SQL exception
     */
    public static void executeBatch(final DataSource dataSource, final String sql, final List<Object[]> paramsList) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            int batchSize = 1000;
            int count = 0;
            for (Object[] each : paramsList) {
                for (int i = 0; i < each.length; i++) {
                    preparedStatement.setObject(i + 1, each[i]);
                }
                preparedStatement.addBatch();
                ++count;
                if (0 == count % batchSize) {
                    preparedStatement.executeBatch();
                }
            }
            if (count % batchSize > 0) {
                preparedStatement.executeBatch();
            }
        }
    }
}
