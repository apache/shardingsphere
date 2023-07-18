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

package org.apache.shardingsphere.mode.manager.context;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereDatabaseData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereSchemaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereTableData;
import org.apache.shardingsphere.infra.yaml.data.pojo.YamlShardingSphereRowData;
import org.apache.shardingsphere.infra.yaml.data.swapper.YamlShardingSphereRowDataSwapper;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ShardingSphere database context manager.
 */
@RequiredArgsConstructor
public final class ShardingSphereDatabaseContextManager {
    
    private final AtomicReference<MetaDataContexts> metaDataContexts;
    
    /**
     * Add ShardingSphere database data.
     *
     * @param databaseName database name
     */
    public synchronized void addShardingSphereDatabaseData(final String databaseName) {
        if (metaDataContexts.get().getStatistics().containsDatabase(databaseName)) {
            return;
        }
        metaDataContexts.get().getStatistics().putDatabase(databaseName, new ShardingSphereDatabaseData());
    }
    
    /**
     * Drop ShardingSphere data database.
     *
     * @param databaseName database name
     */
    public synchronized void dropShardingSphereDatabaseData(final String databaseName) {
        if (!metaDataContexts.get().getStatistics().containsDatabase(databaseName)) {
            return;
        }
        metaDataContexts.get().getStatistics().dropDatabase(databaseName);
    }
    
    /**
     * Add ShardingSphere schema data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     */
    public synchronized void addShardingSphereSchemaData(final String databaseName, final String schemaName) {
        if (metaDataContexts.get().getStatistics().getDatabase(databaseName).containsSchema(schemaName)) {
            return;
        }
        metaDataContexts.get().getStatistics().getDatabase(databaseName).putSchema(schemaName, new ShardingSphereSchemaData());
    }
    
    /**
     * Drop ShardingSphere schema data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     */
    public synchronized void dropShardingSphereSchemaData(final String databaseName, final String schemaName) {
        ShardingSphereDatabaseData databaseData = metaDataContexts.get().getStatistics().getDatabase(databaseName);
        if (null == databaseData || !databaseData.containsSchema(schemaName)) {
            return;
        }
        databaseData.removeSchema(schemaName);
    }
    
    /**
     * Add ShardingSphere table data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     */
    public synchronized void addShardingSphereTableData(final String databaseName, final String schemaName, final String tableName) {
        if (!metaDataContexts.get().getStatistics().containsDatabase(databaseName) || !metaDataContexts.get().getStatistics().getDatabase(databaseName).containsSchema(schemaName)) {
            return;
        }
        if (metaDataContexts.get().getStatistics().getDatabase(databaseName).getSchema(schemaName).containsTable(tableName)) {
            return;
        }
        metaDataContexts.get().getStatistics().getDatabase(databaseName).getSchema(schemaName).putTable(tableName, new ShardingSphereTableData(tableName));
    }
    
    /**
     * Drop ShardingSphere table data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     */
    public synchronized void dropShardingSphereTableData(final String databaseName, final String schemaName, final String tableName) {
        if (!metaDataContexts.get().getStatistics().containsDatabase(databaseName) || !metaDataContexts.get().getStatistics().getDatabase(databaseName).containsSchema(schemaName)) {
            return;
        }
        metaDataContexts.get().getStatistics().getDatabase(databaseName).getSchema(schemaName).removeTable(tableName);
    }
    
    /**
     * Alter ShardingSphere row data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @param yamlRowData yaml row data
     */
    public synchronized void alterShardingSphereRowData(final String databaseName, final String schemaName, final String tableName, final YamlShardingSphereRowData yamlRowData) {
        if (!metaDataContexts.get().getStatistics().containsDatabase(databaseName) || !metaDataContexts.get().getStatistics().getDatabase(databaseName).containsSchema(schemaName)
                || !metaDataContexts.get().getStatistics().getDatabase(databaseName).getSchema(schemaName).containsTable(tableName)) {
            return;
        }
        if (!metaDataContexts.get().getMetaData().containsDatabase(databaseName) || !metaDataContexts.get().getMetaData().getDatabase(databaseName).containsSchema(schemaName)
                || !metaDataContexts.get().getMetaData().getDatabase(databaseName).getSchema(schemaName).containsTable(tableName)) {
            return;
        }
        ShardingSphereTableData tableData = metaDataContexts.get().getStatistics().getDatabase(databaseName).getSchema(schemaName).getTable(tableName);
        List<ShardingSphereColumn> columns = new ArrayList<>(metaDataContexts.get().getMetaData().getDatabase(databaseName).getSchema(schemaName).getTable(tableName).getColumnValues());
        tableData.getRows().add(new YamlShardingSphereRowDataSwapper(columns).swapToObject(yamlRowData));
    }
    
    /**
     * Delete ShardingSphere row data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @param uniqueKey row uniqueKey
     */
    public synchronized void deleteShardingSphereRowData(final String databaseName, final String schemaName, final String tableName, final String uniqueKey) {
        if (!metaDataContexts.get().getStatistics().containsDatabase(databaseName) || !metaDataContexts.get().getStatistics().getDatabase(databaseName).containsSchema(schemaName)
                || !metaDataContexts.get().getStatistics().getDatabase(databaseName).getSchema(schemaName).containsTable(tableName)) {
            return;
        }
        metaDataContexts.get().getStatistics().getDatabase(databaseName).getSchema(schemaName).getTable(tableName).getRows().removeIf(each -> uniqueKey.equals(each.getUniqueKey()));
    }
}
