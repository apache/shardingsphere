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

package org.apache.shardingsphere.mode.manager.cluster.statistics;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.DatabaseStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.RowStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.SchemaStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.TableStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.utils.RowStatisticsCollectorUtils;
import org.apache.shardingsphere.infra.yaml.data.swapper.YamlRowStatisticsSwapper;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.persist.statistics.AlteredDatabaseStatistics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Statistics storage engine.
 */
@RequiredArgsConstructor
public final class StatisticsStorageEngine {
    
    private final ContextManager contextManager;
    
    private final String databaseName;
    
    private final String schemaName;
    
    private final String tableName;
    
    private final Collection<Map<String, Object>> rowColumnValues;
    
    /**
     * Storage.
     */
    public void storage() {
        ShardingSphereTable table = contextManager.getDatabase(databaseName).getSchema(schemaName).getTable(tableName);
        TableStatistics changedTableStatistics = new TableStatistics(table.getName());
        for (Map<String, Object> each : rowColumnValues) {
            changedTableStatistics.getRows().add(new RowStatistics(RowStatisticsCollectorUtils.createRowValues(each, table)));
        }
        AlteredDatabaseStatistics alteredDatabaseStatistics = createAlteredDatabaseStatistics(table, getCurrentTableStatistics(), changedTableStatistics);
        contextManager.getPersistServiceFacade().getMetaDataFacade().getStatisticsService().update(alteredDatabaseStatistics);
    }
    
    private TableStatistics getCurrentTableStatistics() {
        TableStatistics result = new TableStatistics(tableName);
        ShardingSphereStatistics currentStatistics = contextManager.getMetaDataContexts().getStatistics();
        if (!currentStatistics.containsDatabaseStatistics(databaseName)) {
            return result;
        }
        DatabaseStatistics databaseStatistics = currentStatistics.getDatabaseStatistics(databaseName);
        if (!databaseStatistics.containsSchemaStatistics(schemaName)) {
            return result;
        }
        SchemaStatistics schemaStatistics = databaseStatistics.getSchemaStatistics(schemaName);
        Optional.ofNullable(schemaStatistics.getTableStatistics(tableName)).ifPresent(optional -> result.getRows().addAll(optional.getRows()));
        return result;
    }
    
    private AlteredDatabaseStatistics createAlteredDatabaseStatistics(final ShardingSphereTable table, final TableStatistics currentTableStatistics, final TableStatistics changedTableStatistics) {
        AlteredDatabaseStatistics result = new AlteredDatabaseStatistics(databaseName, schemaName, tableName);
        Map<String, RowStatistics> tableStatisticsMap = currentTableStatistics.getRows().stream().collect(Collectors.toMap(RowStatistics::getUniqueKey, Function.identity()));
        Map<String, RowStatistics> changedTableStatisticsMap = changedTableStatistics.getRows().stream().collect(Collectors.toMap(RowStatistics::getUniqueKey, Function.identity()));
        YamlRowStatisticsSwapper swapper = new YamlRowStatisticsSwapper(new ArrayList<>(table.getAllColumns()));
        for (Entry<String, RowStatistics> entry : changedTableStatisticsMap.entrySet()) {
            if (!tableStatisticsMap.containsKey(entry.getKey())) {
                result.getAddedRows().add(swapper.swapToYamlConfiguration(entry.getValue()));
            } else if (!tableStatisticsMap.get(entry.getKey()).equals(entry.getValue())) {
                result.getUpdatedRows().add(swapper.swapToYamlConfiguration(entry.getValue()));
            }
        }
        for (Entry<String, RowStatistics> entry : tableStatisticsMap.entrySet()) {
            if (!changedTableStatisticsMap.containsKey(entry.getKey())) {
                result.getDeletedRows().add(swapper.swapToYamlConfiguration(entry.getValue()));
            }
        }
        return result;
    }
}
