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
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.identifier.ShardingSphereIdentifier;

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
    
    private final Map<ShardingSphereIdentifier, ShardingSphereTable> tables;
    
    private final Map<ShardingSphereIdentifier, ShardingSphereView> views;
    
    @Getter
    private final DatabaseType protocolType;
    
    @SuppressWarnings("CollectionWithoutInitialCapacity")
    public ShardingSphereSchema(final String name, final DatabaseType protocolType) {
        this.name = name;
        tables = new ConcurrentHashMap<>();
        views = new ConcurrentHashMap<>();
        this.protocolType = protocolType;
    }
    
    public ShardingSphereSchema(final String name, final Collection<ShardingSphereTable> tables, final Collection<ShardingSphereView> views, final DatabaseType protocolType) {
        this.name = name;
        this.tables = new ConcurrentHashMap<>(tables.size(), 1F);
        this.views = new ConcurrentHashMap<>(views.size(), 1F);
        tables.forEach(each -> this.tables.put(new ShardingSphereIdentifier(each.getName()), each));
        views.forEach(each -> this.views.put(new ShardingSphereIdentifier(each.getName()), each));
        this.protocolType = protocolType;
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
        return tables.containsKey(new ShardingSphereIdentifier(tableName));
    }
    
    /**
     * Get table.
     *
     * @param tableName table name
     * @return table
     */
    public ShardingSphereTable getTable(final String tableName) {
        return tables.get(new ShardingSphereIdentifier(tableName));
    }
    
    /**
     * Add table.
     *
     * @param table table
     */
    public void putTable(final ShardingSphereTable table) {
        tables.put(new ShardingSphereIdentifier(table.getName()), table);
    }
    
    /**
     * Remove table.
     *
     * @param tableName table name
     */
    public void removeTable(final String tableName) {
        tables.remove(new ShardingSphereIdentifier(tableName));
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
        return views.containsKey(new ShardingSphereIdentifier(viewName));
    }
    
    /**
     * Get view.
     *
     * @param viewName view name
     * @return view
     */
    public ShardingSphereView getView(final String viewName) {
        return views.get(new ShardingSphereIdentifier(viewName));
    }
    
    /**
     * Add view.
     *
     * @param view view
     */
    public void putView(final ShardingSphereView view) {
        views.put(new ShardingSphereIdentifier(view.getName()), view);
    }
    
    /**
     * Remove view.
     *
     * @param viewName view name
     */
    public void removeView(final String viewName) {
        views.remove(new ShardingSphereIdentifier(viewName));
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
