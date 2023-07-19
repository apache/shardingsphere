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
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Tables context.
 */
@Getter
@ToString
public final class TablesContext {
    
    private final Collection<TableSegment> tableSegments = new LinkedList<>();
    
    private final Collection<SimpleTableSegment> simpleTableSegments = new LinkedList<>();
    
    private final Collection<String> tableNames = new HashSet<>();
    
    private final Collection<String> schemaNames = new HashSet<>();
    
    private final Collection<String> databaseNames = new HashSet<>();
    
    private final Map<String, Collection<SubqueryTableContext>> subqueryTables = new HashMap<>();
    
    public TablesContext(final SimpleTableSegment tableSegment, final DatabaseType databaseType) {
        this(Collections.singletonList(tableSegment), databaseType);
    }
    
    public TablesContext(final Collection<SimpleTableSegment> tableSegments, final DatabaseType databaseType) {
        this(tableSegments, Collections.emptyMap(), databaseType);
    }
    
    public TablesContext(final Collection<? extends TableSegment> tableSegments, final Map<Integer, SelectStatementContext> subqueryContexts, final DatabaseType databaseType) {
        if (tableSegments.isEmpty()) {
            return;
        }
        this.tableSegments.addAll(tableSegments);
        for (TableSegment each : tableSegments) {
            if (each instanceof SimpleTableSegment) {
                SimpleTableSegment simpleTableSegment = (SimpleTableSegment) each;
                simpleTableSegments.add(simpleTableSegment);
                tableNames.add(simpleTableSegment.getTableName().getIdentifier().getValue());
                simpleTableSegment.getOwner().ifPresent(optional -> schemaNames.add(optional.getIdentifier().getValue()));
                findDatabaseName(simpleTableSegment, databaseType).ifPresent(databaseNames::add);
            }
            if (each instanceof SubqueryTableSegment) {
                subqueryTables.putAll(createSubqueryTables(subqueryContexts, (SubqueryTableSegment) each));
            }
        }
    }
    
    private Optional<String> findDatabaseName(final SimpleTableSegment tableSegment, final DatabaseType databaseType) {
        Optional<OwnerSegment> owner = databaseType.getDefaultSchema().isPresent() ? tableSegment.getOwner().flatMap(OwnerSegment::getOwner) : tableSegment.getOwner();
        return owner.map(optional -> optional.getIdentifier().getValue());
    }
    
    private Map<String, Collection<SubqueryTableContext>> createSubqueryTables(final Map<Integer, SelectStatementContext> subqueryContexts, final SubqueryTableSegment subqueryTable) {
        SelectStatementContext subqueryContext = subqueryContexts.get(subqueryTable.getSubquery().getStartIndex());
        Map<String, SubqueryTableContext> subqueryTableContexts = new SubqueryTableContextEngine().createSubqueryTableContexts(subqueryContext, subqueryTable.getAliasName().orElse(null));
        Map<String, Collection<SubqueryTableContext>> result = new HashMap<>();
        for (SubqueryTableContext each : subqueryTableContexts.values()) {
            if (null != each.getAliasName()) {
                result.computeIfAbsent(each.getAliasName(), unused -> new LinkedList<>()).add(each);
            }
        }
        return result;
    }
    
    /**
     * Get table names.
     * 
     * @return table names
     */
    public Collection<String> getTableNames() {
        return tableNames;
    }
    
    /**
     * Find expression table name map by column segment.
     *
     * @param columns column segment collection
     * @param schema schema meta data
     * @return expression table name map
     */
    public Map<String, String> findTableNamesByColumnSegment(final Collection<ColumnSegment> columns, final ShardingSphereSchema schema) {
        if (1 == simpleTableSegments.size()) {
            return findTableNameFromSingleTableByColumnSegment(columns);
        }
        Map<String, String> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        Map<String, Collection<String>> ownerColumnNames = getOwnerColumnNamesByColumnSegment(columns);
        result.putAll(findTableNameFromSQL(ownerColumnNames));
        Collection<String> noOwnerColumnNames = getNoOwnerColumnNamesByColumnSegment(columns);
        result.putAll(findTableNameFromMetaData(noOwnerColumnNames, schema));
        result.putAll(findTableNameFromSubqueryByColumnSegment(columns, result));
        return result;
    }
    
    /**
     * Find expression table name map by column projection.
     *
     * @param columns column segment collection
     * @param schema schema meta data
     * @return expression table name map
     */
    public Map<String, String> findTableNamesByColumnProjection(final Collection<ColumnProjection> columns, final ShardingSphereSchema schema) {
        if (1 == simpleTableSegments.size()) {
            return findTableNameFromSingleTableByColumnProjection(columns);
        }
        Map<String, String> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        Map<String, Collection<String>> ownerColumnNames = getOwnerColumnNamesByColumnProjection(columns);
        result.putAll(findTableNameFromSQL(ownerColumnNames));
        Collection<String> noOwnerColumnNames = getNoOwnerColumnNamesByColumnProjection(columns);
        result.putAll(findTableNameFromMetaData(noOwnerColumnNames, schema));
        result.putAll(findTableNameFromSubqueryByColumnProjection(columns, result));
        return result;
    }
    
    private Map<String, String> findTableNameFromSubqueryByColumnSegment(final Collection<ColumnSegment> columns, final Map<String, String> ownerTableNames) {
        if (ownerTableNames.size() == columns.size() || subqueryTables.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new LinkedHashMap<>(columns.size(), 1F);
        for (ColumnSegment each : columns) {
            if (ownerTableNames.containsKey(each.getExpression())) {
                continue;
            }
            String owner = each.getOwner().map(optional -> optional.getIdentifier().getValue()).orElse("");
            Collection<SubqueryTableContext> subqueryTableContexts = subqueryTables.getOrDefault(owner, Collections.emptyList());
            for (SubqueryTableContext subqueryTableContext : subqueryTableContexts) {
                if (subqueryTableContext.getColumnNames().contains(each.getIdentifier().getValue())) {
                    result.put(each.getExpression(), subqueryTableContext.getTableName());
                }
            }
        }
        return result;
    }
    
    private Map<String, String> findTableNameFromSubqueryByColumnProjection(final Collection<ColumnProjection> columns, final Map<String, String> ownerTableNames) {
        if (ownerTableNames.size() == columns.size() || subqueryTables.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new LinkedHashMap<>(columns.size(), 1F);
        for (ColumnProjection each : columns) {
            if (ownerTableNames.containsKey(each.getColumnName())) {
                continue;
            }
            Collection<SubqueryTableContext> subqueryTableContexts = each.getOwner().map(optional -> subqueryTables.get(each.getOwner().get().getValue())).orElseGet(Collections::emptyList);
            for (SubqueryTableContext subqueryTableContext : subqueryTableContexts) {
                if (subqueryTableContext.getColumnNames().contains(each.getName().getValue())) {
                    result.put(each.getColumnName(), subqueryTableContext.getTableName());
                }
            }
        }
        return result;
    }
    
    private Map<String, String> findTableNameFromSingleTableByColumnSegment(final Collection<ColumnSegment> columns) {
        String tableName = simpleTableSegments.iterator().next().getTableName().getIdentifier().getValue();
        Map<String, String> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (ColumnSegment each : columns) {
            result.putIfAbsent(each.getExpression(), tableName);
        }
        return result;
    }
    
    private Map<String, String> findTableNameFromSingleTableByColumnProjection(final Collection<ColumnProjection> columns) {
        String tableName = simpleTableSegments.iterator().next().getTableName().getIdentifier().getValue();
        Map<String, String> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (ColumnProjection each : columns) {
            result.putIfAbsent(each.getColumnName(), tableName);
        }
        return result;
    }
    
    private Map<String, Collection<String>> getOwnerColumnNamesByColumnSegment(final Collection<ColumnSegment> columns) {
        Map<String, Collection<String>> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (ColumnSegment each : columns) {
            if (!each.getOwner().isPresent()) {
                continue;
            }
            result.computeIfAbsent(each.getOwner().get().getIdentifier().getValue(), unused -> new LinkedList<>()).add(each.getExpression());
        }
        return result;
    }
    
    private Map<String, Collection<String>> getOwnerColumnNamesByColumnProjection(final Collection<ColumnProjection> columns) {
        Map<String, Collection<String>> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (ColumnProjection each : columns) {
            if (each.getOwner().isPresent()) {
                result.computeIfAbsent(each.getOwner().get().getValue(), unused -> new LinkedList<>()).add(each.getColumnName());
            }
        }
        return result;
    }
    
    private Map<String, String> findTableNameFromSQL(final Map<String, Collection<String>> ownerColumnNames) {
        if (ownerColumnNames.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new LinkedHashMap<>();
        for (SimpleTableSegment each : simpleTableSegments) {
            String tableName = each.getTableName().getIdentifier().getValue();
            if (ownerColumnNames.containsKey(tableName)) {
                ownerColumnNames.get(tableName).forEach(column -> result.put(column, tableName));
            }
            Optional<String> alias = each.getAliasName();
            if (alias.isPresent() && ownerColumnNames.containsKey(alias.get())) {
                ownerColumnNames.get(alias.get()).forEach(column -> result.put(column, tableName));
            }
        }
        return result;
    }
    
    private Map<String, String> findTableNameFromMetaData(final Collection<String> noOwnerColumnNames, final ShardingSphereSchema schema) {
        if (noOwnerColumnNames.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new LinkedHashMap<>(noOwnerColumnNames.size(), 1F);
        for (SimpleTableSegment each : simpleTableSegments) {
            String tableName = each.getTableName().getIdentifier().getValue();
            for (String columnName : schema.getAllColumnNames(tableName)) {
                if (noOwnerColumnNames.contains(columnName)) {
                    result.put(columnName, tableName);
                }
            }
        }
        return result;
    }
    
    private Collection<String> getNoOwnerColumnNamesByColumnSegment(final Collection<ColumnSegment> columns) {
        Collection<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (ColumnSegment each : columns) {
            if (!each.getOwner().isPresent()) {
                result.add(each.getIdentifier().getValue());
            }
        }
        return result;
    }
    
    private Collection<String> getNoOwnerColumnNamesByColumnProjection(final Collection<ColumnProjection> columns) {
        Collection<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (ColumnProjection each : columns) {
            if (!each.getOwner().isPresent()) {
                result.add(each.getName().getValue());
            }
        }
        return result;
    }
    
    /**
     * Get database name.
     *
     * @return database name
     */
    public Optional<String> getDatabaseName() {
        Preconditions.checkState(databaseNames.size() <= 1, "Can not support multiple different database.");
        return databaseNames.isEmpty() ? Optional.empty() : Optional.of(databaseNames.iterator().next());
    }
    
    /**
     * Get schema name.
     *
     * @return schema name
     */
    public Optional<String> getSchemaName() {
        return schemaNames.isEmpty() ? Optional.empty() : Optional.of(schemaNames.iterator().next());
    }
}
