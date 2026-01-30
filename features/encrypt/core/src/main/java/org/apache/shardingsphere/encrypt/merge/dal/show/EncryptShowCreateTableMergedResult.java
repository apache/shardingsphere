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

package org.apache.shardingsphere.encrypt.merge.dal.show;

import org.apache.shardingsphere.encrypt.exception.syntax.UnsupportedEncryptSQLException;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.decorator.DecoratorMergedResult;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.parser.SQLParserEngine;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.ConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.TableInResultSetSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Encrypt show create table merged result.
 */
public final class EncryptShowCreateTableMergedResult extends DecoratorMergedResult {
    
    private static final String COMMA = ", ";
    
    private final EncryptRule rule;
    
    private final String tableName;
    
    private final int tableNameResultSetIndex;
    
    private final SQLParserEngine sqlParserEngine;
    
    public EncryptShowCreateTableMergedResult(final RuleMetaData globalRuleMetaData, final MergedResult mergedResult, final SQLStatementContext sqlStatementContext, final EncryptRule rule) {
        super(mergedResult);
        ShardingSpherePreconditions.checkState(1 == sqlStatementContext.getTablesContext().getSimpleTables().size(),
                () -> new UnsupportedEncryptSQLException("SHOW CREATE TABLE FOR MULTI TABLES"));
        this.rule = rule;
        tableName = sqlStatementContext.getTablesContext().getSimpleTables().iterator().next().getTableName().getIdentifier().getValue();
        TableInResultSetSQLStatementAttribute attribute = sqlStatementContext.getSqlStatement().getAttributes().getAttribute(TableInResultSetSQLStatementAttribute.class);
        tableNameResultSetIndex = attribute.getNameResultSetIndex();
        sqlParserEngine = globalRuleMetaData.getSingleRule(SQLParserRule.class).getSQLParserEngine(sqlStatementContext.getSqlStatement().getDatabaseType());
    }
    
    @Override
    public Object getValue(final int columnIndex, final Class<?> type) throws SQLException {
        if (tableNameResultSetIndex != columnIndex) {
            return getMergedResult().getValue(columnIndex, type);
        }
        String createTableSQL = getMergedResult().getValue(tableNameResultSetIndex, type).toString();
        Optional<EncryptTable> encryptTable = rule.findEncryptTable(tableName);
        if (!encryptTable.isPresent() || !createTableSQL.contains("(")) {
            return createTableSQL;
        }
        return rewriteCreateTableSQL(createTableSQL, encryptTable.get());
    }
    
    private String rewriteCreateTableSQL(final String createTableSQL, final EncryptTable encryptTable) {
        CreateTableStatement createTableStatement = (CreateTableStatement) sqlParserEngine.parse(createTableSQL, false);
        List<ColumnDefinitionSegment> columnDefinitions = new ArrayList<>(createTableStatement.getColumnDefinitions());
        List<ConstraintDefinitionSegment> constraintDefinitions = new ArrayList<>(createTableStatement.getConstraintDefinitions());
        constraintDefinitions.sort((a, b) -> Integer.compare(a.getStartIndex(), b.getStartIndex()));
        StringBuilder result = new StringBuilder(createTableSQL.substring(0, columnDefinitions.get(0).getStartIndex()));
        Collection<String> existedVisibleColumnsLowerCase = collectExistedVisibleColumnsLowerCase(columnDefinitions, encryptTable);
        List<String> definitions = processColumnDefinitions(columnDefinitions, encryptTable, createTableSQL, existedVisibleColumnsLowerCase);
        processConstraintDefinitions(constraintDefinitions, encryptTable, createTableSQL, definitions);
        appendDefinitions(result, definitions);
        int lastStopIndex = getLastStopIndex(columnDefinitions, constraintDefinitions);
        result.append(createTableSQL.substring(lastStopIndex + 1));
        return result.toString();
    }
    
    private List<String> processColumnDefinitions(final List<ColumnDefinitionSegment> columnDefinitions, final EncryptTable encryptTable,
                                                  final String createTableSQL, final Collection<String> existedVisibleColumnsLowerCase) {
        List<String> definitions = new ArrayList<>();
        for (ColumnDefinitionSegment each : columnDefinitions) {
            processColumnDefinition(each, encryptTable, createTableSQL, definitions, existedVisibleColumnsLowerCase);
        }
        return definitions;
    }
    
    private void processConstraintDefinitions(final List<ConstraintDefinitionSegment> constraintDefinitions, final EncryptTable encryptTable,
                                              final String createTableSQL, final List<String> definitions) {
        for (ConstraintDefinitionSegment each : constraintDefinitions) {
            findLogicConstraintDefinition(each, encryptTable, createTableSQL).ifPresent(definitions::add);
        }
    }
    
    private void appendDefinitions(final StringBuilder result, final List<String> definitions) {
        for (int i = 0; i < definitions.size(); i++) {
            if (i > 0) {
                result.append(COMMA);
            }
            result.append(definitions.get(i));
        }
    }
    
    private Optional<String> findLogicColumnDefinition(final ColumnDefinitionSegment columnDefinition, final EncryptTable encryptTable, final String createTableSQL) {
        ColumnSegment columnSegment = columnDefinition.getColumnName();
        String columnName = columnSegment.getIdentifier().getValue();
        if (encryptTable.isDerivedColumn(columnName)) {
            return Optional.empty();
        }
        if (encryptTable.isCipherColumn(columnName)) {
            return Optional.of(buildLogicColumnDefinitionString(columnDefinition, columnSegment, encryptTable, createTableSQL));
        }
        return Optional.of(extractOriginalColumnDefinition(columnDefinition, createTableSQL));
    }
    
    private String buildLogicColumnDefinitionString(final ColumnDefinitionSegment columnDefinition, final ColumnSegment columnSegment,
                                                    final EncryptTable encryptTable, final String createTableSQL) {
        String logicColumn = encryptTable.getLogicColumnByCipherColumn(columnSegment.getIdentifier().getValue());
        String beforeColumnName = createTableSQL.substring(columnDefinition.getStartIndex(), columnSegment.getStartIndex());
        String wrappedLogicColumn = columnSegment.getIdentifier().getQuoteCharacter().wrap(logicColumn);
        String afterColumnName = createTableSQL.substring(columnSegment.getStopIndex() + 1, columnDefinition.getStopIndex() + 1);
        return beforeColumnName + wrappedLogicColumn + afterColumnName;
    }
    
    private String extractOriginalColumnDefinition(final ColumnDefinitionSegment columnDefinition, final String createTableSQL) {
        return createTableSQL.substring(columnDefinition.getStartIndex(), columnDefinition.getStopIndex() + 1);
    }
    
    private void processColumnDefinition(final ColumnDefinitionSegment columnDefinition,
                                         final EncryptTable encryptTable,
                                         final String createTableSQL,
                                         final List<String> definitions,
                                         final Collection<String> existedVisibleColumnsLowerCase) {
        String columnName = columnDefinition.getColumnName().getIdentifier().getValue();
        if (shouldSkipColumn(encryptTable, columnName)) {
            return;
        }
        String visibleColumnName = getVisibleColumnName(encryptTable, columnName);
        if (shouldSkipDuplicateCipherColumn(encryptTable, columnName, existedVisibleColumnsLowerCase, visibleColumnName)) {
            return;
        }
        String columnDefinitionStr = generateColumnDefinition(columnDefinition, encryptTable, columnName, createTableSQL);
        definitions.add(columnDefinitionStr);
        existedVisibleColumnsLowerCase.add(toLowerCase(visibleColumnName));
    }
    
    private boolean shouldSkipDuplicateCipherColumn(final EncryptTable encryptTable, final String columnName,
                                                    final Collection<String> existedVisibleColumnsLowerCase, final String visibleColumnName) {
        return encryptTable.isCipherColumn(columnName) && isColumnAlreadyAdded(existedVisibleColumnsLowerCase, visibleColumnName);
    }
    
    private boolean shouldSkipColumn(final EncryptTable encryptTable, final String columnName) {
        return encryptTable.isDerivedColumn(columnName);
    }
    
    private String getVisibleColumnName(final EncryptTable encryptTable, final String columnName) {
        if (encryptTable.isCipherColumn(columnName)) {
            return encryptTable.getLogicColumnByCipherColumn(columnName);
        }
        return columnName;
    }
    
    private boolean isColumnAlreadyAdded(
                                         final Collection<String> existedVisibleColumnsLowerCase,
                                         final String columnName) {
        return existedVisibleColumnsLowerCase.contains(toLowerCase(columnName));
    }
    
    private String generateColumnDefinition(final ColumnDefinitionSegment columnDefinition, final EncryptTable encryptTable, final String columnName, final String createTableSQL) {
        return findLogicColumnDefinition(columnDefinition, encryptTable, createTableSQL)
                .orElseGet(() -> extractOriginalColumnDefinition(columnDefinition, createTableSQL));
    }
    
    private Collection<String> collectExistedVisibleColumnsLowerCase(final Collection<ColumnDefinitionSegment> columnDefinitions, final EncryptTable encryptTable) {
        Collection<String> result = new HashSet<>();
        for (ColumnDefinitionSegment each : columnDefinitions) {
            String columnName = each.getColumnName().getIdentifier().getValue();
            if (encryptTable.isDerivedColumn(columnName) || encryptTable.isCipherColumn(columnName)) {
                continue;
            }
            result.add(toLowerCase(columnName));
        }
        return result;
    }
    
    private int getLastStopIndex(final List<ColumnDefinitionSegment> columnDefinitions, final List<ConstraintDefinitionSegment> constraintDefinitions) {
        int last = columnDefinitions.get(columnDefinitions.size() - 1).getStopIndex();
        for (ConstraintDefinitionSegment each : constraintDefinitions) {
            last = Math.max(last, each.getStopIndex());
        }
        return last;
    }
    
    private String toLowerCase(final String value) {
        return null == value ? null : value.toLowerCase(Locale.ENGLISH);
    }
    
    private Optional<String> findLogicConstraintDefinition(final ConstraintDefinitionSegment constraint, final EncryptTable encryptTable, final String createTableSQL) {
        Collection<ColumnSegment> columns = collectConstraintColumns(constraint);
        if (columns.isEmpty()) {
            return Optional.of(extractOriginalConstraintDefinition(constraint, createTableSQL));
        }
        if (containsDerivedColumn(columns, encryptTable)) {
            return Optional.empty();
        }
        if (!containsCipherColumn(columns, encryptTable)) {
            return Optional.of(extractOriginalConstraintDefinition(constraint, createTableSQL));
        }
        return Optional.of(rewriteConstraintWithLogicColumns(constraint, columns, encryptTable, createTableSQL));
    }
    
    private Collection<ColumnSegment> collectConstraintColumns(final ConstraintDefinitionSegment constraint) {
        Collection<ColumnSegment> columns = new ArrayList<>(constraint.getIndexColumns());
        columns.addAll(constraint.getPrimaryKeyColumns());
        return columns;
    }
    
    private boolean containsDerivedColumn(final Collection<ColumnSegment> columns, final EncryptTable encryptTable) {
        return columns.stream().anyMatch(each -> encryptTable.isDerivedColumn(each.getIdentifier().getValue()));
    }
    
    private boolean containsCipherColumn(final Collection<ColumnSegment> columns, final EncryptTable encryptTable) {
        return columns.stream().anyMatch(each -> encryptTable.isCipherColumn(each.getIdentifier().getValue()));
    }
    
    private String extractOriginalConstraintDefinition(final ConstraintDefinitionSegment constraint, final String createTableSQL) {
        return createTableSQL.substring(constraint.getStartIndex(), constraint.getStopIndex() + 1);
    }
    
    private String rewriteConstraintWithLogicColumns(final ConstraintDefinitionSegment constraint, final Collection<ColumnSegment> columns,
                                                     final EncryptTable encryptTable, final String createTableSQL) {
        List<ColumnSegment> sortedColumns = sortColumnsByStartIndex(columns);
        StringBuilder rewritten = new StringBuilder();
        int cursor = constraint.getStartIndex();
        for (ColumnSegment each : sortedColumns) {
            rewritten.append(createTableSQL.substring(cursor, each.getStartIndex()));
            rewritten.append(buildColumnSegmentWithLogicName(each, encryptTable, createTableSQL));
            cursor = each.getStopIndex() + 1;
        }
        rewritten.append(createTableSQL.substring(cursor, constraint.getStopIndex() + 1));
        return rewritten.toString();
    }
    
    private List<ColumnSegment> sortColumnsByStartIndex(final Collection<ColumnSegment> columns) {
        List<ColumnSegment> sortedColumns = new ArrayList<>(columns);
        sortedColumns.sort((a, b) -> Integer.compare(a.getStartIndex(), b.getStartIndex()));
        return sortedColumns;
    }
    
    private String buildColumnSegmentWithLogicName(final ColumnSegment columnSegment, final EncryptTable encryptTable, final String createTableSQL) {
        String columnName = columnSegment.getIdentifier().getValue();
        Optional<String> logicColumn = findLogicColumnByActualColumn(encryptTable, columnName);
        return logicColumn.map(opt -> columnSegment.getIdentifier().getQuoteCharacter().wrap(opt))
                .orElseGet(() -> createTableSQL.substring(columnSegment.getStartIndex(), columnSegment.getStopIndex() + 1));
    }
    
    private Optional<String> findLogicColumnByActualColumn(final EncryptTable encryptTable, final String actualColumnName) {
        return encryptTable.isCipherColumn(actualColumnName)
                ? Optional.of(encryptTable.getLogicColumnByCipherColumn(actualColumnName))
                : Optional.empty();
    }
}
