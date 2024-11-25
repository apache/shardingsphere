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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.infra.database.core.metadata.database.enums.TableType;
import org.apache.shardingsphere.infra.metadata.identifier.ShardingSphereMetaDataIdentifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ShardingSphere table.
 */
@Getter
@EqualsAndHashCode
@ToString
public final class ShardingSphereTable {
    
    private final String name;
    
    @Getter(AccessLevel.NONE)
    private final Map<ShardingSphereMetaDataIdentifier, ShardingSphereColumn> columns;
    
    private final List<String> columnNames = new ArrayList<>();
    
    private final List<String> primaryKeyColumns = new ArrayList<>();
    
    private final List<String> visibleColumns = new ArrayList<>();
    
    private final Map<String, Integer> visibleColumnAndIndexMap = new CaseInsensitiveMap<>();
    
    @Getter(AccessLevel.NONE)
    private final Map<ShardingSphereMetaDataIdentifier, ShardingSphereIndex> indexes;
    
    @Getter(AccessLevel.NONE)
    private final Map<ShardingSphereMetaDataIdentifier, ShardingSphereConstraint> constraints;
    
    private final TableType type;
    
    public ShardingSphereTable() {
        this("", Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), TableType.TABLE);
    }
    
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
    
    private Map<ShardingSphereMetaDataIdentifier, ShardingSphereColumn> createColumns(final Collection<ShardingSphereColumn> columns) {
        Map<ShardingSphereMetaDataIdentifier, ShardingSphereColumn> result = new LinkedHashMap<>(columns.size(), 1F);
        int index = 0;
        for (ShardingSphereColumn each : columns) {
            result.put(new ShardingSphereMetaDataIdentifier(each.getName()), each);
            columnNames.add(each.getName());
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
    
    private Map<ShardingSphereMetaDataIdentifier, ShardingSphereIndex> createIndexes(final Collection<ShardingSphereIndex> indexes) {
        return indexes.stream().collect(Collectors.toMap(each -> new ShardingSphereMetaDataIdentifier(each.getName()), each -> each, (a, b) -> b, () -> new LinkedHashMap<>(indexes.size(), 1F)));
    }
    
    private Map<ShardingSphereMetaDataIdentifier, ShardingSphereConstraint> createConstraints(final Collection<ShardingSphereConstraint> constraints) {
        return constraints.stream()
                .collect(Collectors.toMap(each -> new ShardingSphereMetaDataIdentifier(each.getName()), each -> each, (a, b) -> b, () -> new LinkedHashMap<>(constraints.size(), 1F)));
    }
    
    /**
     * Judge whether contains column.
     *
     * @param columnName column name
     * @return contains column or not
     */
    public boolean containsColumn(final String columnName) {
        return null != columnName && columns.containsKey(new ShardingSphereMetaDataIdentifier(columnName));
    }
    
    /**
     * Get column.
     *
     * @param columnName column name
     * @return column
     */
    public ShardingSphereColumn getColumn(final String columnName) {
        return columns.get(new ShardingSphereMetaDataIdentifier(columnName));
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
     * Judge whether contains index.
     *
     * @param indexName index name
     * @return contains index or not
     */
    public boolean containsIndex(final String indexName) {
        return null != indexName && indexes.containsKey(new ShardingSphereMetaDataIdentifier(indexName));
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
        indexes.put(new ShardingSphereMetaDataIdentifier(index.getName()), index);
    }
    
    /**
     * Remove index.
     *
     * @param indexName index name
     */
    public void removeIndex(final String indexName) {
        indexes.remove(new ShardingSphereMetaDataIdentifier(indexName));
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
