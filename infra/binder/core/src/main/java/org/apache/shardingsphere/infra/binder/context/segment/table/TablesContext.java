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
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.infra.binder.context.segment.select.subquery.SubqueryTableContext;
import org.apache.shardingsphere.infra.binder.context.segment.select.subquery.engine.SubqueryTableContextEngine;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/**
 * Tables context.
 */
@Getter
@ToString
public final class TablesContext {
    
    private final Collection<SimpleTableSegment> simpleTables = new LinkedList<>();
    
    private final Collection<String> tableNames = new CaseInsensitiveSet<>();
    
    private final Collection<String> schemaNames = new CaseInsensitiveSet<>();
    
    private final Collection<String> databaseNames = new CaseInsensitiveSet<>();
    
    @Getter(AccessLevel.NONE)
    private final Map<String, Collection<SubqueryTableContext>> subqueryTables = new CaseInsensitiveMap<>();
    
    @Getter(AccessLevel.NONE)
    private final Collection<? extends TableSegment> tables;
    
    public TablesContext(final SimpleTableSegment table) {
        this(null == table ? Collections.emptyList() : Collections.singletonList(table));
    }
    
    public TablesContext(final Collection<SimpleTableSegment> tables) {
        this(tables, Collections.emptyMap());
    }
    
    public TablesContext(final Collection<? extends TableSegment> tables, final Map<Integer, SelectStatementContext> subqueryContexts) {
        this.tables = tables;
        for (TableSegment each : tables) {
            if (each instanceof SubqueryTableSegment) {
                subqueryTables.putAll(createSubqueryTables(subqueryContexts, (SubqueryTableSegment) each));
            }
            if (!(each instanceof SimpleTableSegment)) {
                continue;
            }
            SimpleTableSegment simpleTable = (SimpleTableSegment) each;
            TableNameSegment tableName = simpleTable.getTableName();
            if ("DUAL".equalsIgnoreCase(tableName.getIdentifier().getValue())) {
                continue;
            }
            handleSimpleTable(simpleTable, tableName);
        }
    }
    
    private void handleSimpleTable(final SimpleTableSegment simpleTable, final TableNameSegment tableName) {
        simpleTables.add(simpleTable);
        tableNames.add(tableName.getIdentifier().getValue());
        // TODO support bind with all statement contains table segment @duanzhengqiang
        tableName.getTableBoundInfo().filter(optional -> !Strings.isNullOrEmpty(optional.getOriginalSchema().getValue()))
                .ifPresent(optional -> schemaNames.add(optional.getOriginalSchema().getValue()));
        tableName.getTableBoundInfo().filter(optional -> !Strings.isNullOrEmpty(optional.getOriginalDatabase().getValue()))
                .ifPresent(optional -> databaseNames.add(optional.getOriginalDatabase().getValue()));
    }
    
    private Map<String, Collection<SubqueryTableContext>> createSubqueryTables(final Map<Integer, SelectStatementContext> subqueryContexts, final SubqueryTableSegment subqueryTable) {
        if (!subqueryContexts.containsKey(subqueryTable.getSubquery().getStartIndex())) {
            return Collections.emptyMap();
        }
        SelectStatementContext subqueryContext = subqueryContexts.get(subqueryTable.getSubquery().getStartIndex());
        Map<String, SubqueryTableContext> subqueryTableContexts = new SubqueryTableContextEngine().createSubqueryTableContexts(subqueryContext, subqueryTable.getAliasName().orElse(null));
        Map<String, Collection<SubqueryTableContext>> result = new CaseInsensitiveMap<>(subqueryTableContexts.size(), 1F);
        for (SubqueryTableContext each : subqueryTableContexts.values()) {
            if (null != each.getAliasName()) {
                result.computeIfAbsent(each.getAliasName(), unused -> new LinkedList<>()).add(each);
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
