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

package org.apache.shardingsphere.infra.binder.engine.statement.ddl;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.shardingsphere.infra.binder.engine.segment.ddl.column.ColumnDefinitionSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.type.SimpleTableSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementCopyUtils;
import org.apache.shardingsphere.infra.binder.engine.statement.dml.SelectStatementBinder;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Create table statement binder.
 */
public final class CreateTableStatementBinder implements SQLStatementBinder<CreateTableStatement> {
    
    @Override
    public CreateTableStatement bind(final CreateTableStatement sqlStatement, final SQLStatementBinderContext binderContext) {
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        SelectStatement boundSelectStatement = bindSelectStatement(sqlStatement, binderContext);
        SQLStatementBinderContext createTableBinderContext = createCreateTableBinderContext(sqlStatement, binderContext, boundSelectStatement);
        SimpleTableSegment boundTable = SimpleTableSegmentBinder.bind(sqlStatement.getTable(), createTableBinderContext, tableBinderContexts);
        Collection<ColumnDefinitionSegment> boundColumnDefinitions = sqlStatement.getColumnDefinitions().stream()
                .map(each -> ColumnDefinitionSegmentBinder.bind(each, createTableBinderContext, tableBinderContexts)).collect(Collectors.toList());
        return copy(sqlStatement, boundTable, boundSelectStatement, boundColumnDefinitions);
    }
    
    private SelectStatement bindSelectStatement(final CreateTableStatement sqlStatement, final SQLStatementBinderContext binderContext) {
        return sqlStatement.getSelectStatement().map(optional -> new SelectStatementBinder().bind(optional,
                new SQLStatementBinderContext(binderContext.getMetaData(), binderContext.getCurrentDatabaseName(), binderContext.getHintValueContext(), optional,
                        binderContext.getCurrentSchema())))
                .orElse(null);
    }
    
    private SQLStatementBinderContext createCreateTableBinderContext(final CreateTableStatement sqlStatement, final SQLStatementBinderContext binderContext,
                                                                     final SelectStatement boundSelectStatement) {
        Collection<IdentifierValue> schemaSearchPath = getCreateTableSchemaSearchPath(sqlStatement, binderContext, boundSelectStatement);
        return schemaSearchPath.isEmpty() ? binderContext
                : new SQLStatementBinderContext(binderContext.getMetaData(), binderContext.getCurrentDatabaseName(), binderContext.getHintValueContext(), sqlStatement, schemaSearchPath);
    }
    
    private Collection<IdentifierValue> getCreateTableSchemaSearchPath(final CreateTableStatement sqlStatement, final SQLStatementBinderContext binderContext,
                                                                       final SelectStatement boundSelectStatement) {
        if (!binderContext.getCurrentSchema().isEmpty() || null == boundSelectStatement || sqlStatement.getTable().getOwner().isPresent()) {
            return Collections.emptyList();
        }
        if (containsOwner(sqlStatement.getSelectStatement().get())) {
            return Collections.emptyList();
        }
        Collection<IdentifierValue> result = new LinkedList<>();
        TableExtractor tableExtractor = new TableExtractor();
        tableExtractor.extractTablesFromSelect(boundSelectStatement);
        for (SimpleTableSegment each : tableExtractor.getRewriteTables()) {
            appendSchemaSearchPath(result, each, binderContext);
            if (result.size() > 1) {
                return Collections.emptyList();
            }
        }
        return result;
    }
    
    private boolean containsOwner(final SelectStatement selectStatement) {
        TableExtractor tableExtractor = new TableExtractor();
        tableExtractor.extractTablesFromSelect(selectStatement);
        for (SimpleTableSegment each : tableExtractor.getRewriteTables()) {
            if (each.getOwner().isPresent()) {
                return true;
            }
        }
        return false;
    }
    
    private void appendSchemaSearchPath(final Collection<IdentifierValue> schemaSearchPath, final SimpleTableSegment tableSegment, final SQLStatementBinderContext binderContext) {
        if (tableSegment.getOwner().isPresent()) {
            return;
        }
        if (!tableSegment.getTableName().getTableBoundInfo().isPresent()) {
            return;
        }
        if (!isCurrentDatabase(tableSegment.getTableName().getTableBoundInfo().get().getOriginalDatabase(), binderContext.getCurrentDatabaseName())) {
            return;
        }
        IdentifierValue schemaName = tableSegment.getTableName().getTableBoundInfo().get().getOriginalSchema();
        if (schemaName.getValue().isEmpty() || containsSchema(schemaSearchPath, schemaName)
                || !binderContext.getMetaData().getDatabase(binderContext.getCurrentDatabaseName()).containsSchema(schemaName)) {
            return;
        }
        schemaSearchPath.add(schemaName);
    }
    
    private boolean isCurrentDatabase(final IdentifierValue databaseName, final String currentDatabaseName) {
        return databaseName.getValue().isEmpty() || databaseName.getValue().equalsIgnoreCase(currentDatabaseName);
    }
    
    private boolean containsSchema(final Collection<IdentifierValue> schemaSearchPath, final IdentifierValue schemaName) {
        for (IdentifierValue each : schemaSearchPath) {
            if (each.getValue().equalsIgnoreCase(schemaName.getValue())) {
                return true;
            }
        }
        return false;
    }
    
    private CreateTableStatement copy(final CreateTableStatement sqlStatement,
                                      final SimpleTableSegment boundTable, final SelectStatement boundSelectStatement, final Collection<ColumnDefinitionSegment> boundColumnDefinitions) {
        CreateTableStatement result = CreateTableStatement.builder()
                .databaseType(sqlStatement.getDatabaseType())
                .table(boundTable)
                .selectStatement(boundSelectStatement)
                .ifNotExists(sqlStatement.isIfNotExists())
                .temporary(sqlStatement.isTemporary())
                .likeTable(sqlStatement.getLikeTable().orElse(null))
                .createTableOption(sqlStatement.getCreateTableOption().orElse(null))
                .columnDefinitions(boundColumnDefinitions)
                .constraintDefinitions(sqlStatement.getConstraintDefinitions())
                .columns(sqlStatement.getColumns())
                .rollups(sqlStatement.getRollups())
                .build();
        SQLStatementCopyUtils.copyAttributes(sqlStatement, result);
        return result;
    }
}
