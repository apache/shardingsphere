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

import lombok.EqualsAndHashCode;
import lombok.Getter;
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
@Getter
@EqualsAndHashCode
@ToString
public final class ShardingSphereTable {
    
    private final String name;
    
    private final Map<String, ShardingSphereColumn> columns;
    
    private final Map<String, ShardingSphereIndex> indexes;
    
    private final Map<String, ShardingSphereConstraint> constrains;
    
    private final List<String> columnNames = new ArrayList<>();
    
    private final List<String> visibleColumns = new ArrayList<>();
    
    private final List<String> primaryKeyColumns = new ArrayList<>();
    
    public ShardingSphereTable() {
        this("", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }
    
    public ShardingSphereTable(final String name, final Collection<ShardingSphereColumn> columnList,
                               final Collection<ShardingSphereIndex> indexList, final Collection<ShardingSphereConstraint> constraintList) {
        this.name = name;
        columns = getColumns(columnList);
        indexes = getIndexes(indexList);
        constrains = getConstrains(constraintList);
    }
    
    private Map<String, ShardingSphereColumn> getColumns(final Collection<ShardingSphereColumn> columnList) {
        Map<String, ShardingSphereColumn> result = new LinkedHashMap<>(columnList.size(), 1);
        for (ShardingSphereColumn each : columnList) {
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
    
    private Map<String, ShardingSphereIndex> getIndexes(final Collection<ShardingSphereIndex> indexList) {
        Map<String, ShardingSphereIndex> result = new LinkedHashMap<>(indexList.size(), 1);
        for (ShardingSphereIndex each : indexList) {
            result.put(each.getName().toLowerCase(), each);
        }
        return result;
    }
    
    private Map<String, ShardingSphereConstraint> getConstrains(final Collection<ShardingSphereConstraint> constraintList) {
        Map<String, ShardingSphereConstraint> result = new LinkedHashMap<>(constraintList.size(), 1);
        for (ShardingSphereConstraint each : constraintList) {
            result.put(each.getName().toLowerCase(), each);
        }
        return result;
    }
}
