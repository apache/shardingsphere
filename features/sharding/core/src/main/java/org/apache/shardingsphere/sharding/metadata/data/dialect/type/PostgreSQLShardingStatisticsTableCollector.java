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

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Sharding statistics table data collector of PostgreSQL.
 */
public final class PostgreSQLShardingStatisticsTableCollector implements DialectShardingStatisticsTableCollector {
    
    private static final String POSTGRESQL_TABLE_ROWS_LENGTH = "SELECT RELTUPLES FROM PG_CLASS WHERE RELNAMESPACE = (SELECT OID FROM PG_NAMESPACE WHERE NSPNAME='%s') AND RELNAME = '%s'";
    
    private static final String POSTGRESQL_TABLE_DATA_LENGTH = "SELECT PG_RELATION_SIZE(RELID) as DATA_LENGTH  FROM PG_STAT_ALL_TABLES T WHERE SCHEMANAME='%s' AND RELNAME = '%s'";
    
    @Override
    public boolean appendRow(final DataSource dataSource, final DataNode dataNode, final List<Object> row) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(String.format(POSTGRESQL_TABLE_ROWS_LENGTH, dataNode.getSchemaName(), dataNode.getTableName()))) {
                row.add(resultSet.next() ? resultSet.getBigDecimal(TABLE_ROWS_COLUMN_NAME) : BigDecimal.ZERO);
            }
            try (ResultSet resultSet = statement.executeQuery(String.format(POSTGRESQL_TABLE_DATA_LENGTH, dataNode.getSchemaName(), dataNode.getTableName()))) {
                row.add(resultSet.next() ? resultSet.getBigDecimal(DATA_LENGTH_COLUMN_NAME) : BigDecimal.ZERO);
            }
        }
        return true;
    }
    
    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }
}
