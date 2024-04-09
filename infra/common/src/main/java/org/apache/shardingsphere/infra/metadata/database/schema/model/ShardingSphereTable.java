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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.infra.database.core.metadata.database.enums.TableType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ShardingSphere table.
 */
@Getter
@EqualsAndHashCode
@ToString
public final class ShardingSphereTable {
    
    private final String name;
    
    private final Map<String, ShardingSphereColumn> columns;
    
    private final Map<String, ShardingSphereIndex> indexes;
    
    private final Map<String, ShardingSphereConstraint> constraints;
    
    private final List<String> columnNames = new ArrayList<>();
    
    private final List<String> visibleColumns = new ArrayList<>();
    
    private final List<String> primaryKeyColumns = new ArrayList<>();
    
    private final TableType type;
    
    public ShardingSphereTable() {
        this("", Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), TableType.TABLE);
    }
    
    public ShardingSphereTable(final String name, final Collection<ShardingSphereColumn> columns,
                               final Collection<ShardingSphereIndex> indexes, final Collection<ShardingSphereConstraint> constraints) {
        this.name = name;
        this.columns = createColumns(columns);
        this.indexes = createIndexes(indexes);
        this.constraints = createConstraints(constraints);
        this.type = TableType.TABLE;
    }
    
    public ShardingSphereTable(final String name, final Collection<ShardingSphereColumn> columns,
                               final Collection<ShardingSphereIndex> indexes, final Collection<ShardingSphereConstraint> constraints, final TableType type) {
        this.name = name;
        this.columns = createColumns(columns);
        this.indexes = createIndexes(indexes);
        this.constraints = createConstraints(constraints);
        this.type = type;
    }
    
    private Map<String, ShardingSphereColumn> createColumns(final Collection<ShardingSphereColumn> columns) {
        Map<String, ShardingSphereColumn> result = new LinkedHashMap<>(columns.size(), 1F);
        for (ShardingSphereColumn each : columns) {
            String lowerColumnName = each.getName().toLowerCase();
            result.put(lowerColumnName, each);
            columnNames.add(each.getName());
            if (each.isPrimaryKey()) {
                primaryKeyColumns.add(lowerColumnName);
            }
            if (each.isVisible()) {
                visibleColumns.add(each.getName());
            }
        }
        return result;
    }
    
    private Map<String, ShardingSphereIndex> createIndexes(final Collection<ShardingSphereIndex> indexes) {
        Map<String, ShardingSphereIndex> result = new LinkedHashMap<>(indexes.size(), 1F);
        for (ShardingSphereIndex each : indexes) {
            result.put(each.getName().toLowerCase(), each);
        }
        return result;
    }
    
    private Map<String, ShardingSphereConstraint> createConstraints(final Collection<ShardingSphereConstraint> constraints) {
        Map<String, ShardingSphereConstraint> result = new LinkedHashMap<>(constraints.size(), 1F);
        for (ShardingSphereConstraint each : constraints) {
            result.put(each.getName().toLowerCase(), each);
        }
        return result;
    }
    
    /**
     * Put column meta data.
     *
     * @param column column meta data
     */
    public void putColumn(final ShardingSphereColumn column) {
        columns.put(column.getName().toLowerCase(), column);
    }
    
    /**
     * Get column meta data via column name.
     *
     * @param columnName column name
     * @return column meta data
     */
    public ShardingSphereColumn getColumn(final String columnName) {
        return columns.get(columnName.toLowerCase());
    }
    
    /**
     * Get column meta data collection.
     *
     * @return column meta data collection
     */
    public Collection<ShardingSphereColumn> getColumnValues() {
        return columns.values();
    }
    
    /**
     * Judge whether contains column or not.
     *
     * @param columnName column name
     * @return whether contains column or not
     */
    public boolean containsColumn(final String columnName) {
        return null != columnName && columns.containsKey(columnName.toLowerCase());
    }
    
    /**
     * Put index meta data.
     *
     * @param index index meta data
     */
    public void putIndex(final ShardingSphereIndex index) {
        indexes.put(index.getName().toLowerCase(), index);
    }
    
    /**
     * Remove index meta data via index name.
     *
     * @param indexName index name
     */
    public void removeIndex(final String indexName) {
        indexes.remove(indexName.toLowerCase());
    }
    
    /**
     * Get index meta data via index name.
     *
     * @param indexName index name
     * @return index meta data
     */
    public ShardingSphereIndex getIndex(final String indexName) {
        return indexes.get(indexName.toLowerCase());
    }
    
    /**
     * Get index meta data collection.
     *
     * @return index meta data collection
     */
    public Collection<ShardingSphereIndex> getIndexValues() {
        return indexes.values();
    }
    
    /**
     * Judge whether contains index or not.
     *
     * @param indexName index name
     * @return whether contains index or not
     */
    public boolean containsIndex(final String indexName) {
        return null != indexName && indexes.containsKey(indexName.toLowerCase());
    }
    
    /**
     * Get constraint meta data collection.
     *
     * @return constraint meta data collection
     */
    public Collection<ShardingSphereConstraint> getConstraintValues() {
        return constraints.values();
    }
}
