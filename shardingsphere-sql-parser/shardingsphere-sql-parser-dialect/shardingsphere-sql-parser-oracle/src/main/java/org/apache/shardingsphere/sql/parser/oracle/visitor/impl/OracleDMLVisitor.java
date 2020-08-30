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

package org.apache.shardingsphere.sql.parser.oracle.visitor.impl;

import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.DMLVisitor;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AliasContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AssignmentContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AssignmentValueContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AssignmentValuesContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ColumnNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ColumnNamesContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DeleteContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DuplicateSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ExprContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.FromClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.GroupByClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.InsertContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.InsertValuesClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.JoinSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.JoinedTableContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.MultipleTableNamesContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.MultipleTablesClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.OrderByItemContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ProjectionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ProjectionsContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.QualifiedShorthandContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.SelectClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.SelectContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.SetAssignmentsClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.SingleTableClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.SubqueryContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.TableFactorContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.TableNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.TableReferenceContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.TableReferencesContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.UnionClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.UpdateContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.WhereClauseContext;
import org.apache.shardingsphere.sql.parser.oracle.visitor.OracleVisitor;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.JoinSpecificationSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.JoinedTableSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.TableFactorSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.TableReferenceSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.LockSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.OrPredicateSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.sql.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.value.literal.impl.BooleanLiteralValue;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * DML visitor for Oracle.
 */
public final class OracleDMLVisitor extends OracleVisitor implements DMLVisitor {
    
    @Override
    public ASTNode visitInsert(final InsertContext ctx) {
        // TODO :FIXME, since there is no segment for insertValuesClause, InsertStatement is created by sub rule.
        // TODO :deal with insert select
        if (null != ctx.insertSingleTable()) {
            InsertStatement result = (InsertStatement) visit(ctx.insertSingleTable().insertValuesClause());
            result.setTable((SimpleTableSegment) visit(ctx.insertSingleTable().insertIntoClause().tableName()));
            result.setParameterCount(getCurrentParameterIndex());
            return result;
        }
        return new InsertStatement();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitInsertValuesClause(final InsertValuesClauseContext ctx) {
        InsertStatement result = new InsertStatement();
        if (null != ctx.columnNames()) {
            ColumnNamesContext columnNames = ctx.columnNames();
            CollectionValue<ColumnSegment> columnSegments = (CollectionValue<ColumnSegment>) visit(columnNames);
            result.setInsertColumns(new InsertColumnsSegment(columnNames.start.getStartIndex(), columnNames.stop.getStopIndex(), columnSegments.getValue()));
        } else {
            result.setInsertColumns(new InsertColumnsSegment(ctx.start.getStartIndex() - 1, ctx.start.getStartIndex() - 1, Collections.emptyList()));
        }
        result.getValues().addAll(createInsertValuesSegments(ctx.assignmentValues()));
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
        CollectionValue<TableReferenceSegment> tableReferences = (CollectionValue<TableReferenceSegment>) visit(ctx.tableReferences());
        for (TableReferenceSegment each : tableReferences.getValue()) {
            result.getTables().addAll(each.getSimpleTableSegments());
        }
        result.setSetAssignment((SetAssignmentSegment) visit(ctx.setAssignmentsClause()));
        if (null != ctx.whereClause()) {
            result.setWhere((WhereSegment) visit(ctx.whereClause()));
        }
        result.setParameterCount(getCurrentParameterIndex());
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
            result.getTables().addAll(((CollectionValue<SimpleTableSegment>) visit(ctx.multipleTablesClause())).getValue());
        } else {
            result.getTables().add((SimpleTableSegment) visit(ctx.singleTableClause()));
        }
        if (null != ctx.whereClause()) {
            result.setWhere((WhereSegment) visit(ctx.whereClause()));
        }
        result.setParameterCount(getCurrentParameterIndex());
        return result;
    }
    
    @Override
    public ASTNode visitSingleTableClause(final SingleTableClauseContext ctx) {
        SimpleTableSegment result = (SimpleTableSegment) visit(ctx.tableName());
        if (null != ctx.alias()) {
            result.setAlias((AliasSegment) visit(ctx.alias()));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitMultipleTablesClause(final MultipleTablesClauseContext ctx) {
        CollectionValue<SimpleTableSegment> result = new CollectionValue<>();
        result.combine((CollectionValue<SimpleTableSegment>) visit(ctx.multipleTableNames()));
        CollectionValue<TableReferenceSegment> tableReferences = (CollectionValue<TableReferenceSegment>) visit(ctx.tableReferences());
        for (TableReferenceSegment each : tableReferences.getValue()) {
            result.getValue().addAll(each.getSimpleTableSegments());
        }
        return result;
    }
    
    @Override
    public ASTNode visitMultipleTableNames(final MultipleTableNamesContext ctx) {
        CollectionValue<SimpleTableSegment> result = new CollectionValue<>();
        for (TableNameContext each : ctx.tableName()) {
            result.getValue().add((SimpleTableSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitSelect(final SelectContext ctx) {
        // TODO :Unsupported for withClause.
        SelectStatement result = (SelectStatement) visit(ctx.unionClause());
        result.setParameterCount(getCurrentParameterIndex());
        return result;
    }
    
    @Override
    public ASTNode visitUnionClause(final UnionClauseContext ctx) {
        // TODO :Unsupported for union SQL.
        return visit(ctx.selectClause(0));
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitSelectClause(final SelectClauseContext ctx) {
        SelectStatement result = new SelectStatement();
        result.setProjections((ProjectionsSegment) visit(ctx.projections()));
        if (null != ctx.duplicateSpecification()) {
            result.getProjections().setDistinctRow(isDistinct(ctx));
        }
        if (null != ctx.fromClause()) {
            CollectionValue<TableReferenceSegment> tableReferences = (CollectionValue<TableReferenceSegment>) visit(ctx.fromClause());
            for (TableReferenceSegment each : tableReferences.getValue()) {
                result.getTableReferences().add(each);
            }
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
        if (null != ctx.lockClause()) {
            result.setLock((LockSegment) visit(ctx.lockClause()));
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
            projections.add(new ShorthandProjectionSegment(ctx.unqualifiedShorthand().getStart().getStartIndex(), ctx.unqualifiedShorthand().getStop().getStopIndex()));
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
        // FIXME :The stop index of project is the stop index of projection, instead of alias.
        if (null != ctx.qualifiedShorthand()) {
            QualifiedShorthandContext shorthand = ctx.qualifiedShorthand();
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
    public ASTNode visitAlias(final AliasContext ctx) {
        if (null != ctx.identifier()) {
            return new AliasSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), (IdentifierValue) visit(ctx.identifier()));
        }
        return new AliasSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), new IdentifierValue(ctx.STRING_().getText()));
    }
    
    private ASTNode createProjection(final ProjectionContext ctx, final AliasSegment alias) {
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
            ColumnProjectionSegment result = new ColumnProjectionSegment((ColumnSegment) projection);
            result.setAlias(alias);
            return result;
        }
        if (projection instanceof SubqueryExpressionSegment) {
            SubqueryProjectionSegment result = new SubqueryProjectionSegment(((SubqueryExpressionSegment) projection).getSubquery());
            result.setAlias(alias);
            return result;
        }
        LiteralExpressionSegment column = (LiteralExpressionSegment) projection;
        ExpressionProjectionSegment result = null == alias ? new ExpressionProjectionSegment(column.getStartIndex(), column.getStopIndex(), String.valueOf(column.getLiterals()))
                : new ExpressionProjectionSegment(column.getStartIndex(), ctx.alias().stop.getStopIndex(), String.valueOf(column.getLiterals()));
        result.setAlias(alias);
        return result;
    }
    
    @Override
    public ASTNode visitFromClause(final FromClauseContext ctx) {
        return visit(ctx.tableReferences());
    }
    
    @Override
    public ASTNode visitTableReferences(final TableReferencesContext ctx) {
        CollectionValue<TableReferenceSegment> result = new CollectionValue<>();
        for (TableReferenceContext each : ctx.tableReference()) {
            result.getValue().add((TableReferenceSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitTableReference(final TableReferenceContext ctx) {
        TableReferenceSegment result = new TableReferenceSegment();
        if (null != ctx.tableFactor()) {
            TableFactorSegment tableFactor = (TableFactorSegment) visit(ctx.tableFactor());
            result.setTableFactor(tableFactor);
        }
        if (!ctx.joinedTable().isEmpty()) {
            for (JoinedTableContext each : ctx.joinedTable()) {
                JoinedTableSegment joinedTableSegment = (JoinedTableSegment) visit(each);
                result.getJoinedTables().add(joinedTableSegment);
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitTableFactor(final TableFactorContext ctx) {
        TableFactorSegment result = new TableFactorSegment();
        if (null != ctx.subquery()) {
            SelectStatement subquery = (SelectStatement) visit(ctx.subquery());
            SubquerySegment subquerySegment = new SubquerySegment(ctx.subquery().start.getStartIndex(), ctx.subquery().stop.getStopIndex(), subquery);
            SubqueryTableSegment subqueryTableSegment = new SubqueryTableSegment(subquerySegment);
            if (null != ctx.alias()) {
                subqueryTableSegment.setAlias((AliasSegment) visit(ctx.alias()));
            }
            result.setTable(subqueryTableSegment);
        }
        if (null != ctx.tableName()) {
            SimpleTableSegment table = (SimpleTableSegment) visit(ctx.tableName());
            if (null != ctx.alias()) {
                table.setAlias((AliasSegment) visit(ctx.alias()));
            }
            result.setTable(table);
        }
        if (null != ctx.tableReferences()) {
            CollectionValue<TableReferenceSegment> tableReferences = (CollectionValue) visit(ctx.tableReferences());
            result.getTableReferences().addAll(tableReferences.getValue());
        }
        return result;
    }
    
    @Override
    public ASTNode visitJoinedTable(final JoinedTableContext ctx) {
        JoinedTableSegment result = new JoinedTableSegment();
        TableFactorSegment tableFactor = (TableFactorSegment) visit(ctx.tableFactor());
        result.setTableFactor(tableFactor);
        if (null != ctx.joinSpecification()) {
            result.setJoinSpecification((JoinSpecificationSegment) visit(ctx.joinSpecification()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitJoinSpecification(final JoinSpecificationContext ctx) {
        JoinSpecificationSegment result = new JoinSpecificationSegment();
        if (null != ctx.expr()) {
            ASTNode expr = visit(ctx.expr());
            if (expr instanceof PredicateSegment) {
                AndPredicate andPredicate = new AndPredicate();
                andPredicate.getPredicates().add((PredicateSegment) expr);
                result.getAndPredicates().add(andPredicate);
            }
            if (expr instanceof OrPredicateSegment) {
                result.getAndPredicates().addAll(((OrPredicateSegment) expr).getAndPredicates());
            }
        }
        if (null != ctx.USING()) {
            Collection<ColumnSegment> columnSegmentList = new LinkedList<>();
            for (ColumnNameContext each : ctx.columnNames().columnName()) {
                columnSegmentList.add((ColumnSegment) visit(each));
            }
            result.getUsingColumns().addAll(columnSegmentList);
        }
        return result;
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
    
    @Override
    public ASTNode visitSubquery(final SubqueryContext ctx) {
        return visit(ctx.unionClause());
    }
}
