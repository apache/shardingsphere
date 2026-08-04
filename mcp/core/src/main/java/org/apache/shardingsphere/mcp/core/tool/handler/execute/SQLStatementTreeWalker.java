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

package org.apache.shardingsphere.mcp.core.tool.handler.execute;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.IntervalExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.KeyValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.NotExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.QuantifySubqueryExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.RowExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.TypeCastExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.UnaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ValuesExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.DatetimeProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.IntervalExpressionProjection;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.join.OuterJoinExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.merge.MergeWhenAndThenSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.multiset.MultisetExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.ExpressionPaginationValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.PaginationValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.table.MultiTableConditionalIntoSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.table.MultiTableConditionalIntoWhenThenSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.xml.XmlElementFunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.xml.XmlPiFunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.xml.XmlQueryAndExistsFunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.xml.XmlSerializeFunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.xml.XmlTableColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.xml.XmlTableFunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.xml.XmlTableOptionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.ModelColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.ModelSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OutputSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WindowItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.match.MatchAgainstExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.CollectionTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.DeleteMultiTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.FunctionTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.procedure.SQLStatementSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.function.CreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.procedure.CreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.trigger.CreateTriggerStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.AlterViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.CreateMaterializedViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.MergeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;

import java.util.function.Consumer;

@RequiredArgsConstructor
final class SQLStatementTreeWalker {
    
    private final Consumer<SQLStatement> statementConsumer;
    
    private final Consumer<ExpressionSegment> expressionConsumer;
    
    void walk(final SQLStatement sqlStatement) {
        if (null == sqlStatement) {
            return;
        }
        statementConsumer.accept(sqlStatement);
        if (sqlStatement instanceof SelectStatement) {
            walkSelect((SelectStatement) sqlStatement);
        } else if (sqlStatement instanceof InsertStatement) {
            walkInsert((InsertStatement) sqlStatement);
        } else if (sqlStatement instanceof UpdateStatement) {
            walkUpdate((UpdateStatement) sqlStatement);
        } else if (sqlStatement instanceof DeleteStatement) {
            walkDelete((DeleteStatement) sqlStatement);
        } else if (sqlStatement instanceof MergeStatement) {
            walkMerge((MergeStatement) sqlStatement);
        } else {
            walkDDL(sqlStatement);
        }
    }
    
    private void walkDDL(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof CreateTableStatement) {
            ((CreateTableStatement) sqlStatement).getSelectStatement().ifPresent(this::walk);
        } else if (sqlStatement instanceof CreateViewStatement) {
            walk(((CreateViewStatement) sqlStatement).getSelect());
        } else if (sqlStatement instanceof AlterViewStatement) {
            ((AlterViewStatement) sqlStatement).getSelect().ifPresent(this::walk);
        } else if (sqlStatement instanceof CreateMaterializedViewStatement) {
            walk(((CreateMaterializedViewStatement) sqlStatement).getSelect());
        } else if (sqlStatement instanceof CreateFunctionStatement) {
            walkRoutine(((CreateFunctionStatement) sqlStatement).getSqlStatements(), ((CreateFunctionStatement) sqlStatement).getDynamicSqlStatementExpressions());
        } else if (sqlStatement instanceof CreateProcedureStatement) {
            walkRoutine(((CreateProcedureStatement) sqlStatement).getSqlStatements(), ((CreateProcedureStatement) sqlStatement).getDynamicSqlStatementExpressions());
        } else if (sqlStatement instanceof CreateTriggerStatement) {
            walkRoutine(((CreateTriggerStatement) sqlStatement).getSqlStatements(), ((CreateTriggerStatement) sqlStatement).getDynamicSqlStatementExpressions());
        }
    }
    
    private void walkRoutine(final Iterable<SQLStatementSegment> sqlStatements, final Iterable<ExpressionSegment> expressions) {
        sqlStatements.forEach(each -> walk(each.getSqlStatement()));
        expressions.forEach(this::walkExpression);
    }
    
    private void walkSelect(final SelectStatement sqlStatement) {
        walkProjections(sqlStatement.getProjections());
        sqlStatement.getWhere().ifPresent(each -> walkExpression(each.getExpr()));
        sqlStatement.getHaving().ifPresent(each -> walkExpression(each.getExpr()));
        sqlStatement.getGroupBy().ifPresent(each -> walkOrderByItems(each.getGroupByItems()));
        sqlStatement.getOrderBy().ifPresent(each -> walkOrderByItems(each.getOrderByItems()));
        sqlStatement.getFrom().ifPresent(this::walkTable);
        sqlStatement.getHierarchicalQuery().ifPresent(each -> {
            walkExpression(each.getStartWith());
            walkExpression(each.getConnectBy());
        });
        sqlStatement.getCombine().ifPresent(this::walkCombine);
        sqlStatement.getWith().ifPresent(this::walkWith);
        sqlStatement.getLimit().ifPresent(this::walkLimit);
        sqlStatement.getWindow().ifPresent(each -> each.getItemSegments().forEach(this::walkWindow));
        sqlStatement.getModel().ifPresent(this::walkModel);
        sqlStatement.getTransformSegments().forEach(this::walkExpression);
    }
    
    private void walkInsert(final InsertStatement sqlStatement) {
        sqlStatement.getInsertSelect().ifPresent(each -> walk(each.getSelect()));
        sqlStatement.getSetAssignment().ifPresent(each -> each.getAssignments().forEach(assignment -> walkExpression(assignment.getValue())));
        sqlStatement.getOnDuplicateKeyColumns().ifPresent(each -> each.getColumns().forEach(assignment -> walkExpression(assignment.getValue())));
        sqlStatement.getValues().forEach(each -> each.getValues().forEach(this::walkExpression));
        sqlStatement.getWhere().ifPresent(each -> walkExpression(each.getExpr()));
        sqlStatement.getExec().ifPresent(each -> each.getExpressionSegments().forEach(this::walkExpression));
        sqlStatement.getRowSetFunction().ifPresent(this::walkExpression);
        sqlStatement.getMultiTableInsertInto().ifPresent(each -> each.getInsertStatements().forEach(this::walk));
        sqlStatement.getMultiTableConditionalInto().ifPresent(this::walkMultiTableConditionalInto);
        sqlStatement.getWith().ifPresent(this::walkWith);
        sqlStatement.getReturning().ifPresent(each -> walkProjections(each.getProjections()));
        sqlStatement.getOutput().ifPresent(this::walkOutput);
    }
    
    private void walkMultiTableConditionalInto(final MultiTableConditionalIntoSegment segment) {
        for (MultiTableConditionalIntoWhenThenSegment each : segment.getWhenThenSegments()) {
            walkExpression(each.getWhenSegment());
            each.getThenSegment().getInsertStatements().forEach(this::walk);
        }
        segment.getElseSegment().ifPresent(each -> each.getInsertStatements().forEach(this::walk));
    }
    
    private void walkUpdate(final UpdateStatement sqlStatement) {
        walkTable(sqlStatement.getTable());
        sqlStatement.getAssignment().ifPresent(each -> each.getAssignments().forEach(assignment -> walkExpression(assignment.getValue())));
        sqlStatement.getWhere().ifPresent(each -> walkExpression(each.getExpr()));
        sqlStatement.getDeleteWhere().ifPresent(each -> walkExpression(each.getExpr()));
        sqlStatement.getOrderBy().ifPresent(each -> walkOrderByItems(each.getOrderByItems()));
        sqlStatement.getLimit().ifPresent(this::walkLimit);
        sqlStatement.getFrom().ifPresent(this::walkTable);
        sqlStatement.getWith().ifPresent(this::walkWith);
        sqlStatement.getReturning().ifPresent(each -> walkProjections(each.getProjections()));
        sqlStatement.getOutput().ifPresent(this::walkOutput);
    }
    
    private void walkDelete(final DeleteStatement sqlStatement) {
        walkTable(sqlStatement.getTable());
        sqlStatement.getWhere().ifPresent(each -> walkExpression(each.getExpr()));
        sqlStatement.getOrderBy().ifPresent(each -> walkOrderByItems(each.getOrderByItems()));
        sqlStatement.getLimit().ifPresent(this::walkLimit);
        sqlStatement.getWith().ifPresent(this::walkWith);
        sqlStatement.getReturning().ifPresent(each -> walkProjections(each.getProjections()));
        sqlStatement.getOutput().ifPresent(this::walkOutput);
    }
    
    private void walkMerge(final MergeStatement sqlStatement) {
        walkTable(sqlStatement.getTarget());
        walkTable(sqlStatement.getSource());
        if (null != sqlStatement.getExpression()) {
            walkExpression(sqlStatement.getExpression().getExpr());
        }
        sqlStatement.getUpdate().ifPresent(this::walk);
        sqlStatement.getInsert().ifPresent(this::walk);
        for (MergeWhenAndThenSegment each : sqlStatement.getWhenAndThens()) {
            walkExpression(each.getAndExpr());
            if (null != each.getUpdate()) {
                walk(each.getUpdate());
            }
            if (null != each.getInsert()) {
                walk(each.getInsert());
            }
        }
        sqlStatement.getWith().ifPresent(this::walkWith);
        sqlStatement.getOutput().ifPresent(this::walkOutput);
    }
    
    private void walkWith(final WithSegment with) {
        with.getCommonTableExpressions().forEach(each -> walk(each.getSubquery().getSelect()));
    }
    
    private void walkCombine(final CombineSegment combine) {
        walk(combine.getLeft().getSelect());
        walk(combine.getRight().getSelect());
    }
    
    private void walkProjections(final ProjectionsSegment projections) {
        if (null == projections) {
            return;
        }
        for (ProjectionSegment each : projections.getProjections()) {
            if (each instanceof ExpressionProjectionSegment) {
                walkExpression(((ExpressionProjectionSegment) each).getExpr());
            } else if (each instanceof ExpressionSegment) {
                walkExpression((ExpressionSegment) each);
            } else if (each instanceof ColumnProjectionSegment) {
                walkExpression(((ColumnProjectionSegment) each).getColumn());
            } else if (each instanceof SubqueryProjectionSegment) {
                walk(((SubqueryProjectionSegment) each).getSubquery().getSelect());
            } else if (each instanceof DatetimeProjectionSegment) {
                walkExpression(((DatetimeProjectionSegment) each).getLeft());
                walkExpression(((DatetimeProjectionSegment) each).getRight());
            }
        }
    }
    
    private void walkOrderByItems(final Iterable<OrderByItemSegment> items) {
        for (OrderByItemSegment each : items) {
            if (each instanceof ExpressionOrderByItemSegment) {
                walkExpression(((ExpressionOrderByItemSegment) each).getExpr());
            }
        }
    }
    
    private void walkLimit(final LimitSegment limit) {
        limit.getOffset().ifPresent(this::walkPaginationValue);
        limit.getRowCount().ifPresent(this::walkPaginationValue);
    }
    
    private void walkPaginationValue(final PaginationValueSegment value) {
        if (value instanceof ExpressionPaginationValueSegment) {
            walkExpression(((ExpressionPaginationValueSegment) value).getExpression());
        }
    }
    
    private void walkOutput(final OutputSegment output) {
        walkProjections(output.getOutputColumns());
    }
    
    private void walkWindow(final WindowItemSegment window) {
        if (null != window.getPartitionListSegments()) {
            window.getPartitionListSegments().forEach(this::walkExpression);
        }
        if (null != window.getOrderBySegment()) {
            walkOrderByItems(window.getOrderBySegment().getOrderByItems());
        }
        walkExpression(window.getFrameClause());
    }
    
    private void walkModel(final ModelSegment model) {
        model.getReferenceModelSelects().forEach(each -> walk(each.getSelect()));
        model.getCellAssignmentSelects().forEach(each -> walk(each.getSelect()));
        model.getOrderBySegments().forEach(each -> walkOrderByItems(each.getOrderByItems()));
        model.getPartitionColumns().forEach(this::walkModelColumn);
        model.getDimensionColumns().forEach(this::walkModelColumn);
        model.getMeasureColumns().forEach(this::walkModelColumn);
        model.getCellAssignmentColumns().forEach(this::walkExpression);
    }
    
    private void walkModelColumn(final ModelColumnSegment modelColumn) {
        walkExpression(modelColumn.getExpression());
    }
    
    private void walkTable(final TableSegment table) {
        if (table instanceof SimpleTableSegment) {
            ((SimpleTableSegment) table).getTableSampleExpression().ifPresent(this::walkExpression);
        } else if (table instanceof FunctionTableSegment) {
            walkExpression(((FunctionTableSegment) table).getTableFunction());
        } else if (table instanceof JoinTableSegment) {
            JoinTableSegment join = (JoinTableSegment) table;
            walkTable(join.getLeft());
            walkTable(join.getRight());
            walkExpression(join.getCondition());
            join.getLeftQueryPartitionListSegments().forEach(this::walkExpression);
            join.getRightQueryPartitionListSegments().forEach(this::walkExpression);
        } else if (table instanceof SubqueryTableSegment) {
            walk(((SubqueryTableSegment) table).getSubquery().getSelect());
        } else if (table instanceof CollectionTableSegment) {
            walkExpression(((CollectionTableSegment) table).getExpressionSegment());
        } else if (table instanceof DeleteMultiTableSegment) {
            walkTable(((DeleteMultiTableSegment) table).getRelationTable());
        }
    }
    
    private void walkExpression(final ExpressionSegment expression) {
        if (null == expression) {
            return;
        }
        expressionConsumer.accept(expression);
        if (expression instanceof FunctionSegment) {
            walkFunction((FunctionSegment) expression);
        } else if (expression instanceof AggregationProjectionSegment) {
            AggregationProjectionSegment aggregation = (AggregationProjectionSegment) expression;
            aggregation.getParameters().forEach(this::walkExpression);
            aggregation.getWindow().ifPresent(this::walkWindow);
        } else if (expression instanceof BinaryOperationExpression) {
            walkExpression(((BinaryOperationExpression) expression).getLeft());
            walkExpression(((BinaryOperationExpression) expression).getRight());
        } else if (expression instanceof ColumnAssignmentSegment) {
            walkExpression(((ColumnAssignmentSegment) expression).getValue());
        } else if (expression instanceof ListExpression) {
            ((ListExpression) expression).getItems().forEach(this::walkExpression);
        } else if (expression instanceof BetweenExpression) {
            BetweenExpression between = (BetweenExpression) expression;
            walkExpression(between.getLeft());
            walkExpression(between.getBetweenExpr());
            walkExpression(between.getAndExpr());
        } else if (expression instanceof InExpression) {
            walkExpression(((InExpression) expression).getLeft());
            walkExpression(((InExpression) expression).getRight());
        } else if (expression instanceof CaseWhenExpression) {
            walkCaseWhen((CaseWhenExpression) expression);
        } else if (expression instanceof NotExpression) {
            walkExpression(((NotExpression) expression).getExpression());
        } else if (expression instanceof TypeCastExpression) {
            walkExpression(((TypeCastExpression) expression).getExpression());
        } else if (expression instanceof CollateExpression) {
            ((CollateExpression) expression).getExpr().ifPresent(this::walkExpression);
        } else if (expression instanceof DatetimeExpression) {
            walkExpression(((DatetimeExpression) expression).getLeft());
            walkExpression(((DatetimeExpression) expression).getRight());
        } else if (expression instanceof IntervalExpression) {
            walkExpression(((IntervalExpression) expression).getValue());
        } else if (expression instanceof IntervalExpressionProjection) {
            walkIntervalProjection((IntervalExpressionProjection) expression);
        } else if (expression instanceof KeyValueSegment) {
            walkExpression(((KeyValueSegment) expression).getKey());
            walkExpression(((KeyValueSegment) expression).getValue());
        } else if (expression instanceof RowExpression) {
            ((RowExpression) expression).getItems().forEach(this::walkExpression);
        } else if (expression instanceof UnaryOperationExpression) {
            walkExpression(((UnaryOperationExpression) expression).getExpression());
        } else if (expression instanceof ValuesExpression) {
            ((ValuesExpression) expression).getRowConstructorList().forEach(each -> each.getValues().forEach(this::walkExpression));
        } else if (expression instanceof MatchAgainstExpression) {
            walkMatchAgainst((MatchAgainstExpression) expression);
        } else if (expression instanceof MultisetExpression) {
            walkExpression(((MultisetExpression) expression).getLeft());
            walkExpression(((MultisetExpression) expression).getRight());
        } else if (expression instanceof OuterJoinExpression) {
            walkExpression(((OuterJoinExpression) expression).getColumnName());
        } else if (expression instanceof SubquerySegment) {
            walk(((SubquerySegment) expression).getSelect());
        } else if (expression instanceof SubqueryExpressionSegment) {
            walk(((SubqueryExpressionSegment) expression).getSubquery().getSelect());
        } else if (expression instanceof ExistsSubqueryExpression) {
            walk(((ExistsSubqueryExpression) expression).getSubquery().getSelect());
        } else if (expression instanceof QuantifySubqueryExpression) {
            walk(((QuantifySubqueryExpression) expression).getSubquery().getSelect());
        } else {
            walkXMLExpression(expression);
        }
    }
    
    private void walkFunction(final FunctionSegment function) {
        function.getParameters().forEach(this::walkExpression);
        function.getWindow().ifPresent(this::walkWindow);
    }
    
    private void walkCaseWhen(final CaseWhenExpression caseWhen) {
        walkExpression(caseWhen.getCaseExpr());
        caseWhen.getWhenExprs().forEach(this::walkExpression);
        caseWhen.getThenExprs().forEach(this::walkExpression);
        walkExpression(caseWhen.getElseExpr());
    }
    
    private void walkIntervalProjection(final IntervalExpressionProjection interval) {
        walkExpression(interval.getLeft());
        walkExpression(interval.getMinus());
        walkExpression(interval.getRight());
    }
    
    private void walkMatchAgainst(final MatchAgainstExpression match) {
        match.getColumns().forEach(this::walkExpression);
        walkExpression(match.getExpr());
    }
    
    private void walkXMLExpression(final ExpressionSegment expression) {
        if (expression instanceof XmlElementFunctionSegment) {
            XmlElementFunctionSegment xmlElement = (XmlElementFunctionSegment) expression;
            xmlElement.getXmlAttributes().forEach(this::walkExpression);
            xmlElement.getParameters().forEach(this::walkExpression);
        } else if (expression instanceof XmlPiFunctionSegment) {
            walkExpression(((XmlPiFunctionSegment) expression).getEvalNameValueExpr());
            walkExpression(((XmlPiFunctionSegment) expression).getValueExpr());
        } else if (expression instanceof XmlQueryAndExistsFunctionSegment) {
            ((XmlQueryAndExistsFunctionSegment) expression).getParameters().forEach(this::walkExpression);
        } else if (expression instanceof XmlSerializeFunctionSegment) {
            walkExpression(((XmlSerializeFunctionSegment) expression).getParameter());
        } else if (expression instanceof XmlTableFunctionSegment) {
            walkXmlTableOptions(((XmlTableFunctionSegment) expression).getXmlTableOption());
        } else if (expression instanceof XmlTableOptionsSegment) {
            walkXmlTableOptions((XmlTableOptionsSegment) expression);
        } else if (expression instanceof XmlTableColumnSegment) {
            walkExpression(((XmlTableColumnSegment) expression).getDefaultExpr());
        }
    }
    
    private void walkXmlTableOptions(final XmlTableOptionsSegment options) {
        if (null == options) {
            return;
        }
        options.getParameters().forEach(this::walkExpression);
        options.getXmlTableColumnSegments().forEach(each -> walkExpression(each.getDefaultExpr()));
    }
}
