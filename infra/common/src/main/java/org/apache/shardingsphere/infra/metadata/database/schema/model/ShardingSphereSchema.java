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

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.identifier.DatabaseIdentifierContext;
import org.apache.shardingsphere.infra.metadata.identifier.DatabaseIdentifierContextFactory;
import org.apache.shardingsphere.infra.metadata.identifier.IdentifierIndex;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ShardingSphere schema.
 */
public final class ShardingSphereSchema {
    
    @Getter
    private final String name;
    
    @Getter
    private final DatabaseType protocolType;
    
    @Getter(AccessLevel.NONE)
    private DatabaseIdentifierContext identifierContext;
    
    @Getter(AccessLevel.NONE)
    private IdentifierIndex<ShardingSphereTable> tableIndex;
    
    @Getter(AccessLevel.NONE)
    private IdentifierIndex<ShardingSphereView> viewIndex;
    
    /**
     * Construct schema with the temporary default identifier context.
     *
     * <p>TODO(haoran): Replace this fallback with explicit identifier context injection after all schema creation paths migrate.</p>
     *
     * @param name schema name
     * @param protocolType protocol type
     */
    public ShardingSphereSchema(final String name, final DatabaseType protocolType) {
        this.name = name;
        this.protocolType = protocolType;
        identifierContext = DatabaseIdentifierContextFactory.createDefault();
        tableIndex = new IdentifierIndex<>(identifierContext, IdentifierScope.TABLE);
        viewIndex = new IdentifierIndex<>(identifierContext, IdentifierScope.VIEW);
        tableIndex.rebuild(Collections.emptyMap());
        viewIndex.rebuild(Collections.emptyMap());
    }
    
    /**
     * Construct schema with tables and views by using the temporary default identifier context.
     *
     * <p>TODO(haoran): Replace this fallback with explicit identifier context injection after all schema creation paths migrate.</p>
     *
     * @param name schema name
     * @param protocolType protocol type
     * @param tables tables
     * @param views views
     */
    public ShardingSphereSchema(final String name, final DatabaseType protocolType, final Collection<ShardingSphereTable> tables, final Collection<ShardingSphereView> views) {
        this(name, protocolType);
        Map<String, ShardingSphereTable> tableMap = createTableMap(tables);
        Map<String, ShardingSphereView> viewMap = createViewMap(views);
        tableMap.values().forEach(this::refreshTableIdentifierContext);
        tableIndex.rebuild(tableMap);
        viewIndex.rebuild(viewMap);
    }
    
    /**
     * Get all tables.
     *
     * @return all tables
     */
    public Collection<ShardingSphereTable> getAllTables() {
        return tableIndex.getAll();
    }
    
    /**
     * Find table.
     *
     * @param tableName table name
     * @return table
     */
    private Optional<ShardingSphereTable> findTable(final IdentifierValue tableName) {
        return tableIndex.find(tableName);
    }
    
    /**
     * Judge whether contains table.
     *
     * @param tableName table name
     * @return contains table or not
     */
    public boolean containsTable(final String tableName) {
        return containsTable(new IdentifierValue(tableName, QuoteCharacter.NONE));
    }
    
    /**
     * Judge whether contains table.
     *
     * @param tableName table name
     * @return contains table or not
     */
    public boolean containsTable(final IdentifierValue tableName) {
        return findTable(tableName).isPresent();
    }
    
    /**
     * Get table.
     *
     * @param tableName table name
     * @return table
     */
    public ShardingSphereTable getTable(final String tableName) {
        return getTable(new IdentifierValue(tableName, QuoteCharacter.NONE));
    }
    
    /**
     * Get table.
     *
     * @param tableName table name
     * @return table
     */
    public ShardingSphereTable getTable(final IdentifierValue tableName) {
        return findTable(tableName).orElse(null);
    }
    
    /**
     * Add table.
     *
     * @param table table
     */
    public void putTable(final ShardingSphereTable table) {
        refreshTableIdentifierContext(table);
        tableIndex.put(table.getName(), table);
    }
    
    /**
     * Remove table.
     *
     * @param tableName table name
     */
    public void removeTable(final String tableName) {
        ShardingSphereTable table = getTable(tableName);
        if (null == table) {
            return;
        }
        tableIndex.remove(table.getName());
    }
    
    /**
     * Get all views.
     *
     * @return all views
     */
    public Collection<ShardingSphereView> getAllViews() {
        return viewIndex.getAll();
    }
    
    /**
     * Find view.
     *
     * @param viewName view name
     * @return view
     */
    private Optional<ShardingSphereView> findView(final IdentifierValue viewName) {
        return viewIndex.find(viewName);
    }
    
    /**
     * Judge whether contains view.
     *
     * @param viewName view name
     * @return contains view or not
     */
    public boolean containsView(final String viewName) {
        return containsView(new IdentifierValue(viewName, QuoteCharacter.NONE));
    }
    
    /**
     * Judge whether contains view.
     *
     * @param viewName view name
     * @return contains view or not
     */
    private boolean containsView(final IdentifierValue viewName) {
        return findView(viewName).isPresent();
    }
    
    /**
     * Get view.
     *
     * @param viewName view name
     * @return view
     */
    public ShardingSphereView getView(final String viewName) {
        return getView(new IdentifierValue(viewName, QuoteCharacter.NONE));
    }
    
    /**
     * Get view.
     *
     * @param viewName view name
     * @return view
     */
    private ShardingSphereView getView(final IdentifierValue viewName) {
        return findView(viewName).orElse(null);
    }
    
    /**
     * Add view.
     *
     * @param view view
     */
    public void putView(final ShardingSphereView view) {
        viewIndex.put(view.getName(), view);
    }
    
    /**
     * Remove view.
     *
     * @param viewName view name
     */
    public void removeView(final String viewName) {
        ShardingSphereView view = getView(viewName);
        if (null == view) {
            return;
        }
        viewIndex.remove(view.getName());
    }
    
    /**
     * Refresh shared database identifier context.
     *
     * @param identifierContext database identifier context
     */
    public void refreshIdentifierContext(final DatabaseIdentifierContext identifierContext) {
        final Collection<ShardingSphereTable> tables = new LinkedList<>(tableIndex.getAll());
        final Collection<ShardingSphereView> views = new LinkedList<>(viewIndex.getAll());
        this.identifierContext = identifierContext;
        tableIndex = new IdentifierIndex<>(identifierContext, IdentifierScope.TABLE);
        viewIndex = new IdentifierIndex<>(identifierContext, IdentifierScope.VIEW);
        tables.forEach(this::refreshTableIdentifierContext);
        tableIndex.rebuild(createTableMap(tables));
        viewIndex.rebuild(createViewMap(views));
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
     * Get visible column names.
     *
     * @param tableName table name
     * @return visible column names
     */
    public List<String> getVisibleColumnNames(final IdentifierValue tableName) {
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
     * Get visible column and index map.
     *
     * @param tableName table name
     * @return visible column and index map
     */
    public Map<String, Integer> getVisibleColumnAndIndexMap(final IdentifierValue tableName) {
        return containsTable(tableName) ? getTable(tableName).getVisibleColumnAndIndexMap() : Collections.emptyMap();
    }
    
    /**
     * Whether empty schema.
     *
     * @return empty schema or not
     */
    public boolean isEmpty() {
        return tableIndex.isEmpty() && viewIndex.isEmpty();
    }
    
    private void refreshTableIdentifierContext(final ShardingSphereTable table) {
        table.refreshIdentifierContext(identifierContext);
    }
    
    private Map<String, ShardingSphereTable> createTableMap(final Collection<ShardingSphereTable> tables) {
        return tables.stream().collect(Collectors.toMap(ShardingSphereTable::getName, each -> each, (oldValue, currentValue) -> currentValue,
                () -> new LinkedHashMap<>(tables.size(), 1F)));
    }
    
    private Map<String, ShardingSphereView> createViewMap(final Collection<ShardingSphereView> views) {
        return views.stream().collect(Collectors.toMap(ShardingSphereView::getName, each -> each, (oldValue, currentValue) -> currentValue,
                () -> new LinkedHashMap<>(views.size(), 1F)));
    }
}
