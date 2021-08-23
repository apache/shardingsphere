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

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/**
 * Tables context.
 */
@Getter
@ToString
public final class TablesContext {
    
    private final Collection<SimpleTableSegment> originalTables = new LinkedList<>();
    
    private final Map<String, SimpleTableSegment> uniqueTables = new HashMap<>();
    
    private final Collection<String> schemaNames = new HashSet<>();
    
    public TablesContext(final SimpleTableSegment tableSegment) {
        this(null == tableSegment ? Collections.emptyList() : Collections.singletonList(tableSegment));
    }
    
    public TablesContext(final Collection<SimpleTableSegment> tableSegments) {
        if (tableSegments.isEmpty()) {
            return;
        }
        for (SimpleTableSegment each : tableSegments) {
            originalTables.add(each);
            uniqueTables.putIfAbsent(each.getTableName().getIdentifier().getValue(), each);
            each.getOwner().ifPresent(optional -> schemaNames.add(optional.getIdentifier().getValue()));
        }
    }
    
    /**
     * Get table names.
     * 
     * @return table names
     */
    public Collection<String> getTableNames() {
        return uniqueTables.keySet();
    }
    
    /**
     * Find table name.
     *
     * @param column column segment
     * @param schema schema meta data
     * @return table name
     */
    public Optional<String> findTableName(final ColumnSegment column, final ShardingSphereSchema schema) {
        if (1 == uniqueTables.size()) {
            return Optional.of(uniqueTables.keySet().iterator().next());
        }
        if (column.getOwner().isPresent()) {
            return findTableNameFromSQL(column.getOwner().get().getIdentifier().getValue());
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
        if (1 == uniqueTables.size()) {
            return Optional.of(uniqueTables.keySet().iterator().next());
        }
        if (null != column.getOwner()) {
            return findTableNameFromSQL(column.getOwner());
        }
        return findTableNameFromMetaData(column.getName(), schema);
    }
    
    /**
     * Find table name from SQL.
     * @param tableNameOrAlias table name or alias
     * @return table name
     */
    public Optional<String> findTableNameFromSQL(final String tableNameOrAlias) {
        for (String each : uniqueTables.keySet()) {
            if (tableNameOrAlias.equalsIgnoreCase(each) || tableNameOrAlias.equalsIgnoreCase(uniqueTables.get(each).getAlias().orElse(null))) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    private Optional<String> findTableNameFromMetaData(final String columnName, final ShardingSphereSchema schema) {
        for (String each : uniqueTables.keySet()) {
            if (schema.containsColumn(each, columnName)) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    /**
     * Get schema name.
     *
     * @return schema name
     */
    public Optional<String> getSchemaName() {
        Preconditions.checkState(schemaNames.size() <= 1, "Can not support multiple different schema.");
        return schemaNames.stream().findFirst();
    }
}
