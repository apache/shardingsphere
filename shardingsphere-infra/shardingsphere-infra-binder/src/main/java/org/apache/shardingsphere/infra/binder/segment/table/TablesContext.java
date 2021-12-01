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
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
    
    private final Collection<SimpleTableSegment> tables = new LinkedList<>();
    
    private final Collection<String> tableNames = new HashSet<>();
    
    private final Collection<String> schemaNames = new HashSet<>();
    
    private final Map<String, Collection<SubqueryTableContext>> subqueryTables = new HashMap<>();
    
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
        for (TableSegment each : tableSegments) {
            if (!(each instanceof SimpleTableSegment)) {
                continue;
            }
            SimpleTableSegment simpleTableSegment = (SimpleTableSegment) each;
            tables.add(simpleTableSegment);
            tableNames.add(simpleTableSegment.getTableName().getIdentifier().getValue());
            simpleTableSegment.getOwner().ifPresent(owner -> schemaNames.add(owner.getIdentifier().getValue()));
        }
        for (TableSegment each : tableSegments) {
            if (!(each instanceof SubqueryTableSegment)) {
                continue;
            }
            SubqueryTableSegment subqueryTableSegment = (SubqueryTableSegment) each;
            SelectStatementContext subqueryContext = subqueryContexts.get(subqueryTableSegment.getSubquery().getStartIndex());
            Collection<SubqueryTableContext> subqueryTableContexts = new SubqueryTableContextEngine().createSubqueryTableContexts(subqueryContext, each.getAlias().orElse(null));
            Map<String, List<SubqueryTableContext>> result = new HashMap<>();
            for (SubqueryTableContext subQuery : subqueryTableContexts) {
                if (null != subQuery.getAlias()) {
                    result.computeIfAbsent(subQuery.getAlias(), unused -> new LinkedList<>()).add(subQuery);
                }
            }
            subqueryTables.putAll(result);
        }
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
     * Find table name.
     *
     * @param columns column projection collection
     * @param schema schema meta data
     * @return table name map
     */
    public Map<String, String> findTableName(final Collection<ColumnProjection> columns, final ShardingSphereSchema schema) {
        if (1 == tables.size()) {
            String tableName = tables.iterator().next().getTableName().getIdentifier().getValue();
            Map<String, String> result = new LinkedHashMap<>(columns.size(), 1);
            for (ColumnProjection each : columns) {
                result.putIfAbsent(each.getExpression(), tableName);
            }
            return result;
        }
        Map<String, String> result = new HashMap<>(columns.size(), 1);
        result.putAll(findTableNameFromSQL(getOwnerColumnNames(columns)));
        Collection<String> columnNames = new LinkedHashSet<>();
        for (ColumnProjection each : columns) {
            if (null == each.getOwner()) {
                columnNames.add(each.getName());
            }
        }
        result.putAll(findTableNameFromMetaData(columnNames, schema));
        if (result.size() < columns.size() && !subqueryTables.isEmpty()) {
            appendRemainingResult(columns, result);
        }
        return result;
    }
    
    private void appendRemainingResult(final Collection<ColumnProjection> columns, final Map<String, String> result) {
        Collection<ColumnProjection> remainingColumns = columns.stream().filter(each -> !result.containsKey(each.getExpression())).collect(Collectors.toList());
        for (ColumnProjection each : remainingColumns) {
            findTableNameFromSubquery(each.getName(), each.getOwner()).ifPresent(optional -> result.put(each.getExpression(), optional));
        }
    }
    
    private Map<String, Collection<String>> getOwnerColumnNames(final Collection<ColumnProjection> columns) {
        Map<String, Collection<String>> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (ColumnProjection each : columns) {
            if (null == each.getOwner()) {
                continue;
            }
            Collection<String> columnExpressions = result.getOrDefault(each.getOwner(), new LinkedList<>());
            columnExpressions.add(each.getExpression());
            result.put(each.getOwner(), columnExpressions);
        }
        return result;
    }
    
    private Map<String, String> findTableNameFromSQL(final Map<String, Collection<String>> ownerColumnNames) {
        if (ownerColumnNames.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new HashMap<>();
        for (SimpleTableSegment each : tables) {
            String tableName = each.getTableName().getIdentifier().getValue();
            if (ownerColumnNames.containsKey(tableName)) {
                ownerColumnNames.get(tableName).forEach(column -> result.put(column, tableName));
            }
            Optional<String> alias = each.getAlias();
            if (alias.isPresent() && ownerColumnNames.containsKey(alias.get())) {
                ownerColumnNames.get(alias.get()).forEach(column -> result.put(column, tableName));
            }
        }
        return result;
    }
    
    private Map<String, String> findTableNameFromMetaData(final Collection<String> columnNames, final ShardingSphereSchema schema) {
        if (columnNames.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new HashMap<>();
        for (SimpleTableSegment each : tables) {
            String tableName = each.getTableName().getIdentifier().getValue();
            Collection<String> tableColumnNames = schema.getAllColumnNames(tableName);
            if (tableColumnNames.isEmpty()) {
                continue;
            }
            Collection<String> intersectColumnNames = tableColumnNames.stream().filter(columnNames::contains).collect(Collectors.toList());
            for (String columnName : intersectColumnNames) {
                result.put(columnName, tableName);
            }
        }
        return result;
    }
    
    private Optional<String> findTableNameFromSubquery(final String columnName, final String owner) {
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
        for (String each : schemaNames) {
            return Optional.of(each);
        }
        return Optional.empty();
    }
}
