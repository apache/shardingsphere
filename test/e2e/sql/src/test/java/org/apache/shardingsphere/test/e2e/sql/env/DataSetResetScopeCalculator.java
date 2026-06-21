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

package org.apache.shardingsphere.test.e2e.sql.env;

import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.sql.DialectSQLParsingException;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.expr.entry.InlineExpressionParserFactory;
import org.apache.shardingsphere.infra.parser.SQLParserEngine;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.engine.exception.SQLParsingException;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.CaseWhenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.CollateExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExistsSubqueryExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.KeyValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.NotExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.QuantifySubqueryExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.RowExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.TypeCastExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.UnaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ValuesExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.table.MultiTableConditionalIntoSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.table.MultiTableInsertIntoSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.DeleteMultiTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.MergeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.util.MultiSQLSplitter;
import org.apache.shardingsphere.test.e2e.sql.cases.casse.assertion.SQLE2ETestCaseAssertion;
import org.apache.shardingsphere.test.e2e.sql.cases.casse.assertion.SQLE2ETestCaseAssertionSQL;
import org.apache.shardingsphere.test.e2e.sql.cases.dataset.DataSetLoader;
import org.apache.shardingsphere.test.e2e.sql.cases.dataset.metadata.DataSetMetaData;
import org.apache.shardingsphere.test.e2e.sql.framework.param.model.AssertionTestParameter;
import org.apache.shardingsphere.test.e2e.sql.framework.param.model.CaseTestParameter;
import org.apache.shardingsphere.test.e2e.sql.framework.param.model.E2ETestParameter;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Calculator for data set reset table scope.
 */
public final class DataSetResetScopeCalculator {
    
    private static final CacheOption CACHE_OPTION = new CacheOption(128, 1024L);
    
    private static final Map<String, SQLParserEngine> SQL_PARSER_ENGINE_MAP = new ConcurrentHashMap<>();
    
    private static final Map<String, Collection<String>> RESET_TABLE_NAMES_CACHE = new ConcurrentHashMap<>();
    
    /**
     * Get reset table names.
     *
     * @param testParam test parameter
     * @return reset table names, or empty collection for full reset fallback
     */
    public Collection<String> getResetTableNames(final E2ETestParameter testParam) {
        return RESET_TABLE_NAMES_CACHE.computeIfAbsent(getResetTableNamesCacheKey(testParam), unused -> calculateResetTableNames(testParam));
    }
    
    private Collection<String> calculateResetTableNames(final E2ETestParameter testParam) {
        Collection<String> result = new LinkedHashSet<>();
        if (!addParsedTableNames(result, testParam.getTestCaseContext().getTestCase().getSql(), testParam.getDatabaseType())) {
            return Collections.emptyList();
        }
        for (SQLE2ETestCaseAssertion each : getAssertions(testParam)) {
            if (!addAssertionScope(result, testParam, each)) {
                return Collections.emptyList();
            }
        }
        return result;
    }
    
    private String getResetTableNamesCacheKey(final E2ETestParameter testParam) {
        StringBuilder result = new StringBuilder();
        appendCacheKeyValue(result, testParam.getKey());
        appendCacheKeyValue(result, testParam.getMode().name());
        appendCacheKeyValue(result, testParam instanceof AssertionTestParameter ? ((AssertionTestParameter) testParam).getSqlExecuteType().name() : "");
        appendCacheKeyValue(result, testParam.getTestCaseContext().getParentPath());
        appendCacheKeyValue(result, testParam.getTestCaseContext().getTestCase().getSql());
        for (SQLE2ETestCaseAssertion each : getAssertions(testParam)) {
            appendAssertionCacheKey(result, each);
        }
        return result.toString();
    }
    
    private void appendAssertionCacheKey(final StringBuilder cacheKey, final SQLE2ETestCaseAssertion assertion) {
        if (null == assertion) {
            appendCacheKeyValue(cacheKey, "");
            return;
        }
        appendCacheKeyValue(cacheKey, assertion.getExpectedDataFile());
        appendCacheKeyValue(cacheKey, assertion.getExpectedGeneratedKeyDataFile());
        appendCacheKeyValue(cacheKey, assertion.getParameters());
        appendSQLCacheKey(cacheKey, assertion.getInitialSQL());
        appendSQLCacheKey(cacheKey, assertion.getAssertionSQL());
    }
    
    private void appendSQLCacheKey(final StringBuilder cacheKey, final SQLE2ETestCaseAssertionSQL sql) {
        appendCacheKeyValue(cacheKey, null == sql ? "" : sql.getSql());
    }
    
    private void appendCacheKeyValue(final StringBuilder cacheKey, final String value) {
        String actualValue = null == value ? "" : value;
        cacheKey.append(actualValue.length()).append(':').append(actualValue);
    }
    
    private boolean addAssertionScope(final Collection<String> tableNames, final E2ETestParameter testParam, final SQLE2ETestCaseAssertion assertion) {
        if (null == assertion) {
            return true;
        }
        if (!addSQLTableNames(tableNames, assertion.getInitialSQL(), testParam.getDatabaseType()) || !addSQLTableNames(tableNames, assertion.getAssertionSQL(), testParam.getDatabaseType())) {
            return false;
        }
        return addDataSetTableNames(tableNames, testParam, assertion.getExpectedDataFile());
    }
    
    private Collection<SQLE2ETestCaseAssertion> getAssertions(final E2ETestParameter testParam) {
        if (testParam instanceof AssertionTestParameter) {
            return Collections.singleton(((AssertionTestParameter) testParam).getAssertion());
        }
        if (testParam instanceof CaseTestParameter) {
            return testParam.getTestCaseContext().getTestCase().getAssertions();
        }
        return Collections.emptyList();
    }
    
    private boolean addSQLTableNames(final Collection<String> tableNames, final SQLE2ETestCaseAssertionSQL sql, final DatabaseType databaseType) {
        return null == sql || null == sql.getSql() || addParsedTableNames(tableNames, sql.getSql(), databaseType);
    }
    
    private boolean addParsedTableNames(final Collection<String> tableNames, final String sql, final DatabaseType databaseType) {
        if (null == sql || sql.isEmpty()) {
            return true;
        }
        for (String each : MultiSQLSplitter.split(sql)) {
            if (!addParsedSingleSQLTableNames(tableNames, each, databaseType)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean addParsedSingleSQLTableNames(final Collection<String> tableNames, final String sql, final DatabaseType databaseType) {
        try {
            SQLStatement sqlStatement = getSQLParserEngine(databaseType).parse(sql, true);
            if (isUnextractedWithSQL(sql, sqlStatement)) {
                return false;
            }
            Collection<String> virtualTableNames = new LinkedHashSet<>();
            Collection<String> parsedTableNames = new LinkedHashSet<>();
            TableExtractor tableExtractor = new TableExtractor();
            tableExtractor.extractTablesFromSQLStatement(sqlStatement);
            addExtractedRewriteTableNames(parsedTableNames, tableExtractor);
            tableExtractor.getTableContext().forEach(each -> addVirtualTableNames(virtualTableNames, each));
            addSupplementalTableNames(parsedTableNames, virtualTableNames, sqlStatement);
            removeVirtualTableNames(parsedTableNames, virtualTableNames);
            tableNames.addAll(parsedTableNames);
            return true;
        } catch (final SQLParsingException | ParseCancellationException | DialectSQLParsingException ignored) {
            return isTransactionControlSQL(sql);
        }
    }
    
    private void addSupplementalTableNames(final Collection<String> tableNames, final Collection<String> virtualTableNames, final SQLStatement sqlStatement) {
        if (sqlStatement instanceof InsertStatement) {
            addInsertStatementTableNames(tableNames, virtualTableNames, (InsertStatement) sqlStatement);
        } else if (sqlStatement instanceof UpdateStatement) {
            addUpdateStatementTableNames(tableNames, virtualTableNames, (UpdateStatement) sqlStatement);
        } else if (sqlStatement instanceof DeleteStatement) {
            addDeleteStatementTableNames(tableNames, virtualTableNames, (DeleteStatement) sqlStatement);
        } else if (sqlStatement instanceof MergeStatement) {
            addMergeStatementTableNames(tableNames, virtualTableNames, (MergeStatement) sqlStatement);
        } else if (sqlStatement instanceof SelectStatement) {
            addSelectStatementTableNames(tableNames, virtualTableNames, (SelectStatement) sqlStatement);
        }
    }
    
    private void addSelectStatementTableNames(final Collection<String> tableNames, final Collection<String> virtualTableNames, final SelectStatement selectStatement) {
        selectStatement.getWith().ifPresent(optional -> addWithTableNames(tableNames, virtualTableNames, optional));
    }
    
    private void addInsertStatementTableNames(final Collection<String> tableNames, final Collection<String> virtualTableNames, final InsertStatement insertStatement) {
        insertStatement.getTable().ifPresent(optional -> addTableSegmentTableNames(tableNames, virtualTableNames, optional));
        insertStatement.getInsertSelect().ifPresent(optional -> addSubqueryTableNames(tableNames, virtualTableNames, optional));
        insertStatement.getWith().ifPresent(optional -> addWithTableNames(tableNames, virtualTableNames, optional));
        insertStatement.getMultiTableInsertInto().ifPresent(optional -> addMultiTableInsertIntoTableNames(tableNames, virtualTableNames, optional));
        insertStatement.getMultiTableConditionalInto().ifPresent(optional -> addMultiTableConditionalIntoTableNames(tableNames, virtualTableNames, optional));
        insertStatement.getValues().forEach(each -> addInsertValuesTableNames(tableNames, virtualTableNames, each));
        insertStatement.getSetAssignment().ifPresent(optional -> addSetAssignmentTableNames(tableNames, virtualTableNames, optional));
        insertStatement.getOnDuplicateKeyColumns().ifPresent(optional -> addOnDuplicateKeyColumnsTableNames(tableNames, virtualTableNames, optional));
    }
    
    private void addUpdateStatementTableNames(final Collection<String> tableNames, final Collection<String> virtualTableNames, final UpdateStatement updateStatement) {
        addTableSegmentTableNames(tableNames, virtualTableNames, updateStatement.getTable());
        updateStatement.getFrom().ifPresent(optional -> addTableSegmentTableNames(tableNames, virtualTableNames, optional));
        updateStatement.getWith().ifPresent(optional -> addWithTableNames(tableNames, virtualTableNames, optional));
        updateStatement.getAssignment().ifPresent(optional -> addSetAssignmentTableNames(tableNames, virtualTableNames, optional));
    }
    
    private void addDeleteStatementTableNames(final Collection<String> tableNames, final Collection<String> virtualTableNames, final DeleteStatement deleteStatement) {
        addTableSegmentTableNames(tableNames, virtualTableNames, deleteStatement.getTable());
        deleteStatement.getWith().ifPresent(optional -> addWithTableNames(tableNames, virtualTableNames, optional));
    }
    
    private void addMergeStatementTableNames(final Collection<String> tableNames, final Collection<String> virtualTableNames, final MergeStatement mergeStatement) {
        addTableSegmentTableNames(tableNames, virtualTableNames, mergeStatement.getTarget());
        addTableSegmentTableNames(tableNames, virtualTableNames, mergeStatement.getSource());
        mergeStatement.getUpdate().ifPresent(optional -> addUpdateStatementTableNames(tableNames, virtualTableNames, optional));
        mergeStatement.getInsert().ifPresent(optional -> addInsertStatementTableNames(tableNames, virtualTableNames, optional));
        mergeStatement.getWith().ifPresent(optional -> addWithTableNames(tableNames, virtualTableNames, optional));
    }
    
    private void addMultiTableInsertIntoTableNames(final Collection<String> tableNames, final Collection<String> virtualTableNames, final MultiTableInsertIntoSegment insertIntoSegment) {
        insertIntoSegment.getInsertStatements().forEach(each -> addInsertStatementTableNames(tableNames, virtualTableNames, each));
    }
    
    private void addMultiTableConditionalIntoTableNames(final Collection<String> tableNames, final Collection<String> virtualTableNames,
                                                        final MultiTableConditionalIntoSegment conditionalIntoSegment) {
        conditionalIntoSegment.getWhenThenSegments()
                .forEach(each -> each.getThenSegment().getInsertStatements().forEach(optional -> addInsertStatementTableNames(tableNames, virtualTableNames, optional)));
        conditionalIntoSegment.getElseSegment().ifPresent(optional -> optional.getInsertStatements().forEach(each -> addInsertStatementTableNames(tableNames, virtualTableNames, each)));
    }
    
    private void addInsertValuesTableNames(final Collection<String> tableNames, final Collection<String> virtualTableNames, final InsertValuesSegment valuesSegment) {
        addExpressionTableNames(tableNames, virtualTableNames, valuesSegment.getValues());
    }
    
    private void addSetAssignmentTableNames(final Collection<String> tableNames, final Collection<String> virtualTableNames, final SetAssignmentSegment setAssignmentSegment) {
        addColumnAssignmentTableNames(tableNames, virtualTableNames, setAssignmentSegment.getAssignments());
    }
    
    private void addOnDuplicateKeyColumnsTableNames(final Collection<String> tableNames, final Collection<String> virtualTableNames, final OnDuplicateKeyColumnsSegment onDuplicateKeyColumnsSegment) {
        addColumnAssignmentTableNames(tableNames, virtualTableNames, onDuplicateKeyColumnsSegment.getColumns());
    }
    
    private void addColumnAssignmentTableNames(final Collection<String> tableNames, final Collection<String> virtualTableNames, final Collection<ColumnAssignmentSegment> assignments) {
        assignments.forEach(each -> addExpressionTableNames(tableNames, virtualTableNames, each.getValue()));
    }
    
    private void addExpressionTableNames(final Collection<String> tableNames, final Collection<String> virtualTableNames, final Collection<ExpressionSegment> expressionSegments) {
        expressionSegments.forEach(each -> addExpressionTableNames(tableNames, virtualTableNames, each));
    }
    
    private void addExpressionTableNames(final Collection<String> tableNames, final Collection<String> virtualTableNames, final ExpressionSegment expressionSegment) {
        if (null == expressionSegment) {
            return;
        }
        if (expressionSegment instanceof SubquerySegment) {
            addSubqueryTableNames(tableNames, virtualTableNames, (SubquerySegment) expressionSegment);
        } else if (expressionSegment instanceof SubqueryExpressionSegment) {
            addSubqueryTableNames(tableNames, virtualTableNames, ((SubqueryExpressionSegment) expressionSegment).getSubquery());
        } else if (expressionSegment instanceof ExistsSubqueryExpression) {
            addSubqueryTableNames(tableNames, virtualTableNames, ((ExistsSubqueryExpression) expressionSegment).getSubquery());
        } else if (expressionSegment instanceof QuantifySubqueryExpression) {
            addSubqueryTableNames(tableNames, virtualTableNames, ((QuantifySubqueryExpression) expressionSegment).getSubquery());
        } else {
            addNestedExpressionTableNames(tableNames, virtualTableNames, expressionSegment);
        }
    }
    
    private void addNestedExpressionTableNames(final Collection<String> tableNames, final Collection<String> virtualTableNames, final ExpressionSegment expressionSegment) {
        if (expressionSegment instanceof BinaryOperationExpression) {
            BinaryOperationExpression binaryOperationExpression = (BinaryOperationExpression) expressionSegment;
            addExpressionTableNames(tableNames, virtualTableNames, binaryOperationExpression.getLeft());
            addExpressionTableNames(tableNames, virtualTableNames, binaryOperationExpression.getRight());
        } else if (expressionSegment instanceof FunctionSegment) {
            addExpressionTableNames(tableNames, virtualTableNames, ((FunctionSegment) expressionSegment).getParameters());
        } else if (expressionSegment instanceof ListExpression) {
            addExpressionTableNames(tableNames, virtualTableNames, ((ListExpression) expressionSegment).getItems());
        } else if (expressionSegment instanceof RowExpression) {
            addExpressionTableNames(tableNames, virtualTableNames, ((RowExpression) expressionSegment).getItems());
        } else if (expressionSegment instanceof InExpression) {
            InExpression inExpression = (InExpression) expressionSegment;
            addExpressionTableNames(tableNames, virtualTableNames, inExpression.getLeft());
            addExpressionTableNames(tableNames, virtualTableNames, inExpression.getRight());
        } else if (expressionSegment instanceof BetweenExpression) {
            BetweenExpression betweenExpression = (BetweenExpression) expressionSegment;
            addExpressionTableNames(tableNames, virtualTableNames, betweenExpression.getLeft());
            addExpressionTableNames(tableNames, virtualTableNames, betweenExpression.getBetweenExpr());
            addExpressionTableNames(tableNames, virtualTableNames, betweenExpression.getAndExpr());
        } else if (expressionSegment instanceof CaseWhenExpression) {
            addCaseWhenTableNames(tableNames, virtualTableNames, (CaseWhenExpression) expressionSegment);
        } else {
            addOtherExpressionTableNames(tableNames, virtualTableNames, expressionSegment);
        }
    }
    
    private void addCaseWhenTableNames(final Collection<String> tableNames, final Collection<String> virtualTableNames, final CaseWhenExpression caseWhenExpression) {
        addExpressionTableNames(tableNames, virtualTableNames, caseWhenExpression.getCaseExpr());
        addExpressionTableNames(tableNames, virtualTableNames, caseWhenExpression.getWhenExprs());
        addExpressionTableNames(tableNames, virtualTableNames, caseWhenExpression.getThenExprs());
        addExpressionTableNames(tableNames, virtualTableNames, caseWhenExpression.getElseExpr());
    }
    
    private void addOtherExpressionTableNames(final Collection<String> tableNames, final Collection<String> virtualTableNames, final ExpressionSegment expressionSegment) {
        if (expressionSegment instanceof TypeCastExpression) {
            addExpressionTableNames(tableNames, virtualTableNames, ((TypeCastExpression) expressionSegment).getExpression());
        } else if (expressionSegment instanceof UnaryOperationExpression) {
            addExpressionTableNames(tableNames, virtualTableNames, ((UnaryOperationExpression) expressionSegment).getExpression());
        } else if (expressionSegment instanceof NotExpression) {
            addExpressionTableNames(tableNames, virtualTableNames, ((NotExpression) expressionSegment).getExpression());
        } else if (expressionSegment instanceof CollateExpression) {
            ((CollateExpression) expressionSegment).getExpr().ifPresent(optional -> addExpressionTableNames(tableNames, virtualTableNames, optional));
        } else if (expressionSegment instanceof ValuesExpression) {
            ((ValuesExpression) expressionSegment).getRowConstructorList().forEach(each -> addInsertValuesTableNames(tableNames, virtualTableNames, each));
        } else if (expressionSegment instanceof KeyValueSegment) {
            KeyValueSegment keyValueSegment = (KeyValueSegment) expressionSegment;
            addExpressionTableNames(tableNames, virtualTableNames, keyValueSegment.getKey());
            addExpressionTableNames(tableNames, virtualTableNames, keyValueSegment.getValue());
        } else if (expressionSegment instanceof ColumnAssignmentSegment) {
            addExpressionTableNames(tableNames, virtualTableNames, ((ColumnAssignmentSegment) expressionSegment).getValue());
        }
    }
    
    private void addWithTableNames(final Collection<String> tableNames, final Collection<String> virtualTableNames, final WithSegment withSegment) {
        for (CommonTableExpressionSegment each : withSegment.getCommonTableExpressions()) {
            each.getAliasName().ifPresent(optional -> virtualTableNames.add(optional.toLowerCase(Locale.ENGLISH)));
            addSubqueryTableNames(tableNames, virtualTableNames, each.getSubquery());
        }
    }
    
    private void addTableSegmentTableNames(final Collection<String> tableNames, final Collection<String> virtualTableNames, final TableSegment tableSegment) {
        addVirtualTableNames(virtualTableNames, tableSegment);
        if (tableSegment instanceof SimpleTableSegment) {
            addTableName(tableNames, (SimpleTableSegment) tableSegment);
        } else if (tableSegment instanceof JoinTableSegment) {
            JoinTableSegment joinTableSegment = (JoinTableSegment) tableSegment;
            addTableSegmentTableNames(tableNames, virtualTableNames, joinTableSegment.getLeft());
            addTableSegmentTableNames(tableNames, virtualTableNames, joinTableSegment.getRight());
        } else if (tableSegment instanceof SubqueryTableSegment) {
            addSubqueryTableNames(tableNames, virtualTableNames, ((SubqueryTableSegment) tableSegment).getSubquery());
        } else if (tableSegment instanceof DeleteMultiTableSegment) {
            DeleteMultiTableSegment deleteMultiTableSegment = (DeleteMultiTableSegment) tableSegment;
            deleteMultiTableSegment.getActualDeleteTables().forEach(each -> addTableName(tableNames, each));
            addTableSegmentTableNames(tableNames, virtualTableNames, deleteMultiTableSegment.getRelationTable());
        }
    }
    
    private void addSubqueryTableNames(final Collection<String> tableNames, final Collection<String> virtualTableNames, final SubquerySegment subquerySegment) {
        if (null != subquerySegment.getSelect()) {
            TableExtractor tableExtractor = new TableExtractor();
            tableExtractor.extractTablesFromSelect(subquerySegment.getSelect());
            tableExtractor.getTableContext().forEach(each -> addVirtualTableNames(virtualTableNames, each));
            addSelectStatementTableNames(tableNames, virtualTableNames, subquerySegment.getSelect());
            addExtractedRewriteTableNames(tableNames, tableExtractor);
        }
        if (null != subquerySegment.getMerge()) {
            addMergeStatementTableNames(tableNames, virtualTableNames, subquerySegment.getMerge());
        }
    }
    
    private void addExtractedRewriteTableNames(final Collection<String> tableNames, final TableExtractor tableExtractor) {
        for (SimpleTableSegment each : tableExtractor.getRewriteTables()) {
            addTableName(tableNames, each);
        }
    }
    
    private void addTableName(final Collection<String> tableNames, final SimpleTableSegment tableSegment) {
        tableNames.add(tableSegment.getTableName().getIdentifier().getValue());
    }
    
    private void addVirtualTableNames(final Collection<String> virtualTableNames, final TableSegment tableSegment) {
        if (tableSegment instanceof SimpleTableSegment) {
            SimpleTableSegment simpleTableSegment = (SimpleTableSegment) tableSegment;
            simpleTableSegment.getAliasName().filter(optional -> !optional.equalsIgnoreCase(simpleTableSegment.getTableName().getIdentifier().getValue()))
                    .ifPresent(optional -> virtualTableNames.add(optional.toLowerCase(Locale.ENGLISH)));
        } else if (tableSegment instanceof JoinTableSegment) {
            JoinTableSegment joinTableSegment = (JoinTableSegment) tableSegment;
            joinTableSegment.getAliasName().ifPresent(optional -> virtualTableNames.add(optional.toLowerCase(Locale.ENGLISH)));
            addVirtualTableNames(virtualTableNames, joinTableSegment.getLeft());
            addVirtualTableNames(virtualTableNames, joinTableSegment.getRight());
        } else if (tableSegment instanceof SubqueryTableSegment) {
            tableSegment.getAliasName().ifPresent(optional -> virtualTableNames.add(optional.toLowerCase(Locale.ENGLISH)));
        }
    }
    
    private void removeVirtualTableNames(final Collection<String> tableNames, final Collection<String> virtualTableNames) {
        tableNames.removeIf(each -> virtualTableNames.contains(each.toLowerCase(Locale.ENGLISH)));
    }
    
    private boolean isUnextractedWithSQL(final String sql, final SQLStatement sqlStatement) {
        return sql.trim().toUpperCase(Locale.ENGLISH).startsWith("WITH") && !containsWithSegment(sqlStatement);
    }
    
    private boolean containsWithSegment(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof SelectStatement) {
            return ((SelectStatement) sqlStatement).getWith().isPresent();
        }
        if (sqlStatement instanceof InsertStatement) {
            return ((InsertStatement) sqlStatement).getWith().isPresent();
        }
        if (sqlStatement instanceof UpdateStatement) {
            return ((UpdateStatement) sqlStatement).getWith().isPresent();
        }
        if (sqlStatement instanceof DeleteStatement) {
            return ((DeleteStatement) sqlStatement).getWith().isPresent();
        }
        return sqlStatement instanceof MergeStatement && ((MergeStatement) sqlStatement).getWith().isPresent();
    }
    
    private boolean isTransactionControlSQL(final String sql) {
        String trimSQL = sql.trim().toUpperCase(Locale.ENGLISH);
        return trimSQL.startsWith("BEGIN") || trimSQL.startsWith("COMMIT") || trimSQL.startsWith("ROLLBACK") || trimSQL.startsWith("START TRANSACTION")
                || trimSQL.startsWith("SAVEPOINT") || trimSQL.startsWith("RELEASE SAVEPOINT") || trimSQL.startsWith("SET AUTOCOMMIT");
    }
    
    private SQLParserEngine getSQLParserEngine(final DatabaseType databaseType) {
        return SQL_PARSER_ENGINE_MAP.computeIfAbsent(databaseType.getType(), unused -> new ShardingSphereSQLParserEngine(databaseType, CACHE_OPTION, CACHE_OPTION));
    }
    
    private boolean addDataSetTableNames(final Collection<String> tableNames, final E2ETestParameter testParam, final String expectedDataFile) {
        if (null == expectedDataFile) {
            return true;
        }
        try {
            for (DataSetMetaData each : DataSetLoader.load(testParam.getTestCaseContext().getParentPath(), testParam.getScenario(), testParam.getDatabaseType(), testParam.getMode(), expectedDataFile)
                    .getMetaDataList()) {
                addDataSetMetaDataTableNames(tableNames, each);
            }
            return true;
        } catch (final IllegalArgumentException ignored) {
            return false;
        }
    }
    
    private void addDataSetMetaDataTableNames(final Collection<String> tableNames, final DataSetMetaData dataSetMetaData) {
        if (null != dataSetMetaData.getTableName()) {
            tableNames.add(dataSetMetaData.getTableName());
        }
        for (String each : InlineExpressionParserFactory.newInstance(dataSetMetaData.getDataNodes()).splitAndEvaluate()) {
            String tableName = new DataNode(each).getTableName();
            tableNames.add(tableName);
            tableNames.add(tableName.replaceFirst("_[0-9]+$", ""));
            tableNames.add(tableName.replaceFirst("[0-9]+$", ""));
        }
    }
}
