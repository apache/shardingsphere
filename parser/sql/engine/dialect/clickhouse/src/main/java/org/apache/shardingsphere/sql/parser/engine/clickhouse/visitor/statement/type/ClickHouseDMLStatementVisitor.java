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

package org.apache.shardingsphere.sql.parser.engine.clickhouse.visitor.statement.type;

import org.antlr.v4.runtime.misc.Interval;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DMLStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.ClickHouseStatementParser;
import org.apache.shardingsphere.sql.parser.autogen.ClickHouseStatementParser.AssignmentValuesContext;
import org.apache.shardingsphere.sql.parser.autogen.ClickHouseStatementParser.ColumnNamesContext;
import org.apache.shardingsphere.sql.parser.autogen.ClickHouseStatementParser.DeleteContext;
import org.apache.shardingsphere.sql.parser.autogen.ClickHouseStatementParser.ExprContext;
import org.apache.shardingsphere.sql.parser.autogen.ClickHouseStatementParser.InsertContext;
import org.apache.shardingsphere.sql.parser.autogen.ClickHouseStatementParser.InsertValuesClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.ClickHouseStatementParser.SelectClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.ClickHouseStatementParser.SubqueryContext;
import org.apache.shardingsphere.sql.parser.engine.clickhouse.visitor.statement.ClickHouseStatementVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.enums.JoinType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.HavingSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.BooleanLiteralValue;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * DML Statement visitor for ClickHouse.
 */
public final class ClickHouseDMLStatementVisitor extends ClickHouseStatementVisitor implements DMLStatementVisitor {
    
    public ClickHouseDMLStatementVisitor(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    @Override
    public ASTNode visitInsert(final InsertContext ctx) {
        InsertStatement result = (InsertStatement) visit(ctx.insertValuesClause());
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        result.addParameterMarkers(getParameterMarkerSegments());
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitInsertValuesClause(final InsertValuesClauseContext ctx) {
        InsertStatement result = new InsertStatement(getDatabaseType());
        if (null != ctx.columnNames()) {
            ColumnNamesContext columnNames = ctx.columnNames();
            CollectionValue<ColumnSegment> columnSegments = (CollectionValue<ColumnSegment>) visit(columnNames);
            result.setInsertColumns(new InsertColumnsSegment(columnNames.start.getStartIndex(), columnNames.stop.getStopIndex(), columnSegments.getValue()));
        } else {
            result.setInsertColumns(new InsertColumnsSegment(ctx.start.getStartIndex() - 1, ctx.start.getStartIndex() - 1, Collections.emptyList()));
        }
        result.getValues().addAll(ctx.assignmentValues().stream().map(each -> (InsertValuesSegment) visit(each)).collect(Collectors.toList()));
        return result;
    }
    
    @Override
    public ASTNode visitUpdate(final ClickHouseStatementParser.UpdateContext ctx) {
        UpdateStatement result = new UpdateStatement(getDatabaseType());
        result.setTable((TableSegment) visit(ctx.tableReferences()));
        result.setSetAssignment((SetAssignmentSegment) visit(ctx.setAssignmentsClause()));
        if (null != ctx.whereClause()) {
            result.setWhere((WhereSegment) visit(ctx.whereClause()));
        }
        result.addParameterMarkers(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitSetAssignmentsClause(final ClickHouseStatementParser.SetAssignmentsClauseContext ctx) {
        return new SetAssignmentSegment(
                ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.assignment().stream().map(each -> (ColumnAssignmentSegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitAssignmentValues(final AssignmentValuesContext ctx) {
        return new InsertValuesSegment(
                ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.assignmentValue().stream().map(each -> (ExpressionSegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitAssignment(final ClickHouseStatementParser.AssignmentContext ctx) {
        ColumnSegment column = (ColumnSegment) visitColumnName(ctx.columnName());
        ExpressionSegment value = (ExpressionSegment) visit(ctx.assignmentValue());
        ColumnAssignmentSegment result = new ColumnAssignmentSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), Collections.singletonList(column), value);
        result.getColumns().add(column);
        return result;
    }
    
    @Override
    public ASTNode visitAssignmentValue(final ClickHouseStatementParser.AssignmentValueContext ctx) {
        ExprContext expr = ctx.expr();
        return null == expr ? new CommonExpressionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText()) : visit(expr);
    }
    
    @Override
    public ASTNode visitDelete(final DeleteContext ctx) {
        DeleteStatement result = new DeleteStatement(getDatabaseType());
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        if (null != ctx.whereClause()) {
            result.setWhere((WhereSegment) visit(ctx.whereClause()));
        }
        result.addParameterMarkers(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitSingleTableClause(final ClickHouseStatementParser.SingleTableClauseContext ctx) {
        SimpleTableSegment result = (SimpleTableSegment) visit(ctx.tableName());
        if (null != ctx.alias()) {
            result.setAlias((AliasSegment) visit(ctx.alias()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitSelect(final ClickHouseStatementParser.SelectContext ctx) {
        // TODO :Unsupported for withClause.
        SelectStatement result = (SelectStatement) visit(ctx.combineClause());
        result.addParameterMarkers(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitCombineClause(final ClickHouseStatementParser.CombineClauseContext ctx) {
        // TODO :Unsupported for union SQL.
        return visit(ctx.selectClause(0));
    }
    
    @Override
    public ASTNode visitSelectClause(final SelectClauseContext ctx) {
        SelectStatement result = new SelectStatement(getDatabaseType());
        result.setProjections((ProjectionsSegment) visit(ctx.projections()));
        if (!ctx.selectSpecification().isEmpty()) {
            result.getProjections().setDistinctRow(isDistinct(ctx.selectSpecification().get(0)));
        }
        if (null != ctx.fromClause()) {
            TableSegment tableSegment = (TableSegment) visit(ctx.fromClause());
            result.setFrom(tableSegment);
        }
        if (null != ctx.whereClause()) {
            result.setWhere((WhereSegment) visit(ctx.whereClause()));
        }
        if (null != ctx.groupByClause()) {
            result.setGroupBy((GroupBySegment) visit(ctx.groupByClause()));
        }
        if (null != ctx.orderByClause()) {
            result.setOrderBy((OrderBySegment) visit(ctx.orderByClause()));
        }
        if (null != ctx.havingClause()) {
            result.setHaving((HavingSegment) visit(ctx.havingClause()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitHavingClause(final ClickHouseStatementParser.HavingClauseContext ctx) {
        return new HavingSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (ExpressionSegment) visit(ctx.expr()));
    }
    
    private boolean isDistinct(final ClickHouseStatementParser.SelectSpecificationContext ctx) {
        return ((BooleanLiteralValue) visit(ctx.duplicateSpecification())).getValue();
    }
    
    @Override
    public ASTNode visitDuplicateSpecification(final ClickHouseStatementParser.DuplicateSpecificationContext ctx) {
        return new BooleanLiteralValue(null != ctx.DISTINCT());
    }
    
    @Override
    public ASTNode visitProjections(final ClickHouseStatementParser.ProjectionsContext ctx) {
        Collection<ProjectionSegment> projections = new LinkedList<>();
        if (null != ctx.unqualifiedShorthand()) {
            projections.add(new ShorthandProjectionSegment(ctx.unqualifiedShorthand().getStart().getStartIndex(), ctx.unqualifiedShorthand().getStop().getStopIndex()));
        }
        for (ClickHouseStatementParser.ProjectionContext each : ctx.projection()) {
            projections.add((ProjectionSegment) visit(each));
        }
        ProjectionsSegment result = new ProjectionsSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
        result.getProjections().addAll(projections);
        return result;
    }
    
    @Override
    public ASTNode visitProjection(final ClickHouseStatementParser.ProjectionContext ctx) {
        // FIXME :The stop index of project is the stop index of projection, instead of alias.
        if (null != ctx.qualifiedShorthand()) {
            ClickHouseStatementParser.QualifiedShorthandContext shorthand = ctx.qualifiedShorthand();
            ShorthandProjectionSegment result = new ShorthandProjectionSegment(shorthand.getStart().getStartIndex(), shorthand.getStop().getStopIndex());
            IdentifierValue identifier = new IdentifierValue(shorthand.identifier().getText());
            result.setOwner(new OwnerSegment(shorthand.identifier().getStart().getStartIndex(), shorthand.identifier().getStop().getStopIndex(), identifier));
            return result;
        }
        AliasSegment alias = null == ctx.alias() ? null : (AliasSegment) visit(ctx.alias());
        if (null != ctx.columnName()) {
            ColumnSegment column = (ColumnSegment) visit(ctx.columnName());
            ColumnProjectionSegment result = new ColumnProjectionSegment(column);
            result.setAlias(alias);
            return result;
        }
        return createProjection(ctx, alias);
    }
    
    @Override
    public ASTNode visitAlias(final ClickHouseStatementParser.AliasContext ctx) {
        return null == ctx.identifier()
                ? new AliasSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), new IdentifierValue(ctx.STRING_().getText()))
                : new AliasSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), (IdentifierValue) visit(ctx.identifier()));
    }
    
    private ASTNode createProjection(final ClickHouseStatementParser.ProjectionContext ctx, final AliasSegment alias) {
        ASTNode projection = visit(ctx.expr());
        if (projection instanceof AggregationProjectionSegment) {
            ((AggregationProjectionSegment) projection).setAlias(alias);
            return projection;
        }
        if (projection instanceof ExpressionProjectionSegment) {
            ((ExpressionProjectionSegment) projection).setAlias(alias);
            return projection;
        }
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
        // FIXME :For DISTINCT()
        if (projection instanceof ColumnSegment) {
            ExpressionProjectionSegment result = new ExpressionProjectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), getOriginalText(ctx), (ColumnSegment) projection);
            result.setAlias(alias);
            return result;
        }
        if (projection instanceof SubqueryExpressionSegment) {
            SubqueryExpressionSegment subqueryExpressionSegment = (SubqueryExpressionSegment) projection;
            String text = ctx.start.getInputStream().getText(new Interval(subqueryExpressionSegment.getStartIndex(), subqueryExpressionSegment.getStopIndex()));
            SubqueryProjectionSegment result = new SubqueryProjectionSegment(((SubqueryExpressionSegment) projection).getSubquery(), text);
            result.setAlias(alias);
            return result;
        }
        if (projection instanceof BinaryOperationExpression) {
            BinaryOperationExpression binaryExpression = (BinaryOperationExpression) projection;
            int startIndex = binaryExpression.getStartIndex();
            int stopIndex = null == alias ? binaryExpression.getStopIndex() : alias.getStopIndex();
            ExpressionProjectionSegment result = new ExpressionProjectionSegment(startIndex, stopIndex, binaryExpression.getText(), binaryExpression);
            result.setAlias(alias);
            return result;
        }
        if (projection instanceof ParameterMarkerExpressionSegment) {
            ParameterMarkerExpressionSegment result = (ParameterMarkerExpressionSegment) projection;
            result.setAlias(alias);
            return projection;
        }
        ExpressionSegment column = (ExpressionSegment) projection;
        ExpressionProjectionSegment result = null == alias ? new ExpressionProjectionSegment(column.getStartIndex(), column.getStopIndex(), column.getText(), column)
                : new ExpressionProjectionSegment(column.getStartIndex(), ctx.alias().stop.getStopIndex(), String.valueOf(column.getText()), column);
        result.setAlias(alias);
        return result;
    }
    
    @Override
    public ASTNode visitFromClause(final ClickHouseStatementParser.FromClauseContext ctx) {
        return visit(ctx.tableReferences());
    }
    
    @Override
    public ASTNode visitTableReferences(final ClickHouseStatementParser.TableReferencesContext ctx) {
        TableSegment result = (TableSegment) visit(ctx.escapedTableReference(0));
        if (ctx.escapedTableReference().size() > 1) {
            for (int i = 1; i < ctx.escapedTableReference().size(); i++) {
                result = generateJoinTableSourceFromEscapedTableReference(ctx.escapedTableReference(i), result);
            }
        }
        return result;
    }
    
    private JoinTableSegment generateJoinTableSourceFromEscapedTableReference(final ClickHouseStatementParser.EscapedTableReferenceContext ctx, final TableSegment tableSegment) {
        JoinTableSegment result = new JoinTableSegment();
        result.setStartIndex(tableSegment.getStartIndex());
        result.setStopIndex(ctx.stop.getStopIndex());
        result.setLeft(tableSegment);
        result.setRight((TableSegment) visit(ctx));
        result.setJoinType(JoinType.COMMA.name());
        return result;
    }
    
    @Override
    public ASTNode visitEscapedTableReference(final ClickHouseStatementParser.EscapedTableReferenceContext ctx) {
        return visit(ctx.tableReference());
    }
    
    @Override
    public ASTNode visitTableReference(final ClickHouseStatementParser.TableReferenceContext ctx) {
        TableSegment result;
        TableSegment left;
        left = (TableSegment) visit(ctx.tableFactor());
        if (!ctx.joinedTable().isEmpty()) {
            for (ClickHouseStatementParser.JoinedTableContext each : ctx.joinedTable()) {
                left = visitJoinedTable(each, left);
            }
        }
        result = left;
        return result;
    }
    
    @Override
    public ASTNode visitTableFactor(final ClickHouseStatementParser.TableFactorContext ctx) {
        if (null != ctx.subquery()) {
            SelectStatement subquery = (SelectStatement) visit(ctx.subquery());
            SubquerySegment subquerySegment = new SubquerySegment(ctx.subquery().start.getStartIndex(), ctx.subquery().stop.getStopIndex(), subquery, getOriginalText(ctx.subquery()));
            SubqueryTableSegment result = new SubqueryTableSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), subquerySegment);
            if (null != ctx.alias()) {
                result.setAlias((AliasSegment) visit(ctx.alias()));
            }
            return result;
        }
        if (null != ctx.tableName()) {
            SimpleTableSegment result = (SimpleTableSegment) visit(ctx.tableName());
            if (null != ctx.alias()) {
                result.setAlias((AliasSegment) visit(ctx.alias()));
            }
            return result;
        }
        return visit(ctx.tableReferences());
    }
    
    private JoinTableSegment visitJoinedTable(final ClickHouseStatementParser.JoinedTableContext ctx, final TableSegment tableSegment) {
        JoinTableSegment result = new JoinTableSegment();
        result.setLeft(tableSegment);
        result.setStartIndex(tableSegment.getStartIndex());
        result.setStopIndex(ctx.stop.getStopIndex());
        TableSegment right = (TableSegment) visit(ctx.tableFactor());
        result.setRight(right);
        result.setJoinType(getJoinType(ctx));
        if (null != ctx.joinSpecification()) {
            visitJoinSpecification(ctx.joinSpecification(), result);
        }
        return result;
    }
    
    private String getJoinType(final ClickHouseStatementParser.JoinedTableContext ctx) {
        if (null != ctx.LEFT()) {
            return JoinType.LEFT.name();
        }
        if (null != ctx.RIGHT()) {
            return JoinType.RIGHT.name();
        }
        if (null != ctx.INNER()) {
            return JoinType.INNER.name();
        }
        if (null != ctx.CROSS()) {
            return JoinType.CROSS.name();
        }
        return JoinType.INNER.name();
    }
    
    private void visitJoinSpecification(final ClickHouseStatementParser.JoinSpecificationContext ctx, final JoinTableSegment joinTableSource) {
        if (null != ctx.expr()) {
            joinTableSource.setCondition((ExpressionSegment) visit(ctx.expr()));
        }
        if (null != ctx.USING()) {
            joinTableSource.setUsing(ctx.columnNames().columnName().stream().map(each -> (ColumnSegment) visit(each)).collect(Collectors.toList()));
        }
    }
    
    @Override
    public ASTNode visitWhereClause(final ClickHouseStatementParser.WhereClauseContext ctx) {
        return new WhereSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (ExpressionSegment) visit(ctx.expr()));
    }
    
    @Override
    public ASTNode visitGroupByClause(final ClickHouseStatementParser.GroupByClauseContext ctx) {
        return new GroupBySegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.orderByItem().stream().map(each -> (OrderByItemSegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitSubquery(final SubqueryContext ctx) {
        return visit(ctx.combineClause());
    }
}
