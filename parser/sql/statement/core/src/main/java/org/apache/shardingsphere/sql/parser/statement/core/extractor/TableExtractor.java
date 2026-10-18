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

package org.apache.shardingsphere.sql.parser.statement.core.extractor;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicy;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierNormalizeEngine;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.routine.RoutineBodySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.routine.ValidStatementSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.ReturningSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.combine.CombineSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.datetime.DatetimeExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.CaseWhenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.CollateExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExistsSubqueryExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.NotExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.QuantifySubqueryExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.TypeCastExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.UnaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.merge.MergeWhenAndThenSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.LockSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerAvailable;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.match.MatchAgainstExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.DeleteMultiTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.FunctionTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CommentStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.MergeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Table extractor.
 */
@Getter
public final class TableExtractor {
    
    private final Collection<SimpleTableSegment> rewriteTables = new LinkedList<>();
    
    private final Collection<TableSegment> tableContext = new LinkedList<>();
    
    private final Collection<JoinTableSegment> joinTables = new LinkedList<>();
    
    @Nullable
    @Getter(AccessLevel.NONE)
    private IdentifierCasePolicy identifierCasePolicy;
    
    /**
     * Extract table that should be rewritten from select statement.
     *
     * @param selectStatement select statement
     */
    public void extractTablesFromSelect(final SelectStatement selectStatement) {
        TableSegment from = selectStatement.getFrom().orElse(null);
        extractTablesFromSelect(selectStatement, from, createTableScope(selectStatement));
    }
    
    private void extractTablesFromSelect(final SelectStatement selectStatement, final TableSegment from, final TableScope tableScope) {
        if (selectStatement.getCombine().isPresent()) {
            CombineSegment combineSegment = selectStatement.getCombine().get();
            extractTablesFromNestedStatement(combineSegment.getLeft().getSelect(), tableScope);
            extractTablesFromNestedStatement(combineSegment.getRight().getSelect(), tableScope);
        }
        if (null != from && !selectStatement.getCombine().isPresent()) {
            extractTablesFromTableSegment(from, tableScope);
        }
        if (selectStatement.getWhere().isPresent()) {
            extractTablesFromExpression(selectStatement.getWhere().get().getExpr(), tableScope);
        }
        if (null != selectStatement.getProjections() && !selectStatement.getCombine().isPresent()) {
            extractTablesFromProjections(selectStatement.getProjections(), tableScope);
        }
        if (selectStatement.getGroupBy().isPresent()) {
            extractTablesFromOrderByItems(selectStatement.getGroupBy().get().getGroupByItems(), tableScope);
        }
        if (selectStatement.getOrderBy().isPresent()) {
            extractTablesFromOrderByItems(selectStatement.getOrderBy().get().getOrderByItems(), tableScope);
        }
        if (selectStatement.getHaving().isPresent()) {
            extractTablesFromExpression(selectStatement.getHaving().get().getExpr(), tableScope);
        }
        if (selectStatement.getWith().isPresent()) {
            extractTablesFromCTEs(selectStatement.getWith().get().getCommonTableExpressions(), tableScope);
        }
        selectStatement.getLock().ifPresent(this::extractTablesFromLock);
    }
    
    /**
     * Use the flat table context when no USING alias needs a scope.
     *
     * @param sqlStatement statement whose table scope is inspected
     * @return scope for USING aliases, or null to use the flat table context
     */
    private TableScope createTableScope(final SQLStatement sqlStatement) {
        if (!containsUsingAlias(sqlStatement)) {
            identifierCasePolicy = null;
            return null;
        }
        identifierCasePolicy = IdentifierNormalizeEngine.resolvePolicy(sqlStatement.getDatabaseType(), null, IdentifierScope.TABLE);
        TableScope result = new TableScope(null, tableContext);
        result.identifierCasePolicy = identifierCasePolicy;
        result.usingAliasScope = true;
        return result;
    }
    
    private boolean containsUsingAlias(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof SelectStatement) {
            SelectStatement selectStatement = (SelectStatement) sqlStatement;
            return containsUsingAlias(selectStatement.getFrom().orElse(null));
        }
        if (sqlStatement instanceof UpdateStatement) {
            UpdateStatement updateStatement = (UpdateStatement) sqlStatement;
            return containsUsingAlias(updateStatement.getTable()) || updateStatement.getFrom().isPresent() && containsUsingAlias(updateStatement.getFrom().get());
        }
        if (sqlStatement instanceof DeleteStatement) {
            return containsUsingAlias(((DeleteStatement) sqlStatement).getTable());
        }
        if (sqlStatement instanceof InsertStatement) {
            InsertStatement insertStatement = (InsertStatement) sqlStatement;
            return insertStatement.getInsertSelect().isPresent() && containsUsingAlias(insertStatement.getInsertSelect().get().getSelect());
        }
        if (sqlStatement instanceof MergeStatement) {
            MergeStatement mergeStatement = (MergeStatement) sqlStatement;
            return containsUsingAlias(mergeStatement.getTarget()) || containsUsingAlias(mergeStatement.getSource());
        }
        return false;
    }
    
    private boolean containsUsingAlias(final TableSegment tableSegment) {
        if (tableSegment instanceof DeleteMultiTableSegment) {
            return containsUsingAlias(((DeleteMultiTableSegment) tableSegment).getRelationTable());
        }
        if (!(tableSegment instanceof JoinTableSegment)) {
            return false;
        }
        JoinTableSegment joinTableSegment = (JoinTableSegment) tableSegment;
        return joinTableSegment.getUsingAliasSegment().isPresent() || containsUsingAlias(joinTableSegment.getLeft()) || containsUsingAlias(joinTableSegment.getRight());
    }
    
    private void extractTablesFromNestedStatement(final SQLStatement sqlStatement, final TableScope tableScope) {
        boolean usingAlias = containsUsingAlias(sqlStatement);
        TableScope nestedScope = createNestedTableScope(tableScope, null != tableScope || usingAlias);
        if (usingAlias) {
            nestedScope.usingAliasScope = true;
            if (null == nestedScope.identifierCasePolicy) {
                nestedScope.identifierCasePolicy = IdentifierNormalizeEngine.resolvePolicy(sqlStatement.getDatabaseType(), null, IdentifierScope.TABLE);
            }
        }
        if (sqlStatement instanceof SelectStatement) {
            SelectStatement selectStatement = (SelectStatement) sqlStatement;
            TableSegment from = selectStatement.getFrom().orElse(null);
            extractTablesFromSelect(selectStatement, from, nestedScope);
            return;
        }
        if (sqlStatement instanceof MergeStatement) {
            extractTablesFromMerge((MergeStatement) sqlStatement, nestedScope);
        } else if (sqlStatement instanceof InsertStatement) {
            extractTablesFromInsert((InsertStatement) sqlStatement, nestedScope);
        } else if (sqlStatement instanceof UpdateStatement) {
            extractTablesFromUpdate((UpdateStatement) sqlStatement, nestedScope);
        } else if (sqlStatement instanceof DeleteStatement) {
            extractTablesFromDelete((DeleteStatement) sqlStatement, nestedScope);
        }
    }
    
    private TableScope createNestedTableScope(final TableScope tableScope, final boolean scopeRequired) {
        if (!scopeRequired) {
            return null;
        }
        TableScope parentScope = null == tableScope ? new TableScope(null, tableContext) : tableScope;
        return new TableScope(parentScope, null);
    }
    
    private void extractTablesFromCTEs(final Collection<CommonTableExpressionSegment> commonTableExpressionSegments, final TableScope tableScope) {
        for (CommonTableExpressionSegment each : commonTableExpressionSegments) {
            extractTablesFromNestedStatement(each.getStatement(), tableScope);
        }
    }
    
    private void extractTablesFromTableSegment(final TableSegment tableSegment, final TableScope tableScope) {
        if (tableSegment instanceof SimpleTableSegment) {
            addTableContext(tableSegment, tableScope);
            rewriteTables.add((SimpleTableSegment) tableSegment);
        }
        if (tableSegment instanceof SubqueryTableSegment) {
            TableExtractor tableExtractor = new TableExtractor();
            if (null != tableScope) {
                tableExtractor.extractTablesFromNestedStatement(((SubqueryTableSegment) tableSegment).getSubquery().getSelect(), tableScope);
            } else {
                tableExtractor.extractTablesFromSelect(((SubqueryTableSegment) tableSegment).getSubquery().getSelect());
            }
            rewriteTables.addAll(tableExtractor.rewriteTables);
            joinTables.addAll(tableExtractor.joinTables);
            addTableContext(tableSegment, tableScope);
        }
        if (tableSegment instanceof JoinTableSegment) {
            joinTables.add((JoinTableSegment) tableSegment);
            extractTablesFromJoinTableSegment((JoinTableSegment) tableSegment, tableScope);
        }
        if (tableSegment instanceof DeleteMultiTableSegment) {
            DeleteMultiTableSegment deleteMultiTableSegment = (DeleteMultiTableSegment) tableSegment;
            rewriteTables.addAll(deleteMultiTableSegment.getActualDeleteTables());
            extractTablesFromTableSegment(deleteMultiTableSegment.getRelationTable(), tableScope);
        }
        if (tableSegment instanceof FunctionTableSegment) {
            addTableContext(tableSegment, tableScope);
            extractTablesFromExpression(((FunctionTableSegment) tableSegment).getTableFunction(), tableScope);
        }
    }
    
    private void addTableContext(final TableSegment tableSegment, final TableScope tableScope) {
        tableContext.add(tableSegment);
        if (null != tableScope) {
            tableScope.tables.add(tableSegment);
        }
    }
    
    private void extractTablesFromJoinTableSegment(final JoinTableSegment tableSegment, final TableScope tableScope) {
        boolean scopedAlias = null != tableScope && tableScope.usingAliasScope && tableSegment.getAlias().isPresent();
        boolean registerAlias = scopedAlias || tableSegment.getUsingAliasSegment().isPresent();
        if (registerAlias && null == tableScope.parent) {
            tableContext.add(tableSegment);
        }
        TableScope joinScope = scopedAlias ? new TableScope(tableScope, null) : tableScope;
        extractTablesFromTableSegment(tableSegment.getLeft(), joinScope);
        extractTablesFromTableSegment(tableSegment.getRight(), joinScope);
        extractTablesFromExpression(tableSegment.getCondition(), joinScope);
        if (registerAlias) {
            tableScope.tables.add(tableSegment);
        }
    }
    
    private void extractTablesFromExpression(final ExpressionSegment expressionSegment, final TableScope tableScope) {
        if (expressionSegment instanceof ColumnSegment) {
            extractTablesFromColumnSegments(Collections.singleton((ColumnSegment) expressionSegment), tableScope);
        }
        if (expressionSegment instanceof ListExpression) {
            extractTablesFromExpressions(((ListExpression) expressionSegment).getItems(), tableScope);
        }
        if (expressionSegment instanceof CommonExpressionSegment) {
            extractTablesFromExpressions(((CommonExpressionSegment) expressionSegment).getChildren(), tableScope);
        }
        if (expressionSegment instanceof UnaryOperationExpression) {
            extractTablesFromExpression(((UnaryOperationExpression) expressionSegment).getExpression(), tableScope);
        }
        if (expressionSegment instanceof ExistsSubqueryExpression) {
            extractTablesFromNestedStatement(((ExistsSubqueryExpression) expressionSegment).getSubquery().getSelect(), tableScope);
        }
        if (expressionSegment instanceof BetweenExpression) {
            extractTablesFromExpression(((BetweenExpression) expressionSegment).getLeft(), tableScope);
            extractTablesFromExpression(((BetweenExpression) expressionSegment).getBetweenExpr(), tableScope);
            extractTablesFromExpression(((BetweenExpression) expressionSegment).getAndExpr(), tableScope);
        }
        if (expressionSegment instanceof InExpression) {
            extractTablesFromExpression(((InExpression) expressionSegment).getLeft(), tableScope);
            extractTablesFromExpression(((InExpression) expressionSegment).getRight(), tableScope);
        }
        if (expressionSegment instanceof SubqueryExpressionSegment) {
            extractTablesFromNestedStatement(((SubqueryExpressionSegment) expressionSegment).getSubquery().getSelect(), tableScope);
        }
        if (expressionSegment instanceof SubquerySegment) {
            extractTablesFromNestedStatement(((SubquerySegment) expressionSegment).getSelect(), tableScope);
        }
        if (expressionSegment instanceof BinaryOperationExpression) {
            extractTablesFromExpression(((BinaryOperationExpression) expressionSegment).getLeft(), tableScope);
            extractTablesFromExpression(((BinaryOperationExpression) expressionSegment).getRight(), tableScope);
        }
        if (expressionSegment instanceof MatchAgainstExpression) {
            extractTablesFromExpressions(((MatchAgainstExpression) expressionSegment).getColumns(), tableScope);
            extractTablesFromExpression(((MatchAgainstExpression) expressionSegment).getExpr(), tableScope);
        }
        if (expressionSegment instanceof FunctionSegment) {
            extractTablesFromExpressions(((FunctionSegment) expressionSegment).getParameters(), tableScope);
        }
        if (expressionSegment instanceof AggregationProjectionSegment) {
            extractTablesFromExpressions(((AggregationProjectionSegment) expressionSegment).getParameters(), tableScope);
        }
        if (expressionSegment instanceof CaseWhenExpression) {
            extractTablesFromCaseWhenExpression((CaseWhenExpression) expressionSegment, tableScope);
        }
        if (expressionSegment instanceof CollateExpression) {
            extractTablesFromExpression(((CollateExpression) expressionSegment).getCollateName(), tableScope);
        }
        if (expressionSegment instanceof DatetimeExpression) {
            extractTablesFromExpression(((DatetimeExpression) expressionSegment).getLeft(), tableScope);
            extractTablesFromExpression(((DatetimeExpression) expressionSegment).getRight(), tableScope);
        }
        if (expressionSegment instanceof NotExpression) {
            extractTablesFromExpression(((NotExpression) expressionSegment).getExpression(), tableScope);
        }
        if (expressionSegment instanceof TypeCastExpression) {
            extractTablesFromExpression(((TypeCastExpression) expressionSegment).getExpression(), tableScope);
        }
        if (expressionSegment instanceof QuantifySubqueryExpression) {
            extractTablesFromExpression(((QuantifySubqueryExpression) expressionSegment).getSubquery(), tableScope);
        }
    }
    
    private void extractTablesFromExpressions(final Collection<? extends ExpressionSegment> expressionSegments, final TableScope tableScope) {
        for (ExpressionSegment each : expressionSegments) {
            extractTablesFromExpression(each, tableScope);
        }
    }
    
    private void extractTablesFromCaseWhenExpression(final CaseWhenExpression expressionSegment, final TableScope tableScope) {
        extractTablesFromExpression(expressionSegment.getCaseExpr(), tableScope);
        extractTablesFromExpressions(expressionSegment.getWhenExprs(), tableScope);
        extractTablesFromExpressions(expressionSegment.getThenExprs(), tableScope);
        extractTablesFromExpression(expressionSegment.getElseExpr(), tableScope);
    }
    
    private void extractTablesFromProjections(final ProjectionsSegment projections, final TableScope tableScope) {
        for (ProjectionSegment each : projections.getProjections()) {
            if (each instanceof SubqueryProjectionSegment) {
                extractTablesFromNestedStatement(((SubqueryProjectionSegment) each).getSubquery().getSelect(), tableScope);
            } else if (each instanceof OwnerAvailable) {
                if (((OwnerAvailable) each).getOwner().isPresent() && needRewrite(((OwnerAvailable) each).getOwner().get(), tableScope)) {
                    OwnerSegment ownerSegment = ((OwnerAvailable) each).getOwner().get();
                    rewriteTables.add(createSimpleTableSegment(ownerSegment));
                }
            } else if (each instanceof ColumnProjectionSegment) {
                if (((ColumnProjectionSegment) each).getColumn().getOwner().isPresent() && needRewrite(((ColumnProjectionSegment) each).getColumn().getOwner().get(), tableScope)) {
                    OwnerSegment ownerSegment = ((ColumnProjectionSegment) each).getColumn().getOwner().get();
                    rewriteTables.add(createSimpleTableSegment(ownerSegment));
                }
            } else if (each instanceof AggregationProjectionSegment) {
                extractTablesFromExpressions(((AggregationProjectionSegment) each).getParameters(), tableScope);
            } else if (each instanceof ExpressionProjectionSegment) {
                extractTablesFromExpression(((ExpressionProjectionSegment) each).getExpr(), tableScope);
            }
        }
    }
    
    private SimpleTableSegment createSimpleTableSegment(final OwnerSegment ownerSegment) {
        TableNameSegment tableNameSegment = new TableNameSegment(ownerSegment.getStartIndex(), ownerSegment.getStopIndex(), ownerSegment.getIdentifier());
        ownerSegment.getTableBoundInfo().ifPresent(tableNameSegment::setTableBoundInfo);
        SimpleTableSegment result = new SimpleTableSegment(tableNameSegment);
        ownerSegment.getOwner().ifPresent(result::setOwner);
        return result;
    }
    
    private void extractTablesFromOrderByItems(final Collection<OrderByItemSegment> orderByItems, final TableScope tableScope) {
        for (OrderByItemSegment each : orderByItems) {
            if (each instanceof ColumnOrderByItemSegment) {
                Optional<OwnerSegment> owner = ((ColumnOrderByItemSegment) each).getColumn().getOwner();
                if (owner.isPresent() && needRewrite(owner.get(), tableScope)) {
                    TableNameSegment tableNameSegment = new TableNameSegment(owner.get().getStartIndex(), owner.get().getStopIndex(), owner.get().getIdentifier());
                    owner.get().getTableBoundInfo().ifPresent(tableNameSegment::setTableBoundInfo);
                    rewriteTables.add(new SimpleTableSegment(tableNameSegment));
                }
            }
        }
    }
    
    private void extractTablesFromLock(final LockSegment lockSegment) {
        rewriteTables.addAll(lockSegment.getTables());
    }
    
    /**
     * Extract table that should be rewritten from delete statement.
     *
     * @param deleteStatement delete statement
     */
    public void extractTablesFromDelete(final DeleteStatement deleteStatement) {
        extractTablesFromDelete(deleteStatement, createTableScope(deleteStatement));
    }
    
    private void extractTablesFromDelete(final DeleteStatement deleteStatement, final TableScope tableScope) {
        extractTablesFromTableSegment(deleteStatement.getTable(), tableScope);
        if (deleteStatement.getWhere().isPresent()) {
            extractTablesFromExpression(deleteStatement.getWhere().get().getExpr(), tableScope);
        }
    }
    
    /**
     * Extract table that should be rewritten from insert statement.
     *
     * @param insertStatement insert statement
     */
    public void extractTablesFromInsert(final InsertStatement insertStatement) {
        extractTablesFromInsert(insertStatement, createTableScope(insertStatement));
    }
    
    private void extractTablesFromInsert(final InsertStatement insertStatement, final TableScope tableScope) {
        if (insertStatement.getTable().isPresent()) {
            extractTablesFromTableSegment(insertStatement.getTable().get(), tableScope);
        }
        if (!insertStatement.getColumns().isEmpty()) {
            for (ColumnSegment each : insertStatement.getColumns()) {
                extractTablesFromExpression(each, tableScope);
            }
        }
        if (insertStatement.getOnDuplicateKeyColumns().isPresent()) {
            extractTablesFromAssignmentItems(insertStatement.getOnDuplicateKeyColumns().get().getColumns(), tableScope);
        }
        if (insertStatement.getInsertSelect().isPresent()) {
            SelectStatement selectStatement = insertStatement.getInsertSelect().get().getSelect();
            extractTablesFromSelect(selectStatement, selectStatement.getFrom().orElse(null), tableScope);
        }
    }
    
    private void extractTablesFromAssignmentItems(final Collection<ColumnAssignmentSegment> assignmentItems, final TableScope tableScope) {
        for (ColumnAssignmentSegment each : assignmentItems) {
            extractTablesFromColumnSegments(each.getColumns(), tableScope);
        }
    }
    
    private void extractTablesFromColumnSegments(final Collection<ColumnSegment> columnSegments, final TableScope tableScope) {
        for (ColumnSegment each : columnSegments) {
            if (each.getOwner().isPresent() && needRewrite(each.getOwner().get(), tableScope)) {
                OwnerSegment ownerSegment = each.getOwner().get();
                TableNameSegment tableSegment = new TableNameSegment(ownerSegment.getStartIndex(), ownerSegment.getStopIndex(), ownerSegment.getIdentifier());
                ownerSegment.getTableBoundInfo().ifPresent(tableSegment::setTableBoundInfo);
                rewriteTables.add(new SimpleTableSegment(tableSegment));
            }
        }
    }
    
    /**
     * Extract table that should be rewritten from update statement.
     *
     * @param updateStatement update statement.
     */
    public void extractTablesFromUpdate(final UpdateStatement updateStatement) {
        extractTablesFromUpdate(updateStatement, createTableScope(updateStatement));
    }
    
    private void extractTablesFromUpdate(final UpdateStatement updateStatement, final TableScope tableScope) {
        if (!updateStatement.isTargetTableIsFromAlias()) {
            extractTablesFromTableSegment(updateStatement.getTable(), tableScope);
        }
        if (updateStatement.getFrom().isPresent()) {
            extractTablesFromTableSegment(updateStatement.getFrom().get(), tableScope);
        }
        for (ColumnAssignmentSegment each : updateStatement.getSetAssignment().getAssignments()) {
            extractTablesFromExpression(each.getColumns().get(0), tableScope);
        }
        if (updateStatement.getWhere().isPresent()) {
            extractTablesFromExpression(updateStatement.getWhere().get().getExpr(), tableScope);
        }
    }
    
    /**
     * Extract tables from a MERGE body of a common table expression or prepared statement.
     *
     * @param mergeStatement merge statement
     */
    public void extractTablesFromMerge(final MergeStatement mergeStatement) {
        extractTablesFromMerge(mergeStatement, createTableScope(mergeStatement));
    }
    
    private void extractTablesFromMerge(final MergeStatement mergeStatement, final TableScope tableScope) {
        TableScope mergeScope = null == tableScope ? new TableScope(null, tableContext) : tableScope;
        extractTablesFromTableSegment(mergeStatement.getTarget(), mergeScope);
        extractTablesFromTableSegment(mergeStatement.getSource(), mergeScope);
        if (null != mergeStatement.getExpression()) {
            extractTablesFromExpression(mergeStatement.getExpression().getExpr(), mergeScope);
        }
        for (MergeWhenAndThenSegment each : mergeStatement.getWhenAndThens()) {
            extractTablesFromExpression(each.getAndExpr(), mergeScope);
            if (null != each.getUpdate()) {
                extractTablesFromMergeUpdate(each.getUpdate(), mergeScope);
            }
            if (null != each.getInsert()) {
                extractTablesFromMergeInsert(each.getInsert(), mergeScope);
            }
        }
        if (mergeStatement.getReturning().isPresent()) {
            extractTablesFromMergeReturning(mergeStatement, mergeScope);
        }
        if (mergeStatement.getWith().isPresent()) {
            extractTablesFromCTEs(mergeStatement.getWith().get().getCommonTableExpressions(), mergeScope);
        }
    }
    
    private void extractTablesFromMergeUpdate(final UpdateStatement updateStatement, final TableScope tableScope) {
        for (ColumnAssignmentSegment each : updateStatement.getSetAssignment().getAssignments()) {
            extractTablesFromExpression(each.getValue(), tableScope);
        }
    }
    
    private void extractTablesFromMergeInsert(final InsertStatement insertStatement, final TableScope tableScope) {
        for (InsertValuesSegment each : insertStatement.getValues()) {
            extractTablesFromExpressions(each.getValues(), tableScope);
        }
    }
    
    private void extractTablesFromMergeReturning(final MergeStatement mergeStatement, final TableScope tableScope) {
        ReturningSegment returning = mergeStatement.getReturning().get();
        TableScope returningScope = new TableScope(tableScope, null);
        returningScope.identifierCasePolicy = IdentifierNormalizeEngine.resolvePolicy(mergeStatement.getDatabaseType(), null, IdentifierScope.TABLE);
        returningScope.tables.addAll(tableScope.tables);
        returningScope.aliases.add(null == returning.getOldAlias() ? new IdentifierValue("old") : returning.getOldAlias().getIdentifier());
        returningScope.aliases.add(null == returning.getNewAlias() ? new IdentifierValue("new") : returning.getNewAlias().getIdentifier());
        extractTablesFromProjections(returning.getProjections(), returningScope);
    }
    
    /**
     * Check if the table needs to be overwritten.
     *
     * @param owner owner
     * @return boolean
     */
    public boolean needRewrite(final OwnerSegment owner) {
        for (TableSegment each : tableContext) {
            if (isAlias(owner.getIdentifier(), each, identifierCasePolicy)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean needRewrite(final OwnerSegment owner, final TableScope tableScope) {
        return null == tableScope ? needRewrite(owner) : needRewriteFromLocalTableContext(owner.getIdentifier(), tableScope);
    }
    
    private boolean needRewriteFromLocalTableContext(final IdentifierValue owner, final TableScope scope) {
        for (TableSegment each : scope.tables) {
            if (isAlias(owner, each, scope.identifierCasePolicy)) {
                return false;
            }
        }
        for (TableSegment each : scope.tables) {
            if (each instanceof SimpleTableSegment && !each.getAliasName().isPresent()
                    && matchesIdentifier(owner, ((SimpleTableSegment) each).getTableName().getIdentifier(), scope.identifierCasePolicy)) {
                return true;
            }
        }
        if (scope.aliases.stream().anyMatch(each -> matchesIdentifier(owner, each, scope.identifierCasePolicy))) {
            return false;
        }
        if (null != scope.parent) {
            return needRewriteFromLocalTableContext(owner, scope.parent);
        }
        int inheritedTableIndex = 0;
        for (TableSegment each : scope.inheritedTables) {
            if (inheritedTableIndex++ == scope.inheritedTableCount) {
                break;
            }
            if (isAlias(owner, each, scope.identifierCasePolicy)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isAlias(final IdentifierValue owner, final TableSegment tableSegment, @Nullable final IdentifierCasePolicy identifierCasePolicy) {
        if (null == identifierCasePolicy) {
            return isAlias(owner.getValue(), tableSegment);
        }
        return tableSegment.getAlias().filter(alias -> matchesIdentifier(owner, alias, identifierCasePolicy)).isPresent()
                || tableSegment instanceof JoinTableSegment && !tableSegment.getAlias().isPresent() && ((JoinTableSegment) tableSegment).getUsingAliasSegment()
                        .filter(usingAlias -> matchesIdentifier(owner, usingAlias.getIdentifier(), identifierCasePolicy)).isPresent();
    }
    
    private boolean isAlias(final String ownerName, final TableSegment tableSegment) {
        return ownerName.equalsIgnoreCase(tableSegment.getAliasName().orElse(null));
    }
    
    private boolean matchesIdentifier(final IdentifierValue owner, final IdentifierValue declared, @Nullable final IdentifierCasePolicy identifierCasePolicy) {
        return null == identifierCasePolicy ? owner.getValue().equalsIgnoreCase(declared.getValue())
                : identifierCasePolicy.matches(identifierCasePolicy.normalizeForDefinition(declared.getValue(), declared.getQuoteCharacter()), owner.getValue(), owner.getQuoteCharacter());
    }
    
    /**
     * Extract the tables that should exist from routine body segment.
     *
     * @param routineBody routine body segment
     * @return the tables that should exist
     */
    public Collection<SimpleTableSegment> extractExistTableFromRoutineBody(final RoutineBodySegment routineBody) {
        Collection<SimpleTableSegment> result = new LinkedList<>();
        for (ValidStatementSegment each : routineBody.getValidStatements()) {
            if (each.getAlterTable().isPresent()) {
                result.add(each.getAlterTable().get().getTable());
            }
            if (each.getDropTable().isPresent()) {
                result.addAll(each.getDropTable().get().getTables());
            }
            if (each.getTruncate().isPresent()) {
                result.addAll(each.getTruncate().get().getTables());
            }
            result.addAll(extractExistTableFromDMLStatement(each));
        }
        return result;
    }
    
    private Collection<SimpleTableSegment> extractExistTableFromDMLStatement(final ValidStatementSegment validStatementSegment) {
        if (validStatementSegment.getInsert().isPresent()) {
            extractTablesFromInsert(validStatementSegment.getInsert().get());
        } else if (validStatementSegment.getReplace().isPresent()) {
            extractTablesFromInsert(validStatementSegment.getReplace().get());
        } else if (validStatementSegment.getUpdate().isPresent()) {
            extractTablesFromUpdate(validStatementSegment.getUpdate().get());
        } else if (validStatementSegment.getDelete().isPresent()) {
            extractTablesFromDelete(validStatementSegment.getDelete().get());
        } else if (validStatementSegment.getSelect().isPresent()) {
            extractTablesFromSelect(validStatementSegment.getSelect().get());
        }
        return rewriteTables;
    }
    
    /**
     * Extract the tables that should not exist from routine body segment.
     *
     * @param routineBody routine body segment
     * @return the tables that should not exist
     */
    public Collection<SimpleTableSegment> extractNotExistTableFromRoutineBody(final RoutineBodySegment routineBody) {
        Collection<SimpleTableSegment> result = new LinkedList<>();
        for (ValidStatementSegment each : routineBody.getValidStatements()) {
            Optional<CreateTableStatement> createTable = each.getCreateTable();
            if (createTable.isPresent() && !createTable.get().isIfNotExists()) {
                result.add(createTable.get().getTable());
            }
        }
        return result;
    }
    
    /**
     * Extract table that should be rewritten from SQL statement.
     *
     * @param sqlStatement SQL statement
     */
    public void extractTablesFromSQLStatement(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof SelectStatement) {
            extractTablesFromSelect((SelectStatement) sqlStatement);
        } else if (sqlStatement instanceof InsertStatement) {
            extractTablesFromInsert((InsertStatement) sqlStatement);
        } else if (sqlStatement instanceof UpdateStatement) {
            extractTablesFromUpdate((UpdateStatement) sqlStatement);
        } else if (sqlStatement instanceof DeleteStatement) {
            extractTablesFromDelete((DeleteStatement) sqlStatement);
        } else if (sqlStatement instanceof CreateTableStatement) {
            extractTablesFromTableSegment(((CreateTableStatement) sqlStatement).getTable(), null);
        } else if (sqlStatement instanceof AlterTableStatement) {
            extractTablesFromTableSegment(((AlterTableStatement) sqlStatement).getTable(), null);
        } else if (sqlStatement instanceof CommentStatement) {
            extractTablesFromTableSegment(((CommentStatement) sqlStatement).getTable(), null);
        } else if (sqlStatement instanceof CreateViewStatement) {
            extractTablesFromCreateViewStatement((CreateViewStatement) sqlStatement);
        } else if (sqlStatement instanceof CreateIndexStatement) {
            extractTablesFromTableSegment(((CreateIndexStatement) sqlStatement).getTable(), null);
        } else if (sqlStatement instanceof DropIndexStatement && ((DropIndexStatement) sqlStatement).getSimpleTable().isPresent()) {
            extractTablesFromTableSegment(((DropIndexStatement) sqlStatement).getSimpleTable().get(), null);
        }
    }
    
    /**
     * Extract table that should be rewritten from create view statement.
     *
     * @param createViewStatement create view statement
     */
    public void extractTablesFromCreateViewStatement(final CreateViewStatement createViewStatement) {
        tableContext.add(createViewStatement.getView());
        rewriteTables.add(createViewStatement.getView());
        extractTablesFromSelect(createViewStatement.getSelect());
    }
    
    private static final class TableScope {
        
        private final TableScope parent;
        
        private final Collection<TableSegment> inheritedTables;
        
        private final int inheritedTableCount;
        
        private final Collection<TableSegment> tables = new LinkedList<>();
        
        private final Collection<IdentifierValue> aliases = new LinkedList<>();
        
        private IdentifierCasePolicy identifierCasePolicy;
        
        private boolean usingAliasScope;
        
        private TableScope(final TableScope parent, final Collection<TableSegment> inheritedTables) {
            this.parent = parent;
            this.inheritedTables = inheritedTables;
            inheritedTableCount = null == inheritedTables ? 0 : inheritedTables.size();
            identifierCasePolicy = null == parent ? null : parent.identifierCasePolicy;
            usingAliasScope = null != parent && parent.usingAliasScope;
        }
    }
}
