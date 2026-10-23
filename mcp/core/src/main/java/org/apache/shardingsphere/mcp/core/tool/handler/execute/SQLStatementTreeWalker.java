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
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPUnsupportedSQLStatementException;
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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonExpressionSegment;
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
        if (sqlStatement instanceof SelectStatement select) {
            walkSelect(select);
        } else if (sqlStatement instanceof InsertStatement insert) {
            walkInsert(insert);
        } else if (sqlStatement instanceof UpdateStatement update) {
            walkUpdate(update);
        } else if (sqlStatement instanceof DeleteStatement delete) {
            walkDelete(delete);
        } else if (sqlStatement instanceof MergeStatement merge) {
            walkMerge(merge);
        } else {
            walkDDL(sqlStatement);
        }
    }
    
    private void walkDDL(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof CreateTableStatement createTableStatement) {
            createTableStatement.getSelectStatement().ifPresent(this::walk);
        } else if (sqlStatement instanceof CreateViewStatement createViewStatement) {
            walk(createViewStatement.getSelect());
        } else if (sqlStatement instanceof AlterViewStatement alterViewStatement) {
            alterViewStatement.getSelect().ifPresent(this::walk);
        } else if (sqlStatement instanceof CreateMaterializedViewStatement createMaterializedViewStatement) {
            walk(createMaterializedViewStatement.getSelect());
        } else if (sqlStatement instanceof CreateFunctionStatement createFunctionStatement) {
            walkRoutine(createFunctionStatement.getSqlStatements(), createFunctionStatement.getDynamicSqlStatementExpressions());
        } else if (sqlStatement instanceof CreateProcedureStatement createProcedureStatement) {
            walkRoutine(createProcedureStatement.getSqlStatements(), createProcedureStatement.getDynamicSqlStatementExpressions());
        } else if (sqlStatement instanceof CreateTriggerStatement createTriggerStatement) {
            walkRoutine(createTriggerStatement.getSqlStatements(), createTriggerStatement.getDynamicSqlStatementExpressions());
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
        with.getCommonTableExpressions().forEach(each -> {
            ShardingSpherePreconditions.checkState(null == each.getDataModification(), MCPUnsupportedSQLStatementException::new);
            walk(each.getSubquery().getSelect());
        });
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
            if (each instanceof ExpressionProjectionSegment expressionProjection) {
                walkExpression(expressionProjection.getExpr());
            } else if (each instanceof ExpressionSegment expression) {
                walkExpression(expression);
            } else if (each instanceof ColumnProjectionSegment columnProjection) {
                walkExpression(columnProjection.getColumn());
            } else if (each instanceof SubqueryProjectionSegment subqueryProjection) {
                walk(subqueryProjection.getSubquery().getSelect());
            } else if (each instanceof DatetimeProjectionSegment datetimeProjection) {
                walkExpression(datetimeProjection.getLeft());
                walkExpression(datetimeProjection.getRight());
            }
        }
    }
    
    private void walkOrderByItems(final Iterable<OrderByItemSegment> items) {
        for (OrderByItemSegment each : items) {
            if (each instanceof ExpressionOrderByItemSegment expressionOrderByItem) {
                walkExpression(expressionOrderByItem.getExpr());
            }
        }
    }
    
    private void walkLimit(final LimitSegment limit) {
        limit.getOffset().ifPresent(this::walkPaginationValue);
        limit.getRowCount().ifPresent(this::walkPaginationValue);
    }
    
    private void walkPaginationValue(final PaginationValueSegment value) {
        if (value instanceof ExpressionPaginationValueSegment expressionPaginationValue) {
            walkExpression(expressionPaginationValue.getExpression());
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
        if (table instanceof SimpleTableSegment simpleTable) {
            simpleTable.getTableSampleExpression().ifPresent(this::walkExpression);
        } else if (table instanceof FunctionTableSegment functionTable) {
            walkExpression(functionTable.getTableFunction());
        } else if (table instanceof JoinTableSegment join) {
            walkTable(join.getLeft());
            walkTable(join.getRight());
            walkExpression(join.getCondition());
            join.getLeftQueryPartitionListSegments().forEach(this::walkExpression);
            join.getRightQueryPartitionListSegments().forEach(this::walkExpression);
        } else if (table instanceof SubqueryTableSegment subqueryTable) {
            walk(subqueryTable.getSubquery().getSelect());
        } else if (table instanceof CollectionTableSegment collectionTable) {
            walkExpression(collectionTable.getExpressionSegment());
        } else if (table instanceof DeleteMultiTableSegment deleteMultiTable) {
            walkTable(deleteMultiTable.getRelationTable());
        }
    }
    
    private void walkExpression(final ExpressionSegment expression) {
        if (null == expression) {
            return;
        }
        expressionConsumer.accept(expression);
        switch (expression) {
            case FunctionSegment function -> walkFunction(function);
            case CommonExpressionSegment common -> common.getChildren().forEach(this::walkExpression);
            case AggregationProjectionSegment aggregation -> {
                aggregation.getParameters().forEach(this::walkExpression);
                aggregation.getWindow().ifPresent(this::walkWindow);
            }
            case BinaryOperationExpression binary -> {
                walkExpression(binary.getLeft());
                walkExpression(binary.getRight());
            }
            case ColumnAssignmentSegment assignment -> walkExpression(assignment.getValue());
            case ListExpression list -> list.getItems().forEach(this::walkExpression);
            case BetweenExpression between -> {
                walkExpression(between.getLeft());
                walkExpression(between.getBetweenExpr());
                walkExpression(between.getAndExpr());
            }
            case InExpression in -> {
                walkExpression(in.getLeft());
                walkExpression(in.getRight());
            }
            case CaseWhenExpression caseWhen -> walkCaseWhen(caseWhen);
            case NotExpression not -> walkExpression(not.getExpression());
            case TypeCastExpression typeCast -> walkExpression(typeCast.getExpression());
            case CollateExpression collate -> collate.getExpr().ifPresent(this::walkExpression);
            case DatetimeExpression datetime -> {
                walkExpression(datetime.getLeft());
                walkExpression(datetime.getRight());
            }
            case IntervalExpression interval -> walkExpression(interval.getValue());
            case IntervalExpressionProjection intervalProjection -> walkIntervalProjection(intervalProjection);
            case KeyValueSegment keyValue -> {
                walkExpression(keyValue.getKey());
                walkExpression(keyValue.getValue());
            }
            case RowExpression row -> row.getItems().forEach(this::walkExpression);
            case UnaryOperationExpression unary -> walkExpression(unary.getExpression());
            case ValuesExpression values -> values.getRowConstructorList().forEach(each -> each.getValues().forEach(this::walkExpression));
            case MatchAgainstExpression match -> walkMatchAgainst(match);
            case MultisetExpression multiset -> {
                walkExpression(multiset.getLeft());
                walkExpression(multiset.getRight());
            }
            case OuterJoinExpression outerJoin -> walkExpression(outerJoin.getColumnName());
            case SubquerySegment subquery -> walk(subquery.getSelect());
            case SubqueryExpressionSegment subqueryExpression -> walk(subqueryExpression.getSubquery().getSelect());
            case ExistsSubqueryExpression exists -> walk(exists.getSubquery().getSelect());
            case QuantifySubqueryExpression quantify -> walk(quantify.getSubquery().getSelect());
            default -> walkXMLExpression(expression);
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
        if (expression instanceof XmlElementFunctionSegment xmlElement) {
            xmlElement.getXmlAttributes().forEach(this::walkExpression);
            xmlElement.getParameters().forEach(this::walkExpression);
        } else if (expression instanceof XmlPiFunctionSegment xmlPi) {
            walkExpression(xmlPi.getEvalNameValueExpr());
            walkExpression(xmlPi.getValueExpr());
        } else if (expression instanceof XmlQueryAndExistsFunctionSegment xmlQueryAndExists) {
            xmlQueryAndExists.getParameters().forEach(this::walkExpression);
        } else if (expression instanceof XmlSerializeFunctionSegment xmlSerialize) {
            walkExpression(xmlSerialize.getParameter());
        } else if (expression instanceof XmlTableFunctionSegment xmlTable) {
            walkXmlTableOptions(xmlTable.getXmlTableOption());
        } else if (expression instanceof XmlTableOptionsSegment xmlTableOptions) {
            walkXmlTableOptions(xmlTableOptions);
        } else if (expression instanceof XmlTableColumnSegment xmlTableColumn) {
            walkExpression(xmlTableColumn.getDefaultExpr());
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
