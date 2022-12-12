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

package org.apache.shardingsphere.infra.metadata.database.schema.decorator.model;

import lombok.Getter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ShardingSphere schema.
 */
@Getter
public final class ShardingSphereSchema {
    
    private final Map<String, ShardingSphereTable> tables;
    
    private final Map<String, ShardingSphereView> views;
    
    @SuppressWarnings("CollectionWithoutInitialCapacity")
    public ShardingSphereSchema() {
        tables = new ConcurrentHashMap<>();
        views = new ConcurrentHashMap<>();
    }
    
    public ShardingSphereSchema(final Map<String, ShardingSphereTable> tables, final Map<String, ShardingSphereView> views) {
        this.tables = new ConcurrentHashMap<>(tables.size(), 1);
        this.views = new ConcurrentHashMap<>(views.size(), 1);
        tables.forEach((key, value) -> this.tables.put(key.toLowerCase(), value));
        views.forEach((key, value) -> this.views.put(key.toLowerCase(), value));
    }
    
    /**
     * Get all table names.
     *
     * @return all table names
     */
    public Collection<String> getAllTableNames() {
        return tables.keySet();
    }
    
    /**
     * Get all view names.
     *
     * @return all view names
     */
    public Collection<String> getAllViewNames() {
        return views.keySet();
    }
    
    /**
     * Get table meta data via table name.
     * 
     * @param tableName tableName table name
     * @return table meta data
     */
    public ShardingSphereTable getTable(final String tableName) {
        return tables.get(tableName.toLowerCase());
    }
    
    /**
     * Get view meta data via view name.
     *
     * @param viewName viewName view name
     * @return view meta data
     */
    public ShardingSphereView getView(final String viewName) {
        return views.get(viewName.toLowerCase());
    }
    
    /**
     * Add table.
     * 
     * @param tableName table name
     * @param table table
     */
    public void putTable(final String tableName, final ShardingSphereTable table) {
        tables.put(tableName.toLowerCase(), table);
    }
    
    /**
     * Add view.
     *
     * @param viewName view name
     * @param view view
     */
    public void putView(final String viewName, final ShardingSphereView view) {
        views.put(viewName.toLowerCase(), view);
    }
    
    /**
     * Add tables.
     *
     * @param tables tables
     */
    public void putAll(final Map<String, ShardingSphereTable> tables) {
        for (Entry<String, ShardingSphereTable> entry : tables.entrySet()) {
            putTable(entry.getKey(), entry.getValue());
        }
    }
    
    /**
     * Remove table meta data.
     *
     * @param tableName table name
     */
    public void removeTable(final String tableName) {
        tables.remove(tableName.toLowerCase());
    }
    
    /**
     * Remove view meta data.
     *
     * @param viewName view name
     */
    public void removeView(final String viewName) {
        views.remove(viewName.toLowerCase());
    }
    
    /**
     * Judge contains table from table metadata or not.
     *
     * @param tableName table name
     * @return contains table from table metadata or not
     */
    public boolean containsTable(final String tableName) {
        return tables.containsKey(tableName.toLowerCase());
    }
    
    /**
     * Judge whether contains column name.
     *
     * @param tableName table name
     * @param columnName column name
     * @return contains column name or not
     */
    public boolean containsColumn(final String tableName, final String columnName) {
        return containsTable(tableName) && getTable(tableName).getColumns().containsKey(columnName.toLowerCase());
    }
    
    /**
     * Judge whether contains index name.
     *
     * @param tableName table name
     * @param indexName index name
     * @return whether contains index name or not
     */
    public boolean containsIndex(final String tableName, final String indexName) {
        return containsTable(tableName) && getTable(tableName).getIndexes().containsKey(indexName.toLowerCase());
    }
    
    /**
     * Judge contains view from table metadata or not.
     *
     * @param viewName view name
     * @return contains view from table metadata or not
     */
    public boolean containsView(final String viewName) {
        return views.containsKey(viewName.toLowerCase());
    }
    
    /**
     * Get all column names via table.
     *
     * @param tableName table name
     * @return column names
     */
    public List<String> getAllColumnNames(final String tableName) {
        return containsTable(tableName) ? getTable(tableName).getColumnNames() : Collections.emptyList();
    }
    
    /**
     * Get visible column names via table.
     *
     * @param tableName table name
     * @return visible column names
     */
    public List<String> getVisibleColumnNames(final String tableName) {
        return containsTable(tableName) ? getTable(tableName).getVisibleColumns() : Collections.emptyList();
    }
}
