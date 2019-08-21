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

package org.apache.shardingsphere.core.optimize.api.segment;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.ToString;
import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.sql.segment.generic.AliasAvailable;
import org.apache.shardingsphere.core.parse.sql.segment.generic.TableAvailable;
import org.apache.shardingsphere.core.parse.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.TreeSet;

/**
 * Tables.
 * 
 * @author zhangliang
 */
@ToString
public final class Tables {
    
    private final Collection<Table> tables = new ArrayList<>();
    
    private String schema;
    
    public Tables(final SQLStatement sqlStatement) {
        for (TableAvailable each : sqlStatement.findSQLSegments(TableAvailable.class)) {
            String alias = each instanceof AliasAvailable ? ((AliasAvailable) each).getAlias().orNull() : null;
            tables.add(new Table(each.getTableName(), alias));
            if (each instanceof TableSegment) {
                setSchema((TableSegment) each);
            }
        }
    }
    
    private void setSchema(final TableSegment tableSegment) {
        if (tableSegment.getOwner().isPresent()) {
            if (null != schema && !tableSegment.getOwner().get().getName().equalsIgnoreCase(schema)) {
                throw new ShardingException("Cannot support multiple schemas in one SQL");
            }
            schema = tableSegment.getOwner().get().getName();
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
    
    private Optional<Table> findTableFromAlias(final String alias) {
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
     * @param tableMetas table metas
     * @return table name
     */
    public Optional<String> findTableName(final ColumnSegment columnSegment, final TableMetas tableMetas) {
        if (isSingleTable()) {
            return Optional.of(getSingleTableName());
        }
        if (columnSegment.getOwner().isPresent()) {
            Optional<Table> table = find(columnSegment.getOwner().get().getTableName());
            return table.isPresent() ? Optional.of(table.get().getName()) : Optional.<String>absent();
        }
        return findTableNameFromMetaData(columnSegment.getName(), tableMetas);
    }
    
    private Optional<String> findTableNameFromMetaData(final String columnName, final TableMetas tableMetas) {
        for (String each : getTableNames()) {
            if (tableMetas.containsColumn(each, columnName)) {
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
