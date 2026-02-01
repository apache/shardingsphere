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

package org.apache.shardingsphere.sql.parser.engine.oracle.visitor.statement.type;

import org.antlr.v4.runtime.misc.Interval;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DMLStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AliasContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AssignmentValueContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AssignmentValuesContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CellAssignmentContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CollectionExprContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ColumnNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ColumnNamesContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ConditionalInsertClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ConditionalInsertElsePartContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ConditionalInsertWhenPartContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ContainersClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CrossOuterApplyClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DeleteContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DeleteSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DeleteWhereClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DimensionColumnContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DmlSubqueryClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DmlTableClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DuplicateSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ExecuteContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ExprContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ExpressionListContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ForUpdateClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ForUpdateClauseListContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ForUpdateClauseOptionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.FromClauseListContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.FromClauseOptionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.GroupByClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.GroupByItemContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.GroupingExprListContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.GroupingSetsClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.HavingClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.InnerCrossJoinClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.InsertContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.InsertIntoClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.InsertMultiTableContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.InsertSingleTableContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.InsertValuesClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.IntoClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.JoinClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.MergeAssignmentContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.MergeAssignmentValueContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.MergeColumnValueContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.MergeContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.MergeInsertClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.MergeInsertColumnContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.MergeSetAssignmentsClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.MergeUpdateClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ModelClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.MultiColumnForLoopContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.MultiTableElementContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.OrderByClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.OuterJoinClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ParenthesisSelectSubqueryContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.PivotClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.QueryBlockContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.QueryNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.QueryTableExprClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.QueryTableExprContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ReferenceModelContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.RollupCubeClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.SelectContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.SelectFromClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.SelectIntoStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.SelectJoinOptionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.SelectJoinSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.SelectListContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.SelectProjectionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.SelectProjectionExprClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.SelectSubqueryContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.SelectTableReferenceContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ShardsClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.SingleColumnForLoopContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.SubqueryContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.SubqueryFactoringClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.TableCollectionExprContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.TableNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.UnpivotClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.UpdateContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.UpdateSetClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.UpdateSetColumnClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.UpdateSetValueClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.UpdateSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.UsingClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.WhereClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.WithClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.HierarchicalQueryClauseContext;
import org.apache.shardingsphere.sql.parser.engine.oracle.visitor.statement.OracleStatementVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.enums.CombineType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.JoinType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.VariableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.combine.CombineSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.datetime.DatetimeExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.CaseWhenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.CollateExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionWithParamsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.ComplexExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.SimpleExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.DatetimeProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.IntervalExpressionProjection;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.multiset.MultisetExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.HavingSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.HierarchicalQuerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.LockSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.table.MultiTableConditionalIntoElseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.table.MultiTableConditionalIntoSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.table.MultiTableConditionalIntoThenSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.table.MultiTableConditionalIntoWhenThenSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.table.MultiTableInsertIntoSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.table.MultiTableInsertType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.xml.XmlElementFunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.xml.XmlPiFunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.xml.XmlQueryAndExistsFunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.xml.XmlSerializeFunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.xml.XmlTableFunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasAvailable;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.ModelSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.ParameterMarkerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.PivotSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.CollectionTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.FunctionTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.MergeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.util.SQLUtils;
import org.apache.shardingsphere.sql.parser.statement.core.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.BooleanLiteralValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DML statement visitor for Oracle.
 */
public final class OracleDMLStatementVisitor extends OracleStatementVisitor implements DMLStatementVisitor {
    
    public OracleDMLStatementVisitor(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    @Override
    public ASTNode visitUpdate(final UpdateContext ctx) {
        UpdateStatement result = new UpdateStatement(getDatabaseType());
        result.setTable((TableSegment) visit(ctx.updateSpecification()));
        if (null != ctx.alias()) {
            result.getTable().setAlias((AliasSegment) visit(ctx.alias()));
        }
        result.setSetAssignment((SetAssignmentSegment) visit(ctx.updateSetClause()));
        if (null != ctx.whereClause()) {
            result.setWhere((WhereSegment) visit(ctx.whereClause()));
        }
        result.addParameterMarkers(ctx.getParent() instanceof ExecuteContext ? getGlobalParameterMarkerSegments() : popAllStatementParameterMarkerSegments());
        result.getVariableNames().addAll(getVariableNames());
        return result;
    }
    
    @Override
    public ASTNode visitUpdateSpecification(final UpdateSpecificationContext ctx) {
        if (null != ctx.dmlTableExprClause().dmlTableClause()) {
            return visit(ctx.dmlTableExprClause().dmlTableClause());
        }
        if (null != ctx.dmlTableExprClause().dmlSubqueryClause()) {
            SubquerySegment subquerySegment = (SubquerySegment) visit(ctx.dmlTableExprClause().dmlSubqueryClause());
            return new SubqueryTableSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), subquerySegment);
        }
        SubquerySegment subquerySegment = (SubquerySegment) visit(ctx.dmlTableExprClause().tableCollectionExpr());
        return new SubqueryTableSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), subquerySegment);
    }
    
    @Override
    public ASTNode visitUpdateSetClause(final UpdateSetClauseContext ctx) {
        Collection<ColumnAssignmentSegment> assignments = new LinkedList<>();
        if (null != ctx.updateSetColumnList()) {
            for (UpdateSetColumnClauseContext each : ctx.updateSetColumnList().updateSetColumnClause()) {
                assignments.add((ColumnAssignmentSegment) visit(each));
            }
        } else {
            assignments.add((ColumnAssignmentSegment) visit(ctx.updateSetValueClause()));
        }
        return new SetAssignmentSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), assignments);
    }
    
    @Override
    public ASTNode visitUpdateSetColumnClause(final UpdateSetColumnClauseContext ctx) {
        return 1 == ctx.columnName().size() ? createAssignmentSegmentFromSingleColumnAssignment(ctx) : createAssignmentSegmentFromMultiColumnAssignment(ctx);
    }
    
    private ColumnAssignmentSegment createAssignmentSegmentFromSingleColumnAssignment(final UpdateSetColumnClauseContext ctx) {
        ColumnSegment column = (ColumnSegment) visitColumnName(ctx.columnName(0));
        List<ColumnSegment> columnSegments = new LinkedList<>();
        columnSegments.add(column);
        if (null != ctx.expr()) {
            ExpressionSegment value = (ExpressionSegment) visit(ctx.expr());
            return new ColumnAssignmentSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), columnSegments, value);
        }
        if (null != ctx.selectSubquery()) {
            SubquerySegment subquerySegment = new SubquerySegment(ctx.selectSubquery().start.getStartIndex(), ctx.selectSubquery().stop.getStopIndex(),
                    (SelectStatement) visit(ctx.selectSubquery()), getOriginalText(ctx.selectSubquery()));
            SubqueryExpressionSegment value = new SubqueryExpressionSegment(subquerySegment);
            ColumnAssignmentSegment result = new ColumnAssignmentSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), columnSegments, value);
            result.getColumns().add(column);
            return result;
        }
        CommonExpressionSegment value = new CommonExpressionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.DEFAULT().getText());
        ColumnAssignmentSegment result = new ColumnAssignmentSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), columnSegments, value);
        result.getColumns().add(column);
        return result;
    }
    
    private ColumnAssignmentSegment createAssignmentSegmentFromMultiColumnAssignment(final UpdateSetColumnClauseContext ctx) {
        List<ColumnSegment> columnSegments = new LinkedList<>();
        for (ColumnNameContext each : ctx.columnName()) {
            columnSegments.add((ColumnSegment) visit(each));
        }
        SubquerySegment subquerySegment = new SubquerySegment(ctx.selectSubquery().start.getStartIndex(), ctx.selectSubquery().stop.getStopIndex(),
                (SelectStatement) visit(ctx.selectSubquery()), getOriginalText(ctx.selectSubquery()));
        SubqueryExpressionSegment value = new SubqueryExpressionSegment(subquerySegment);
        return new ColumnAssignmentSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), columnSegments, value);
    }
    
    @Override
    public ASTNode visitUpdateSetValueClause(final UpdateSetValueClauseContext ctx) {
        ColumnSegment column = (ColumnSegment) visit(ctx.columnName());
        List<ColumnSegment> columnSegments = new LinkedList<>();
        columnSegments.add(column);
        if (null != ctx.expr()) {
            ExpressionSegment value = (ExpressionSegment) visit(ctx.expr());
            ColumnAssignmentSegment result = new ColumnAssignmentSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), columnSegments, value);
            result.getColumns().add(column);
            return result;
        } else {
            SubquerySegment subquerySegment = new SubquerySegment(ctx.selectSubquery().start.getStartIndex(), ctx.selectSubquery().stop.getStopIndex(),
                    (SelectStatement) visit(ctx.selectSubquery()), getOriginalText(ctx.selectSubquery()));
            SubqueryExpressionSegment value = new SubqueryExpressionSegment(subquerySegment);
            ColumnAssignmentSegment result = new ColumnAssignmentSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), columnSegments, value);
            result.getColumns().add(column);
            return result;
        }
    }
    
    @Override
    public ASTNode visitInsert(final InsertContext ctx) {
        // TODO :FIXME, since there is no segment for insertValuesClause, insertStatement is created by sub rule.
        InsertStatement result = (InsertStatement) (null == ctx.insertSingleTable() ? visit(ctx.insertMultiTable()) : visit(ctx.insertSingleTable()));
        result.addParameterMarkers(ctx.getParent() instanceof ExecuteContext ? getGlobalParameterMarkerSegments() : popAllStatementParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitInsertSingleTable(final InsertSingleTableContext ctx) {
        InsertStatement result = (InsertStatement) visit(ctx.insertIntoClause());
        if (null != ctx.insertValuesClause()) {
            result.getValues().addAll(createInsertValuesSegments(ctx.insertValuesClause().assignmentValues()));
        }
        if (null != ctx.selectSubquery()) {
            SelectStatement subquery = (SelectStatement) visit(ctx.selectSubquery());
            SubquerySegment subquerySegment = new SubquerySegment(ctx.selectSubquery().start.getStartIndex(), ctx.selectSubquery().stop.getStopIndex(), subquery,
                    getOriginalText(ctx.selectSubquery()));
            result.setInsertSelect(subquerySegment);
        }
        result.getVariableNames().addAll(getVariableNames());
        return result;
    }
    
    private Collection<InsertValuesSegment> createInsertValuesSegments(final AssignmentValuesContext ctx) {
        Collection<InsertValuesSegment> result = new LinkedList<>();
        result.add((InsertValuesSegment) visit(ctx));
        return result;
    }
    
    @Override
    public ASTNode visitInsertMultiTable(final InsertMultiTableContext ctx) {
        InsertStatement result = new InsertStatement(getDatabaseType());
        result.setMultiTableInsertType(null != ctx.conditionalInsertClause() && null != ctx.conditionalInsertClause().FIRST() ? MultiTableInsertType.FIRST : MultiTableInsertType.ALL);
        List<MultiTableElementContext> multiTableElementContexts = ctx.multiTableElement();
        if (null != multiTableElementContexts && !multiTableElementContexts.isEmpty()) {
            MultiTableInsertIntoSegment multiTableInsertIntoSegment = new MultiTableInsertIntoSegment(
                    multiTableElementContexts.get(0).getStart().getStartIndex(), multiTableElementContexts.get(multiTableElementContexts.size() - 1).getStop().getStopIndex());
            multiTableInsertIntoSegment.getInsertStatements().addAll(createInsertIntoSegments(multiTableElementContexts));
            result.setMultiTableInsertInto(multiTableInsertIntoSegment);
        } else {
            result.setMultiTableConditionalInto((MultiTableConditionalIntoSegment) visit(ctx.conditionalInsertClause()));
        }
        result.setInsertSelect(new SubquerySegment(ctx.selectSubquery().start.getStartIndex(), ctx.selectSubquery().stop.getStopIndex(), (SelectStatement) visit(ctx.selectSubquery()),
                getOriginalText(ctx.selectSubquery())));
        result.getVariableNames().addAll(getVariableNames());
        return result;
    }
    
    private Collection<InsertStatement> createInsertIntoSegments(final List<MultiTableElementContext> ctx) {
        Collection<InsertStatement> result = new LinkedList<>();
        Collection<ParameterMarkerSegment> addedSegments = new HashSet<>();
        for (MultiTableElementContext each : ctx) {
            InsertStatement insertStatement = (InsertStatement) visit(each);
            addParameterMarkerSegments(addedSegments, insertStatement);
            result.add(insertStatement);
        }
        return result;
    }
    
    private void addParameterMarkerSegments(final Collection<ParameterMarkerSegment> addedSegments, final InsertStatement insertStatement) {
        for (ParameterMarkerSegment parameterMarkerSegment : popAllStatementParameterMarkerSegments()) {
            if (addedSegments.add(parameterMarkerSegment)) {
                insertStatement.addParameterMarkers(Collections.singletonList(parameterMarkerSegment));
            }
        }
    }
    
    @Override
    public ASTNode visitInsertValuesClause(final InsertValuesClauseContext ctx) {
        InsertStatement result = new InsertStatement(getDatabaseType());
        result.getValues().addAll(createInsertValuesSegments(ctx.assignmentValues()));
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitInsertIntoClause(final InsertIntoClauseContext ctx) {
        InsertStatement result = new InsertStatement(getDatabaseType());
        if (null != ctx.dmlTableExprClause().dmlTableClause()) {
            SimpleTableSegment simpleTableSegment = (SimpleTableSegment) visit(ctx.dmlTableExprClause().dmlTableClause());
            if (null != ctx.dmlTableExprClause().alias()) {
                simpleTableSegment.setAlias((AliasSegment) visit(ctx.dmlTableExprClause().alias()));
            }
            result.setTable(simpleTableSegment);
        } else if (null != ctx.dmlTableExprClause().dmlSubqueryClause()) {
            result.setInsertSelect((SubquerySegment) visit(ctx.dmlTableExprClause().dmlSubqueryClause()));
        } else {
            result.setInsertSelect((SubquerySegment) visit(ctx.dmlTableExprClause().tableCollectionExpr()));
        }
        if (null != ctx.columnNames()) {
            ColumnNamesContext columnNames = ctx.columnNames();
            CollectionValue<ColumnSegment> columnSegments = (CollectionValue<ColumnSegment>) visit(columnNames);
            result.setInsertColumns(new InsertColumnsSegment(columnNames.start.getStartIndex(), columnNames.stop.getStopIndex(), columnSegments.getValue()));
        } else {
            result.setInsertColumns(new InsertColumnsSegment(ctx.stop.getStopIndex() + 1, ctx.stop.getStopIndex() + 1, Collections.emptyList()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitDelete(final DeleteContext ctx) {
        DeleteStatement result = new DeleteStatement(getDatabaseType());
        result.setTable((TableSegment) visit(ctx.deleteSpecification()));
        if (null != ctx.alias()) {
            result.getTable().setAlias((AliasSegment) visit(ctx.alias()));
        }
        if (null != ctx.whereClause()) {
            result.setWhere((WhereSegment) visit(ctx.whereClause()));
        }
        result.addParameterMarkers(ctx.getParent() instanceof ExecuteContext ? getGlobalParameterMarkerSegments() : popAllStatementParameterMarkerSegments());
        result.getVariableNames().addAll(getVariableNames());
        return result;
    }
    
    @Override
    public ASTNode visitDeleteSpecification(final DeleteSpecificationContext ctx) {
        if (null != ctx.dmlTableExprClause().dmlTableClause()) {
            return visit(ctx.dmlTableExprClause().dmlTableClause());
        }
        if (null != ctx.dmlTableExprClause().dmlSubqueryClause()) {
            SubquerySegment subquerySegment = (SubquerySegment) visit(ctx.dmlTableExprClause().dmlSubqueryClause());
            return new SubqueryTableSegment(ctx.dmlTableExprClause().dmlSubqueryClause().start.getStartIndex(), ctx.dmlTableExprClause().dmlSubqueryClause().stop.getStopIndex(), subquerySegment);
        }
        SubquerySegment subquerySegment = (SubquerySegment) visit(ctx.dmlTableExprClause().tableCollectionExpr());
        return new SubqueryTableSegment(ctx.dmlTableExprClause().tableCollectionExpr().start.getStartIndex(), ctx.dmlTableExprClause().tableCollectionExpr().stop.getStopIndex(), subquerySegment);
    }
    
    @Override
    public SelectStatement visitSelectIntoStatement(final SelectIntoStatementContext ctx) {
        SelectStatement result = new SelectStatement(getDatabaseType());
        result.setProjections((ProjectionsSegment) visit(ctx.selectList()));
        // TODO Visit selectIntoClause, bulkCollectIntoClause
        result.setFrom((TableSegment) visit(ctx.fromClauseList()));
        if (null != ctx.whereClause()) {
            result.setWhere((WhereSegment) visit(ctx.whereClause()));
        }
        if (null != ctx.hierarchicalQueryClause()) {
            result.setHierarchicalQuery((HierarchicalQuerySegment) visit(ctx.hierarchicalQueryClause()));
        }
        if (null != ctx.groupByClause()) {
            result.setGroupBy((GroupBySegment) visit(ctx.groupByClause()));
        }
        if (null != ctx.modelClause()) {
            result.setModel((ModelSegment) visit(ctx.modelClause()));
        }
        // TODO Visit windowClause
        if (null != ctx.orderByClause()) {
            result.setOrderBy((OrderBySegment) visit(ctx.orderByClause()));
        }
        // TODO Visit rowLimitingClause
        result.addParameterMarkers(ctx.getParent() instanceof ExecuteContext ? getGlobalParameterMarkerSegments() : popAllStatementParameterMarkerSegments());
        result.getVariableNames().addAll(getVariableNames());
        return result;
    }
    
    @Override
    public ASTNode visitMultiTableElement(final MultiTableElementContext ctx) {
        InsertStatement result = (InsertStatement) visit(ctx.insertIntoClause());
        if (null != ctx.insertValuesClause()) {
            result.getValues().addAll(createInsertValuesSegments(ctx.insertValuesClause().assignmentValues()));
        }
        result.addParameterMarkers(ctx.getParent() instanceof ExecuteContext ? getGlobalParameterMarkerSegments() : popAllStatementParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitSelect(final SelectContext ctx) {
        SelectStatement result = (SelectStatement) visit(ctx.selectSubquery());
        result.addParameterMarkers(ctx.getParent() instanceof ExecuteContext ? getGlobalParameterMarkerSegments() : popAllStatementParameterMarkerSegments());
        result.getVariableNames().addAll(getVariableNames());
        if (null != ctx.forUpdateClause()) {
            result.setLock((LockSegment) visit(ctx.forUpdateClause()));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitPivotClause(final PivotClauseContext ctx) {
        Collection<ColumnSegment> pivotInColumns = new LinkedList<>();
        if (null != ctx.pivotInClause()) {
            ctx.pivotInClause().pivotInClauseExpr().forEach(each -> {
                ExpressionSegment expr = (ExpressionSegment) visit(each.expr());
                String columnName = null == each.alias() || null == each.alias().identifier() ? expr.getText() : each.alias().identifier().IDENTIFIER_().getText();
                ColumnSegment columnSegment = new ColumnSegment(each.getStart().getStartIndex(), each.getStop().getStopIndex(), new IdentifierValue(columnName));
                pivotInColumns.add(columnSegment);
            });
        }
        return new PivotSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ((CollectionValue<ColumnSegment>) visit(ctx.pivotForClause().columnNames())).getValue(), pivotInColumns);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitUnpivotClause(final UnpivotClauseContext ctx) {
        Collection<ColumnSegment> unpivotInColumns = new LinkedList<>();
        if (null != ctx.unpivotInClause()) {
            ctx.unpivotInClause().unpivotInClauseExpr().forEach(each -> unpivotInColumns.addAll(((CollectionValue<ColumnSegment>) visit(ctx.columnNames())).getValue()));
        }
        PivotSegment result = new PivotSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ((CollectionValue<ColumnSegment>) visit(ctx.pivotForClause().columnNames())).getValue(),
                unpivotInColumns, true);
        result.setUnpivotColumns(((CollectionValue<ColumnSegment>) visit(ctx.columnNames())).getValue());
        return result;
    }
    
    @Override
    public ASTNode visitDmlTableClause(final DmlTableClauseContext ctx) {
        if (null != ctx.AT_() && null != ctx.dbLink()) {
            SimpleTableSegment result = new SimpleTableSegment(new TableNameSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), new IdentifierValue(ctx.tableName().name().getText())));
            if (null != ctx.tableName().owner()) {
                result.setOwner(
                        new OwnerSegment(ctx.tableName().owner().start.getStartIndex(), ctx.tableName().owner().stop.getStopIndex(), (IdentifierValue) visit(ctx.tableName().owner().identifier())));
            }
            result.setAt(new IdentifierValue(ctx.AT_().getText()));
            result.setDbLink(new IdentifierValue(ctx.dbLink().identifier(0).getText()));
            return result;
        }
        return visit(ctx.tableName());
    }
    
    @Override
    public ASTNode visitDmlSubqueryClause(final DmlSubqueryClauseContext ctx) {
        SelectStatement subquery = (SelectStatement) visit(ctx.selectSubquery());
        return new SubquerySegment(ctx.selectSubquery().start.getStartIndex(), ctx.selectSubquery().stop.getStopIndex(), subquery, getOriginalText(ctx.selectSubquery()));
    }
    
    @Override
    public ASTNode visitTableCollectionExpr(final TableCollectionExprContext ctx) {
        if (null != ctx.collectionExpr().selectSubquery()) {
            SelectStatement subquery = (SelectStatement) visit(ctx.collectionExpr().selectSubquery());
            return new SubquerySegment(ctx.collectionExpr().selectSubquery().start.getStartIndex(), ctx.collectionExpr().selectSubquery().stop.getStopIndex(), subquery,
                    getOriginalText(ctx.collectionExpr().selectSubquery()));
        }
        if (null != ctx.collectionExpr().functionCall()) {
            return visit(ctx.collectionExpr().functionCall());
        }
        if (null != ctx.collectionExpr().columnName()) {
            return visit(ctx.collectionExpr().columnName());
        }
        if (null != ctx.collectionExpr().expr()) {
            return visit(ctx.collectionExpr().expr());
        }
        throw new UnsupportedOperationException("Unhandled table collection expr");
    }
    
    @Override
    public ASTNode visitConditionalInsertClause(final ConditionalInsertClauseContext ctx) {
        Collection<MultiTableConditionalIntoWhenThenSegment> whenThenSegments = new LinkedList<>();
        for (ConditionalInsertWhenPartContext each : ctx.conditionalInsertWhenPart()) {
            whenThenSegments.add((MultiTableConditionalIntoWhenThenSegment) visit(each));
        }
        MultiTableConditionalIntoSegment result = new MultiTableConditionalIntoSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
        result.getWhenThenSegments().addAll(whenThenSegments);
        if (null != ctx.conditionalInsertElsePart()) {
            result.setElseSegment((MultiTableConditionalIntoElseSegment) visit(ctx.conditionalInsertElsePart()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitConditionalInsertWhenPart(final ConditionalInsertWhenPartContext ctx) {
        List<MultiTableElementContext> multiTableElementContexts = ctx.multiTableElement();
        MultiTableConditionalIntoThenSegment thenSegment = new MultiTableConditionalIntoThenSegment(multiTableElementContexts.get(0).start.getStartIndex(),
                multiTableElementContexts.get(multiTableElementContexts.size() - 1).stop.getStopIndex(), createInsertIntoSegments(multiTableElementContexts));
        return new MultiTableConditionalIntoWhenThenSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), (ExpressionSegment) visit(ctx.expr()), thenSegment);
    }
    
    @Override
    public ASTNode visitConditionalInsertElsePart(final ConditionalInsertElsePartContext ctx) {
        return new MultiTableConditionalIntoElseSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), createInsertIntoSegments(ctx.multiTableElement()));
    }
    
    @Override
    public ASTNode visitAssignmentValues(final AssignmentValuesContext ctx) {
        List<ExpressionSegment> segments = new LinkedList<>();
        for (AssignmentValueContext each : ctx.assignmentValue()) {
            segments.add((ExpressionSegment) visit(each));
        }
        return new InsertValuesSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), segments);
    }
    
    @Override
    public ASTNode visitAssignmentValue(final AssignmentValueContext ctx) {
        ExprContext expr = ctx.expr();
        return null == expr ? new CommonExpressionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText()) : visit(expr);
    }
    
    @Override
    public ASTNode visitSelectSubquery(final SelectSubqueryContext ctx) {
        SelectStatement result;
        if (null != ctx.combineType()) {
            result = new SelectStatement(getDatabaseType());
            SelectStatement left = (SelectStatement) visit(ctx.selectSubquery(0));
            result.setProjections(left.getProjections());
            left.getFrom().ifPresent(result::setFrom);
            left.getWith().ifPresent(result::setWith);
            createSelectCombineClause(ctx, result, left);
        } else {
            result = null == ctx.queryBlock() ? (SelectStatement) visit(ctx.parenthesisSelectSubquery()) : (SelectStatement) visit(ctx.queryBlock());
        }
        if (null != ctx.orderByClause()) {
            result.setOrderBy((OrderBySegment) visit(ctx.orderByClause()));
        }
        result.addParameterMarkers(ctx.getParent() instanceof ExecuteContext ? getGlobalParameterMarkerSegments() : popAllStatementParameterMarkerSegments());
        result.getVariableNames().addAll(getVariableNames());
        return result;
    }
    
    private void createSelectCombineClause(final SelectSubqueryContext ctx, final SelectStatement result, final SelectStatement left) {
        CombineType combineType;
        if (null != ctx.combineType().UNION() && null != ctx.combineType().ALL()) {
            combineType = CombineType.UNION_ALL;
        } else if (null != ctx.combineType().UNION()) {
            combineType = CombineType.UNION;
        } else if (null != ctx.combineType().INTERSECT()) {
            combineType = CombineType.INTERSECT;
        } else {
            combineType = CombineType.MINUS;
        }
        SelectStatement right = (SelectStatement) visit(ctx.selectSubquery(1));
        result.setCombine(new CombineSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), createSubquerySegment(ctx.selectSubquery(0), left), combineType,
                createSubquerySegment(ctx.selectSubquery(1), right)));
        result.addParameterMarkers(left.getParameterMarkers());
        result.addParameterMarkers(right.getParameterMarkers());
    }
    
    private SubquerySegment createSubquerySegment(final SelectSubqueryContext ctx, final SelectStatement selectStatement) {
        return new SubquerySegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), selectStatement, getOriginalText(ctx));
    }
    
    @Override
    public ASTNode visitQueryBlock(final QueryBlockContext ctx) {
        SelectStatement result = new SelectStatement(getDatabaseType());
        result.setProjections((ProjectionsSegment) visit(ctx.selectList()));
        if (null != ctx.withClause()) {
            result.setWith((WithSegment) visit(ctx.withClause()));
        }
        if (null != ctx.duplicateSpecification()) {
            result.getProjections().setDistinctRow(isDistinct(ctx));
        }
        if (null != ctx.selectFromClause()) {
            TableSegment tableSegment = (TableSegment) visit(ctx.selectFromClause());
            result.setFrom(tableSegment);
        }
        if (null != ctx.whereClause()) {
            result.setWhere((WhereSegment) visit(ctx.whereClause()));
        }
        if (null != ctx.hierarchicalQueryClause()) {
            result.setHierarchicalQuery((HierarchicalQuerySegment) visit(ctx.hierarchicalQueryClause()));
        }
        if (null != ctx.groupByClause()) {
            result.setGroupBy((GroupBySegment) visit(ctx.groupByClause()));
            if (null != ctx.groupByClause().havingClause()) {
                result.setHaving((HavingSegment) visit(ctx.groupByClause().havingClause()));
            }
        }
        if (null != ctx.modelClause()) {
            result.setModel((ModelSegment) visit(ctx.modelClause()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitHierarchicalQueryClause(final HierarchicalQueryClauseContext ctx) {
        if (null == ctx || null == ctx.getStart() || null == ctx.getStop()) {
            return null;
        }
        HierarchicalQuerySegment result = new HierarchicalQuerySegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
        result.setNoCycle(null != ctx.NOCYCLE());
        if (ctx.expr().isEmpty()) {
            return result;
        }
        boolean connectByFirst = "CONNECT".equalsIgnoreCase(ctx.getStart().getText());
        if (connectByFirst) {
            result.setConnectBy((ExpressionSegment) visit(ctx.expr(0)));
            if (ctx.expr().size() > 1) {
                result.setStartWith((ExpressionSegment) visit(ctx.expr(1)));
            }
        } else {
            result.setStartWith((ExpressionSegment) visit(ctx.expr(0)));
            if (ctx.expr().size() > 1) {
                result.setConnectBy((ExpressionSegment) visit(ctx.expr(1)));
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitHavingClause(final HavingClauseContext ctx) {
        ExpressionSegment expr = (ExpressionSegment) visit(ctx.expr());
        return new HavingSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), expr);
    }
    
    @Override
    public ASTNode visitModelClause(final ModelClauseContext ctx) {
        ModelSegment result = new ModelSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
        if (null != ctx.referenceModel()) {
            for (ReferenceModelContext each : ctx.referenceModel()) {
                result.getReferenceModelSelects().add((SubquerySegment) visit(each));
            }
        }
        if (null != ctx.mainModel().modelRulesClause().orderByClause()) {
            for (OrderByClauseContext each : ctx.mainModel().modelRulesClause().orderByClause()) {
                result.getOrderBySegments().add((OrderBySegment) visit(each));
            }
        }
        for (CellAssignmentContext each : ctx.mainModel().modelRulesClause().cellAssignment()) {
            result.getCellAssignmentColumns().add((ColumnSegment) visit(each.measureColumn().columnName()));
            if (null != each.singleColumnForLoop()) {
                result.getCellAssignmentColumns().addAll(extractColumnValuesFromSingleColumnForLoop(each.singleColumnForLoop()));
                result.getCellAssignmentSelects().addAll(extractSelectSubqueryValuesFromSingleColumnForLoop(each.singleColumnForLoop()));
            }
            if (null != each.multiColumnForLoop()) {
                result.getCellAssignmentColumns().addAll(extractColumnValuesFromMultiColumnForLoop(each.multiColumnForLoop()));
                result.getCellAssignmentSelects().add(extractSelectSubqueryValueFromMultiColumnForLoop(each.multiColumnForLoop()));
            }
        }
        return result;
    }
    
    private Collection<ColumnSegment> extractColumnValuesFromSingleColumnForLoop(final List<SingleColumnForLoopContext> ctx) {
        Collection<ColumnSegment> result = new LinkedList<>();
        for (SingleColumnForLoopContext each : ctx) {
            result.add((ColumnSegment) visit(each.dimensionColumn().columnName()));
        }
        return result;
    }
    
    private Collection<SubquerySegment> extractSelectSubqueryValuesFromSingleColumnForLoop(final List<SingleColumnForLoopContext> ctx) {
        Collection<SubquerySegment> result = new LinkedList<>();
        for (SingleColumnForLoopContext each : ctx) {
            if (null != each.selectSubquery()) {
                SelectStatement subquery = (SelectStatement) visit(each.selectSubquery());
                SubquerySegment subquerySegment = new SubquerySegment(each.selectSubquery().start.getStartIndex(), each.selectSubquery().stop.getStopIndex(), subquery,
                        getOriginalText(each.selectSubquery()));
                result.add(subquerySegment);
            }
        }
        return result;
    }
    
    private Collection<ColumnSegment> extractColumnValuesFromMultiColumnForLoop(final MultiColumnForLoopContext ctx) {
        Collection<ColumnSegment> result = new LinkedList<>();
        for (DimensionColumnContext each : ctx.dimensionColumn()) {
            result.add((ColumnSegment) visit(each.columnName()));
        }
        return result;
    }
    
    private SubquerySegment extractSelectSubqueryValueFromMultiColumnForLoop(final MultiColumnForLoopContext ctx) {
        SelectStatement subquery = (SelectStatement) visit(ctx.selectSubquery());
        return new SubquerySegment(ctx.selectSubquery().start.getStartIndex(), ctx.selectSubquery().stop.getStopIndex(), subquery, getOriginalText(ctx.selectSubquery()));
    }
    
    @Override
    public ASTNode visitReferenceModel(final ReferenceModelContext ctx) {
        SelectStatement subquery = (SelectStatement) visit(ctx.selectSubquery());
        return new SubquerySegment(ctx.selectSubquery().start.getStartIndex(), ctx.selectSubquery().stop.getStopIndex(), subquery, getOriginalText(ctx.selectSubquery()));
    }
    
    @Override
    public ASTNode visitParenthesisSelectSubquery(final ParenthesisSelectSubqueryContext ctx) {
        return visit(ctx.selectSubquery());
    }
    
    @Override
    public ASTNode visitWithClause(final WithClauseContext ctx) {
        Collection<CommonTableExpressionSegment> commonTableExpressions = new LinkedList<>();
        if (null != ctx.subqueryFactoringClause()) {
            for (SubqueryFactoringClauseContext each : ctx.subqueryFactoringClause()) {
                SubquerySegment subquery = new SubquerySegment(each.selectSubquery().start.getStartIndex(), each.selectSubquery().stop.getStopIndex(), (SelectStatement) visit(each),
                        getOriginalText(each));
                CommonTableExpressionSegment commonTableExpression = new CommonTableExpressionSegment(each.start.getStartIndex(), each.stop.getStopIndex(),
                        (AliasSegment) visit(each.queryName().alias()), subquery);
                if (null != each.searchClause()) {
                    ColumnNameContext columnName = each.searchClause().orderingColumn().columnName();
                    commonTableExpression.getColumns().add((ColumnSegment) visit(columnName));
                }
                commonTableExpressions.add(commonTableExpression);
            }
        }
        return new WithSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), commonTableExpressions);
    }
    
    @Override
    public ASTNode visitSubqueryFactoringClause(final SubqueryFactoringClauseContext ctx) {
        return visit(ctx.selectSubquery());
    }
    
    private boolean isDistinct(final QueryBlockContext ctx) {
        return ((BooleanLiteralValue) visit(ctx.duplicateSpecification())).getValue();
    }
    
    @Override
    public ASTNode visitDuplicateSpecification(final DuplicateSpecificationContext ctx) {
        if (null != ctx.DISTINCT() || null != ctx.UNIQUE()) {
            return new BooleanLiteralValue(true);
        }
        return new BooleanLiteralValue(false);
    }
    
    @Override
    public ASTNode visitSelectList(final SelectListContext ctx) {
        ProjectionsSegment result = new ProjectionsSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
        Collection<ProjectionSegment> projections = new LinkedList<>();
        if (null != ctx.unqualifiedShorthand()) {
            projections.add(new ShorthandProjectionSegment(ctx.unqualifiedShorthand().getStart().getStartIndex(), ctx.unqualifiedShorthand().getStop().getStopIndex()));
            result.getProjections().addAll(projections);
            return result;
        }
        for (SelectProjectionContext each : ctx.selectProjection()) {
            projections.add((ProjectionSegment) visit(each));
        }
        result.getProjections().addAll(projections);
        return result;
    }
    
    @Override
    public ASTNode visitSelectProjection(final SelectProjectionContext ctx) {
        // FIXME :The stop index of project is the stop index of projection, instead of alias.
        if (null != ctx.queryName()) {
            QueryNameContext queryName = ctx.queryName();
            ShorthandProjectionSegment result = new ShorthandProjectionSegment(queryName.getStart().getStartIndex(), ctx.DOT_ASTERISK_().getSymbol().getStopIndex());
            IdentifierValue identifier = new IdentifierValue(queryName.getText());
            result.setOwner(new OwnerSegment(queryName.getStart().getStartIndex(), queryName.getStop().getStopIndex(), identifier));
            return result;
        }
        if (null != ctx.tableName()) {
            TableNameContext tableName = ctx.tableName();
            ShorthandProjectionSegment result = new ShorthandProjectionSegment(tableName.getStart().getStartIndex(), ctx.DOT_ASTERISK_().getSymbol().getStopIndex());
            IdentifierValue identifier = new IdentifierValue(tableName.getText());
            result.setOwner(new OwnerSegment(tableName.getStart().getStartIndex(), tableName.getStop().getStopIndex(), identifier));
            return result;
        }
        if (null != ctx.alias()) {
            AliasContext aliasContext = ctx.alias();
            ShorthandProjectionSegment result = new ShorthandProjectionSegment(aliasContext.getStart().getStartIndex(), ctx.DOT_ASTERISK_().getSymbol().getStopIndex());
            IdentifierValue identifier = new IdentifierValue(aliasContext.getText());
            result.setOwner(new OwnerSegment(aliasContext.getStart().getStartIndex(), aliasContext.getStop().getStopIndex(), identifier));
            return result;
        }
        return createProjection(ctx.selectProjectionExprClause());
    }
    
    @Override
    public ASTNode visitAlias(final AliasContext ctx) {
        if (null != ctx.identifier()) {
            return new AliasSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), (IdentifierValue) visit(ctx.identifier()));
        }
        return new AliasSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), new IdentifierValue(ctx.STRING_().getText()));
    }
    
    private ASTNode createProjection(final SelectProjectionExprClauseContext ctx) {
        AliasSegment alias = null == ctx.alias() ? null : (AliasSegment) visit(ctx.alias());
        ASTNode projection = visit(ctx.expr());
        if (projection instanceof AliasAvailable) {
            ((AliasAvailable) projection).setAlias(alias);
            return projection;
        }
        if (projection instanceof ComplexExpressionSegment) {
            return createProjectionForComplexExpressionSegment(projection, alias);
        }
        if (projection instanceof SimpleExpressionSegment) {
            return createProjectionForSimpleExpressionSegment(projection, alias, ctx);
        }
        if (projection instanceof ExpressionSegment) {
            return createProjectionForExpressionSegment(projection, alias);
        }
        throw new UnsupportedOperationException("Unhandled case");
    }
    
    private ASTNode createProjectionForComplexExpressionSegment(final ASTNode projection, final AliasSegment alias) {
        if (projection instanceof FunctionSegment) {
            FunctionSegment segment = (FunctionSegment) projection;
            ExpressionProjectionSegment result = new ExpressionProjectionSegment(segment.getStartIndex(), segment.getStopIndex(), segment.getText(), segment);
            result.setAlias(alias);
            return result;
        }
        if (projection instanceof CommonExpressionSegment) {
            CommonExpressionSegment segment = (CommonExpressionSegment) projection;
            ExpressionProjectionSegment result = new ExpressionProjectionSegment(segment.getStartIndex(), segment.getStopIndex(), segment.getText(), segment);
            result.setAlias(alias);
            return result;
        }
        if (projection instanceof XmlQueryAndExistsFunctionSegment || projection instanceof XmlPiFunctionSegment || projection instanceof XmlSerializeFunctionSegment
                || projection instanceof XmlElementFunctionSegment) {
            return projection;
        }
        throw new UnsupportedOperationException("Unsupported Complex Expression");
    }
    
    private ASTNode createProjectionForSimpleExpressionSegment(final ASTNode projection, final AliasSegment alias, final SelectProjectionExprClauseContext ctx) {
        if (projection instanceof SubqueryExpressionSegment) {
            SubqueryExpressionSegment subqueryExpressionSegment = (SubqueryExpressionSegment) projection;
            String text = ctx.start.getInputStream().getText(new Interval(subqueryExpressionSegment.getStartIndex(), subqueryExpressionSegment.getStopIndex()));
            SubqueryProjectionSegment result = new SubqueryProjectionSegment(((SubqueryExpressionSegment) projection).getSubquery(), text);
            result.setAlias(alias);
            return result;
        }
        if (projection instanceof LiteralExpressionSegment) {
            LiteralExpressionSegment column = (LiteralExpressionSegment) projection;
            ExpressionProjectionSegment result = null == alias
                    ? new ExpressionProjectionSegment(column.getStartIndex(), column.getStopIndex(), String.valueOf(column.getLiterals()), column)
                    : new ExpressionProjectionSegment(column.getStartIndex(), ctx.alias().stop.getStopIndex(), String.valueOf(column.getLiterals()), column);
            result.setAlias(alias);
            return result;
        }
        throw new UnsupportedOperationException("Unsupported Simple Expression");
    }
    
    private ASTNode createProjectionForExpressionSegment(final ASTNode projection, final AliasSegment alias) {
        // FIXME :For DISTINCT()
        if (projection instanceof ColumnSegment) {
            return createColumnProjectionSegment((ColumnSegment) projection, alias);
        }
        if (projection instanceof BinaryOperationExpression) {
            return createExpressionProjectionSegment((BinaryOperationExpression) projection, alias);
        }
        if (projection instanceof MultisetExpression) {
            return createExpressionProjectionSegment((MultisetExpression) projection, alias);
        }
        if (projection instanceof DatetimeExpression) {
            return createDatetimeProjectionSegment((DatetimeExpression) projection);
        }
        if (projection instanceof IntervalExpressionProjection) {
            return createIntervalExpressionProjection((IntervalExpressionProjection) projection);
        }
        if (projection instanceof CaseWhenExpression || projection instanceof VariableSegment
                || projection instanceof BetweenExpression || projection instanceof InExpression || projection instanceof CollateExpression) {
            return createExpressionProjectionSegment((ExpressionSegment) projection, alias);
        }
        throw new UnsupportedOperationException("Unsupported Expression");
    }
    
    private ColumnProjectionSegment createColumnProjectionSegment(final ColumnSegment projection, final AliasSegment alias) {
        ColumnProjectionSegment result = new ColumnProjectionSegment(projection);
        result.setAlias(alias);
        return result;
    }
    
    private ExpressionProjectionSegment createExpressionProjectionSegment(final BinaryOperationExpression projection, final AliasSegment alias) {
        int startIndex = projection.getStartIndex();
        int stopIndex = null == alias ? projection.getStopIndex() : alias.getStopIndex();
        ExpressionProjectionSegment result = new ExpressionProjectionSegment(startIndex, stopIndex, projection.getText(), projection);
        result.setAlias(alias);
        return result;
    }
    
    private ExpressionProjectionSegment createExpressionProjectionSegment(final MultisetExpression projection, final AliasSegment alias) {
        int startIndex = projection.getStartIndex();
        int stopIndex = null == alias ? projection.getStopIndex() : alias.getStopIndex();
        ExpressionProjectionSegment result = new ExpressionProjectionSegment(startIndex, stopIndex, projection.getText(), projection);
        result.setAlias(alias);
        return result;
    }
    
    private ExpressionProjectionSegment createExpressionProjectionSegment(final ExpressionSegment projection, final AliasSegment alias) {
        ExpressionProjectionSegment result = new ExpressionProjectionSegment(projection.getStartIndex(), projection.getStopIndex(), projection.getText(), projection);
        result.setAlias(alias);
        return result;
    }
    
    private DatetimeProjectionSegment createDatetimeProjectionSegment(final DatetimeExpression projection) {
        return null == projection.getRight()
                ? new DatetimeProjectionSegment(projection.getStartIndex(), projection.getStopIndex(), projection.getLeft(), projection.getText())
                : new DatetimeProjectionSegment(projection.getStartIndex(), projection.getStopIndex(), projection.getLeft(), projection.getRight(), projection.getText());
    }
    
    private IntervalExpressionProjection createIntervalExpressionProjection(final IntervalExpressionProjection projection) {
        IntervalExpressionProjection result = new IntervalExpressionProjection(
                projection.getStartIndex(), projection.getStopIndex(), projection.getLeft(), projection.getMinus(), projection.getRight(), projection.getText());
        if (null != projection.getDayToSecondExpression()) {
            result.setDayToSecondExpression(projection.getDayToSecondExpression());
        } else {
            result.setYearToMonthExpression(projection.getYearToMonthExpression());
        }
        return result;
    }
    
    @Override
    public ASTNode visitSelectFromClause(final SelectFromClauseContext ctx) {
        return visit(ctx.fromClauseList());
    }
    
    @Override
    public ASTNode visitFromClauseList(final FromClauseListContext ctx) {
        TableSegment result = (TableSegment) visit(ctx.fromClauseOption(0));
        if (ctx.fromClauseOption().size() > 1) {
            for (int i = 1; i < ctx.fromClauseOption().size(); i++) {
                result = generateJoinTableSourceFromFromClauseOption(ctx.fromClauseOption(i), result);
            }
        }
        return result;
    }
    
    private JoinTableSegment generateJoinTableSourceFromFromClauseOption(final FromClauseOptionContext ctx, final TableSegment tableSegment) {
        JoinTableSegment result = new JoinTableSegment();
        result.setStartIndex(tableSegment.getStartIndex());
        result.setStopIndex(ctx.stop.getStopIndex());
        result.setLeft(tableSegment);
        result.setJoinType(JoinType.COMMA.name());
        result.setRight((TableSegment) visit(ctx));
        return result;
    }
    
    @Override
    public ASTNode visitFromClauseOption(final FromClauseOptionContext ctx) {
        if (null != ctx.joinClause()) {
            return visit(ctx.joinClause());
        }
        if (null != ctx.regularFunction()) {
            FunctionSegment functionSegment = (FunctionSegment) visit(ctx.regularFunction());
            FunctionTableSegment result = new FunctionTableSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), functionSegment);
            if (null != ctx.alias()) {
                result.setAlias((AliasSegment) visit(ctx.alias()));
            }
            return result;
        }
        if (null != ctx.xmlTableFunction()) {
            XmlTableFunctionSegment functionSegment = (XmlTableFunctionSegment) visit(ctx.xmlTableFunction());
            FunctionTableSegment result = new FunctionTableSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), functionSegment);
            if (null != ctx.alias()) {
                result.setAlias((AliasSegment) visit(ctx.alias()));
            }
            return result;
        }
        return visit(ctx.selectTableReference());
    }
    
    @Override
    public ASTNode visitJoinClause(final JoinClauseContext ctx) {
        TableSegment result;
        TableSegment left;
        left = (TableSegment) visit(ctx.selectTableReference());
        for (SelectJoinOptionContext each : ctx.selectJoinOption()) {
            left = visitJoinedTable(each, left);
        }
        result = left;
        return result;
    }
    
    private JoinTableSegment visitJoinedTable(final SelectJoinOptionContext ctx, final TableSegment tableSegment) {
        JoinTableSegment result = new JoinTableSegment();
        result.setLeft(tableSegment);
        result.setStartIndex(tableSegment.getStartIndex());
        result.setStopIndex(ctx.stop.getStopIndex());
        result.setJoinType(getJoinType(ctx));
        result.setNatural(isNatural(ctx));
        if (null != ctx.innerCrossJoinClause()) {
            TableSegment right = (TableSegment) visit(ctx.innerCrossJoinClause().selectTableReference());
            result.setRight(right);
            if (null != ctx.innerCrossJoinClause().selectJoinSpecification()) {
                visitSelectJoinSpecification(ctx.innerCrossJoinClause().selectJoinSpecification(), result);
            }
        } else if (null != ctx.outerJoinClause()) {
            TableSegment right = (TableSegment) visit(ctx.outerJoinClause().selectTableReference());
            result.setRight(right);
            if (null != ctx.outerJoinClause().selectJoinSpecification()) {
                visitSelectJoinSpecification(ctx.outerJoinClause().selectJoinSpecification(), result);
            }
        } else {
            TableSegment right = (TableSegment) visit(ctx.crossOuterApplyClause());
            result.setRight(right);
        }
        return result;
    }
    
    private boolean isNatural(final SelectJoinOptionContext ctx) {
        if (null != ctx.innerCrossJoinClause()) {
            return null != ctx.innerCrossJoinClause().NATURAL();
        }
        if (null != ctx.outerJoinClause()) {
            return null != ctx.outerJoinClause().NATURAL();
        }
        return false;
    }
    
    private String getJoinType(final SelectJoinOptionContext ctx) {
        if (null != ctx.innerCrossJoinClause()) {
            return getInnerCrossJoinType(ctx.innerCrossJoinClause());
        }
        if (null != ctx.outerJoinClause()) {
            return getOuterJoinType(ctx.outerJoinClause());
        }
        if (null != ctx.crossOuterApplyClause()) {
            return getCrossOuterApplyType(ctx.crossOuterApplyClause());
        }
        return JoinType.COMMA.name();
    }
    
    private String getCrossOuterApplyType(final CrossOuterApplyClauseContext ctx) {
        if (null != ctx.CROSS()) {
            return JoinType.CROSS.name();
        }
        return JoinType.LEFT.name();
    }
    
    private String getOuterJoinType(final OuterJoinClauseContext ctx) {
        if (null != ctx.outerJoinType().FULL()) {
            return JoinType.FULL.name();
        } else if (null != ctx.outerJoinType().LEFT()) {
            return JoinType.LEFT.name();
        }
        return JoinType.RIGHT.name();
    }
    
    private String getInnerCrossJoinType(final InnerCrossJoinClauseContext ctx) {
        return null == ctx.CROSS() ? JoinType.INNER.name() : JoinType.CROSS.name();
    }
    
    private void visitSelectJoinSpecification(final SelectJoinSpecificationContext ctx, final JoinTableSegment joinTableSource) {
        if (null != ctx.expr()) {
            ExpressionSegment condition = (ExpressionSegment) visit(ctx.expr());
            joinTableSource.setCondition(condition);
        }
        if (null != ctx.USING()) {
            joinTableSource.setUsing(ctx.columnNames().columnName().stream().map(each -> (ColumnSegment) visit(each)).collect(Collectors.toList()));
        }
    }
    
    @Override
    public ASTNode visitCrossOuterApplyClause(final CrossOuterApplyClauseContext ctx) {
        TableSegment result;
        if (null != ctx.selectTableReference()) {
            result = (TableSegment) visit(ctx.selectTableReference());
        } else {
            SubquerySegment subquerySegment = (SubquerySegment) visit(ctx.collectionExpr());
            result = new SubqueryTableSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), subquerySegment);
        }
        return result;
    }
    
    @Override
    public ASTNode visitCollectionExpr(final CollectionExprContext ctx) {
        SelectStatement subquery = (SelectStatement) visit(ctx.selectSubquery());
        return new SubquerySegment(ctx.selectSubquery().start.getStartIndex(), ctx.selectSubquery().stop.getStopIndex(), subquery, getOriginalText(ctx.selectSubquery()));
    }
    
    @Override
    public ASTNode visitSelectTableReference(final SelectTableReferenceContext ctx) {
        TableSegment result;
        if (null != ctx.containersClause()) {
            result = (TableSegment) visit(ctx.containersClause());
        } else if (null != ctx.shardsClause()) {
            result = (TableSegment) visit(ctx.shardsClause());
        } else if (null != ctx.joinClause()) {
            result = (TableSegment) visit(ctx.joinClause());
        } else {
            result = (TableSegment) visit(ctx.queryTableExprClause());
        }
        if (null != ctx.alias()) {
            result.setAlias((AliasSegment) visit(ctx.alias()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitContainersClause(final ContainersClauseContext ctx) {
        return visit(ctx.tableName());
    }
    
    @Override
    public ASTNode visitShardsClause(final ShardsClauseContext ctx) {
        return visit(ctx.tableName());
    }
    
    @Override
    public ASTNode visitQueryTableExprClause(final QueryTableExprClauseContext ctx) {
        ASTNode result = visit(ctx.queryTableExpr());
        if (null != ctx.pivotClause()) {
            PivotSegment pivotClause = (PivotSegment) visit(ctx.pivotClause());
            if (result instanceof SubqueryTableSegment) {
                ((SubqueryTableSegment) result).setPivot(pivotClause);
            }
            if (result instanceof SimpleTableSegment) {
                ((SimpleTableSegment) result).setPivot(pivotClause);
            }
        }
        if (null != ctx.unpivotClause()) {
            PivotSegment pivotClause = (PivotSegment) visit(ctx.unpivotClause());
            if (result instanceof SubqueryTableSegment) {
                ((SubqueryTableSegment) result).setPivot(pivotClause);
            }
            if (result instanceof SimpleTableSegment) {
                ((SimpleTableSegment) result).setPivot(pivotClause);
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitQueryTableExpr(final QueryTableExprContext ctx) {
        TableSegment result;
        if (null != ctx.queryTableExprSampleClause()) {
            result = (SimpleTableSegment) visit(ctx.queryTableExprSampleClause().queryTableExprTableClause().tableName());
        } else if (null != ctx.lateralClause()) {
            SelectStatement subquery = (SelectStatement) visit(ctx.lateralClause().selectSubquery());
            SubquerySegment subquerySegment = new SubquerySegment(ctx.lateralClause().selectSubquery().start.getStartIndex(), ctx.lateralClause().selectSubquery().stop.getStopIndex(), subquery,
                    getOriginalText(ctx.lateralClause().selectSubquery()));
            result = new SubqueryTableSegment(ctx.lateralClause().LP_().getSymbol().getStartIndex(), ctx.lateralClause().RP_().getSymbol().getStopIndex(), subquerySegment);
            if (null != ctx.alias()) {
                result.setAlias((AliasSegment) visit(ctx.alias()));
            }
        } else {
            if (null != ctx.tableCollectionExpr().collectionExpr().selectSubquery()) {
                SubquerySegment subquerySegment = (SubquerySegment) visit(ctx.tableCollectionExpr());
                result = new SubqueryTableSegment(ctx.tableCollectionExpr().start.getStartIndex(), ctx.tableCollectionExpr().stop.getStopIndex(), subquerySegment);
            } else {
                result = new CollectionTableSegment((ExpressionSegment) visit(ctx.tableCollectionExpr()));
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitWhereClause(final WhereClauseContext ctx) {
        ASTNode segment = visit(ctx.expr());
        return new WhereSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (ExpressionSegment) segment);
    }
    
    @Override
    public ASTNode visitGroupByClause(final GroupByClauseContext ctx) {
        Collection<OrderByItemSegment> items = new LinkedList<>();
        for (GroupByItemContext each : ctx.groupByItem()) {
            items.addAll(generateOrderByItemsFromGroupByItem(each));
        }
        return new GroupBySegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), items);
    }
    
    private Collection<OrderByItemSegment> generateOrderByItemsFromGroupByItem(final GroupByItemContext ctx) {
        Collection<OrderByItemSegment> result = new LinkedList<>();
        if (null != ctx.expr()) {
            OrderByItemSegment item = (OrderByItemSegment) extractValueFromGroupByItemExpression(ctx.expr());
            result.add(item);
        } else if (null != ctx.rollupCubeClause()) {
            result.addAll(generateOrderByItemSegmentsFromRollupCubeClause(ctx.rollupCubeClause()));
        } else {
            result.addAll(generateOrderByItemSegmentsFromGroupingSetsClause(ctx.groupingSetsClause()));
        }
        return result;
    }
    
    private ASTNode extractValueFromGroupByItemExpression(final ExprContext ctx) {
        ASTNode expression = visit(ctx);
        if (expression instanceof ColumnSegment) {
            ColumnSegment column = (ColumnSegment) expression;
            return new ColumnOrderByItemSegment(column, OrderDirection.ASC, null);
        }
        if (expression instanceof LiteralExpressionSegment) {
            LiteralExpressionSegment literalExpression = (LiteralExpressionSegment) expression;
            return new IndexOrderByItemSegment(literalExpression.getStartIndex(), literalExpression.getStopIndex(),
                    SQLUtils.getExactlyNumber(literalExpression.getLiterals().toString(), 10).intValue(), OrderDirection.ASC, null);
        }
        return new ExpressionOrderByItemSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), getOriginalText(ctx), OrderDirection.ASC, null, (ExpressionSegment) expression);
    }
    
    private Collection<OrderByItemSegment> generateOrderByItemSegmentsFromRollupCubeClause(final RollupCubeClauseContext ctx) {
        return new LinkedList<>(generateOrderByItemSegmentsFromGroupingExprList(ctx.groupingExprList()));
    }
    
    private Collection<OrderByItemSegment> generateOrderByItemSegmentsFromGroupingSetsClause(final GroupingSetsClauseContext ctx) {
        Collection<OrderByItemSegment> result = new LinkedList<>();
        if (null != ctx.rollupCubeClause()) {
            for (RollupCubeClauseContext each : ctx.rollupCubeClause()) {
                result.addAll(generateOrderByItemSegmentsFromRollupCubeClause(each));
            }
        }
        if (null != ctx.groupingExprList()) {
            for (GroupingExprListContext each : ctx.groupingExprList()) {
                result.addAll(generateOrderByItemSegmentsFromGroupingExprList(each));
            }
        }
        return result;
    }
    
    private Collection<OrderByItemSegment> generateOrderByItemSegmentsFromGroupingExprList(final GroupingExprListContext ctx) {
        Collection<OrderByItemSegment> result = new LinkedList<>();
        for (ExpressionListContext each : ctx.expressionList()) {
            result.addAll(generateOrderByItemSegmentsFromExpressionList(each));
        }
        return result;
    }
    
    private Collection<OrderByItemSegment> generateOrderByItemSegmentsFromExpressionList(final ExpressionListContext ctx) {
        Collection<OrderByItemSegment> result = new LinkedList<>();
        if (null != ctx.expr()) {
            for (ExprContext each : ctx.expr()) {
                result.add((OrderByItemSegment) extractValueFromGroupByItemExpression(each));
            }
        }
        if (null != ctx.exprs()) {
            for (ExprContext each : ctx.exprs().expr()) {
                result.add((OrderByItemSegment) extractValueFromGroupByItemExpression(each));
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitSubquery(final SubqueryContext ctx) {
        return visit(ctx.selectSubquery());
    }
    
    @Override
    public ASTNode visitForUpdateClause(final ForUpdateClauseContext ctx) {
        LockSegment result = new LockSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
        if (null != ctx.forUpdateClauseList()) {
            result.getTables().addAll(generateTablesFromForUpdateClauseOption(ctx.forUpdateClauseList()));
            result.getColumns().addAll(generateColumnsFromForUpdateClauseOption(ctx.forUpdateClauseList()));
        }
        return result;
    }
    
    private List<SimpleTableSegment> generateTablesFromForUpdateClauseOption(final ForUpdateClauseListContext ctx) {
        List<SimpleTableSegment> result = new LinkedList<>();
        for (ForUpdateClauseOptionContext each : ctx.forUpdateClauseOption()) {
            if (null != each.tableName()) {
                result.add((SimpleTableSegment) visit(each.tableName()));
            }
        }
        return result;
    }
    
    private List<ColumnSegment> generateColumnsFromForUpdateClauseOption(final ForUpdateClauseListContext ctx) {
        List<ColumnSegment> result = new LinkedList<>();
        for (ForUpdateClauseOptionContext each : ctx.forUpdateClauseOption()) {
            if (null != each.columnName()) {
                result.add((ColumnSegment) visit(each.columnName()));
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitMerge(final MergeContext ctx) {
        MergeStatement result = new MergeStatement(getDatabaseType());
        result.setTarget((TableSegment) visit(ctx.intoClause()));
        result.setSource((TableSegment) visit(ctx.usingClause()));
        ExpressionWithParamsSegment onExpression = new ExpressionWithParamsSegment(ctx.usingClause().expr().start.getStartIndex(), ctx.usingClause().expr().stop.getStopIndex(),
                (ExpressionSegment) visit(ctx.usingClause().expr()));
        onExpression.getParameterMarkerSegments().addAll(popAllStatementParameterMarkerSegments());
        result.setExpression(onExpression);
        if (null != ctx.mergeUpdateClause() && null != ctx.mergeInsertClause() && ctx.mergeUpdateClause().start.getStartIndex() > ctx.mergeInsertClause().start.getStartIndex()) {
            result.setInsert((InsertStatement) visitMergeInsertClause(ctx.mergeInsertClause()));
            result.setUpdate((UpdateStatement) visitMergeUpdateClause(ctx.mergeUpdateClause()));
            result.addParameterMarkers(ctx.getParent() instanceof ExecuteContext ? getGlobalParameterMarkerSegments() : popAllStatementParameterMarkerSegments());
            return result;
        }
        if (null != ctx.mergeUpdateClause()) {
            result.setUpdate((UpdateStatement) visitMergeUpdateClause(ctx.mergeUpdateClause()));
        }
        if (null != ctx.mergeInsertClause()) {
            result.setInsert((InsertStatement) visitMergeInsertClause(ctx.mergeInsertClause()));
        }
        result.addParameterMarkers(ctx.getParent() instanceof ExecuteContext ? getGlobalParameterMarkerSegments() : popAllStatementParameterMarkerSegments());
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitMergeInsertClause(final MergeInsertClauseContext ctx) {
        InsertStatement result = new InsertStatement(getDatabaseType());
        if (null != ctx.mergeInsertColumn()) {
            result.setInsertColumns((InsertColumnsSegment) visit(ctx.mergeInsertColumn()));
        }
        if (null != ctx.mergeColumnValue()) {
            result.getValues().addAll(((CollectionValue<InsertValuesSegment>) visit(ctx.mergeColumnValue())).getValue());
        }
        if (null != ctx.whereClause()) {
            result.setWhere((WhereSegment) visit(ctx.whereClause()));
        }
        result.addParameterMarkers(popAllStatementParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitMergeInsertColumn(final MergeInsertColumnContext ctx) {
        Collection<ColumnSegment> columnSegments = new ArrayList<>(ctx.columnName().size());
        for (ColumnNameContext each : ctx.columnName()) {
            if (null != each.name()) {
                columnSegments.add((ColumnSegment) visit(each));
            }
        }
        return new InsertColumnsSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), columnSegments);
    }
    
    @Override
    public ASTNode visitMergeColumnValue(final MergeColumnValueContext ctx) {
        CollectionValue<InsertValuesSegment> result = new CollectionValue<>();
        List<ExpressionSegment> segments = new LinkedList<>();
        for (ExprContext each : ctx.expr()) {
            segments.add(null == each ? new CommonExpressionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText()) : (ExpressionSegment) visit(each));
        }
        result.getValue().add(new InsertValuesSegment(ctx.LP_().getSymbol().getStartIndex(), ctx.RP_().getSymbol().getStopIndex(), segments));
        return result;
    }
    
    @Override
    public ASTNode visitIntoClause(final IntoClauseContext ctx) {
        if (null != ctx.tableName()) {
            SimpleTableSegment result = (SimpleTableSegment) visit(ctx.tableName());
            if (null != ctx.alias()) {
                result.setAlias((AliasSegment) visit(ctx.alias()));
            }
            return result;
        }
        if (null != ctx.viewName()) {
            SimpleTableSegment result = (SimpleTableSegment) visit(ctx.viewName());
            if (null != ctx.alias()) {
                result.setAlias((AliasSegment) visit(ctx.alias()));
            }
            return result;
        }
        SelectStatement subquery = (SelectStatement) visit(ctx.subquery());
        SubquerySegment subquerySegment = new SubquerySegment(ctx.subquery().start.getStartIndex(), ctx.subquery().stop.getStopIndex(), subquery, getOriginalText(ctx.subquery()));
        SubqueryTableSegment result = new SubqueryTableSegment(ctx.subquery().start.getStartIndex(), ctx.subquery().stop.getStopIndex(), subquerySegment);
        if (null != ctx.alias()) {
            result.setAlias((AliasSegment) visit(ctx.alias()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitUsingClause(final UsingClauseContext ctx) {
        if (null != ctx.tableName()) {
            SimpleTableSegment result = (SimpleTableSegment) visit(ctx.tableName());
            if (null != ctx.alias()) {
                result.setAlias((AliasSegment) visit(ctx.alias()));
            }
            return result;
        }
        if (null != ctx.viewName()) {
            SimpleTableSegment result = (SimpleTableSegment) visit(ctx.viewName());
            if (null != ctx.alias()) {
                result.setAlias((AliasSegment) visit(ctx.alias()));
            }
            return result;
        }
        SelectStatement subquery = (SelectStatement) visit(ctx.subquery());
        SubquerySegment subquerySegment = new SubquerySegment(ctx.subquery().start.getStartIndex(), ctx.subquery().stop.getStopIndex(), subquery, getOriginalText(ctx.subquery()));
        subquerySegment.getSelect().addParameterMarkers(popAllStatementParameterMarkerSegments());
        SubqueryTableSegment result = new SubqueryTableSegment(ctx.subquery().start.getStartIndex(), ctx.subquery().stop.getStopIndex(), subquerySegment);
        if (null != ctx.alias()) {
            result.setAlias((AliasSegment) visit(ctx.alias()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitMergeUpdateClause(final MergeUpdateClauseContext ctx) {
        UpdateStatement result = new UpdateStatement(getDatabaseType());
        result.setSetAssignment((SetAssignmentSegment) visit(ctx.mergeSetAssignmentsClause()));
        if (null != ctx.whereClause()) {
            result.setWhere((WhereSegment) visit(ctx.whereClause()));
        }
        if (null != ctx.deleteWhereClause()) {
            result.setDeleteWhere((WhereSegment) visit(ctx.deleteWhereClause()));
        }
        result.addParameterMarkers(popAllStatementParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitMergeSetAssignmentsClause(final MergeSetAssignmentsClauseContext ctx) {
        Collection<ColumnAssignmentSegment> assignments = new LinkedList<>();
        for (MergeAssignmentContext each : ctx.mergeAssignment()) {
            assignments.add((ColumnAssignmentSegment) visit(each));
        }
        return new SetAssignmentSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), assignments);
    }
    
    @Override
    public ASTNode visitMergeAssignment(final MergeAssignmentContext ctx) {
        ColumnSegment column = (ColumnSegment) visitColumnName(ctx.columnName());
        ExpressionSegment value = (ExpressionSegment) visit(ctx.mergeAssignmentValue());
        List<ColumnSegment> columnSegments = new LinkedList<>();
        columnSegments.add(column);
        return new ColumnAssignmentSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), columnSegments, value);
    }
    
    @Override
    public ASTNode visitMergeAssignmentValue(final MergeAssignmentValueContext ctx) {
        ExprContext expr = ctx.expr();
        return null == expr ? new CommonExpressionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText()) : visit(expr);
    }
    
    @Override
    public ASTNode visitDeleteWhereClause(final DeleteWhereClauseContext ctx) {
        ASTNode segment = visit(ctx.whereClause().expr());
        return new WhereSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (ExpressionSegment) segment);
    }
}
