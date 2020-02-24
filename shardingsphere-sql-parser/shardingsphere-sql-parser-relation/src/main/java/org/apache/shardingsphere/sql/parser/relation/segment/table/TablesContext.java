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

package org.apache.shardingsphere.sql.parser.relation.segment.table;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.ToString;
import org.apache.shardingsphere.sql.parser.relation.metadata.RelationMetas;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.generic.TableSegmentAvailable;
import org.apache.shardingsphere.sql.parser.sql.statement.generic.TableSegmentsAvailable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.TreeSet;

/**
 * Tables context.
 */
@ToString
public final class TablesContext {
    
    private final Collection<Table> tables = new ArrayList<>();
    
    private String schema;
    
    public TablesContext(final SQLStatement sqlStatement) {
        Collection<String> aliases = new HashSet<>();
        Collection<TableSegment> tableSegments = getTableSegments(sqlStatement);
        for (TableSegment each : tableSegments) {
            Optional<String> alias = each.getAlias();
            if (alias.isPresent()) {
                aliases.add(alias.get());
            }
        }
        for (TableSegment each : tableSegments) {
            Optional<String> alias = each.getAlias();
            if (aliases.contains(each.getIdentifier().getValue()) && !alias.isPresent()) {
                continue;
            }
            tables.add(new Table(each.getIdentifier().getValue(), alias.orNull()));
            setSchema(each);
        }
    }
    
    private Collection<TableSegment> getTableSegments(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof TableSegmentAvailable) {
            TableSegment table = ((TableSegmentAvailable) sqlStatement).getTable();
            return null == table ? Collections.<TableSegment>emptyList() : Collections.singletonList(table);
        }
        if (sqlStatement instanceof TableSegmentsAvailable) {
            return ((TableSegmentsAvailable) sqlStatement).getTables();
        }
        return Collections.emptyList();
    }
    
    private void setSchema(final TableSegment tableSegment) {
        if (tableSegment.getOwner().isPresent()) {
            if (null != schema && !tableSegment.getOwner().get().getIdentifier().getValue().equalsIgnoreCase(schema)) {
                throw new UnsupportedOperationException("Cannot support multiple schemas in one SQL");
            }
            schema = tableSegment.getOwner().get().getIdentifier().getValue();
        }
    }
    
    /**
     * Judge table is empty or not.
     *
     * @return table is empty or not
     */
    public boolean isEmpty() {
        return tables.isEmpty();
    }
    
    /**
     * Judge is single table or not.
     * 
     * @return is single table or not
     */
    public boolean isSingleTable() {
        Collection<String> tableNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Table each : tables) {
            tableNames.add(each.getName());
        }
        return 1 == tableNames.size();
    }
    
    /**
     * Get single table name.
     *
     * @return single table name
     */
    public String getSingleTableName() {
        Preconditions.checkArgument(!isEmpty());
        return tables.iterator().next().getName();
    }
    
    /**
     * Get table names.
     * 
     * @return table names
     */
    public Collection<String> getTableNames() {
        Collection<String> result = new LinkedHashSet<>(tables.size(), 1);
        for (Table each : tables) {
            result.add(each.getName());
        }
        return result;
    }
    
    /**
     * Find table via table name or alias.
     * 
     * @param tableNameOrAlias table name or alias
     * @return table
     */
    public Optional<Table> find(final String tableNameOrAlias) {
        Optional<Table> tableFromName = findTableFromName(tableNameOrAlias);
        return tableFromName.isPresent() ? tableFromName : findTableFromAlias(tableNameOrAlias);
    }
    
    private Optional<Table> findTableFromName(final String name) {
        for (Table each : tables) {
            if (each.getName().equals(name)) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
    
    /**
     * Find table via table alias.
     * 
     * @param alias alias
     * @return table
     */
    public Optional<Table> findTableFromAlias(final String alias) {
        for (Table each : tables) {
            if (each.getAlias().isPresent() && each.getAlias().get().equalsIgnoreCase(alias)) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
    
    /**
     * Find table name.
     *
     * @param columnSegment column segment
     * @param relationMetas relation metas
     * @return table name
     */
    public Optional<String> findTableName(final ColumnSegment columnSegment, final RelationMetas relationMetas) {
        if (isSingleTable()) {
            return Optional.of(getSingleTableName());
        }
        if (columnSegment.getOwner().isPresent()) {
            Optional<Table> table = find(columnSegment.getOwner().get().getIdentifier().getValue());
            return table.isPresent() ? Optional.of(table.get().getName()) : Optional.<String>absent();
        }
        return findTableNameFromMetaData(columnSegment.getIdentifier().getValue(), relationMetas);
    }
    
    private Optional<String> findTableNameFromMetaData(final String columnName, final RelationMetas relationMetas) {
        for (String each : getTableNames()) {
            if (relationMetas.containsColumn(each, columnName)) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
    
    /**
     * Get schema.
     * 
     * @return schema
     */
    public Optional<String> getSchema() {
        return Optional.fromNullable(schema);
    }
}
