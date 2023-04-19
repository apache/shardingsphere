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
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ShardingSphere table.
 */
@RequiredArgsConstructor
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
    
    public ShardingSphereTable() {
        this("", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }
    
    public ShardingSphereTable(final String name, final Collection<ShardingSphereColumn> columns,
                               final Collection<ShardingSphereIndex> indexes, final Collection<ShardingSphereConstraint> constraints) {
        this.name = name;
        this.columns = getColumns(columns);
        this.indexes = getIndexes(indexes);
        this.constraints = getConstraints(constraints);
    }
    
    private Map<String, ShardingSphereColumn> getColumns(final Collection<ShardingSphereColumn> columns) {
        Map<String, ShardingSphereColumn> result = new LinkedHashMap<>(columns.size(), 1);
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
    
    private Map<String, ShardingSphereIndex> getIndexes(final Collection<ShardingSphereIndex> indexes) {
        Map<String, ShardingSphereIndex> result = new LinkedHashMap<>(indexes.size(), 1);
        for (ShardingSphereIndex each : indexes) {
            result.put(each.getName().toLowerCase(), each);
        }
        return result;
    }
    
    private Map<String, ShardingSphereConstraint> getConstraints(final Collection<ShardingSphereConstraint> constraints) {
        Map<String, ShardingSphereConstraint> result = new LinkedHashMap<>(constraints.size(), 1);
        for (ShardingSphereConstraint each : constraints) {
            result.put(each.getName().toLowerCase(), each);
        }
        return result;
    }
    
    /**
     * Get table meta data via column name.
     *
     * @param columnName column name
     * @return table meta data
     */
    public ShardingSphereColumn getColumn(final String columnName) {
        return columns.get(columnName.toLowerCase());
    }
}
