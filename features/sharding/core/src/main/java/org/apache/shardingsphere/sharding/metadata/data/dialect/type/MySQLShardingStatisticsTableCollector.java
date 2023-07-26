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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Sharding statistics table data collector of MySQL.
 */
public final class MySQLShardingStatisticsTableCollector implements DialectShardingStatisticsTableCollector {
    
    private static final String MYSQL_TABLE_ROWS_AND_DATA_LENGTH = "SELECT TABLE_ROWS, DATA_LENGTH FROM information_schema.TABLES WHERE TABLE_SCHEMA = '%s' AND TABLE_NAME = '%s'";
    
    @Override
    public boolean appendRow(final Connection connection, final DataNode dataNode, final List<Object> row) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(String.format(MYSQL_TABLE_ROWS_AND_DATA_LENGTH, connection.getCatalog(), dataNode.getTableName()))) {
                if (resultSet.next()) {
                    row.add(resultSet.getBigDecimal(TABLE_ROWS_COLUMN_NAME));
                    row.add(resultSet.getBigDecimal(DATA_LENGTH_COLUMN_NAME));
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
