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

package org.apache.shardingsphere.sharding.metadata.data.dialect.type;

import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.sharding.metadata.data.dialect.DialectShardingStatisticsTableCollector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * Sharding statistics table data collector of MySQL.
 */
public final class MySQLShardingStatisticsTableCollector implements DialectShardingStatisticsTableCollector {
    
    private static final String FETCH_TABLE_ROWS_AND_DATA_LENGTH_SQL = "SELECT TABLE_ROWS, DATA_LENGTH FROM information_schema.TABLES WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?";
    
    @Override
    public boolean appendRow(final Connection connection, final DataNode dataNode, final Map<String, Object> rowColumnValues) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(FETCH_TABLE_ROWS_AND_DATA_LENGTH_SQL)) {
            preparedStatement.setString(1, connection.getCatalog());
            preparedStatement.setString(2, dataNode.getTableName());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    rowColumnValues.put("row_count", resultSet.getBigDecimal(TABLE_ROWS_COLUMN_NAME));
                    rowColumnValues.put("size", resultSet.getBigDecimal(DATA_LENGTH_COLUMN_NAME));
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}
