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
        CreateTableStatement createTableStatement = (CreateTableStatement) sqlParserEngine.parse(createTableSQL, false);
        List<ColumnDefinitionSegment> columnDefinitions = new ArrayList<>(createTableStatement.getColumnDefinitions());
        List<ConstraintDefinitionSegment> constraintDefinitions = new ArrayList<>(createTableStatement.getConstraintDefinitions());
        constraintDefinitions.sort((a, b) -> Integer.compare(a.getStartIndex(), b.getStartIndex()));
        StringBuilder result = new StringBuilder(createTableSQL.substring(0, columnDefinitions.get(0).getStartIndex()));
        Collection<String> existedVisibleColumnsLowerCase = collectExistedVisibleColumnsLowerCase(columnDefinitions, encryptTable.get());
        List<String> definitions = new ArrayList<>();
        for (ColumnDefinitionSegment each : columnDefinitions) {
            processColumnDefinition(each, encryptTable.get(), createTableSQL, definitions, existedVisibleColumnsLowerCase);
        }
        for (ConstraintDefinitionSegment each : constraintDefinitions) {
            findLogicConstraintDefinition(each, encryptTable.get(), createTableSQL).ifPresent(definitions::add);
        }
        for (int i = 0; i < definitions.size(); i++) {
            if (i > 0) {
                result.append(COMMA);
            }
            result.append(definitions.get(i));
        }
        int lastStopIndex = getLastStopIndex(columnDefinitions, constraintDefinitions);
        result.append(createTableSQL.substring(lastStopIndex + 1));
        return result.toString();
    }

    private Optional<String> findLogicColumnDefinition(final ColumnDefinitionSegment columnDefinition, final EncryptTable encryptTable, final String createTableSQL) {
        ColumnSegment columnSegment = columnDefinition.getColumnName();
        String columnName = columnSegment.getIdentifier().getValue();
        if (encryptTable.isCipherColumn(columnName)) {
            String logicColumn = encryptTable.getLogicColumnByCipherColumn(columnName);
            return Optional.of(createTableSQL.substring(columnDefinition.getStartIndex(), columnSegment.getStartIndex())
                    + columnSegment.getIdentifier().getQuoteCharacter().wrap(logicColumn) + createTableSQL.substring(columnSegment.getStopIndex() + 1, columnDefinition.getStopIndex() + 1));
        }
        if (encryptTable.isDerivedColumn(columnName)) {
            return Optional.empty();
        }
        return Optional.of(createTableSQL.substring(columnDefinition.getStartIndex(), columnDefinition.getStopIndex() + 1));
    }

    private void processColumnDefinition(final ColumnDefinitionSegment columnDefinition, final EncryptTable encryptTable, final String createTableSQL, final List<String> definitions, final Collection<String> existedVisibleColumnsLowerCase) {
        String columnName = columnDefinition.getColumnName().getIdentifier().getValue();
        if (shouldSkipColumn(encryptTable, columnName)) {
            return;
        }
        String visibleColumnName = getVisibleColumnName(encryptTable, columnName);
        if (encryptTable.isCipherColumn(columnName)
                && isColumnAlreadyAdded(existedVisibleColumnsLowerCase, visibleColumnName)) {
            return;
        }
        String columnDefinitionStr = generateColumnDefinition(columnDefinition, encryptTable, columnName, createTableSQL);
        definitions.add(columnDefinitionStr);
        existedVisibleColumnsLowerCase.add(toLowerCase(visibleColumnName));
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
        if (encryptTable.isCipherColumn(columnName)) {
            return findLogicColumnDefinition(columnDefinition, encryptTable, createTableSQL).orElse("");
        }
        return createTableSQL.substring(columnDefinition.getStartIndex(), columnDefinition.getStopIndex() + 1);
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
        Collection<ColumnSegment> columns = new ArrayList<>(constraint.getIndexColumns());
        columns.addAll(constraint.getPrimaryKeyColumns());
        boolean containsDerivedColumn = columns.stream()
                .anyMatch(each -> encryptTable.isDerivedColumn(each.getIdentifier().getValue()));
        if (containsDerivedColumn) {
            return Optional.empty();
        }
        boolean containsCipherColumn = columns.stream()
                .anyMatch(each -> encryptTable.isCipherColumn(each.getIdentifier().getValue()));
        if (!containsCipherColumn) {
            return Optional.of(createTableSQL.substring(constraint.getStartIndex(), constraint.getStopIndex() + 1));
        }
        List<ColumnSegment> sortedColumns = new ArrayList<>(columns);
        sortedColumns.sort((a, b) -> Integer.compare(a.getStartIndex(), b.getStartIndex()));
        StringBuilder rewritten = new StringBuilder();
        int cursor = constraint.getStartIndex();
        for (ColumnSegment each : sortedColumns) {
            rewritten.append(createTableSQL.substring(cursor, each.getStartIndex()));
            String columnName = each.getIdentifier().getValue();
            Optional<String> logicColumn = findLogicColumnByActualColumn(encryptTable, columnName);
            rewritten.append(logicColumn.map(opt -> each.getIdentifier().getQuoteCharacter().wrap(opt))
                    .orElseGet(() -> createTableSQL.substring(each.getStartIndex(), each.getStopIndex() + 1)));
            cursor = each.getStopIndex() + 1;
        }
        rewritten.append(createTableSQL.substring(cursor, constraint.getStopIndex() + 1));
        return Optional.of(rewritten.toString());
    }

    private Optional<String> findLogicColumnByActualColumn(final EncryptTable encryptTable, final String actualColumnName) {
        if (encryptTable.isCipherColumn(actualColumnName)) {
            return Optional.of(encryptTable.getLogicColumnByCipherColumn(actualColumnName));
        }
        return Optional.empty();
    }
}
