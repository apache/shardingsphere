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

package org.apache.shardingsphere.sharding.metadata.data;

import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.database.mysql.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.opengauss.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.postgresql.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereRowData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.statistics.collector.ShardingSphereStatisticsCollector;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Sharding statistics table data collector.
 */
public final class ShardingStatisticsTableCollector implements ShardingSphereStatisticsCollector {
    
    private static final String SHARDING_TABLE_STATISTICS = "sharding_table_statistics";
    
    private static final String MYSQL_TABLE_ROWS_AND_DATA_LENGTH = "SELECT TABLE_ROWS, DATA_LENGTH FROM information_schema.TABLES WHERE TABLE_SCHEMA = '%s' AND TABLE_NAME = '%s'";
    
    private static final String POSTGRESQL_TABLE_ROWS_LENGTH = "SELECT RELTUPLES FROM PG_CLASS WHERE RELNAMESPACE = (SELECT OID FROM PG_NAMESPACE WHERE NSPNAME='%s') AND RELNAME = '%s'";
    
    private static final String POSTGRESQL_TABLE_DATA_LENGTH = "SELECT PG_RELATION_SIZE(RELID) as DATA_LENGTH  FROM PG_STAT_ALL_TABLES T WHERE SCHEMANAME='%s' AND RELNAME = '%s'";
    
    private static final String OPENGAUSS_TABLE_ROWS_AND_DATA_LENGTH = "SELECT RELTUPLES AS TABLE_ROWS, PG_TABLE_SIZE('%s') AS DATA_LENGTH FROM PG_CLASS WHERE RELNAME = '%s'";
    
    private static final String TABLE_ROWS_COLUMN_NAME = "TABLE_ROWS";
    
    private static final String DATA_LENGTH_COLUMN_NAME = "DATA_LENGTH";
    
    @Override
    public Optional<ShardingSphereTableData> collect(final String databaseName, final ShardingSphereTable table,
                                                     final Map<String, ShardingSphereDatabase> shardingSphereDatabases) throws SQLException {
        ShardingSphereTableData result = new ShardingSphereTableData(SHARDING_TABLE_STATISTICS);
        DatabaseType protocolType = shardingSphereDatabases.values().iterator().next().getProtocolType();
        if (protocolType.getDefaultSchema().isPresent()) {
            collectFromDatabase(shardingSphereDatabases.get(databaseName), result);
        } else {
            for (ShardingSphereDatabase each : shardingSphereDatabases.values()) {
                collectFromDatabase(each, result);
            }
        }
        return result.getRows().isEmpty() ? Optional.empty() : Optional.of(result);
    }
    
    private void collectFromDatabase(final ShardingSphereDatabase shardingSphereDatabase, final ShardingSphereTableData tableData) throws SQLException {
        Optional<ShardingRule> shardingRule = shardingSphereDatabase.getRuleMetaData().findSingleRule(ShardingRule.class);
        if (!shardingRule.isPresent()) {
            return;
        }
        collectForShardingStatisticTable(shardingSphereDatabase, shardingRule.get(), tableData);
    }
    
    private void collectForShardingStatisticTable(final ShardingSphereDatabase shardingSphereDatabase, final ShardingRule shardingRule, final ShardingSphereTableData tableData) throws SQLException {
        int count = 1;
        for (TableRule each : shardingRule.getTableRules().values()) {
            for (DataNode dataNode : each.getActualDataNodes()) {
                List<Object> row = new LinkedList<>();
                row.add(count++);
                row.add(shardingSphereDatabase.getName());
                row.add(each.getLogicTable());
                row.add(dataNode.getDataSourceName());
                row.add(dataNode.getTableName());
                addTableRowsAndDataLength(shardingSphereDatabase.getResourceMetaData().getStorageTypes(), shardingSphereDatabase.getResourceMetaData().getDataSources(), dataNode, row);
                tableData.getRows().add(new ShardingSphereRowData(row));
            }
        }
    }
    
    private void addTableRowsAndDataLength(final Map<String, DatabaseType> databaseTypes, final Map<String, DataSource> dataSources,
                                           final DataNode dataNode, final List<Object> row) throws SQLException {
        DatabaseType databaseType = databaseTypes.get(dataNode.getDataSourceName());
        if (databaseType instanceof MySQLDatabaseType) {
            addForMySQL(dataSources, dataNode, row);
        } else if (databaseType instanceof PostgreSQLDatabaseType) {
            addForPostgreSQL(dataSources, dataNode, row);
        } else if (databaseType instanceof OpenGaussDatabaseType) {
            addForOpenGauss(dataSources, dataNode, row);
        } else {
            row.add(BigDecimal.ZERO);
            row.add(BigDecimal.ZERO);
        }
    }
    
    private void addForMySQL(final Map<String, DataSource> dataSources, final DataNode dataNode, final List<Object> row) throws SQLException {
        DataSource dataSource = dataSources.get(dataNode.getDataSourceName());
        BigDecimal tableRows = BigDecimal.ZERO;
        BigDecimal dataLength = BigDecimal.ZERO;
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(String.format(MYSQL_TABLE_ROWS_AND_DATA_LENGTH, connection.getCatalog(), dataNode.getTableName()))) {
                if (resultSet.next()) {
                    tableRows = resultSet.getBigDecimal(TABLE_ROWS_COLUMN_NAME);
                    dataLength = resultSet.getBigDecimal(DATA_LENGTH_COLUMN_NAME);
                }
            }
        }
        row.add(tableRows);
        row.add(dataLength);
    }
    
    private void addForPostgreSQL(final Map<String, DataSource> dataSources, final DataNode dataNode, final List<Object> row) throws SQLException {
        DataSource dataSource = dataSources.get(dataNode.getDataSourceName());
        BigDecimal tableRows = BigDecimal.ZERO;
        BigDecimal dataLength = BigDecimal.ZERO;
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(String.format(POSTGRESQL_TABLE_ROWS_LENGTH, dataNode.getSchemaName(), dataNode.getTableName()))) {
                if (resultSet.next()) {
                    tableRows = resultSet.getBigDecimal(TABLE_ROWS_COLUMN_NAME);
                }
            }
            try (ResultSet resultSet = statement.executeQuery(String.format(POSTGRESQL_TABLE_DATA_LENGTH, dataNode.getSchemaName(), dataNode.getTableName()))) {
                if (resultSet.next()) {
                    dataLength = resultSet.getBigDecimal(DATA_LENGTH_COLUMN_NAME);
                }
            }
        }
        row.add(tableRows);
        row.add(dataLength);
    }
    
    private void addForOpenGauss(final Map<String, DataSource> dataSources, final DataNode dataNode, final List<Object> row) throws SQLException {
        try (Connection connection = dataSources.get(dataNode.getDataSourceName()).getConnection()) {
            if (isTableExist(connection, dataNode.getTableName())) {
                doAddForOpenGauss(dataNode, row, connection);
            } else {
                row.add(BigDecimal.ZERO);
                row.add(BigDecimal.ZERO);
            }
        }
    }
    
    private boolean isTableExist(final Connection connection, final String tableNamePattern) throws SQLException {
        try (ResultSet resultSet = connection.getMetaData().getTables(connection.getCatalog(), connection.getSchema(), tableNamePattern, null)) {
            return resultSet.next();
        }
    }
    
    private void doAddForOpenGauss(final DataNode dataNode, final List<Object> row, final Connection connection) throws SQLException {
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
    
    @Override
    public String getType() {
        return SHARDING_TABLE_STATISTICS;
    }
}
