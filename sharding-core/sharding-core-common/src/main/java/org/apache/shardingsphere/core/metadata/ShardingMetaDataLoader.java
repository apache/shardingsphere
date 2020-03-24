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

package org.apache.shardingsphere.core.metadata;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.core.rule.DataNode;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.rule.TableRule;
import org.apache.shardingsphere.spi.database.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaDataLoader;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetaDataLoader;
import org.apache.shardingsphere.underlying.common.exception.ShardingSphereException;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Sharding meta data loader.
 */
@RequiredArgsConstructor
@Slf4j(topic = "ShardingSphere-metadata")
public final class ShardingMetaDataLoader {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final ShardingRule shardingRule;
    
    private final int maxConnectionsSizePerQuery;
    
    private final boolean isCheckingMetaData;
    
    /**
     * Load table meta data.
     * 
     * @param logicTableName logic table name
     * @param databaseType database type
     * @return table meta data
     * @throws SQLException SQL exception
     */
    public TableMetaData load(final String logicTableName, final DatabaseType databaseType) throws SQLException {
        TableRule tableRule = shardingRule.getTableRule(logicTableName);
        if (!isCheckingMetaData) {
            DataNode dataNode = tableRule.getActualDataNodes().iterator().next();
            return TableMetaDataLoader.load(dataSourceMap.get(shardingRule.getShardingDataSourceNames().getRawMasterDataSourceName(
                    dataNode.getDataSourceName())), dataNode.getTableName(), databaseType.getName());
        }
        final Map<String, Map<String, TableMetaData>> tableMetaDataMap = TableMetaDataLoader.asyncLoad(buildDataNodeMap(), databaseType.getName());
        checkUniformed(logicTableName, tableMetaDataMap.get(logicTableName));
        return tableMetaDataMap.get(logicTableName).values().iterator().next();
    }
    
    /**
     * Load schema Meta data.
     *
     * @param databaseType database type
     * @return schema Meta data
     * @throws SQLException SQL exception
     */
    public SchemaMetaData load(final DatabaseType databaseType) throws SQLException {
        SchemaMetaData result = loadShardingSchemaMetaData(databaseType);
        result.merge(loadDefaultSchemaMetaData(databaseType));
        return result;
    }
    
    private SchemaMetaData loadShardingSchemaMetaData(final DatabaseType databaseType) throws SQLException {
        log.info("Loading {} logic tables' meta data.", shardingRule.getTableRules().size());
        Map<String, TableMetaData> metaDataMap = new HashMap<>(shardingRule.getTableRules().size(), 1);
        if (!isCheckingMetaData) {
            for (TableRule each : shardingRule.getTableRules()) {
                metaDataMap.put(each.getLogicTable(), load(each.getLogicTable(), databaseType));
            }
        } else {
            final Map<String, Map<String, TableMetaData>> dataMap = TableMetaDataLoader.asyncLoad(buildDataNodeMap(), databaseType.getName());
            for (TableRule each : shardingRule.getTableRules()) {
                checkUniformed(each.getLogicTable(), dataMap.get(each.getLogicTable()));
                metaDataMap.put(each.getLogicTable(), dataMap.get(each.getLogicTable()).values().iterator().next());
            }
        }
        return new SchemaMetaData(metaDataMap);
    }
    
    private Map<DataSource, Map<String, List<String>>> buildDataNodeMap() {
        Map<DataSource, Map<String, List<String>>> dataNode = new HashMap<>();
        for (TableRule each : shardingRule.getTableRules()) {
            final Map<String, List<DataNode>> dataNodeGroups = each.getDataNodeGroups();
            Map<String, List<String>> maps = new HashMap<>();
            for (Entry<String, List<DataNode>> entry : dataNodeGroups.entrySet()) {
                maps.put(each.getLogicTable(), entry.getValue().stream().map(DataNode::getTableName).collect(Collectors.toList()));
                final DataSource dataSource = dataSourceMap.get(entry.getKey());
                if (dataNode.containsKey(dataSource)) {
                    dataNode.get(dataSource).putAll(maps);
                } else {
                    dataNode.put(dataSource, maps);
                }
            }
        }
        return dataNode;
    }
    
    private SchemaMetaData loadDefaultSchemaMetaData(final DatabaseType databaseType) throws SQLException {
        Optional<String> actualDefaultDataSourceName = shardingRule.findActualDefaultDataSourceName();
        return actualDefaultDataSourceName.isPresent()
                ? SchemaMetaDataLoader.load(dataSourceMap.get(actualDefaultDataSourceName.get()), maxConnectionsSizePerQuery, databaseType.getName()) : new SchemaMetaData(Collections.emptyMap());
    }

    private void checkUniformed(final String logicTableName, final Map<String, TableMetaData> actualTableMetaDataMap) {
        ShardingTableMetaDataDecorator decorator = new ShardingTableMetaDataDecorator();
        TableMetaData sample = decorator.decorate(actualTableMetaDataMap.values().iterator().next(), logicTableName, shardingRule);
        Collection<TableMetaDataViolation> violations = actualTableMetaDataMap.entrySet().stream()
                .filter(entry -> !sample.equals(decorator.decorate(entry.getValue(), logicTableName, shardingRule)))
                .map(entry -> new TableMetaDataViolation(entry.getKey(), entry.getValue())).collect(Collectors.toList());
        throwExceptionIfNecessary(violations, logicTableName);
    }

    private void throwExceptionIfNecessary(final Collection<TableMetaDataViolation> violations, final String logicTableName) {
        if (!violations.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder(
                "Cannot get uniformed table structure for logic table `%s`, it has different meta data of actual tables are as follows:").append(LINE_SEPARATOR);
            for (TableMetaDataViolation each : violations) {
                errorMessage.append("actual table: ").append(each.getActualTableName()).append(", meta data: ").append(each.getTableMetaData()).append(LINE_SEPARATOR);
            }
            throw new ShardingSphereException(errorMessage.toString(), logicTableName);
        }
    }

    @RequiredArgsConstructor
    @Getter
    private final class TableMetaDataViolation {

        private final String actualTableName;

        private final TableMetaData tableMetaData;
    }
}
