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
import org.apache.shardingsphere.infra.metadata.statistics.DatabaseStatistics;
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
     * Add database statistics.
     *
     * @param databaseName database name
     */
    public synchronized void addDatabaseStatistics(final String databaseName) {
        if (metaDataContexts.getStatistics().containsDatabaseStatistics(databaseName)) {
            return;
        }
        metaDataContexts.getStatistics().putDatabaseStatistics(databaseName, new DatabaseStatistics());
    }
    
    /**
     * Drop database statistics.
     *
     * @param databaseName database name
     */
    public synchronized void dropDatabaseStatistics(final String databaseName) {
        if (!metaDataContexts.getStatistics().containsDatabaseStatistics(databaseName)) {
            return;
        }
        metaDataContexts.getStatistics().dropDatabaseStatistics(databaseName);
    }
    
    /**
     * Add schema statistics.
     *
     * @param databaseName database name
     * @param schemaName schema name
     */
    public synchronized void addSchemaStatistics(final String databaseName, final String schemaName) {
        if (metaDataContexts.getStatistics().getDatabaseStatistics(databaseName).containsSchema(schemaName)) {
            return;
        }
        metaDataContexts.getStatistics().getDatabaseStatistics(databaseName).putSchema(schemaName, new ShardingSphereSchemaData());
    }
    
    /**
     * Drop schema statistics.
     *
     * @param databaseName database name
     * @param schemaName schema name
     */
    public synchronized void dropSchemaStatistics(final String databaseName, final String schemaName) {
        DatabaseStatistics databaseStatistics = metaDataContexts.getStatistics().getDatabaseStatistics(databaseName);
        if (null == databaseStatistics || !databaseStatistics.containsSchema(schemaName)) {
            return;
        }
        databaseStatistics.removeSchema(schemaName);
    }
    
    /**
     * Add table statistics.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     */
    public synchronized void addTableStatistics(final String databaseName, final String schemaName, final String tableName) {
        if (!metaDataContexts.getStatistics().containsDatabaseStatistics(databaseName) || !metaDataContexts.getStatistics().getDatabaseStatistics(databaseName).containsSchema(schemaName)) {
            return;
        }
        if (metaDataContexts.getStatistics().getDatabaseStatistics(databaseName).getSchema(schemaName).containsTable(tableName)) {
            return;
        }
        metaDataContexts.getStatistics().getDatabaseStatistics(databaseName).getSchema(schemaName).putTable(tableName, new ShardingSphereTableData(tableName));
    }
    
    /**
     * Drop table statistics.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     */
    public synchronized void dropTableStatistics(final String databaseName, final String schemaName, final String tableName) {
        if (!metaDataContexts.getStatistics().containsDatabaseStatistics(databaseName) || !metaDataContexts.getStatistics().getDatabaseStatistics(databaseName).containsSchema(schemaName)) {
            return;
        }
        metaDataContexts.getStatistics().getDatabaseStatistics(databaseName).getSchema(schemaName).removeTable(tableName);
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
        if (!metaDataContexts.getStatistics().containsDatabaseStatistics(databaseName) || !metaDataContexts.getStatistics().getDatabaseStatistics(databaseName).containsSchema(schemaName)
                || !metaDataContexts.getStatistics().getDatabaseStatistics(databaseName).getSchema(schemaName).containsTable(tableName)) {
            return;
        }
        if (!metaDataContexts.getMetaData().containsDatabase(databaseName) || !metaDataContexts.getMetaData().getDatabase(databaseName).containsSchema(schemaName)
                || !metaDataContexts.getMetaData().getDatabase(databaseName).getSchema(schemaName).containsTable(tableName)) {
            return;
        }
        ShardingSphereTableData tableData = metaDataContexts.getStatistics().getDatabaseStatistics(databaseName).getSchema(schemaName).getTable(tableName);
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
        if (!metaDataContexts.getStatistics().containsDatabaseStatistics(databaseName) || !metaDataContexts.getStatistics().getDatabaseStatistics(databaseName).containsSchema(schemaName)
                || !metaDataContexts.getStatistics().getDatabaseStatistics(databaseName).getSchema(schemaName).containsTable(tableName)) {
            return;
        }
        metaDataContexts.getStatistics().getDatabaseStatistics(databaseName).getSchema(schemaName).getTable(tableName).getRows().removeIf(each -> uniqueKey.equals(each.getUniqueKey()));
    }
}
