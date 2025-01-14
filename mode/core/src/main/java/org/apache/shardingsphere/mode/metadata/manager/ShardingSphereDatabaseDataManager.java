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

package org.apache.shardingsphere.mode.metadata.manager;

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

/**
 * ShardingSphere database data manager.
 */
@RequiredArgsConstructor
public final class ShardingSphereDatabaseDataManager {
    
    private final MetaDataContexts metaDataContexts;
    
    /**
     * Add ShardingSphere database data.
     *
     * @param databaseName database name
     */
    public synchronized void addShardingSphereDatabaseData(final String databaseName) {
        if (metaDataContexts.getStatistics().containsDatabase(databaseName)) {
            return;
        }
        metaDataContexts.getStatistics().putDatabase(databaseName, new ShardingSphereDatabaseData());
    }
    
    /**
     * Drop ShardingSphere data database.
     *
     * @param databaseName database name
     */
    public synchronized void dropShardingSphereDatabaseData(final String databaseName) {
        if (!metaDataContexts.getStatistics().containsDatabase(databaseName)) {
            return;
        }
        metaDataContexts.getStatistics().dropDatabase(databaseName);
    }
    
    /**
     * Add ShardingSphere schema data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     */
    public synchronized void addShardingSphereSchemaData(final String databaseName, final String schemaName) {
        if (metaDataContexts.getStatistics().getDatabase(databaseName).containsSchema(schemaName)) {
            return;
        }
        metaDataContexts.getStatistics().getDatabase(databaseName).putSchema(schemaName, new ShardingSphereSchemaData());
    }
    
    /**
     * Drop ShardingSphere schema data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     */
    public synchronized void dropShardingSphereSchemaData(final String databaseName, final String schemaName) {
        ShardingSphereDatabaseData databaseData = metaDataContexts.getStatistics().getDatabase(databaseName);
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
        if (!metaDataContexts.getStatistics().containsDatabase(databaseName) || !metaDataContexts.getStatistics().getDatabase(databaseName).containsSchema(schemaName)) {
            return;
        }
        if (metaDataContexts.getStatistics().getDatabase(databaseName).getSchema(schemaName).containsTable(tableName)) {
            return;
        }
        metaDataContexts.getStatistics().getDatabase(databaseName).getSchema(schemaName).putTable(tableName, new ShardingSphereTableData(tableName));
    }
    
    /**
     * Drop ShardingSphere table data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     */
    public synchronized void dropShardingSphereTableData(final String databaseName, final String schemaName, final String tableName) {
        if (!metaDataContexts.getStatistics().containsDatabase(databaseName) || !metaDataContexts.getStatistics().getDatabase(databaseName).containsSchema(schemaName)) {
            return;
        }
        metaDataContexts.getStatistics().getDatabase(databaseName).getSchema(schemaName).removeTable(tableName);
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
        if (!metaDataContexts.getStatistics().containsDatabase(databaseName) || !metaDataContexts.getStatistics().getDatabase(databaseName).containsSchema(schemaName)
                || !metaDataContexts.getStatistics().getDatabase(databaseName).getSchema(schemaName).containsTable(tableName)) {
            return;
        }
        if (!metaDataContexts.getMetaData().containsDatabase(databaseName) || !metaDataContexts.getMetaData().getDatabase(databaseName).containsSchema(schemaName)
                || !metaDataContexts.getMetaData().getDatabase(databaseName).getSchema(schemaName).containsTable(tableName)) {
            return;
        }
        ShardingSphereTableData tableData = metaDataContexts.getStatistics().getDatabase(databaseName).getSchema(schemaName).getTable(tableName);
        List<ShardingSphereColumn> columns = new ArrayList<>(metaDataContexts.getMetaData().getDatabase(databaseName).getSchema(schemaName).getTable(tableName).getAllColumns());
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
        if (!metaDataContexts.getStatistics().containsDatabase(databaseName) || !metaDataContexts.getStatistics().getDatabase(databaseName).containsSchema(schemaName)
                || !metaDataContexts.getStatistics().getDatabase(databaseName).getSchema(schemaName).containsTable(tableName)) {
            return;
        }
        metaDataContexts.getStatistics().getDatabase(databaseName).getSchema(schemaName).getTable(tableName).getRows().removeIf(each -> uniqueKey.equals(each.getUniqueKey()));
    }
}
