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

import com.cedarsoftware.util.CaseInsensitiveMap;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.database.core.metadata.database.DialectDatabaseMetaData;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.statistics.collector.shardingsphere.ShardingSphereTableStatisticsCollector;
import org.apache.shardingsphere.infra.rule.attribute.datasource.aggregate.AggregatedDataSourceRuleAttribute;
import org.apache.shardingsphere.sharding.metadata.data.dialect.DialectShardingStatisticsTableCollector;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.ShardingTable;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/**
 * Sharding table statistics collector.
 */
public final class ShardingTableStatisticsCollector implements ShardingSphereTableStatisticsCollector {
    
    private long currentId = 1;
    
    @Override
    public Collection<Map<String, Object>> collect(final String databaseName, final String schemaName, final String tableName, final ShardingSphereMetaData metaData) throws SQLException {
        Collection<Map<String, Object>> result = new LinkedList<>();
        DatabaseType protocolType = metaData.getAllDatabases().iterator().next().getProtocolType();
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(protocolType).getDialectDatabaseMetaData();
        currentId = 1;
        if (dialectDatabaseMetaData.getDefaultSchema().isPresent()) {
            collectFromDatabase(metaData.getDatabase(databaseName), result);
        } else {
            for (ShardingSphereDatabase each : metaData.getAllDatabases()) {
                collectFromDatabase(each, result);
            }
        }
        return result;
    }
    
    private void collectFromDatabase(final ShardingSphereDatabase database, final Collection<Map<String, Object>> rows) throws SQLException {
        Optional<ShardingRule> rule = database.getRuleMetaData().findSingleRule(ShardingRule.class);
        if (!rule.isPresent()) {
            return;
        }
        collectForShardingStatisticTable(database, rule.get(), rows);
    }
    
    private void collectForShardingStatisticTable(final ShardingSphereDatabase database, final ShardingRule rule, final Collection<Map<String, Object>> rows) throws SQLException {
        for (ShardingTable each : rule.getShardingTables().values()) {
            for (DataNode dataNode : each.getActualDataNodes()) {
                Map<String, Object> rowColumnValues = new CaseInsensitiveMap<>();
                rowColumnValues.put("id", currentId++);
                rowColumnValues.put("logic_database_name", database.getName());
                rowColumnValues.put("logic_table_name", each.getLogicTable());
                rowColumnValues.put("actual_database_name", dataNode.getDataSourceName());
                rowColumnValues.put("actual_table_name", dataNode.getTableName());
                addTableRowsAndDataLength(database.getResourceMetaData().getStorageUnits(), dataNode, rowColumnValues, rule);
                rows.add(rowColumnValues);
            }
        }
    }
    
    private void addTableRowsAndDataLength(final Map<String, StorageUnit> storageUnits, final DataNode dataNode, final Map<String, Object> rowColumnValues,
                                           final ShardingRule rule) throws SQLException {
        DataSource dataSource;
        DatabaseType databaseType;
        StorageUnit storageUnit = storageUnits.get(dataNode.getDataSourceName());
        if (null != storageUnit) {
            dataSource = storageUnit.getDataSource();
            databaseType = storageUnit.getStorageType();
        } else {
            Optional<AggregatedDataSourceRuleAttribute> aggregatedDataSourceRuleAttribute = rule.getAttributes().findAttribute(AggregatedDataSourceRuleAttribute.class);
            dataSource = aggregatedDataSourceRuleAttribute.map(optional -> optional.getAggregatedDataSources().get(dataNode.getDataSourceName())).orElse(null);
            databaseType = null != dataSource ? DatabaseTypeEngine.getStorageType(dataSource) : null;
        }
        if (null != dataSource && null != databaseType) {
            addTableRowsAndDataLength(databaseType, dataSource, dataNode, rowColumnValues);
        }
    }
    
    private void addTableRowsAndDataLength(final DatabaseType databaseType, final DataSource dataSource, final DataNode dataNode,
                                           final Map<String, Object> rowColumnValues) throws SQLException {
        boolean isAppended = false;
        Optional<DialectShardingStatisticsTableCollector> dialectCollector = DatabaseTypedSPILoader.findService(DialectShardingStatisticsTableCollector.class, databaseType);
        if (dialectCollector.isPresent()) {
            try (Connection connection = dataSource.getConnection()) {
                isAppended = dialectCollector.get().appendRow(connection, dataNode, rowColumnValues);
            }
        }
        if (!isAppended) {
            rowColumnValues.put("row_count", BigDecimal.ZERO);
            rowColumnValues.put("size", BigDecimal.ZERO);
        }
    }
    
    @Override
    public String getSchemaName() {
        return "shardingsphere";
    }
    
    @Override
    public String getTableName() {
        return "sharding_table_statistics";
    }
    
    @Override
    public String getType() {
        return "shardingsphere.sharding_table_statistics";
    }
}
