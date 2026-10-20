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

package org.apache.shardingsphere.database.connector.firebird.metadata.data.loader;

import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Loader for Firebird column sizes of system tables with not fixed length.
 */
@RequiredArgsConstructor
public final class FirebirdSystemTableColumnSizeLoader {
    
    private static final String SELECT_SYSTEM_TABLE_COLUMN_SIZES_SQL =
            "SELECT TRIM(rf.RDB$RELATION_NAME) AS TABLE_NAME, TRIM(rf.RDB$FIELD_NAME) AS COLUMN_NAME, f.RDB$CHARACTER_LENGTH AS CHAR_LEN, f.RDB$FIELD_LENGTH AS FIELD_LENGTH "
                    + "FROM RDB$RELATION_FIELDS rf "
                    + "JOIN RDB$FIELDS f ON rf.RDB$FIELD_SOURCE = f.RDB$FIELD_NAME "
                    + "WHERE rf.RDB$SYSTEM_FLAG = 1 AND f.RDB$FIELD_TYPE IN (14, 37)";
    
    private final DataSource dataSource;
    
    /**
     * Load column sizes of system tables.
     *
     * @return column sizes, keyed by system table name and upper case column name
     * @throws SQLException SQL exception
     */
    public Map<String, Map<String, Integer>> load() throws SQLException {
        Map<String, Map<String, Integer>> result = new HashMap<>();
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(SELECT_SYSTEM_TABLE_COLUMN_SIZES_SQL);
                ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                int characterLength = resultSet.getInt("CHAR_LEN");
                int columnSize = resultSet.wasNull() || characterLength <= 0 ? resultSet.getInt("FIELD_LENGTH") : characterLength;
                result.computeIfAbsent(resultSet.getString("TABLE_NAME"), key -> new HashMap<>()).put(resultSet.getString("COLUMN_NAME").toUpperCase(Locale.ENGLISH), columnSize);
            }
        }
        return result;
    }
}
