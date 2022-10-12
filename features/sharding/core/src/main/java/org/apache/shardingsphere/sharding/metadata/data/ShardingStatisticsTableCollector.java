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

import org.apache.shardingsphere.data.pipeline.spi.data.collector.ShardingSphereDataCollector;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereRowData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
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
public final class ShardingStatisticsTableCollector implements ShardingSphereDataCollector {
    
    private static final String SHARDING_STATISTICS_TABLE = "sharding_statistics_table";
    
    private static final String SHARDING_SPHERE = "shardingsphere";
    
    private static final String MYSQL_TABLE_ROWS_AND_DATA_LENGTH = "SELECT TABLE_ROWS, DATA_LENGTH FROM information_schema.TABLES WHERE TABLE_SCHEMA = '%s' AND TABLE_NAME = '%s'";
    
    @Override
    public void collect(final ShardingSphereData shardingSphereData, final String databaseName, final ShardingSphereRuleMetaData ruleMetaData,
                        final Map<String, DataSource> dataSources, final DatabaseType databaseType) throws SQLException {
        Optional<ShardingRule> shardingRule = ruleMetaData.findSingleRule(ShardingRule.class);
        if (!shardingRule.isPresent()) {
            return;
        }
        ShardingSphereTableData tableData = collectForShardingStatisticTable(databaseName, dataSources, databaseType, shardingRule.get());
        // TODO refactor by dialect database
        if (databaseType instanceof MySQLDatabaseType) {
            Optional.ofNullable(shardingSphereData.getDatabaseData().get(SHARDING_SPHERE)).map(database -> database.getSchemaData().get(SHARDING_SPHERE))
                    .ifPresent(shardingSphereSchemaData -> shardingSphereSchemaData.getTableData().put(SHARDING_STATISTICS_TABLE, tableData));
        } else if (databaseType instanceof PostgreSQLDatabaseType || databaseType instanceof OpenGaussDatabaseType) {
            Optional.ofNullable(shardingSphereData.getDatabaseData().get(databaseName)).map(database -> database.getSchemaData().get(SHARDING_SPHERE))
                    .ifPresent(shardingSphereSchemaData -> shardingSphereSchemaData.getTableData().put(SHARDING_STATISTICS_TABLE, tableData));
        }
    }
    
    private ShardingSphereTableData collectForShardingStatisticTable(final String databaseName, final Map<String, DataSource> dataSources,
                                                                     final DatabaseType databaseType, final ShardingRule shardingRule) throws SQLException {
        ShardingSphereTableData result = new ShardingSphereTableData(SHARDING_STATISTICS_TABLE);
        int count = 1;
        for (TableRule each : shardingRule.getTableRules().values()) {
            for (DataNode dataNode : each.getActualDataNodes()) {
                List<Object> row = new LinkedList<>();
                row.add(count++);
                row.add(databaseName);
                row.add(each.getLogicTable());
                row.add(dataNode.getDataSourceName());
                row.add(dataNode.getTableName());
                addTableRowsAndDataLength(dataSources, dataNode, row, databaseType);
                result.getRows().add(new ShardingSphereRowData(row));
            }
        }
        return result;
    }
    
    private void addTableRowsAndDataLength(final Map<String, DataSource> dataSources, final DataNode dataNode, final List<Object> row, final DatabaseType databaseType) throws SQLException {
        if (databaseType instanceof MySQLDatabaseType) {
            addForMySQL(dataSources, dataNode, row);
        } else if (databaseType instanceof PostgreSQLDatabaseType || databaseType instanceof OpenGaussDatabaseType) {
            // TODO get postgres rows and data length
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
                    tableRows = resultSet.getBigDecimal("TABLE_ROWS");
                    dataLength = resultSet.getBigDecimal("DATA_LENGTH");
                }
            }
        }
        row.add(tableRows);
        row.add(dataLength);
    }
    
    @Override
    public String getType() {
        return SHARDING_STATISTICS_TABLE;
    }
}
