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
import java.util.Map;

/**
 * Sharding statistics table data collector of openGauss.
 */
public final class OpenGaussShardingStatisticsTableCollector implements DialectShardingStatisticsTableCollector {
    
    private static final String OPENGAUSS_TABLE_ROWS_AND_DATA_LENGTH = "SELECT RELTUPLES AS TABLE_ROWS, PG_TABLE_SIZE('%s') AS DATA_LENGTH FROM PG_CLASS WHERE RELNAME = '%s'";
    
    @Override
    public void appendRow(final Map<String, DataSource> dataSources, final DataNode dataNode, final List<Object> row) throws SQLException {
        try (Connection connection = dataSources.get(dataNode.getDataSourceName()).getConnection()) {
            if (isTableExist(connection, dataNode.getTableName())) {
                appendRow(dataNode, row, connection);
            } else {
                row.add(BigDecimal.ZERO);
                row.add(BigDecimal.ZERO);
            }
        }
    }
    
    private void appendRow(final DataNode dataNode, final List<Object> row, final Connection connection) throws SQLException {
        try (
                Statement statement = connection.createStatement()) {
            try (
                    ResultSet resultSet = statement
                            .executeQuery(String.format(OPENGAUSS_TABLE_ROWS_AND_DATA_LENGTH, dataNode.getTableName(), dataNode.getTableName()))) {
                if (resultSet.next()) {
                    row.add(resultSet.getBigDecimal(TABLE_ROWS_COLUMN_NAME));
                    row.add(resultSet.getBigDecimal(DATA_LENGTH_COLUMN_NAME));
                }
            }
        }
    }
    
    private boolean isTableExist(final Connection connection, final String tableNamePattern) throws SQLException {
        try (ResultSet resultSet = connection.getMetaData().getTables(connection.getCatalog(), connection.getSchema(), tableNamePattern, null)) {
            return resultSet.next();
        }
    }
    
    @Override
    public String getDatabaseType() {
        return "openGauss";
    }
}
