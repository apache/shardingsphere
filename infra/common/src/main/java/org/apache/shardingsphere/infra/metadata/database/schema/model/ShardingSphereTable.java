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

import com.cedarsoftware.util.CaseInsensitiveMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.TableType;
import org.apache.shardingsphere.infra.metadata.identifier.ShardingSphereIdentifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ShardingSphere table.
 */
@Getter
@ToString
public final class ShardingSphereTable {
    
    private final String name;
    
    @Getter(AccessLevel.NONE)
    private final Map<ShardingSphereIdentifier, ShardingSphereColumn> columns;
    
    private final List<ShardingSphereIdentifier> columnNames = new ArrayList<>();
    
    private final List<String> primaryKeyColumns = new ArrayList<>();
    
    private final List<String> visibleColumns = new ArrayList<>();
    
    private final Map<String, Integer> visibleColumnAndIndexMap = new CaseInsensitiveMap<>();
    
    @Getter(AccessLevel.NONE)
    private final Map<ShardingSphereIdentifier, ShardingSphereIndex> indexes;
    
    @Getter(AccessLevel.NONE)
    private final Map<ShardingSphereIdentifier, ShardingSphereConstraint> constraints;
    
    private final TableType type;
    
    public ShardingSphereTable(final String name, final Collection<ShardingSphereColumn> columns,
                               final Collection<ShardingSphereIndex> indexes, final Collection<ShardingSphereConstraint> constraints) {
        this(name, columns, indexes, constraints, TableType.TABLE);
    }
    
    public ShardingSphereTable(final String name, final Collection<ShardingSphereColumn> columns,
                               final Collection<ShardingSphereIndex> indexes, final Collection<ShardingSphereConstraint> constraints, final TableType type) {
        this.name = name;
        this.columns = createColumns(columns);
        this.indexes = createIndexes(indexes);
        this.constraints = createConstraints(constraints);
        this.type = type;
    }
    
    private Map<ShardingSphereIdentifier, ShardingSphereColumn> createColumns(final Collection<ShardingSphereColumn> columns) {
        Map<ShardingSphereIdentifier, ShardingSphereColumn> result = new LinkedHashMap<>(columns.size(), 1F);
        int index = 0;
        for (ShardingSphereColumn each : columns) {
            ShardingSphereIdentifier columnName = new ShardingSphereIdentifier(each.getName());
            if (result.containsKey(columnName)) {
                continue;
            }
            result.put(columnName, each);
            columnNames.add(columnName);
            if (each.isPrimaryKey()) {
                primaryKeyColumns.add(each.getName());
            }
            if (each.isVisible()) {
                visibleColumns.add(each.getName());
                visibleColumnAndIndexMap.put(each.getName(), index++);
            }
        }
        return result;
    }
    
    private Map<ShardingSphereIdentifier, ShardingSphereIndex> createIndexes(final Collection<ShardingSphereIndex> indexes) {
        return indexes.stream()
                .collect(Collectors.toMap(each -> new ShardingSphereIdentifier(each.getName()), each -> each, (oldValue, currentValue) -> currentValue, () -> new LinkedHashMap<>(indexes.size(), 1F)));
    }
    
    private Map<ShardingSphereIdentifier, ShardingSphereConstraint> createConstraints(final Collection<ShardingSphereConstraint> constraints) {
        return constraints.stream()
                .collect(Collectors.toMap(each -> new ShardingSphereIdentifier(each.getName()), each -> each, (oldValue, currentValue) -> currentValue,
                        () -> new LinkedHashMap<>(constraints.size(), 1F)));
    }
    
    /**
     * Judge whether contains column.
     *
     * @param columnName column name
     * @return contains column or not
     */
    public boolean containsColumn(final String columnName) {
        return null != columnName && columns.containsKey(new ShardingSphereIdentifier(columnName));
    }
    
    /**
     * Get column.
     *
     * @param columnName column name
     * @return column
     */
    public ShardingSphereColumn getColumn(final String columnName) {
        return columns.get(new ShardingSphereIdentifier(columnName));
    }
    
    /**
     * Get all columns.
     *
     * @return columns
     */
    public Collection<ShardingSphereColumn> getAllColumns() {
        return columns.values();
    }
    
    /**
     * Find column names If not existed from passing by column names.
     *
     * @param columnNames column names
     * @return found column names
     */
    public Collection<String> findColumnNamesIfNotExistedFrom(final Collection<String> columnNames) {
        if (columnNames.size() == columns.size()) {
            return Collections.emptyList();
        }
        Collection<ShardingSphereIdentifier> result = new LinkedHashSet<>(columns.keySet());
        result.removeAll(columnNames.stream().map(ShardingSphereIdentifier::new).collect(Collectors.toSet()));
        return result.stream().map(ShardingSphereIdentifier::getValue).collect(Collectors.toList());
    }
    
    /**
     * Judge whether contains index.
     *
     * @param indexName index name
     * @return contains index or not
     */
    public boolean containsIndex(final String indexName) {
        return null != indexName && indexes.containsKey(new ShardingSphereIdentifier(indexName));
    }
    
    /**
     * Get all indexes.
     *
     * @return indexes
     */
    public Collection<ShardingSphereIndex> getAllIndexes() {
        return indexes.values();
    }
    
    /**
     * Put index.
     *
     * @param index index
     */
    public void putIndex(final ShardingSphereIndex index) {
        indexes.put(new ShardingSphereIdentifier(index.getName()), index);
    }
    
    /**
     * Remove index.
     *
     * @param indexName index name
     */
    public void removeIndex(final String indexName) {
        indexes.remove(new ShardingSphereIdentifier(indexName));
    }
    
    /**
     * Get all constraint.
     *
     * @return constraint
     */
    public Collection<ShardingSphereConstraint> getAllConstraints() {
        return constraints.values();
    }
}
