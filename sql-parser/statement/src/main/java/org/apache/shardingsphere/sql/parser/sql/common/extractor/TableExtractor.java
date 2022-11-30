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

package org.apache.shardingsphere.sql.parser.sql.common.extractor;

import lombok.Getter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.routine.RoutineBodySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.routine.ValidStatementSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExistsSubqueryExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.LockSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerAvailable;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.DeleteMultiTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl.CreateTableStatementHandler;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.InsertStatementHandler;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.SelectStatementHandler;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

@Getter
public final class TableExtractor {
    
    private final Collection<SimpleTableSegment> rewriteTables = new LinkedList<>();
    
    private final Collection<TableSegment> tableContext = new LinkedList<>();
    
    /**
     * Extract table that should be rewritten from select statement.
     *
     * @param selectStatement select statement
     */
    public void extractTablesFromSelect(final SelectStatement selectStatement) {
        if (null != selectStatement.getFrom()) {
            extractTablesFromTableSegment(selectStatement.getFrom());
        }
        if (selectStatement.getWhere().isPresent()) {
            extractTablesFromExpression(selectStatement.getWhere().get().getExpr());
        }
        if (null != selectStatement.getProjections()) {
            extractTablesFromProjections(selectStatement.getProjections());
        }
        if (selectStatement.getGroupBy().isPresent()) {
            extractTablesFromOrderByItems(selectStatement.getGroupBy().get().getGroupByItems());
        }
        if (selectStatement.getOrderBy().isPresent()) {
            extractTablesFromOrderByItems(selectStatement.getOrderBy().get().getOrderByItems());
        }
        Optional<LockSegment> lockSegment = SelectStatementHandler.getLockSegment(selectStatement);
        lockSegment.ifPresent(this::extractTablesFromLock);
        if (selectStatement.getCombine().isPresent()) {
            extractTablesFromSelect(selectStatement.getCombine().get().getSelectStatement());
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
        }
        if (tableSegment instanceof JoinTableSegment) {
            extractTablesFromJoinTableSegment((JoinTableSegment) tableSegment);
        }
        if (tableSegment instanceof DeleteMultiTableSegment) {
            DeleteMultiTableSegment deleteMultiTableSegment = (DeleteMultiTableSegment) tableSegment;
            rewriteTables.addAll(deleteMultiTableSegment.getActualDeleteTables());
            extractTablesFromTableSegment(deleteMultiTableSegment.getRelationTable());
        }
    }
    
    private void extractTablesFromJoinTableSegment(final JoinTableSegment tableSegment) {
        extractTablesFromTableSegment(tableSegment.getLeft());
        extractTablesFromTableSegment(tableSegment.getRight());
        extractTablesFromExpression(tableSegment.getCondition());
    }
    
    private void extractTablesFromExpression(final ExpressionSegment expressionSegment) {
        if (expressionSegment instanceof ColumnSegment) {
            if (((ColumnSegment) expressionSegment).getOwner().isPresent() && needRewrite(((ColumnSegment) expressionSegment).getOwner().get())) {
                OwnerSegment ownerSegment = ((ColumnSegment) expressionSegment).getOwner().get();
                rewriteTables.add(new SimpleTableSegment(new TableNameSegment(ownerSegment.getStartIndex(), ownerSegment.getStopIndex(), ownerSegment.getIdentifier())));
            }
        }
        if (expressionSegment instanceof ListExpression) {
            for (ExpressionSegment each : ((ListExpression) expressionSegment).getItems()) {
                extractTablesFromExpression(each);
            }
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
        if (expressionSegment instanceof BinaryOperationExpression) {
            extractTablesFromExpression(((BinaryOperationExpression) expressionSegment).getLeft());
            extractTablesFromExpression(((BinaryOperationExpression) expressionSegment).getRight());
        }
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
            }
        }
    }
    
    private SimpleTableSegment createSimpleTableSegment(final OwnerSegment ownerSegment) {
        SimpleTableSegment result = new SimpleTableSegment(new TableNameSegment(ownerSegment.getStartIndex(), ownerSegment.getStopIndex(), ownerSegment.getIdentifier()));
        ownerSegment.getOwner().ifPresent(result::setOwner);
        return result;
    }
    
    private void extractTablesFromOrderByItems(final Collection<OrderByItemSegment> orderByItems) {
        for (OrderByItemSegment each : orderByItems) {
            if (each instanceof ColumnOrderByItemSegment) {
                Optional<OwnerSegment> owner = ((ColumnOrderByItemSegment) each).getColumn().getOwner();
                if (owner.isPresent() && needRewrite(owner.get())) {
                    rewriteTables.add(new SimpleTableSegment(new TableNameSegment(owner.get().getStartIndex(), owner.get().getStopIndex(), owner.get().getIdentifier())));
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
        if (null != insertStatement.getTable()) {
            extractTablesFromTableSegment(insertStatement.getTable());
        }
        if (!insertStatement.getColumns().isEmpty()) {
            for (ColumnSegment each : insertStatement.getColumns()) {
                extractTablesFromExpression(each);
            }
        }
        InsertStatementHandler.getOnDuplicateKeyColumnsSegment(insertStatement).ifPresent(each -> extractTablesFromAssignmentItems(each.getColumns()));
        if (insertStatement.getInsertSelect().isPresent()) {
            extractTablesFromSelect(insertStatement.getInsertSelect().get().getSelect());
        }
    }
    
    private void extractTablesFromAssignmentItems(final Collection<AssignmentSegment> assignmentItems) {
        assignmentItems.forEach(each -> extractTablesFromColumnSegments(each.getColumns()));
    }
    
    private void extractTablesFromColumnSegments(final Collection<ColumnSegment> columnSegments) {
        columnSegments.forEach(each -> {
            if (each.getOwner().isPresent() && needRewrite(each.getOwner().get())) {
                OwnerSegment ownerSegment = each.getOwner().get();
                rewriteTables.add(new SimpleTableSegment(new TableNameSegment(ownerSegment.getStartIndex(), ownerSegment.getStopIndex(), ownerSegment.getIdentifier())));
            }
        });
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
            if (owner.getIdentifier().getValue().equalsIgnoreCase(each.getAlias().orElse(null))) {
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
            if (createTable.isPresent() && !CreateTableStatementHandler.ifNotExists(createTable.get())) {
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
