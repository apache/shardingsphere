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

import lombok.Getter;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.routine.RoutineBodySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.routine.ValidStatementSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
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
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;

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
    
    /**
     * Extract table that should be rewritten from select statement.
     *
     * @param selectStatement select statement
     */
    public void extractTablesFromSelect(final SelectStatement selectStatement) {
        if (selectStatement.getCombine().isPresent()) {
            CombineSegment combineSegment = selectStatement.getCombine().get();
            extractTablesFromSelect(combineSegment.getLeft().getSelect());
            extractTablesFromSelect(combineSegment.getRight().getSelect());
        }
        if (selectStatement.getFrom().isPresent() && !selectStatement.getCombine().isPresent()) {
            extractTablesFromTableSegment(selectStatement.getFrom().get());
        }
        selectStatement.getWhere().ifPresent(optional -> extractTablesFromExpression(optional.getExpr()));
        if (null != selectStatement.getProjections() && !selectStatement.getCombine().isPresent()) {
            extractTablesFromProjections(selectStatement.getProjections());
        }
        selectStatement.getGroupBy().ifPresent(optional -> extractTablesFromOrderByItems(optional.getGroupByItems()));
        selectStatement.getOrderBy().ifPresent(optional -> extractTablesFromOrderByItems(optional.getOrderByItems()));
        selectStatement.getHaving().ifPresent(optional -> extractTablesFromExpression(optional.getExpr()));
        selectStatement.getWith().ifPresent(optional -> extractTablesFromCTEs(optional.getCommonTableExpressions()));
        selectStatement.getLock().ifPresent(this::extractTablesFromLock);
    }
    
    private void extractTablesFromCTEs(final Collection<CommonTableExpressionSegment> commonTableExpressionSegments) {
        for (CommonTableExpressionSegment each : commonTableExpressionSegments) {
            extractTablesFromSelect(each.getSubquery().getSelect());
        }
    }
    
    private void extractTablesFromTableSegment(final TableSegment tableSegment) {
        if (tableSegment instanceof SimpleTableSegment) {
            tableContext.add(tableSegment);
            rewriteTables.add((SimpleTableSegment) tableSegment);
        }
        if (tableSegment instanceof SubqueryTableSegment) {
            tableContext.add(tableSegment);
            TableExtractor tableExtractor = new TableExtractor();
            tableExtractor.extractTablesFromSelect(((SubqueryTableSegment) tableSegment).getSubquery().getSelect());
            rewriteTables.addAll(tableExtractor.rewriteTables);
            joinTables.addAll(tableExtractor.joinTables);
        }
        if (tableSegment instanceof JoinTableSegment) {
            joinTables.add((JoinTableSegment) tableSegment);
            extractTablesFromJoinTableSegment((JoinTableSegment) tableSegment);
        }
        if (tableSegment instanceof DeleteMultiTableSegment) {
            DeleteMultiTableSegment deleteMultiTableSegment = (DeleteMultiTableSegment) tableSegment;
            rewriteTables.addAll(deleteMultiTableSegment.getActualDeleteTables());
            extractTablesFromTableSegment(deleteMultiTableSegment.getRelationTable());
        }
        if (tableSegment instanceof FunctionTableSegment) {
            tableContext.add(tableSegment);
            extractTablesFromExpression(((FunctionTableSegment) tableSegment).getTableFunction());
        }
    }
    
    private void extractTablesFromJoinTableSegment(final JoinTableSegment tableSegment) {
        extractTablesFromTableSegment(tableSegment.getLeft());
        extractTablesFromTableSegment(tableSegment.getRight());
        extractTablesFromExpression(tableSegment.getCondition());
    }
    
    private void extractTablesFromExpression(final ExpressionSegment expressionSegment) {
        if (expressionSegment instanceof ColumnSegment) {
            extractTablesFromColumnSegments(Collections.singleton((ColumnSegment) expressionSegment));
        }
        if (expressionSegment instanceof ListExpression) {
            ((ListExpression) expressionSegment).getItems().forEach(this::extractTablesFromExpression);
        }
        if (expressionSegment instanceof ExistsSubqueryExpression) {
            extractTablesFromSelect(((ExistsSubqueryExpression) expressionSegment).getSubquery().getSelect());
        }
        if (expressionSegment instanceof BetweenExpression) {
            extractTablesFromExpression(((BetweenExpression) expressionSegment).getLeft());
            extractTablesFromExpression(((BetweenExpression) expressionSegment).getBetweenExpr());
            extractTablesFromExpression(((BetweenExpression) expressionSegment).getAndExpr());
        }
        if (expressionSegment instanceof InExpression) {
            extractTablesFromExpression(((InExpression) expressionSegment).getLeft());
            extractTablesFromExpression(((InExpression) expressionSegment).getRight());
        }
        if (expressionSegment instanceof SubqueryExpressionSegment) {
            extractTablesFromSelect(((SubqueryExpressionSegment) expressionSegment).getSubquery().getSelect());
        }
        if (expressionSegment instanceof SubquerySegment) {
            extractTablesFromSelect(((SubquerySegment) expressionSegment).getSelect());
        }
        if (expressionSegment instanceof BinaryOperationExpression) {
            extractTablesFromExpression(((BinaryOperationExpression) expressionSegment).getLeft());
            extractTablesFromExpression(((BinaryOperationExpression) expressionSegment).getRight());
        }
        if (expressionSegment instanceof MatchAgainstExpression) {
            ((MatchAgainstExpression) expressionSegment).getColumns().forEach(this::extractTablesFromExpression);
            extractTablesFromExpression(((MatchAgainstExpression) expressionSegment).getExpr());
        }
        if (expressionSegment instanceof FunctionSegment) {
            ((FunctionSegment) expressionSegment).getParameters().forEach(this::extractTablesFromExpression);
        }
        if (expressionSegment instanceof CaseWhenExpression) {
            extractTablesFromCaseWhenExpression((CaseWhenExpression) expressionSegment);
        }
        if (expressionSegment instanceof CollateExpression) {
            extractTablesFromExpression(((CollateExpression) expressionSegment).getCollateName());
        }
        if (expressionSegment instanceof DatetimeExpression) {
            extractTablesFromExpression(((DatetimeExpression) expressionSegment).getLeft());
            extractTablesFromExpression(((DatetimeExpression) expressionSegment).getRight());
        }
        if (expressionSegment instanceof NotExpression) {
            extractTablesFromExpression(((NotExpression) expressionSegment).getExpression());
        }
        if (expressionSegment instanceof TypeCastExpression) {
            extractTablesFromExpression(((TypeCastExpression) expressionSegment).getExpression());
        }
        if (expressionSegment instanceof QuantifySubqueryExpression) {
            extractTablesFromExpression(((QuantifySubqueryExpression) expressionSegment).getSubquery());
        }
    }
    
    private void extractTablesFromCaseWhenExpression(final CaseWhenExpression expressionSegment) {
        extractTablesFromExpression(expressionSegment.getCaseExpr());
        expressionSegment.getWhenExprs().forEach(this::extractTablesFromExpression);
        expressionSegment.getThenExprs().forEach(this::extractTablesFromExpression);
        extractTablesFromExpression(expressionSegment.getElseExpr());
    }
    
    private void extractTablesFromProjections(final ProjectionsSegment projections) {
        for (ProjectionSegment each : projections.getProjections()) {
            if (each instanceof SubqueryProjectionSegment) {
                extractTablesFromSelect(((SubqueryProjectionSegment) each).getSubquery().getSelect());
            } else if (each instanceof OwnerAvailable) {
                if (((OwnerAvailable) each).getOwner().isPresent() && needRewrite(((OwnerAvailable) each).getOwner().get())) {
                    OwnerSegment ownerSegment = ((OwnerAvailable) each).getOwner().get();
                    rewriteTables.add(createSimpleTableSegment(ownerSegment));
                }
            } else if (each instanceof ColumnProjectionSegment) {
                if (((ColumnProjectionSegment) each).getColumn().getOwner().isPresent() && needRewrite(((ColumnProjectionSegment) each).getColumn().getOwner().get())) {
                    OwnerSegment ownerSegment = ((ColumnProjectionSegment) each).getColumn().getOwner().get();
                    rewriteTables.add(createSimpleTableSegment(ownerSegment));
                }
            } else if (each instanceof AggregationProjectionSegment) {
                ((AggregationProjectionSegment) each).getParameters().forEach(this::extractTablesFromExpression);
            } else if (each instanceof ExpressionProjectionSegment) {
                extractTablesFromExpression(((ExpressionProjectionSegment) each).getExpr());
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
    
    private void extractTablesFromOrderByItems(final Collection<OrderByItemSegment> orderByItems) {
        for (OrderByItemSegment each : orderByItems) {
            if (each instanceof ColumnOrderByItemSegment) {
                Optional<OwnerSegment> owner = ((ColumnOrderByItemSegment) each).getColumn().getOwner();
                if (owner.isPresent() && needRewrite(owner.get())) {
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
        extractTablesFromTableSegment(deleteStatement.getTable());
        if (deleteStatement.getWhere().isPresent()) {
            extractTablesFromExpression(deleteStatement.getWhere().get().getExpr());
        }
    }
    
    /**
     * Extract table that should be rewritten from insert statement.
     *
     * @param insertStatement insert statement
     */
    public void extractTablesFromInsert(final InsertStatement insertStatement) {
        insertStatement.getTable().ifPresent(this::extractTablesFromTableSegment);
        if (!insertStatement.getColumns().isEmpty()) {
            for (ColumnSegment each : insertStatement.getColumns()) {
                extractTablesFromExpression(each);
            }
        }
        insertStatement.getOnDuplicateKeyColumns().ifPresent(optional -> extractTablesFromAssignmentItems(optional.getColumns()));
        if (insertStatement.getInsertSelect().isPresent()) {
            extractTablesFromSelect(insertStatement.getInsertSelect().get().getSelect());
        }
    }
    
    private void extractTablesFromAssignmentItems(final Collection<ColumnAssignmentSegment> assignmentItems) {
        assignmentItems.forEach(each -> extractTablesFromColumnSegments(each.getColumns()));
    }
    
    private void extractTablesFromColumnSegments(final Collection<ColumnSegment> columnSegments) {
        for (ColumnSegment each : columnSegments) {
            if (each.getOwner().isPresent() && needRewrite(each.getOwner().get())) {
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
        extractTablesFromTableSegment(updateStatement.getTable());
        updateStatement.getSetAssignment().getAssignments().forEach(each -> extractTablesFromExpression(each.getColumns().get(0)));
        if (updateStatement.getWhere().isPresent()) {
            extractTablesFromExpression(updateStatement.getWhere().get().getExpr());
        }
    }
    
    /**
     * Check if the table needs to be overwritten.
     *
     * @param owner owner
     * @return boolean
     */
    public boolean needRewrite(final OwnerSegment owner) {
        for (TableSegment each : tableContext) {
            if (owner.getIdentifier().getValue().equalsIgnoreCase(each.getAliasName().orElse(null))) {
                return false;
            }
        }
        return true;
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
            extractTablesFromTableSegment(((CreateTableStatement) sqlStatement).getTable());
        } else if (sqlStatement instanceof AlterTableStatement) {
            extractTablesFromTableSegment(((AlterTableStatement) sqlStatement).getTable());
        } else if (sqlStatement instanceof CommentStatement) {
            extractTablesFromTableSegment(((CommentStatement) sqlStatement).getTable());
        } else if (sqlStatement instanceof CreateViewStatement) {
            extractTablesFromCreateViewStatement((CreateViewStatement) sqlStatement);
        } else if (sqlStatement instanceof CreateIndexStatement) {
            extractTablesFromTableSegment(((CreateIndexStatement) sqlStatement).getTable());
        } else if (sqlStatement instanceof DropIndexStatement) {
            ((DropIndexStatement) sqlStatement).getSimpleTable().ifPresent(this::extractTablesFromTableSegment);
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
}
