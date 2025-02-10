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

package org.apache.shardingsphere.mode.metadata.manager.statistics;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.statistics.DatabaseStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.SchemaStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.TableStatistics;
import org.apache.shardingsphere.infra.yaml.data.pojo.YamlRowStatistics;
import org.apache.shardingsphere.infra.yaml.data.swapper.YamlRowStatisticsSwapper;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;

import java.util.ArrayList;
import java.util.List;

/**
 * Statistics manager.
 */
@RequiredArgsConstructor
public final class StatisticsManager {
    
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
        if (metaDataContexts.getStatistics().getDatabaseStatistics(databaseName).containsSchemaStatistics(schemaName)) {
            return;
        }
        metaDataContexts.getStatistics().getDatabaseStatistics(databaseName).putSchemaStatistics(schemaName, new SchemaStatistics());
    }
    
    /**
     * Drop schema statistics.
     *
     * @param databaseName database name
     * @param schemaName schema name
     */
    public synchronized void dropSchemaStatistics(final String databaseName, final String schemaName) {
        DatabaseStatistics databaseStatistics = metaDataContexts.getStatistics().getDatabaseStatistics(databaseName);
        if (null == databaseStatistics || !databaseStatistics.containsSchemaStatistics(schemaName)) {
            return;
        }
        databaseStatistics.removeSchemaStatistics(schemaName);
    }
    
    /**
     * Add table statistics.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     */
    public synchronized void addTableStatistics(final String databaseName, final String schemaName, final String tableName) {
        if (!metaDataContexts.getStatistics().containsDatabaseStatistics(databaseName) || !metaDataContexts.getStatistics().getDatabaseStatistics(databaseName).containsSchemaStatistics(schemaName)) {
            return;
        }
        if (metaDataContexts.getStatistics().getDatabaseStatistics(databaseName).getSchemaStatistics(schemaName).containsTableStatistics(tableName)) {
            return;
        }
        metaDataContexts.getStatistics().getDatabaseStatistics(databaseName).getSchemaStatistics(schemaName).putTableStatistics(tableName, new TableStatistics(tableName));
    }
    
    /**
     * Drop table statistics.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     */
    public synchronized void dropTableStatistics(final String databaseName, final String schemaName, final String tableName) {
        if (!metaDataContexts.getStatistics().containsDatabaseStatistics(databaseName) || !metaDataContexts.getStatistics().getDatabaseStatistics(databaseName).containsSchemaStatistics(schemaName)) {
            return;
        }
        metaDataContexts.getStatistics().getDatabaseStatistics(databaseName).getSchemaStatistics(schemaName).removeTableStatistics(tableName);
    }
    
    /**
     * Alter row statistics.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @param yamlRowData YAML row data
     */
    public synchronized void alterRowStatistics(final String databaseName, final String schemaName, final String tableName, final YamlRowStatistics yamlRowData) {
        if (!metaDataContexts.getStatistics().containsDatabaseStatistics(databaseName) || !metaDataContexts.getStatistics().getDatabaseStatistics(databaseName).containsSchemaStatistics(schemaName)
                || !metaDataContexts.getStatistics().getDatabaseStatistics(databaseName).getSchemaStatistics(schemaName).containsTableStatistics(tableName)) {
            return;
        }
        if (!metaDataContexts.getMetaData().containsDatabase(databaseName) || !metaDataContexts.getMetaData().getDatabase(databaseName).containsSchema(schemaName)
                || !metaDataContexts.getMetaData().getDatabase(databaseName).getSchema(schemaName).containsTable(tableName)) {
            return;
        }
        TableStatistics tableStatistics = metaDataContexts.getStatistics().getDatabaseStatistics(databaseName).getSchemaStatistics(schemaName).getTableStatistics(tableName);
        List<ShardingSphereColumn> columns = new ArrayList<>(metaDataContexts.getMetaData().getDatabase(databaseName).getSchema(schemaName).getTable(tableName).getAllColumns());
        tableStatistics.getRows().add(new YamlRowStatisticsSwapper(columns).swapToObject(yamlRowData));
    }
    
    /**
     * Delete row statistics.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @param uniqueKey row uniqueKey
     */
    public synchronized void deleteRowStatistics(final String databaseName, final String schemaName, final String tableName, final String uniqueKey) {
        if (!metaDataContexts.getStatistics().containsDatabaseStatistics(databaseName) || !metaDataContexts.getStatistics().getDatabaseStatistics(databaseName).containsSchemaStatistics(schemaName)
                || !metaDataContexts.getStatistics().getDatabaseStatistics(databaseName).getSchemaStatistics(schemaName).containsTableStatistics(tableName)) {
            return;
        }
        metaDataContexts.getStatistics().getDatabaseStatistics(databaseName).getSchemaStatistics(schemaName).getTableStatistics(tableName).getRows()
                .removeIf(each -> uniqueKey.equals(each.getUniqueKey()));
    }
}
