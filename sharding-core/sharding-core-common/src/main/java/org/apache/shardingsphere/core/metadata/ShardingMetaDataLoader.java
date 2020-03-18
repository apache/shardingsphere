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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.core.rule.DataNode;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.rule.TableRule;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaDataLoader;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetaDataLoader;
import org.apache.shardingsphere.underlying.common.exception.ShardingSphereException;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Sharding meta data loader.
 */
@RequiredArgsConstructor
@Slf4j(topic = "ShardingSphere-metadata")
public final class ShardingMetaDataLoader {
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final ShardingRule shardingRule;
    
    private final int maxConnectionsSizePerQuery;
    
    private final boolean isCheckingMetaData;
    
    /**
     * Load table meta data.
     * 
     * @param logicTableName logic table name
     * @return table meta data
     * @throws SQLException SQL exception
     */
    public TableMetaData load(final String logicTableName) throws SQLException {
        TableRule tableRule = shardingRule.getTableRule(logicTableName);
        if (!isCheckingMetaData) {
            DataNode dataNode = tableRule.getActualDataNodes().iterator().next();
            return TableMetaDataLoader.load(dataSourceMap.get(shardingRule.getShardingDataSourceNames().getRawMasterDataSourceName(dataNode.getDataSourceName())), dataNode.getTableName());
        }
        Map<String, List<DataNode>> dataNodeGroups = tableRule.getDataNodeGroups();
        Map<String, TableMetaData> actualTableMetaDataMap = new HashMap<>(dataNodeGroups.size(), 1);
        // TODO use multiple threads to load meta data for different data sources
        for (Entry<String, List<DataNode>> entry : dataNodeGroups.entrySet()) {
            for (DataNode each : entry.getValue()) {
                actualTableMetaDataMap.put(each.getTableName(), TableMetaDataLoader.load(dataSourceMap.get(each.getDataSourceName()), each.getTableName()));
            }
        }
        checkUniformed(logicTableName, actualTableMetaDataMap);
        return actualTableMetaDataMap.values().iterator().next();
    }
    
    /**
     * Load schema Meta data.
     *
     * @return schema Meta data
     * @throws SQLException SQL exception
     */
    public SchemaMetaData load() throws SQLException {
        SchemaMetaData result = loadShardingSchemaMetaData();
        result.merge(loadDefaultSchemaMetaData());
        return result;
    }
    
    private SchemaMetaData loadShardingSchemaMetaData() throws SQLException {
        log.info("Loading {} logic tables' meta data.", shardingRule.getTableRules().size());
        Map<String, TableMetaData> tableMetaDataMap = new HashMap<>(shardingRule.getTableRules().size(), 1);
        for (TableRule each : shardingRule.getTableRules()) {
            tableMetaDataMap.put(each.getLogicTable(), load(each.getLogicTable()));
        }
        return new SchemaMetaData(tableMetaDataMap);
    }
    
    private SchemaMetaData loadDefaultSchemaMetaData() throws SQLException {
        Optional<String> actualDefaultDataSourceName = shardingRule.findActualDefaultDataSourceName();
        return actualDefaultDataSourceName.isPresent()
                ? SchemaMetaDataLoader.load(dataSourceMap.get(actualDefaultDataSourceName.get()), maxConnectionsSizePerQuery) : new SchemaMetaData(Collections.emptyMap());
    }

    private void checkUniformed(final String logicTableName, final Map<String, TableMetaData> actualTableMetaDataMap) {
        ShardingTableMetaDataDecorator decorator = new ShardingTableMetaDataDecorator();
        TableMetaData sample = decorator.decorate(actualTableMetaDataMap.values().iterator().next(), logicTableName, shardingRule);
        Collection<TableMetaDataViolation> violations = new LinkedList<>();
        compareAllTableMetaData(violations, sample, decorator, logicTableName, actualTableMetaDataMap);
        throwExceptionIfNecessary(violations, logicTableName);
    }

    private void compareAllTableMetaData(final Collection<TableMetaDataViolation> violations, final TableMetaData sample,
                      final ShardingTableMetaDataDecorator decorator, final String logicTableName, final Map<String, TableMetaData> actualTableMetaDataMap) {
        for (Entry<String, TableMetaData> entry : actualTableMetaDataMap.entrySet()) {
            TableMetaData tableMetaData = entry.getValue();
            if (!sample.equals(decorator.decorate(tableMetaData, logicTableName, shardingRule))) {
                violations.add(new TableMetaDataViolation(entry.getKey(), tableMetaData));
            }
        }
    }

    private void throwExceptionIfNecessary(final Collection<TableMetaDataViolation> violations, final String logicTableName) {
        if (!violations.isEmpty()) {
            StringBuilder exceptionMessageBuilder = new StringBuilder("Cannot get uniformed table structure for logic table `%s`, it has different meta data of actual tables are as follows: ");
            for (TableMetaDataViolation each : violations) {
                exceptionMessageBuilder.append("\nactual table: ").append(each.getActualTableName())
                        .append(", meta data: ").append(each.getTableMetaData());
            }
            throw new ShardingSphereException(exceptionMessageBuilder.toString(), logicTableName);
        }
    }

    @RequiredArgsConstructor
    @Getter
    private final class TableMetaDataViolation {

        private final String actualTableName;

        private final TableMetaData tableMetaData;
    }
}
