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

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Sharding statistics table data collector of PostgreSQL.
 */
public final class PostgreSQLShardingStatisticsTableCollector implements DialectShardingStatisticsTableCollector {
    
    private static final String POSTGRESQL_TABLE_ROWS_LENGTH = "SELECT RELTUPLES FROM PG_CLASS WHERE RELNAMESPACE = (SELECT OID FROM PG_NAMESPACE WHERE NSPNAME= ?) AND RELNAME = ?";
    
    private static final String POSTGRESQL_TABLE_DATA_LENGTH = "SELECT PG_RELATION_SIZE(RELID) as DATA_LENGTH FROM PG_STAT_ALL_TABLES T WHERE SCHEMANAME= ? AND RELNAME = ?";
    
    @Override
    public boolean appendRow(final Connection connection, final DataNode dataNode, final List<Object> row) throws SQLException {
        row.add(getRowValue(connection, dataNode, POSTGRESQL_TABLE_ROWS_LENGTH, TABLE_ROWS_COLUMN_NAME).orElse(BigDecimal.ZERO));
        row.add(getRowValue(connection, dataNode, POSTGRESQL_TABLE_DATA_LENGTH, DATA_LENGTH_COLUMN_NAME).orElse(BigDecimal.ZERO));
        return true;
    }
    
    private Optional<BigDecimal> getRowValue(final Connection connection, final DataNode dataNode, final String sql, final String columnName) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, dataNode.getSchemaName());
            preparedStatement.setString(2, dataNode.getTableName());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() ? Optional.of(resultSet.getBigDecimal(columnName)) : Optional.empty();
            }
        }
    }
    
    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }
}
