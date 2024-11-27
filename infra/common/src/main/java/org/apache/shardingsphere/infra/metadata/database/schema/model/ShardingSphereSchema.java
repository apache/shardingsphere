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

package org.apache.shardingsphere.infra.metadata.database.schema.model;

import lombok.Getter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ShardingSphere schema.
 */
public final class ShardingSphereSchema {
    
    @Getter
    private final String name;
    
    private final Map<String, ShardingSphereTable> tables;
    
    private final Map<String, ShardingSphereView> views;
    
    @SuppressWarnings("CollectionWithoutInitialCapacity")
    public ShardingSphereSchema(final String name) {
        this.name = name;
        tables = new ConcurrentHashMap<>();
        views = new ConcurrentHashMap<>();
    }
    
    public ShardingSphereSchema(final String name, final Collection<ShardingSphereTable> tables, final Collection<ShardingSphereView> views) {
        this.name = name;
        this.tables = new ConcurrentHashMap<>(tables.size(), 1F);
        this.views = new ConcurrentHashMap<>(views.size(), 1F);
        tables.forEach(each -> this.tables.put(each.getName().toLowerCase(), each));
        views.forEach(each -> this.views.put(each.getName().toLowerCase(), each));
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
     * Get all tables.
     *
     * @return all tables
     */
    public Collection<ShardingSphereTable> getAllTables() {
        return tables.values();
    }
    
    /**
     * Judge whether contains table.
     *
     * @param tableName table name
     * @return contains table or not
     */
    public boolean containsTable(final String tableName) {
        return tables.containsKey(tableName.toLowerCase());
    }
    
    /**
     * Get table.
     *
     * @param tableName table name
     * @return table
     */
    public ShardingSphereTable getTable(final String tableName) {
        return tables.get(tableName.toLowerCase());
    }
    
    /**
     * Add table.
     *
     * @param table table
     */
    public void putTable(final ShardingSphereTable table) {
        tables.put(table.getName().toLowerCase(), table);
    }
    
    /**
     * Remove table.
     *
     * @param tableName table name
     */
    public void removeTable(final String tableName) {
        tables.remove(tableName.toLowerCase());
    }
    
    /**
     * Get all views.
     *
     * @return all views
     */
    public Collection<ShardingSphereView> getAllViews() {
        return views.values();
    }
    
    /**
     * Judge whether contains view.
     *
     * @param viewName view name
     * @return contains view or not
     */
    public boolean containsView(final String viewName) {
        return views.containsKey(viewName.toLowerCase());
    }
    
    /**
     * Get view.
     *
     * @param viewName view name
     * @return view
     */
    public ShardingSphereView getView(final String viewName) {
        return views.get(viewName.toLowerCase());
    }
    
    /**
     * Add view.
     *
     * @param view view
     */
    public void putView(final ShardingSphereView view) {
        views.put(view.getName().toLowerCase(), view);
    }
    
    /**
     * Remove view.
     *
     * @param viewName view name
     */
    public void removeView(final String viewName) {
        views.remove(viewName.toLowerCase());
    }
    
    /**
     * Judge whether contains index.
     *
     * @param tableName table name
     * @param indexName index name
     * @return contains index or not
     */
    public boolean containsIndex(final String tableName, final String indexName) {
        return containsTable(tableName) && getTable(tableName).containsIndex(indexName);
    }
    
    /**
     * Get all column names.
     *
     * @param tableName table name
     * @return column names
     */
    public List<String> getAllColumnNames(final String tableName) {
        return containsTable(tableName) ? getTable(tableName).getColumnNames() : Collections.emptyList();
    }
    
    /**
     * Get visible column names.
     *
     * @param tableName table name
     * @return visible column names
     */
    public List<String> getVisibleColumnNames(final String tableName) {
        return containsTable(tableName) ? getTable(tableName).getVisibleColumns() : Collections.emptyList();
    }
    
    /**
     * Get visible column and index map.
     *
     * @param tableName table name
     * @return visible column and index map
     */
    public Map<String, Integer> getVisibleColumnAndIndexMap(final String tableName) {
        return containsTable(tableName) ? getTable(tableName).getVisibleColumnAndIndexMap() : Collections.emptyMap();
    }
    
    /**
     * Whether empty schema.
     *
     * @return empty schema or not
     */
    public boolean isEmpty() {
        return tables.isEmpty() && views.isEmpty();
    }
}
