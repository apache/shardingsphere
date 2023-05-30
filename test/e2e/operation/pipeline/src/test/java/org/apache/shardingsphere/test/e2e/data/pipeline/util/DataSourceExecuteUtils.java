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

package org.apache.shardingsphere.test.e2e.data.pipeline.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
     * @throws RuntimeException runtime exception
     */
    public static void execute(final DataSource dataSource, final String sql) {
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().execute(sql);
        } catch (final SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Execute SQL.
     *
     * @param dataSource data source
     * @param sql SQL
     * @param parameters parameters
     * @throws RuntimeException runtime exception
     */
    public static void execute(final DataSource dataSource, final String sql, final Object[] parameters) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            for (int i = 0; i < parameters.length; i++) {
                preparedStatement.setObject(i + 1, parameters[i]);
            }
            preparedStatement.execute();
        } catch (final SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Execute SQL with batch mode.
     *
     * @param dataSource data source
     * @param sql SQL
     * @param parameters parameters
     * @throws RuntimeException runtime exception
     */
    public static void execute(final DataSource dataSource, final String sql, final List<Object[]> parameters) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            for (Object[] each : parameters) {
                for (int i = 0; i < each.length; i++) {
                    preparedStatement.setObject(i + 1, each[i]);
                }
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        } catch (final SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
}
