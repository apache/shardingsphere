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

package org.apache.shardingsphere.sql.parser.visitor.impl;

import com.google.common.base.Strings;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AssignmentContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AssignmentValueContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AssignmentValuesContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.DeleteContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.DuplicateSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ExprContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.FromClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.GroupByClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.InsertContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.InsertValuesClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.JoinedTableContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.MultipleTableNamesContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.MultipleTablesClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.OrderByItemContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ProjectionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ProjectionsContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.QualifiedShorthandContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.SelectClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.SelectContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.SetAssignmentsClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.SingleTableClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.TableFactorContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.TableNameContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.TableReferenceContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.TableReferencesContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.UnionClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.UpdateContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.WhereClauseContext;
import org.apache.shardingsphere.sql.parser.sql.ASTNode;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.complex.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.OrPredicateSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.sql.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.value.literal.impl.BooleanLiteralValue;
import org.apache.shardingsphere.sql.parser.visitor.SQLServerVisitor;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * SQLServer DML visitor.
 */
public final class SQLServerDMLVisitor extends SQLServerVisitor {
    
    @Override
    public ASTNode visitInsert(final InsertContext ctx) {
        InsertStatement result = (InsertStatement) visit(ctx.insertValuesClause());
        TableSegment table = (TableSegment) visit(ctx.tableName());
        result.setTable(table);
        result.getAllSQLSegments().add(table);
        result.setParametersCount(getCurrentParameterIndex());
        return result;
    }
    
    @Override
    public ASTNode visitInsertValuesClause(final InsertValuesClauseContext ctx) {
        InsertStatement result = new InsertStatement();
        if (null != ctx.columnNames()) { 
            InsertColumnsSegment insertColumnsSegment = (InsertColumnsSegment) visit(ctx.columnNames());
            result.setInsertColumns(insertColumnsSegment);
            result.getAllSQLSegments().add(insertColumnsSegment);
        } else {
            InsertColumnsSegment insertColumnsSegment =
                    new InsertColumnsSegment(ctx.start.getStartIndex() - 1, ctx.start.getStartIndex() - 1, Collections.<ColumnSegment>emptyList());
            result.setInsertColumns(insertColumnsSegment);
            result.getAllSQLSegments().add(insertColumnsSegment);
        }
        Collection<InsertValuesSegment> insertValuesSegments = createInsertValuesSegments(ctx.assignmentValues());
        result.getValues().addAll(insertValuesSegments);
        result.getAllSQLSegments().addAll(insertValuesSegments);
        return result;
    }
    
    private Collection<InsertValuesSegment> createInsertValuesSegments(final Collection<AssignmentValuesContext> assignmentValuesContexts) {
        Collection<InsertValuesSegment> result = new LinkedList<>();
        for (AssignmentValuesContext each : assignmentValuesContexts) {
            result.add((InsertValuesSegment) visit(each));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitUpdate(final UpdateContext ctx) {
        UpdateStatement result = new UpdateStatement();
        CollectionValue<TableSegment> tables = (CollectionValue<TableSegment>) visit(ctx.tableReferences());
        SetAssignmentSegment setSegment = (SetAssignmentSegment) visit(ctx.setAssignmentsClause());
        result.getTables().addAll(tables.getValue());
        result.setSetAssignment(setSegment);
        result.getAllSQLSegments().addAll(tables.getValue());
        result.getAllSQLSegments().add(setSegment);
        if (null != ctx.whereClause()) {
            WhereSegment whereSegment = (WhereSegment) visit(ctx.whereClause());
            result.setWhere(whereSegment);
            result.getAllSQLSegments().add(whereSegment);
        }
        result.setParametersCount(getCurrentParameterIndex());
        return result;
    }
    
    @Override
    public ASTNode visitSetAssignmentsClause(final SetAssignmentsClauseContext ctx) {
        Collection<AssignmentSegment> assignments = new LinkedList<>();
        for (AssignmentContext each : ctx.assignment()) {
            assignments.add((AssignmentSegment) visit(each));
        }
        return new SetAssignmentSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), assignments);
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
    public ASTNode visitAssignment(final AssignmentContext ctx) {
        ColumnSegment column = (ColumnSegment) visitColumnName(ctx.columnName());
        ExpressionSegment value = (ExpressionSegment) visit(ctx.assignmentValue());
        return new AssignmentSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), column, value);
    }
    
    @Override
    public ASTNode visitAssignmentValue(final AssignmentValueContext ctx) {
        ExprContext expr = ctx.expr();
        if (null != expr) {
            return visit(expr);
        }
        return new CommonExpressionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText());
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitDelete(final DeleteContext ctx) {
        DeleteStatement result = new DeleteStatement();
        if (null != ctx.multipleTablesClause()) {
            CollectionValue<TableSegment> tables = (CollectionValue<TableSegment>) visit(ctx.multipleTablesClause());
            result.getTables().addAll(tables.getValue());
            result.getAllSQLSegments().addAll(tables.getValue());
        } else {
            TableSegment table = (TableSegment) visit(ctx.singleTableClause());
            result.getTables().add(table);
            result.getAllSQLSegments().add(table);
        }
        if (null != ctx.whereClause()) {
            WhereSegment where = (WhereSegment) visit(ctx.whereClause());
            result.setWhere(where);
            result.getAllSQLSegments().add(where);
        }
        result.setParametersCount(getCurrentParameterIndex());
        return result;
    }
    
    @Override
    public ASTNode visitSingleTableClause(final SingleTableClauseContext ctx) {
        TableSegment result = (TableSegment) visit(ctx.tableName());
        if (null != ctx.alias()) {
            result.setAlias(ctx.alias().getText());
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitMultipleTablesClause(final MultipleTablesClauseContext ctx) {
        CollectionValue<TableSegment> result = new CollectionValue<>();
        result.combine((CollectionValue<TableSegment>) visit(ctx.multipleTableNames()));
        result.combine((CollectionValue<TableSegment>) visit(ctx.tableReferences()));
        return result;
    }
    
    @Override
    public ASTNode visitMultipleTableNames(final MultipleTableNamesContext ctx) {
        CollectionValue<TableSegment> result = new CollectionValue<>();
        for (TableNameContext each : ctx.tableName()) {
            result.getValue().add((TableSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitSelect(final SelectContext ctx) {
        // TODO : Unsupported for withClause.
        SelectStatement result = (SelectStatement) visit(ctx.unionClause());
        result.setParametersCount(getCurrentParameterIndex());
        return result;
    }
    
    @Override
    public ASTNode visitUnionClause(final UnionClauseContext ctx) {
        // TODO : Unsupported for union SQL.
        return visit(ctx.selectClause(0));
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitSelectClause(final SelectClauseContext ctx) {
        SelectStatement result = new SelectStatement();
        ProjectionsSegment projections = (ProjectionsSegment) visit(ctx.projections());
        result.setProjections(projections);
        result.getAllSQLSegments().add(projections);
        if (null != ctx.duplicateSpecification()) {
            result.getProjections().setDistinctRow(isDistinct(ctx));
        }
        if (null != ctx.fromClause()) {
            CollectionValue<TableSegment> tables = (CollectionValue<TableSegment>) visit(ctx.fromClause());
            result.getTables().addAll(tables.getValue());
            result.getAllSQLSegments().addAll(tables.getValue());
        }
        if (null != ctx.whereClause()) {
            WhereSegment where = (WhereSegment) visit(ctx.whereClause());
            result.setWhere(where);
            result.getAllSQLSegments().add(where);
        }
        if (null != ctx.groupByClause()) {
            GroupBySegment groupBy = (GroupBySegment) visit(ctx.groupByClause());
            result.setGroupBy(groupBy);
            result.getAllSQLSegments().add(groupBy);
        }
        if (null != ctx.orderByClause()) {
            OrderBySegment orderBy = (OrderBySegment) visit(ctx.orderByClause());
            result.setOrderBy(orderBy);
            result.getAllSQLSegments().add(orderBy);
        }
        return result;
    }

    private boolean isDistinct(final SelectClauseContext ctx) {
        return ((BooleanLiteralValue) visit(ctx.duplicateSpecification())).getValue();
    }
    
    @Override
    public ASTNode visitDuplicateSpecification(final DuplicateSpecificationContext ctx) {
        return new BooleanLiteralValue(null != ctx.DISTINCT());
    }
    
    @Override
    public ASTNode visitProjections(final ProjectionsContext ctx) {
        Collection<ProjectionSegment> projections = new LinkedList<>();
        if (null != ctx.unqualifiedShorthand()) {
            projections.add(
                    new ShorthandProjectionSegment(ctx.unqualifiedShorthand().getStart().getStartIndex(), ctx.unqualifiedShorthand().getStop().getStopIndex(), ctx.unqualifiedShorthand().getText()));
        }
        for (ProjectionContext each : ctx.projection()) {
            projections.add((ProjectionSegment) visit(each));
        }
        ProjectionsSegment result = new ProjectionsSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
        result.getProjections().addAll(projections);
        return result;
    }
    
    @Override
    public ASTNode visitProjection(final ProjectionContext ctx) {
        // FIXME: The stop index of project is the stop index of projection, instead of alias.
        if (null != ctx.qualifiedShorthand()) {
            QualifiedShorthandContext shorthand = ctx.qualifiedShorthand();
            ShorthandProjectionSegment result = new ShorthandProjectionSegment(shorthand.getStart().getStartIndex(), shorthand.getStop().getStopIndex(), shorthand.getText());
            IdentifierValue identifier = new IdentifierValue(shorthand.identifier().getText());
            result.setOwner(new TableSegment(shorthand.identifier().getStart().getStartIndex(), shorthand.identifier().getStop().getStopIndex(), identifier));
            return result;
        }
        String alias = null == ctx.alias() ? null : ctx.alias().getText();
        if (null != ctx.columnName()) {
            ColumnSegment column = (ColumnSegment) visit(ctx.columnName());
            ColumnProjectionSegment result = new ColumnProjectionSegment(ctx.columnName().getText(), column);
            result.setAlias(alias);
            return result;
        }
        return createProjection(ctx, alias);
    }
    
    private ASTNode createProjection(final ProjectionContext ctx, final String alias) {
        ASTNode projection = visit(ctx.expr());
        if (projection instanceof AggregationProjectionSegment) {
            ((AggregationProjectionSegment) projection).setAlias(alias);
            return projection;
        }
        if (projection instanceof ExpressionProjectionSegment) {
            ((ExpressionProjectionSegment) projection).setAlias(alias);
            return projection;
        }
        if (projection instanceof CommonExpressionSegment) {
            CommonExpressionSegment segment = (CommonExpressionSegment) projection;
            ExpressionProjectionSegment result = new ExpressionProjectionSegment(segment.getStartIndex(), segment.getStopIndex(), segment.getText());
            result.setAlias(alias);
            return result;
        }
        // FIXME :For DISTINCT()
        if (projection instanceof ColumnSegment) {
            ExpressionProjectionSegment result = new ExpressionProjectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.getText());
            result.setAlias(alias);
            return result;
        }
        if (projection instanceof SubquerySegment) {
            return new SubquerySegment(((SubquerySegment) projection).getStartIndex(), ((SubquerySegment) projection).getStopIndex(), ((SubquerySegment) projection).getText());
        }
        LiteralExpressionSegment column = (LiteralExpressionSegment) projection;
        ExpressionProjectionSegment result = Strings.isNullOrEmpty(alias) ? new ExpressionProjectionSegment(column.getStartIndex(), column.getStopIndex(), String.valueOf(column.getLiterals()))
                : new ExpressionProjectionSegment(column.getStartIndex(), ctx.alias().stop.getStopIndex(), String.valueOf(column.getLiterals()));
        result.setAlias(alias);
        return result;
    }
    
    @Override
    public ASTNode visitFromClause(final FromClauseContext ctx) {
        return visit(ctx.tableReferences());
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitTableReferences(final TableReferencesContext ctx) {
        CollectionValue<TableSegment> result = new CollectionValue<>();
        for (TableReferenceContext each : ctx.tableReference()) {
            result.combine((CollectionValue<TableSegment>) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitTableReference(final TableReferenceContext ctx) {
        CollectionValue<TableSegment> result = new CollectionValue<>();
        if (null != ctx.tableFactor()) {
            result.getValue().add((TableSegment) visit(ctx.tableFactor()));
        }
        if (null != ctx.joinedTable()) {
            for (JoinedTableContext each : ctx.joinedTable()) {
                result.getValue().add((TableSegment) visit(each));
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitTableFactor(final TableFactorContext ctx) {
        if (null != ctx.tableReferences()) {
            return visit(ctx.tableReferences());
        }
        TableSegment table = (TableSegment) visit(ctx.tableName());
        if (null != ctx.alias()) {
            table.setAlias(ctx.alias().getText());
        }
        return table;
    }
    
    @Override
    public ASTNode visitJoinedTable(final JoinedTableContext ctx) {
        return visit(ctx.tableFactor());
    }
    
    @Override
    public ASTNode visitWhereClause(final WhereClauseContext ctx) {
        WhereSegment result = new WhereSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
        ASTNode segment = visit(ctx.expr());
        if (segment instanceof OrPredicateSegment) {
            result.getAndPredicates().addAll(((OrPredicateSegment) segment).getAndPredicates());
        } else if (segment instanceof PredicateSegment) {
            AndPredicate andPredicate = new AndPredicate();
            andPredicate.getPredicates().add((PredicateSegment) segment);
            result.getAndPredicates().add(andPredicate);
        }
        return result;
    }
    
    @Override
    public ASTNode visitGroupByClause(final GroupByClauseContext ctx) {
        Collection<OrderByItemSegment> items = new LinkedList<>();
        for (OrderByItemContext each : ctx.orderByItem()) {
            items.add((OrderByItemSegment) visit(each));
        }
        return new GroupBySegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), items);
    }
}
