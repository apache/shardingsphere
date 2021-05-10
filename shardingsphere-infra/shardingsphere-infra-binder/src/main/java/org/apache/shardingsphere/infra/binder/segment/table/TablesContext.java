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

package org.apache.shardingsphere.infra.binder.segment.table;

import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Tables context.
 */
@Getter
@ToString
public final class TablesContext {
    
    private final Collection<SimpleTableSegment> tables;
    
    public TablesContext(final SimpleTableSegment tableSegment) {
        this(null == tableSegment ? Collections.emptyList() : Collections.singletonList(tableSegment));
    }
    
    public TablesContext(final Collection<SimpleTableSegment> tableSegments) {
        Collection<SimpleTableSegment> actualTables = new LinkedList<>(tableSegments);
        Set<String> tableSets = new HashSet<>(actualTables.size(), 1);
        actualTables.removeIf(each -> !tableSets.add(each.getTableName().getIdentifier().getValue()));
        tables = actualTables;
    }
    
    /**
     * Get table names.
     * 
     * @return table names
     */
    public Collection<String> getTableNames() {
        return tables.stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(
                Collectors.toSet());
    }
    
    /**
     * Find table name.
     *
     * @param column column segment
     * @param schema schema meta data
     * @return table name
     */
    public Optional<String> findTableName(final ColumnSegment column, final ShardingSphereSchema schema) {
        if (1 == tables.size()) {
            return Optional.of(tables.iterator().next().getTableName().getIdentifier().getValue());
        }
        if (column.getOwner().isPresent()) {
            return Optional.of(findTableNameFromSQL(column.getOwner().get().getIdentifier().getValue()));
        }
        return findTableNameFromMetaData(column.getIdentifier().getValue(), schema);
    }
    
    /**
     * Find table name.
     *
     * @param column column projection
     * @param schema schema meta data
     * @return table name
     */
    public Optional<String> findTableName(final ColumnProjection column, final ShardingSphereSchema schema) {
        if (1 == tables.size()) {
            return Optional.of(tables.iterator().next().getTableName().getIdentifier().getValue());
        }
        if (null != column.getOwner()) {
            return Optional.of(findTableNameFromSQL(column.getOwner()));
        }
        return findTableNameFromMetaData(column.getName(), schema);
    }
    
    /**
     * Find table name from SQL.
     * @param tableNameOrAlias table name or alias
     * @return table name
     */
    public String findTableNameFromSQL(final String tableNameOrAlias) {
        for (SimpleTableSegment each : tables) {
            if (tableNameOrAlias.equalsIgnoreCase(each.getTableName().getIdentifier().getValue()) || tableNameOrAlias.equals(each.getAlias().orElse(null))) {
                return each.getTableName().getIdentifier().getValue();
            }
        }
        throw new IllegalStateException("Can not find owner from table.");
    }
    
    private Optional<String> findTableNameFromMetaData(final String columnName, final ShardingSphereSchema schema) {
        for (SimpleTableSegment each : tables) {
            if (schema.containsColumn(each.getTableName().getIdentifier().getValue(), columnName)) {
                return Optional.of(each.getTableName().getIdentifier().getValue());
            }
        }
        return Optional.empty();
    }
}
