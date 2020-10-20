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

package org.apache.shardingsphere.sql.parser.sqlserver.visitor.impl;

import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.DMLVisitor;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AggregationClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AliasContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AssignmentContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AssignmentValueContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.AssignmentValuesContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ColumnNameContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ColumnNamesContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.CteClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.DeleteContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.DuplicateSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ExprContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.FromClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.GroupByClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.InsertContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.InsertDefaultValueContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.InsertSelectClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.InsertValuesClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.JoinSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.JoinedTableContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.MultipleTableNamesContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.MultipleTablesClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.OrderByClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.OrderByItemContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.OutputClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.OutputTableNameContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.OutputWithColumnContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.OutputWithColumnsContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ProjectionContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ProjectionsContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.QualifiedShorthandContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.SelectClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.SelectContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.SetAssignmentsClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.SingleTableClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.SubqueryContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.TableFactorContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.TableNameContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.TableReferenceContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.TableReferencesContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.TopContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.UpdateContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.WhereClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.WithClauseContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.PaginationValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.ParameterMarkerLimitValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.rownum.NumberLiteralRowNumberValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.rownum.ParameterMarkerRowNumberValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.rownum.RowNumberValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.top.TopProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OutputSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.DeleteMultiTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.BooleanLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.NumberLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.parametermarker.ParameterMarkerValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerDeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerUpdateStatement;
import org.apache.shardingsphere.sql.parser.sqlserver.visitor.SQLServerVisitor;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * DML visitor for SQLServer.
 */
public final class SQLServerDMLVisitor extends SQLServerVisitor implements DMLVisitor {
    
    @Override
    public ASTNode visitInsert(final InsertContext ctx) {
        SQLServerInsertStatement result;
        if (null != ctx.insertDefaultValue()) {
            result = (SQLServerInsertStatement) visit(ctx.insertDefaultValue());
        } else if (null != ctx.insertValuesClause()) {
            result = (SQLServerInsertStatement) visit(ctx.insertValuesClause());
        } else {
            result = (SQLServerInsertStatement) visit(ctx.insertSelectClause());
        }
        if (null != ctx.withClause()) {
            result.setWithSegment((WithSegment) visit(ctx.withClause()));
        }
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        result.setParameterCount(getCurrentParameterIndex());
        return result;
    }
    
    @Override
    public ASTNode visitInsertDefaultValue(final InsertDefaultValueContext ctx) {
        SQLServerInsertStatement result = new SQLServerInsertStatement();
        result.setInsertColumns(createInsertColumns(ctx.columnNames(), ctx.start.getStartIndex()));
        if (null != ctx.outputClause()) {
            result.setOutputSegment((OutputSegment) visit(ctx.outputClause()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitOutputClause(final OutputClauseContext ctx) {
        OutputSegment result = new OutputSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
        if (null != ctx.outputWithColumns()) {
            OutputWithColumnsContext outputWithColumnsContext = ctx.outputWithColumns();
            List<OutputWithColumnContext> outputWithColumnContexts = outputWithColumnsContext.outputWithColumn();
            Collection<ColumnProjectionSegment> outputColumns = new LinkedList<>();
            for (OutputWithColumnContext each : outputWithColumnContexts) {
                ColumnSegment column = new ColumnSegment(each.start.getStartIndex(), each.stop.getStopIndex(), new IdentifierValue(each.name().getText()));
                ColumnProjectionSegment outputColumn = new ColumnProjectionSegment(column);
                if (null != each.alias()) {
                    outputColumn.setAlias(new AliasSegment(each.alias().start.getStartIndex(), each.alias().stop.getStopIndex(), new IdentifierValue(each.name().getText())));
                }
                outputColumns.add(outputColumn);
            }
            result.getOutputColumns().addAll(outputColumns);
        }
        if (null != ctx.outputTableName()) {
            OutputTableNameContext outputTableNameContext = ctx.outputTableName();
            TableNameSegment tableName = new TableNameSegment(outputTableNameContext.start.getStartIndex(), 
                    outputTableNameContext.stop.getStopIndex(), new IdentifierValue(outputTableNameContext.getText()));
            result.setTableName(tableName);
            if (null != ctx.columnNames()) {
                ColumnNamesContext columnNames = ctx.columnNames();
                CollectionValue<ColumnSegment> columns = (CollectionValue<ColumnSegment>) visit(columnNames);
                result.getTableColumns().addAll(columns.getValue());
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitInsertValuesClause(final InsertValuesClauseContext ctx) {
        SQLServerInsertStatement result = new SQLServerInsertStatement();
        result.setInsertColumns(createInsertColumns(ctx.columnNames(), ctx.start.getStartIndex()));
        result.getValues().addAll(createInsertValuesSegments(ctx.assignmentValues()));
        if (null != ctx.outputClause()) {
            result.setOutputSegment((OutputSegment) visit(ctx.outputClause()));
        }
        return result;
    }
    
    private Collection<InsertValuesSegment> createInsertValuesSegments(final Collection<AssignmentValuesContext> assignmentValuesContexts) {
        Collection<InsertValuesSegment> result = new LinkedList<>();
        for (AssignmentValuesContext each : assignmentValuesContexts) {
            result.add((InsertValuesSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitInsertSelectClause(final InsertSelectClauseContext ctx) {
        SQLServerInsertStatement result = new SQLServerInsertStatement();
        result.setInsertColumns(createInsertColumns(ctx.columnNames(), ctx.start.getStartIndex()));
        result.setInsertSelect(createInsertSelectSegment(ctx));
        if (null != ctx.outputClause()) {
            result.setOutputSegment((OutputSegment) visit(ctx.outputClause()));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private InsertColumnsSegment createInsertColumns(final ColumnNamesContext columnNames, final int startIndex) {
        if (null != columnNames) {
            CollectionValue<ColumnSegment> columnSegments = (CollectionValue<ColumnSegment>) visit(columnNames);
            return new InsertColumnsSegment(columnNames.start.getStartIndex(), columnNames.stop.getStopIndex(), columnSegments.getValue());
        } else {
            return new InsertColumnsSegment(startIndex - 1, startIndex - 1, Collections.emptyList());
        }
    }
    
    private SubquerySegment createInsertSelectSegment(final InsertSelectClauseContext ctx) {
        SQLServerSelectStatement selectStatement = (SQLServerSelectStatement) visit(ctx.select());
        return new SubquerySegment(ctx.select().start.getStartIndex(), ctx.select().stop.getStopIndex(), selectStatement);
    }

    @Override
    public ASTNode visitWithClause(final WithClauseContext ctx) {
        List<CteClauseContext> cteClauses = ctx.cteClause();
        Collection<CommonTableExpressionSegment> commonTableExpressions = new LinkedList<>();
        for (CteClauseContext cte : cteClauses) {
            SubquerySegment subquery = new SubquerySegment(cte.start.getStartIndex(), cte.stop.getStopIndex(), (SQLServerSelectStatement) visit(cte.subquery()));
            IdentifierValue identifier = (IdentifierValue) visit(cte.identifier());
            CommonTableExpressionSegment commonTableExpression = new CommonTableExpressionSegment(cte.start.getStartIndex(), cte.stop.getStopIndex(), identifier, subquery);
            if (null != cte.columnNames()) {
                ColumnNamesContext columnNames = cte.columnNames();
                CollectionValue<ColumnSegment> columns = (CollectionValue<ColumnSegment>) visit(columnNames);
                commonTableExpression.getColumns().addAll(columns.getValue());
            }
            commonTableExpressions.add(commonTableExpression);
        }
        return new WithSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), commonTableExpressions);
    }
    
    @Override
    public ASTNode visitUpdate(final UpdateContext ctx) {
        SQLServerUpdateStatement result = new SQLServerUpdateStatement();
        result.setTableSegment((TableSegment) visit(ctx.tableReferences()));
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
    
    @Override
    public ASTNode visitDelete(final DeleteContext ctx) {
        SQLServerDeleteStatement result = new SQLServerDeleteStatement();
        if (null != ctx.multipleTablesClause()) {
            result.setTableSegment((TableSegment) visit(ctx.multipleTablesClause()));
        } else {
            result.setTableSegment((TableSegment) visit(ctx.singleTableClause()));
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
    
    @Override
    public ASTNode visitMultipleTablesClause(final MultipleTablesClauseContext ctx) {
        DeleteMultiTableSegment result = new DeleteMultiTableSegment();
        TableSegment relateTableSource = (TableSegment) visit(ctx.tableReferences());
        result.setRelationTable(relateTableSource);
        result.setActualDeleteTables(generateTablesFromTableMultipleTableNames(ctx.multipleTableNames()));
        return result;
    }
    
    private List<SimpleTableSegment> generateTablesFromTableMultipleTableNames(final MultipleTableNamesContext ctx) {
        List<SimpleTableSegment> result = new LinkedList<>();
        for (TableNameContext each : ctx.tableName()) {
            result.add((SimpleTableSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitSelect(final SelectContext ctx) {
        // TODO :Unsupported for withClause.
        SQLServerSelectStatement result = (SQLServerSelectStatement) visit(ctx.aggregationClause());
        result.setParameterCount(getCurrentParameterIndex());
        return result;
    }
    
    @Override
    public ASTNode visitAggregationClause(final AggregationClauseContext ctx) {
        // TODO :Unsupported for union | except | intersect SQL.
        return visit(ctx.selectClause(0));
    }
    
    @Override
    public ASTNode visitSelectClause(final SelectClauseContext ctx) {
        SQLServerSelectStatement result = new SQLServerSelectStatement();
        result.setProjections((ProjectionsSegment) visit(ctx.projections()));
        if (null != ctx.duplicateSpecification()) {
            result.getProjections().setDistinctRow(isDistinct(ctx));
        }
        if (null != ctx.fromClause()) {
            TableSegment tableSource = (TableSegment) visit(ctx.fromClause().tableReferences());
            result.setFrom(tableSource);
        }
        if (null != ctx.whereClause()) {
            result.setWhere((WhereSegment) visit(ctx.whereClause()));
        }
        if (null != ctx.groupByClause()) {
            result.setGroupBy((GroupBySegment) visit(ctx.groupByClause()));
        }
        if (null != ctx.orderByClause()) {
            result = visitOrderBy(result, ctx.orderByClause());
        }
        return result;
    }
    
    private SQLServerSelectStatement visitOrderBy(final SQLServerSelectStatement selectStatement, final OrderByClauseContext ctx) {
        Collection<OrderByItemSegment> items = new LinkedList<>();
        int orderByStartIndex = ctx.start.getStartIndex();
        int orderByStopIndex = ctx.start.getStartIndex();
        for (OrderByItemContext each : ctx.orderByItem()) {
            items.add((OrderByItemSegment) visit(each));
            orderByStopIndex = each.stop.getStopIndex();
        }
        OrderBySegment orderBySegment = new OrderBySegment(orderByStartIndex, orderByStopIndex, items);
        selectStatement.setOrderBy(orderBySegment);
        PaginationValueSegment offset = null;
        PaginationValueSegment rowcount = null;
        LimitSegment limitSegment = null;
        if (null != ctx.OFFSET()) {
            ASTNode astNode = visit(ctx.expr(0));
            if (astNode instanceof LiteralExpressionSegment && ((LiteralExpressionSegment) astNode).getLiterals() instanceof Number) {
                offset = new NumberLiteralLimitValueSegment(ctx.expr(0).start.getStartIndex(), ctx.expr(0).stop.getStopIndex(),
                        ((Number) ((LiteralExpressionSegment) astNode).getLiterals()).longValue());
            } else if (astNode instanceof ParameterMarkerExpressionSegment) {
                offset = new ParameterMarkerLimitValueSegment(ctx.expr(0).start.getStartIndex(), ctx.expr(0).stop.getStopIndex(), getCurrentParameterIndex());
            }
        }
        if (null != ctx.FETCH()) {
            ASTNode astNode = visit(ctx.expr(1));
            if (astNode instanceof LiteralExpressionSegment && ((LiteralExpressionSegment) astNode).getLiterals() instanceof Number) {
                rowcount = new NumberLiteralLimitValueSegment(ctx.expr(1).start.getStartIndex(), ctx.expr(1).stop.getStopIndex(),
                        ((Number) ((LiteralExpressionSegment) astNode).getLiterals()).longValue());
            } else if (astNode instanceof ParameterMarkerExpressionSegment) {
                rowcount = new ParameterMarkerLimitValueSegment(ctx.expr(1).start.getStartIndex(), ctx.expr(1).stop.getStopIndex(), getCurrentParameterIndex());
            }
        }
        if (null != offset) {
            limitSegment = new LimitSegment(ctx.OFFSET().getSymbol().getStartIndex(), ctx.stop.getStopIndex(), offset, rowcount);
        }
        selectStatement.setLimit(limitSegment);
        return selectStatement;
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
        if (null != ctx.top()) {
            RowNumberValueSegment rowNumber = (RowNumberValueSegment) visit(ctx.top());
            return new TopProjectionSegment(ctx.top().getStart().getStartIndex(), ctx.top().getStop().getStopIndex(), rowNumber,
                    alias == null ? null : alias.getIdentifier().getValue());
        }
        if (null != ctx.columnName()) {
            ColumnSegment column = (ColumnSegment) visit(ctx.columnName());
            ColumnProjectionSegment result = new ColumnProjectionSegment(column);
            result.setAlias(alias);
            return result;
        }
        return createProjection(ctx, alias);
    }
    
    @Override
    public ASTNode visitTop(final TopContext ctx) {
        int startIndex = ctx.topNum().getStart().getStartIndex();
        int stopIndex = ctx.topNum().getStop().getStopIndex();
        ASTNode topNum = visit(ctx.topNum());
        if (topNum instanceof NumberLiteralValue) {
            return new NumberLiteralRowNumberValueSegment(startIndex, stopIndex, ((NumberLiteralValue) topNum).getValue().longValue(), false);
        }
        return new ParameterMarkerRowNumberValueSegment(startIndex, stopIndex, ((ParameterMarkerValue) topNum).getValue(), false);
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
        if (projection instanceof BinaryOperationExpression) {
            int startIndex = ((BinaryOperationExpression) projection).getStartIndex();
            int stopIndex = null != alias ? alias.getStopIndex() : ((BinaryOperationExpression) projection).getStopIndex();
            ExpressionProjectionSegment result = new ExpressionProjectionSegment(startIndex, stopIndex, ((BinaryOperationExpression) projection).getText());
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
        TableSegment result = (TableSegment) visit(ctx.tableReference(0));
        if (ctx.tableReference().size() > 1) {
            for (int i = 1; i < ctx.tableReference().size(); i++) {
                result = generateJoinTableSourceFromTableReference(ctx.tableReference(i), result);
            }
        }
        return result;
    }
    
    private JoinTableSegment generateJoinTableSourceFromTableReference(final TableReferenceContext ctx, final TableSegment tableSegment) {
        JoinTableSegment result = new JoinTableSegment();
        result.setStartIndex(tableSegment.getStartIndex());
        result.setStopIndex(ctx.stop.getStopIndex());
        result.setLeft(tableSegment);
        result.setRight((TableSegment) visit(ctx));
        return result;
    }
    
    @Override
    public ASTNode visitTableReference(final TableReferenceContext ctx) {
        TableSegment result;
        TableSegment left;
        left = (TableSegment) visit(ctx.tableFactor());
        if (!ctx.joinedTable().isEmpty()) {
            for (JoinedTableContext each : ctx.joinedTable()) {
                left = visitJoinedTable(each, left);
            }
        }
        result = left;
        return result;
    }
    
    @Override
    public ASTNode visitTableFactor(final TableFactorContext ctx) {
        if (null != ctx.subquery()) {
            SQLServerSelectStatement subquery = (SQLServerSelectStatement) visit(ctx.subquery());
            SubquerySegment subquerySegment = new SubquerySegment(ctx.subquery().start.getStartIndex(), ctx.subquery().stop.getStopIndex(), subquery);
            SubqueryTableSegment result = new SubqueryTableSegment(subquerySegment);
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
    
    private JoinTableSegment visitJoinedTable(final JoinedTableContext ctx, final TableSegment tableSegment) {
        JoinTableSegment result = new JoinTableSegment();
        result.setLeft(tableSegment);
        result.setStartIndex(tableSegment.getStartIndex());
        result.setStopIndex(ctx.stop.getStopIndex());
        TableSegment right = (TableSegment) visit(ctx.tableFactor());
        result.setRight(right);
        if (null != ctx.joinSpecification()) {
            result = visitJoinSpecification(ctx.joinSpecification(), result);
        }
        return result;
    }
    
    private JoinTableSegment visitJoinSpecification(final JoinSpecificationContext ctx, final JoinTableSegment joinTableSource) {
        if (null != ctx.expr()) {
            ExpressionSegment condition = (ExpressionSegment) visit(ctx.expr());
            joinTableSource.setCondition(condition);
        }
        if (null != ctx.USING()) {
            List<ColumnSegment> columnSegmentList = new LinkedList<>();
            for (ColumnNameContext cname : ctx.columnNames().columnName()) {
                columnSegmentList.add((ColumnSegment) visit(cname));
            }
            joinTableSource.setUsing(columnSegmentList);
        }
        return joinTableSource;
    }
    
    @Override
    public ASTNode visitWhereClause(final WhereClauseContext ctx) {
        return new WhereSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (ExpressionSegment) visit(ctx.expr()));
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
        return visit(ctx.aggregationClause());
    }
}
