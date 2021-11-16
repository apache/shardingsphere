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
import org.apache.shardingsphere.infra.binder.segment.select.subquery.SubqueryTableContext;
import org.apache.shardingsphere.infra.binder.segment.select.subquery.engine.SubqueryTableContextEngine;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Tables context.
 */
@Getter
@ToString
public final class TablesContext {
    
    private final Collection<SimpleTableSegment> originalTables = new LinkedList<>();
    
    private final Map<String, SimpleTableSegment> uniqueTables = new HashMap<>();
    
    private final Map<String, Collection<SubqueryTableContext>> subqueryTables = new HashMap<>();
    
    private final Collection<String> schemaNames = new HashSet<>();
    
    public TablesContext(final SimpleTableSegment tableSegment) {
        this(Collections.singletonList(tableSegment));
    }
    
    public TablesContext(final Collection<SimpleTableSegment> tableSegments) {
        this(tableSegments, Collections.emptyMap());
    }
    
    public TablesContext(final Collection<? extends TableSegment> tableSegments, final Map<Integer, SelectStatementContext> subqueryContexts) {
        if (tableSegments.isEmpty()) {
            return;
        }
        Collection<SimpleTableSegment> simpleTableSegments = tableSegments.stream().filter(each 
            -> each instanceof SimpleTableSegment).map(each -> (SimpleTableSegment) each).collect(Collectors.toList());
        for (SimpleTableSegment each : simpleTableSegments) {
            originalTables.add(each);
            uniqueTables.putIfAbsent(each.getTableName().getIdentifier().getValue(), each);
            each.getOwner().ifPresent(optional -> schemaNames.add(optional.getIdentifier().getValue()));
        }
        Collection<SubqueryTableSegment> subqueryTableSegments = tableSegments.stream().filter(each
            -> each instanceof SubqueryTableSegment).map(each -> (SubqueryTableSegment) each).collect(Collectors.toList());
        for (SubqueryTableSegment each : subqueryTableSegments) {
            SelectStatementContext subqueryContext = subqueryContexts.get(each.getSubquery().getStartIndex());
            subqueryContext.setSubqueryTable(true);
            Collection<SubqueryTableContext> subqueryTableContexts = new SubqueryTableContextEngine().createSubqueryTableContexts(subqueryContext, each.getAlias().orElse(null));
            subqueryTables.putAll(subqueryTableContexts.stream().collect(Collectors.groupingBy(SubqueryTableContext::getAlias)));
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
     * Get all unique table segments.
     *
     * @return all unique table segments
     */
    public Collection<SimpleTableSegment> getAllUniqueTables() {
        return uniqueTables.values();
    }
    
    /**
     * Find table name.
     *
     * @param columns column segment collection
     * @param schema schema meta data
     * @return table name map
     */
    public Map<String, String> findTableName(final Collection<ColumnSegment> columns, final ShardingSphereSchema schema) {
        if (1 == uniqueTables.size()) {
            String tableName = uniqueTables.keySet().iterator().next();
            return columns.stream().collect(Collectors.toMap(ColumnSegment::getQualifiedName, each -> tableName, (oldValue, currentValue) -> oldValue));
        }
        Map<String, String> result = new HashMap<>(columns.size(), 1);
        Map<String, List<ColumnSegment>> ownerColumns = columns.stream().filter(each -> each.getOwner().isPresent()).collect(Collectors.groupingBy(each
            -> each.getOwner().map(optional -> optional.getIdentifier().getValue()).orElse(null), () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER), Collectors.toList()));
        result.putAll(findTableNameFromSQL(ownerColumns));
        Collection<String> columnNames = columns.stream().filter(each -> !each.getOwner().isPresent()).map(each -> each.getIdentifier().getValue()).collect(Collectors.toSet());
        result.putAll(findTableNameFromMetaData(columnNames, schema));
        return result;
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
     * 
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
    
    private Map<String, String> findTableNameFromSQL(final Map<String, List<ColumnSegment>> ownerColumns) {
        if (ownerColumns.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new HashMap<>();
        for (String each : uniqueTables.keySet()) {
            if (ownerColumns.containsKey(each)) {
                ownerColumns.get(each).stream().map(ColumnSegment::getQualifiedName).forEach(column -> result.put(column, each));
            }
            Optional<String> alias = uniqueTables.get(each).getAlias();
            if (alias.isPresent() && ownerColumns.containsKey(alias.get())) {
                ownerColumns.get(alias.get()).stream().map(ColumnSegment::getQualifiedName).forEach(column -> result.put(column, each));
            }
        }
        return result;
    }
    
    private Map<String, String> findTableNameFromMetaData(final Collection<String> columnNames, final ShardingSphereSchema schema) {
        if (columnNames.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new HashMap<>();
        for (String each : uniqueTables.keySet()) {
            Collection<String> tableColumnNames = schema.getAllColumnNames(each);
            if (tableColumnNames.isEmpty()) {
                continue;
            }
            Collection<String> intersectionColumnNames = tableColumnNames.stream().filter(columnNames::contains).collect(Collectors.toList());
            for (String columnName : intersectionColumnNames) {
                result.put(columnName, each);
            }
        }
        return result;
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
     * Find table name from subquery.
     * 
     * @param columnName column name
     * @param owner column owner
     * @return table name
     */
    public Optional<String> findTableNameFromSubquery(final String columnName, final String owner) {
        Collection<SubqueryTableContext> subqueryTableContexts = subqueryTables.get(owner);
        if (null != subqueryTableContexts) {
            return subqueryTableContexts.stream().filter(each -> each.getColumnNames().contains(columnName)).map(SubqueryTableContext::getTableName).findFirst();
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
