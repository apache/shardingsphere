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

package org.apache.shardingsphere.infra.binder.context.segment.table;

import com.cedarsoftware.util.CaseInsensitiveMap;
import com.cedarsoftware.util.CaseInsensitiveSet;
import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.segment.select.subquery.SubqueryTableContext;
import org.apache.shardingsphere.infra.binder.context.segment.select.subquery.engine.SubqueryTableContextEngine;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;

/**
 * Tables context.
 */
@Getter
@ToString
public final class TablesContext {
    
    @Getter(AccessLevel.NONE)
    private final Collection<TableSegment> tables = new LinkedList<>();
    
    private final Collection<SimpleTableSegment> simpleTables = new LinkedList<>();
    
    private final Collection<String> tableNames = new CaseInsensitiveSet<>();
    
    private final Collection<String> schemaNames = new CaseInsensitiveSet<>();
    
    private final Collection<String> databaseNames = new CaseInsensitiveSet<>();
    
    @Getter(AccessLevel.NONE)
    private final Map<String, Collection<SubqueryTableContext>> subqueryTables = new HashMap<>();
    
    public TablesContext(final SimpleTableSegment table) {
        this(null == table ? Collections.emptyList() : Collections.singletonList(table));
    }
    
    public TablesContext(final Collection<SimpleTableSegment> tables) {
        this(tables, Collections.emptyMap());
    }
    
    public TablesContext(final Collection<? extends TableSegment> tables, final Map<Integer, SelectStatementContext> subqueryContexts) {
        if (tables.isEmpty()) {
            return;
        }
        this.tables.addAll(tables);
        for (TableSegment each : tables) {
            if (each instanceof SimpleTableSegment) {
                SimpleTableSegment simpleTableSegment = (SimpleTableSegment) each;
                TableNameSegment tableName = simpleTableSegment.getTableName();
                if (!"DUAL".equalsIgnoreCase(tableName.getIdentifier().getValue())) {
                    simpleTables.add(simpleTableSegment);
                    tableNames.add(tableName.getIdentifier().getValue());
                    // TODO support bind with all statement contains table segement @duanzhengqiang
                    tableName.getTableBoundInfo().ifPresent(optional -> schemaNames.add(optional.getOriginalSchema().getValue()));
                    tableName.getTableBoundInfo().ifPresent(optional -> databaseNames.add(optional.getOriginalDatabase().getValue()));
                }
            }
            if (each instanceof SubqueryTableSegment) {
                subqueryTables.putAll(createSubqueryTables(subqueryContexts, (SubqueryTableSegment) each));
            }
        }
    }
    
    private Map<String, Collection<SubqueryTableContext>> createSubqueryTables(final Map<Integer, SelectStatementContext> subqueryContexts, final SubqueryTableSegment subqueryTable) {
        if (!subqueryContexts.containsKey(subqueryTable.getSubquery().getStartIndex())) {
            return Collections.emptyMap();
        }
        SelectStatementContext subqueryContext = subqueryContexts.get(subqueryTable.getSubquery().getStartIndex());
        Map<String, SubqueryTableContext> subqueryTableContexts = new SubqueryTableContextEngine().createSubqueryTableContexts(subqueryContext, subqueryTable.getAliasName().orElse(null));
        Map<String, Collection<SubqueryTableContext>> result = new HashMap<>(subqueryTableContexts.size(), 1F);
        for (SubqueryTableContext each : subqueryTableContexts.values()) {
            if (null != each.getAliasName()) {
                result.computeIfAbsent(each.getAliasName(), unused -> new LinkedList<>()).add(each);
            }
        }
        return result;
    }
    
    /**
     * Find expression table name map.
     *
     * @param columns column segments
     * @param schema schema
     * @return expression table name map
     */
    @HighFrequencyInvocation
    public Map<String, String> findTableNames(final Collection<ColumnSegment> columns, final ShardingSphereSchema schema) {
        if (1 == simpleTables.size()) {
            return findTableNameFromSingleTable(columns);
        }
        Map<String, String> result = new CaseInsensitiveMap<>();
        Map<String, Collection<String>> ownerColumnNames = getOwnerColumnNames(columns);
        result.putAll(findTableNameFromSQL(ownerColumnNames));
        Collection<String> noOwnerColumnNames = getNoOwnerColumnNames(columns);
        result.putAll(findTableNameFromMetaData(noOwnerColumnNames, schema));
        result.putAll(findTableNameFromSubquery(columns, result));
        return result;
    }
    
    @HighFrequencyInvocation
    private Map<String, String> findTableNameFromSingleTable(final Collection<ColumnSegment> columns) {
        String tableName = simpleTables.iterator().next().getTableName().getIdentifier().getValue();
        Map<String, String> result = new CaseInsensitiveMap<>();
        for (ColumnSegment each : columns) {
            result.putIfAbsent(each.getExpression(), tableName);
        }
        return result;
    }
    
    @HighFrequencyInvocation
    private Map<String, Collection<String>> getOwnerColumnNames(final Collection<ColumnSegment> columns) {
        Map<String, Collection<String>> result = new CaseInsensitiveMap<>();
        for (ColumnSegment each : columns) {
            if (!each.getOwner().isPresent()) {
                continue;
            }
            result.computeIfAbsent(each.getOwner().get().getIdentifier().getValue(), unused -> new LinkedList<>()).add(each.getExpression());
        }
        return result;
    }
    
    @HighFrequencyInvocation
    private Map<String, String> findTableNameFromSQL(final Map<String, Collection<String>> ownerColumnNames) {
        if (ownerColumnNames.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new LinkedHashMap<>(simpleTables.size(), 1F);
        for (SimpleTableSegment each : simpleTables) {
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
    
    @HighFrequencyInvocation
    private Map<String, String> findTableNameFromMetaData(final Collection<String> noOwnerColumnNames, final ShardingSphereSchema schema) {
        if (noOwnerColumnNames.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new LinkedHashMap<>(noOwnerColumnNames.size(), 1F);
        for (SimpleTableSegment each : simpleTables) {
            String tableName = each.getTableName().getIdentifier().getValue();
            if (schema.containsTable(tableName)) {
                filterColumnNames(noOwnerColumnNames, schema.getTable(tableName)).forEach(columnName -> result.put(columnName, tableName));
            }
        }
        return result;
    }
    
    @HighFrequencyInvocation
    private Collection<String> filterColumnNames(final Collection<String> noOwnerColumnNames, final ShardingSphereTable table) {
        Collection<String> result = new LinkedList<>();
        for (String each : noOwnerColumnNames) {
            if (table.containsColumn(each)) {
                result.add(each);
            }
        }
        return result;
    }
    
    @HighFrequencyInvocation
    private Collection<String> getNoOwnerColumnNames(final Collection<ColumnSegment> columns) {
        Collection<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (ColumnSegment each : columns) {
            if (!each.getOwner().isPresent()) {
                result.add(each.getIdentifier().getValue());
            }
        }
        return result;
    }
    
    @HighFrequencyInvocation
    private Map<String, String> findTableNameFromSubquery(final Collection<ColumnSegment> columns, final Map<String, String> ownerTableNames) {
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
