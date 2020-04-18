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
import org.apache.shardingsphere.underlying.common.rule.DataNode;
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
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Collection;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Sharding meta data loader.
 */
@RequiredArgsConstructor
@Slf4j(topic = "ShardingSphere-metadata")
public final class ShardingMetaDataLoader {
    
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    
    private static final int CORES = Runtime.getRuntime().availableProcessors();
    
    private static final int FUTURE_GET_TIME_OUT_SEC = 5;
    
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
        Map<String, List<DataNode>> dataNodeGroups = tableRule.getDataNodeGroups();
        Map<String, TableMetaData> actualTableMetaDataMap = new HashMap<>(dataNodeGroups.size(), 1);
        Map<String, Future<TableMetaData>> tableFutureMap = new HashMap<>(dataNodeGroups.size(), 1);
        ExecutorService executorService = Executors.newFixedThreadPool(Math.min(CORES * 2, dataNodeGroups.size()));
        for (Entry<String, List<DataNode>> entry : dataNodeGroups.entrySet()) {
            for (DataNode each : entry.getValue()) {
                Future<TableMetaData> futures = executorService.submit(() -> load(each, databaseType));
                tableFutureMap.put(each.getTableName(), futures);
            }
        }
        tableFutureMap.forEach((key, value) -> {
            try {
                TableMetaData tableMetaData = value.get(FUTURE_GET_TIME_OUT_SEC, TimeUnit.SECONDS);
                actualTableMetaDataMap.put(key, tableMetaData);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new IllegalStateException(String.format("Error while fetching tableMetaData with key= %s and Value=%s", key, value), e);
            }
        });
        executorService.shutdownNow();
        checkUniformed(logicTableName, actualTableMetaDataMap);
        return actualTableMetaDataMap.values().iterator().next();
    }
    
    private TableMetaData load(final DataNode dataNode, final DatabaseType databaseType) {
        try {
            return TableMetaDataLoader.load(dataSourceMap.get(dataNode.getDataSourceName()), dataNode.getTableName(), databaseType.getName());
        } catch (SQLException e) {
            throw new IllegalStateException(String.format("SQLException for DataNode=%s and databaseType=%s", dataNode, databaseType.getName()), e);
        }
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
        Map<String, TableMetaData> tableMetaDataMap = new HashMap<>(shardingRule.getTableRules().size(), 1);
        for (TableRule each : shardingRule.getTableRules()) {
            tableMetaDataMap.put(each.getLogicTable(), load(each.getLogicTable(), databaseType));
        }
        return new SchemaMetaData(tableMetaDataMap);
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
