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

import org.apache.shardingsphere.infra.database.core.metadata.database.DialectDatabaseMetaData;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereRowData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.statistics.collector.ShardingSphereStatisticsCollector;
import org.apache.shardingsphere.sharding.metadata.data.dialect.DialectShardingStatisticsTableCollector;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.ShardingTable;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Sharding statistics table data collector.
 */
public final class ShardingStatisticsTableCollector implements ShardingSphereStatisticsCollector {
    
    private static final String SHARDING_TABLE_STATISTICS = "sharding_table_statistics";
    
    @Override
    public Optional<ShardingSphereTableData> collect(final String databaseName, final ShardingSphereTable table, final Map<String, ShardingSphereDatabase> databases) throws SQLException {
        ShardingSphereTableData result = new ShardingSphereTableData(SHARDING_TABLE_STATISTICS);
        DatabaseType protocolType = databases.values().iterator().next().getProtocolType();
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(protocolType).getDialectDatabaseMetaData();
        if (dialectDatabaseMetaData.getDefaultSchema().isPresent()) {
            collectFromDatabase(databases.get(databaseName), result);
        } else {
            for (ShardingSphereDatabase each : databases.values()) {
                collectFromDatabase(each, result);
            }
        }
        return result.getRows().isEmpty() ? Optional.empty() : Optional.of(result);
    }
    
    private void collectFromDatabase(final ShardingSphereDatabase database, final ShardingSphereTableData tableData) throws SQLException {
        Optional<ShardingRule> shardingRule = database.getRuleMetaData().findSingleRule(ShardingRule.class);
        if (!shardingRule.isPresent()) {
            return;
        }
        collectForShardingStatisticTable(database, shardingRule.get(), tableData);
    }
    
    private void collectForShardingStatisticTable(final ShardingSphereDatabase database, final ShardingRule shardingRule, final ShardingSphereTableData tableData) throws SQLException {
        int count = 1;
        for (ShardingTable each : shardingRule.getShardingTables().values()) {
            for (DataNode dataNode : each.getActualDataNodes()) {
                List<Object> row = new LinkedList<>();
                row.add(count++);
                row.add(database.getName());
                row.add(each.getLogicTable());
                row.add(dataNode.getDataSourceName());
                row.add(dataNode.getTableName());
                addTableRowsAndDataLength(database.getResourceMetaData().getStorageUnits(), dataNode, row);
                tableData.getRows().add(new ShardingSphereRowData(row));
            }
        }
    }
    
    private void addTableRowsAndDataLength(final Map<String, StorageUnit> storageUnits, final DataNode dataNode, final List<Object> row) throws SQLException {
        DatabaseType databaseType = storageUnits.get(dataNode.getDataSourceName()).getStorageType();
        Optional<DialectShardingStatisticsTableCollector> dialectCollector = DatabaseTypedSPILoader.findService(DialectShardingStatisticsTableCollector.class, databaseType);
        boolean isAppended = false;
        if (dialectCollector.isPresent()) {
            try (Connection connection = storageUnits.get(dataNode.getDataSourceName()).getDataSource().getConnection()) {
                isAppended = dialectCollector.get().appendRow(connection, dataNode, row);
            }
        }
        if (!isAppended) {
            row.add(BigDecimal.ZERO);
            row.add(BigDecimal.ZERO);
        }
    }
    
    @Override
    public String getType() {
        return SHARDING_TABLE_STATISTICS;
    }
}
