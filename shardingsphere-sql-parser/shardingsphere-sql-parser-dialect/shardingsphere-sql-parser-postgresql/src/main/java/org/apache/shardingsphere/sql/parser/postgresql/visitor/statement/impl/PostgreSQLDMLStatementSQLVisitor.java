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

package org.apache.shardingsphere.sql.parser.postgresql.visitor.statement.impl;

import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.shardingsphere.sql.parser.engine.visitor.operation.SQLStatementVisitor;
import org.apache.shardingsphere.sql.parser.engine.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.engine.visitor.type.DMLSQLVisitor;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AExprContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AliasClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AttrNameContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CallContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ColIdContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DeleteContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DoStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ExprListContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ForLockingClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.FromClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.FromListContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.GroupByItemContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.GroupClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.InsertColumnItemContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.InsertColumnListContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.InsertContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.InsertRestContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.InsertTargetContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.JoinQualContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.JoinedTableContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.LimitClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.NameListContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.QualifiedNameContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.RelationExprContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.RelationExprOptAliasContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.SelectClauseNContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.SelectContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.SelectLimitContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.SelectLimitValueContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.SelectNoParensContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.SelectOffsetValueContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.SelectWithParensContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.SetClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.SetClauseListContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.SetTargetContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.SimpleSelectContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.TableReferenceContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.TargetElContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.TargetListContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.UpdateContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ValuesClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.WhereClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.WhereOrCurrentClauseContext;
import org.apache.shardingsphere.sql.parser.sql.common.constant.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationDistinctProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.LimitValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.ParameterMarkerLimitValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.LockSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLCallStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLDeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLDoStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLUpdateStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * DML Statement SQL visitor for PostgreSQL.
 */
public final class PostgreSQLDMLStatementSQLVisitor extends PostgreSQLStatementSQLVisitor implements DMLSQLVisitor, SQLStatementVisitor {
    
    @Override
    public ASTNode visitInsert(final InsertContext ctx) {
        // TODO :FIXME, since there is no segment for insertValuesClause, InsertStatement is created by sub rule.
        // TODO :deal with insert select
        PostgreSQLInsertStatement result = (PostgreSQLInsertStatement) visit(ctx.insertRest());
        result.setTable((SimpleTableSegment) visit(ctx.insertTarget()));
        result.setParameterCount(getCurrentParameterIndex());
        return result;
    }
    
    @Override
    public ASTNode visitInsertTarget(final InsertTargetContext ctx) {
        QualifiedNameContext qualifiedName = ctx.qualifiedName();
        OwnerSegment owner = null;
        TableNameSegment tableName;
        if (null != qualifiedName.indirection()) {
            ColIdContext colId = ctx.colId();
            owner = new OwnerSegment(colId.start.getStartIndex(), colId.stop.getStopIndex(), new IdentifierValue(colId.getText()));
            AttrNameContext attrName = qualifiedName.indirection().indirectionEl().attrName();
            tableName = new TableNameSegment(attrName.start.getStartIndex(), attrName.stop.getStopIndex(), new IdentifierValue(attrName.getText()));
        } else {
            tableName = new TableNameSegment(qualifiedName.colId().start.getStartIndex(), qualifiedName.colId().stop.getStopIndex(), new IdentifierValue(qualifiedName.colId().getText()));
        }
        SimpleTableSegment result = new SimpleTableSegment(tableName);
        result.setOwner(owner);
        if (null != ctx.AS()) {
            ColIdContext colId = ctx.colId();
            result.setAlias(new AliasSegment(colId.start.getStartIndex(), colId.stop.getStopIndex(), new IdentifierValue(colId.getText())));
        }
        return result;
    }
    
    @Override
    public ASTNode visitInsertRest(final InsertRestContext ctx) {
        PostgreSQLInsertStatement result = new PostgreSQLInsertStatement();
        if (null != ctx.insertColumnList()) {
            InsertColumnListContext insertColumns = ctx.insertColumnList();
            CollectionValue<ColumnSegment> columns = (CollectionValue<ColumnSegment>) visit(insertColumns);
            InsertColumnsSegment insertColumnsSegment = new InsertColumnsSegment(insertColumns.start.getStartIndex() - 1, insertColumns.stop.getStopIndex() + 1, columns.getValue());
            result.setInsertColumns(insertColumnsSegment);
        } else {
            result.setInsertColumns(new InsertColumnsSegment(ctx.start.getStartIndex() - 1, ctx.start.getStartIndex() - 1, Collections.emptyList()));
        }
        ValuesClauseContext valuesClause = ctx.select().selectNoParens().selectClauseN().simpleSelect().valuesClause();
        Collection<InsertValuesSegment> insertValuesSegments = createInsertValuesSegments(valuesClause);
        result.getValues().addAll(insertValuesSegments);
        return result;
    }
    
    @Override
    public ASTNode visitInsertColumnList(final InsertColumnListContext ctx) {
        CollectionValue<ColumnSegment> result = new CollectionValue<>();
        if (null != ctx.insertColumnList()) {
            result.getValue().addAll(((CollectionValue<ColumnSegment>) visit(ctx.insertColumnList())).getValue());
        }
        result.getValue().add((ColumnSegment) visit(ctx.insertColumnItem()));
        return result;
    }
    
    @Override
    public ASTNode visitInsertColumnItem(final InsertColumnItemContext ctx) {
        if (null != ctx.optIndirection().indirectionEl()) {
            ColumnSegment result = new ColumnSegment(ctx.colId().start.getStartIndex(), ctx.optIndirection().stop.getStopIndex(),
                    new IdentifierValue(ctx.optIndirection().indirectionEl().attrName().getText()));
            result.setOwner(new OwnerSegment(ctx.colId().start.getStartIndex(), ctx.colId().stop.getStopIndex(), new IdentifierValue(ctx.colId().getText())));
            return result;
    
        } else {
            return new ColumnSegment(ctx.colId().start.getStartIndex(), ctx.colId().stop.getStopIndex(), new IdentifierValue(ctx.colId().getText()));
        }
    }
    
    private Collection<InsertValuesSegment> createInsertValuesSegments(final ValuesClauseContext ctx) {
        Collection<InsertValuesSegment> result = new LinkedList<>();
        if (null != ctx.valuesClause()) {
            Collection<InsertValuesSegment> expressions = createInsertValuesSegments(ctx.valuesClause());
            result.addAll(expressions);
        }
        Collection<ExpressionSegment> expressions = createInsertValuesSegments(ctx.exprList());
        InsertValuesSegment insertValuesSegment = new InsertValuesSegment(ctx.LP_().getSymbol().getStartIndex(), ctx.RP_().getSymbol().getStopIndex(), (List<ExpressionSegment>) expressions);
        result.add(insertValuesSegment);
        return result;
    }
    
    private Collection<ExpressionSegment> createInsertValuesSegments(final ExprListContext ctx) {
        Collection<ExpressionSegment> result = new LinkedList<>();
        if (null != ctx.exprList()) {
            Collection<ExpressionSegment> tmpResult = createInsertValuesSegments(ctx.exprList());
            result.addAll(tmpResult);
        }
        ExpressionSegment expr = (ExpressionSegment) visit(ctx.aExpr());
        result.add(expr);
        return result;
    }
    
    private Collection<AssignmentSegment> generateAssignmentSegments(final SetClauseListContext ctx) {
        Collection<AssignmentSegment> result = new LinkedList<>();
        if (null != ctx.setClauseList()) {
            Collection<AssignmentSegment> tmpResult = generateAssignmentSegments(ctx.setClauseList());
            result.addAll(tmpResult);
        }
        AssignmentSegment assignmentSegment = (AssignmentSegment) visit(ctx.setClause());
        result.add(assignmentSegment);
        return result;
    }
    
    @Override
    public ASTNode visitSetClause(final SetClauseContext ctx) {
        ColumnSegment columnSegment = (ColumnSegment) visit(ctx.setTarget());
        ExpressionSegment expressionSegment = (ExpressionSegment) visit(ctx.aExpr());
        return new AssignmentSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), columnSegment, expressionSegment);
    }
    
    @Override
    public ASTNode visitSetTarget(final SetTargetContext ctx) {
        OwnerSegment owner = null;
        IdentifierValue identifierValue;
        if (null != ctx.optIndirection().indirectionEl()) {
            owner = new OwnerSegment(ctx.colId().start.getStartIndex(), ctx.colId().stop.getStopIndex(), new IdentifierValue(ctx.colId().getText()));
            identifierValue = new IdentifierValue(ctx.optIndirection().getText());
        } else {
            identifierValue = new IdentifierValue(ctx.colId().getText());
        }
        ColumnSegment result = new ColumnSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), identifierValue);
        result.setOwner(owner);
        return result;
    }
    
    @Override
    public ASTNode visitRelationExprOptAlias(final RelationExprOptAliasContext ctx) {
        SimpleTableSegment result;
        if (null != ctx.colId()) {
            ColIdContext colId = ctx.relationExpr().qualifiedName().colId();
            result = new SimpleTableSegment(colId.start.getStartIndex(), colId.stop.getStopIndex(), new IdentifierValue(colId.getText()));
            result.setAlias(new AliasSegment(ctx.colId().start.getStartIndex(), ctx.stop.getStopIndex(), new IdentifierValue(ctx.colId().getText())));
        } else {
            ColIdContext colId = ctx.relationExpr().qualifiedName().colId();
            result = new SimpleTableSegment(colId.start.getStartIndex(), colId.stop.getStopIndex(), new IdentifierValue(colId.getText()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitUpdate(final UpdateContext ctx) {
        PostgreSQLUpdateStatement result = new PostgreSQLUpdateStatement();
        SimpleTableSegment tableSegment = (SimpleTableSegment) visit(ctx.relationExprOptAlias());
        result.setTableSegment(tableSegment);
        result.setSetAssignment((SetAssignmentSegment) visit(ctx.setClauseList()));
        if (null != ctx.whereOrCurrentClause()) {
            result.setWhere((WhereSegment) visit(ctx.whereOrCurrentClause()));
        }
        result.setParameterCount(getCurrentParameterIndex());
        return result;
    }
    
    @Override
    public ASTNode visitSetClauseList(final SetClauseListContext ctx) {
        Collection<AssignmentSegment> assignments = generateAssignmentSegments(ctx);
        return new SetAssignmentSegment(ctx.start.getStartIndex() - 4, ctx.stop.getStopIndex(), assignments);
    }
    
    @Override
    public ASTNode visitDelete(final DeleteContext ctx) {
        PostgreSQLDeleteStatement result = new PostgreSQLDeleteStatement();
        SimpleTableSegment tableSegment = (SimpleTableSegment) visit(ctx.relationExprOptAlias());
        result.setTableSegment(tableSegment);
        if (null != ctx.whereOrCurrentClause()) {
            result.setWhere((WhereSegment) visit(ctx.whereOrCurrentClause()));
        }
        result.setParameterCount(getCurrentParameterIndex());
        return result;
    }
    
    @Override
    public ASTNode visitWhereOrCurrentClause(final WhereOrCurrentClauseContext ctx) {
        return visit(ctx.whereClause());
    }
    
    @Override
    public ASTNode visitSelect(final SelectContext ctx) {
        // TODO :Unsupported for withClause.
        PostgreSQLSelectStatement result = (PostgreSQLSelectStatement) visit(ctx.selectNoParens());
        result.setParameterCount(getCurrentParameterIndex());
        return result;
    }
    
    @Override
    public ASTNode visitSelectNoParens(final SelectNoParensContext ctx) {
        PostgreSQLSelectStatement result = (PostgreSQLSelectStatement) visit(ctx.selectClauseN());
        if (null != ctx.sortClause()) {
            OrderBySegment orderBySegment = (OrderBySegment) visit(ctx.sortClause());
            result.setOrderBy(orderBySegment);
        }
        if (null != ctx.selectLimit()) {
            LimitSegment limitSegment = (LimitSegment) visit(ctx.selectLimit());
            result.setLimit(limitSegment);
        }
        if (null != ctx.forLockingClause()) {
            LockSegment lockSegment = (LockSegment) visit(ctx.forLockingClause());
            result.setLock(lockSegment);
        }
        return result;
    }
    
    @Override
    public ASTNode visitForLockingClause(final ForLockingClauseContext ctx) {
        return new LockSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
    }
    
    @Override
    public ASTNode visitSelectWithParens(final SelectWithParensContext ctx) {
        if (null != ctx.selectWithParens()) {
            return visit(ctx.selectWithParens());
        }
        return visit(ctx.selectNoParens());
    }
    
    @Override
    public ASTNode visitSelectClauseN(final SelectClauseNContext ctx) {
        return null == ctx.simpleSelect() ? new PostgreSQLSelectStatement() : visit(ctx.simpleSelect());
    }
    
    @Override
    public ASTNode visitSimpleSelect(final SimpleSelectContext ctx) {
        PostgreSQLSelectStatement result = new PostgreSQLSelectStatement();
        if (null != ctx.targetList()) {
            ProjectionsSegment projects = (ProjectionsSegment) visit(ctx.targetList());
            if (null != ctx.distinctClause()) {
                projects.setDistinctRow(true);
            }
            result.setProjections(projects);
        }
        if (null != ctx.fromClause()) {
            TableSegment tableSegment = (TableSegment) visit(ctx.fromClause());
            result.setFrom(tableSegment);
        }
        if (null != ctx.whereClause()) {
            result.setWhere((WhereSegment) visit(ctx.whereClause()));
        }
        if (null != ctx.groupClause()) {
            result.setGroupBy((GroupBySegment) visit(ctx.groupClause()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitGroupClause(final GroupClauseContext ctx) {
        Collection<OrderByItemSegment> items = new LinkedList<>();
        for (GroupByItemContext each : ctx.groupByList().groupByItem()) {
            items.add((OrderByItemSegment) visit(each));
        }
        return new GroupBySegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), items);
    }
    
    @Override
    public ASTNode visitGroupByItem(final GroupByItemContext ctx) {
        if (null != ctx.aExpr()) {
            ASTNode astNode = visit(ctx.aExpr());
            if (astNode instanceof ColumnSegment) {
                return new ColumnOrderByItemSegment((ColumnSegment) astNode, OrderDirection.ASC);
            }
            if (astNode instanceof LiteralExpressionSegment) {
                LiteralExpressionSegment index = (LiteralExpressionSegment) astNode;
                return new IndexOrderByItemSegment(index.getStartIndex(), index.getStopIndex(),
                        Integer.parseInt(index.getLiterals().toString()), OrderDirection.ASC);
            }
            return new ExpressionOrderByItemSegment(ctx.start.getStartIndex(), ctx.start.getStopIndex(), ctx.getText(), OrderDirection.ASC);
        }
        return new ExpressionOrderByItemSegment(ctx.start.getStartIndex(), ctx.start.getStopIndex(), ctx.getText(), OrderDirection.ASC);
    }
    
    @Override
    public ASTNode visitTargetList(final TargetListContext ctx) {
        ProjectionsSegment result = new ProjectionsSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
        if (null != ctx.targetList()) {
            ProjectionsSegment projecs = (ProjectionsSegment) visit(ctx.targetList());
            result.getProjections().addAll(projecs.getProjections());
        }
        ProjectionSegment projection = (ProjectionSegment) visit(ctx.targetEl());
        result.getProjections().add(projection);
        return result;
    }
    
    @Override
    public ASTNode visitTargetEl(final TargetElContext ctx) {
        if (null != ctx.ASTERISK_()) {
            return new ShorthandProjectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
        }
        if (null != ctx.DOT_ASTERISK_()) {
            ShorthandProjectionSegment shorthandProjection = new ShorthandProjectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
            shorthandProjection.setOwner(new OwnerSegment(ctx.colId().start.getStartIndex(), ctx.colId().stop.getStopIndex(), new IdentifierValue(ctx.colId().getText())));
            return shorthandProjection;
        }
        AExprContext expr = ctx.aExpr();
        if (1 == expr.getChildCount() && null != expr.cExpr()) {
            ASTNode projection = visit(expr.cExpr());
            AliasSegment alias = null != ctx.identifier()
                    ? new AliasSegment(ctx.identifier().start.getStartIndex(), ctx.identifier().stop.getStopIndex(), new IdentifierValue(ctx.identifier().getText())) : null;
            if (projection instanceof ColumnSegment) {
                ColumnProjectionSegment result = new ColumnProjectionSegment((ColumnSegment) projection);
                result.setAlias(alias);
                return result;
            }
        }
        if (null != expr.cExpr() && null != expr.cExpr().funcExpr()) {
            visit(expr.cExpr().funcExpr());
            ProjectionSegment projection = generateProjectFromFuncExpr(expr.cExpr().funcExpr());
            AliasSegment alias = null != ctx.identifier()
                    ? new AliasSegment(ctx.identifier().start.getStartIndex(), ctx.identifier().stop.getStopIndex(), new IdentifierValue(ctx.identifier().getText())) : null;
            if (projection instanceof AggregationProjectionSegment) {
                ((AggregationProjectionSegment) projection).setAlias(alias);
            }
            if (projection instanceof AggregationDistinctProjectionSegment) {
                ((AggregationDistinctProjectionSegment) projection).setAlias(alias);
            }
            return projection;
        }
        if (null != expr.cExpr() && null != expr.cExpr().selectWithParens()) {
            PostgreSQLSelectStatement select = (PostgreSQLSelectStatement) visit(expr.cExpr().selectWithParens());
            SubquerySegment subquery = new SubquerySegment(expr.cExpr().selectWithParens().start.getStartIndex(), expr.cExpr().selectWithParens().stop.getStopIndex(), select);
            SubqueryProjectionSegment projection = new SubqueryProjectionSegment(subquery);
            AliasSegment alias = null != ctx.identifier()
                    ? new AliasSegment(ctx.identifier().start.getStartIndex(), ctx.identifier().stop.getStopIndex(), new IdentifierValue(ctx.identifier().getText())) : null;
            projection.setAlias(alias);
            return projection;
        }
        ExpressionProjectionSegment result = new ExpressionProjectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), expr.getText());
        if (null != ctx.identifier()) {
            AliasSegment alias = new AliasSegment(ctx.identifier().start.getStartIndex(), ctx.identifier().stop.getStopIndex(), new IdentifierValue(ctx.identifier().getText()));
            result.setAlias(alias);
        }
        return result;
    }
    
    @Override
    public ASTNode visitFromClause(final FromClauseContext ctx) {
        return visit(ctx.fromList());
    }
    
    @Override
    public ASTNode visitFromList(final FromListContext ctx) {
        if (null != ctx.fromList()) {
            JoinTableSegment result = new JoinTableSegment();
            result.setStartIndex(ctx.start.getStartIndex());
            result.setStopIndex(ctx.stop.getStopIndex());
            result.setLeft((TableSegment) visit(ctx.fromList()));
            result.setRight((TableSegment) visit(ctx.tableReference()));
            return result;
        }
        return (TableSegment) visit(ctx.tableReference());
    }
    
    @Override
    public ASTNode visitTableReference(final TableReferenceContext ctx) {
        if (null != ctx.relationExpr()) {
            SimpleTableSegment result = generateTableFromRelationExpr(ctx.relationExpr());
            if (null != ctx.aliasClause()) {
                result.setAlias((AliasSegment) visit(ctx.aliasClause()));
            }
            return result;
        }
        if (null != ctx.selectWithParens()) {
            PostgreSQLSelectStatement select = (PostgreSQLSelectStatement) visit(ctx.selectWithParens());
            SubquerySegment subquery = new SubquerySegment(ctx.selectWithParens().start.getStartIndex(), ctx.selectWithParens().stop.getStopIndex(), select);
            AliasSegment alias = null != ctx.aliasClause() ? (AliasSegment) visit(ctx.aliasClause()) : null;
            SubqueryTableSegment result = new SubqueryTableSegment(subquery);
            result.setAlias(alias);
            return result;
        }
        if (null != ctx.tableReference()) {
            JoinTableSegment result = new JoinTableSegment();
            result.setLeft((TableSegment) visit(ctx.tableReference()));
            int startIndex = null != ctx.LP_() ? ctx.LP_().getSymbol().getStartIndex() : ctx.tableReference().start.getStartIndex();
            int stopIndex = 0;
            AliasSegment alias = null;
            if (null != ctx.aliasClause()) {
                alias = (AliasSegment) visit(ctx.aliasClause());
                startIndex = null != ctx.RP_() ? ctx.RP_().getSymbol().getStopIndex() : ctx.joinedTable().stop.getStopIndex();
            } else {
                stopIndex = null != ctx.RP_() ? ctx.RP_().getSymbol().getStopIndex() : ctx.tableReference().start.getStopIndex();
            }
            result.setStartIndex(startIndex);
            result.setStopIndex(stopIndex);
            result = visitJoinedTable(ctx.joinedTable(), result);
            result.setAlias(alias);
            return result;
        }
//        TODO deal with functionTable and xmlTable
        return new SimpleTableSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), new IdentifierValue("not support"));
    }
    
    private JoinTableSegment visitJoinedTable(final JoinedTableContext ctx, final JoinTableSegment tableSegment) {
        JoinTableSegment result = tableSegment;
        TableSegment right = (TableSegment) visit(ctx.tableReference());
        result.setRight(right);
        if (null != ctx.joinQual()) {
            result = visitJoinQual(ctx.joinQual(), result);
        }
        return result;
    }
    
    private JoinTableSegment visitJoinQual(final JoinQualContext ctx, final JoinTableSegment joinTableSource) {
        if (null != ctx.aExpr()) {
            ExpressionSegment condition = (ExpressionSegment) visit(ctx.aExpr());
            joinTableSource.setCondition(condition);
        }
        if (null != ctx.USING()) {
            joinTableSource.setUsing(generateUsingColumn(ctx.nameList()));
        }
        return joinTableSource;
    }
    
    private List<ColumnSegment> generateUsingColumn(final NameListContext ctx) {
        List<ColumnSegment> result = new LinkedList<>();
        if (null != ctx.nameList()) {
            result.addAll(generateUsingColumn(ctx.nameList()));
        }
        if (null != ctx.name()) {
            ColumnSegment column = new ColumnSegment(ctx.name().start.getStartIndex(), ctx.name().stop.getStopIndex(), new IdentifierValue(ctx.name().getText()));
            result.add(column);
        }
        return result;
    }
    
    @Override
    public ASTNode visitAliasClause(final AliasClauseContext ctx) {
        StringBuilder aliasName = new StringBuilder(ctx.colId().getText());
        if (null != ctx.nameList()) {
            aliasName.append(ctx.LP_().getText());
            aliasName.append(ctx.nameList().getText());
            aliasName.append(ctx.RP_().getText());
        }
        return new AliasSegment(ctx.colId().start.getStartIndex(), ctx.stop.getStopIndex(), new IdentifierValue(aliasName.toString()));
    }
    
    private SimpleTableSegment generateTableFromRelationExpr(final RelationExprContext ctx) {
        QualifiedNameContext qualifiedName = ctx.qualifiedName();
        if (null != qualifiedName.indirection()) {
            AttrNameContext tableName = qualifiedName.indirection().indirectionEl().attrName();
            SimpleTableSegment table = new SimpleTableSegment(tableName.start.getStartIndex(), tableName.stop.getStopIndex(), new IdentifierValue(tableName.getText()));
            table.setOwner(new OwnerSegment(qualifiedName.colId().start.getStartIndex(), qualifiedName.colId().stop.getStopIndex(), new IdentifierValue(qualifiedName.colId().getText())));
            return table;
        }
        return new SimpleTableSegment(qualifiedName.colId().start.getStartIndex(), qualifiedName.colId().stop.getStopIndex(), new IdentifierValue(qualifiedName.colId().getText()));
    }
    
    @Override
    public ASTNode visitWhereClause(final WhereClauseContext ctx) {
        ExpressionSegment expr = (ExpressionSegment) visit(ctx.aExpr());
        return new WhereSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), expr);
    }
    
    @Override
    public ASTNode visitSelectLimit(final SelectLimitContext ctx) {
        if (null != ctx.limitClause() && null != ctx.offsetClause()) {
            return createLimitSegmentWhenLimitAndOffset(ctx);
        }
        return createLimitSegmentWhenRowCountOrOffsetAbsent(ctx);
    }
    
    @Override
    public ASTNode visitSelectLimitValue(final SelectLimitValueContext ctx) {
        if (null != ctx.ALL()) {
            return null;
        }
        ASTNode astNode = visit(ctx.aExpr());
        if (astNode instanceof ParameterMarkerExpressionSegment) {
            return new ParameterMarkerLimitValueSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ((ParameterMarkerExpressionSegment) astNode).getParameterMarkerIndex());
        }
        return new NumberLiteralLimitValueSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), Long.parseLong(((LiteralExpressionSegment) astNode).getLiterals().toString()));
    }
    
    @Override
    public ASTNode visitSelectOffsetValue(final SelectOffsetValueContext ctx) {
        ASTNode astNode = visit(ctx.aExpr());
        if (astNode instanceof ParameterMarkerExpressionSegment) {
            return new ParameterMarkerLimitValueSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ((ParameterMarkerExpressionSegment) astNode).getParameterMarkerIndex());
        }
        return new NumberLiteralLimitValueSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), Long.parseLong(((LiteralExpressionSegment) astNode).getLiterals().toString()));
    }
    
    private LimitSegment createLimitSegmentWhenLimitAndOffset(final SelectLimitContext ctx) {
        ParseTree astNode0 = ctx.getChild(0);
        LimitValueSegment rowCount = null;
        LimitValueSegment offset = null;
        if (astNode0 instanceof LimitClauseContext) {
            rowCount = null == ctx.limitClause().selectLimitValue() ? null : (LimitValueSegment) visit(ctx.limitClause().selectLimitValue());
        } else {
            offset = (LimitValueSegment) visit(ctx.offsetClause().selectOffsetValue());
        }
        ParseTree astNode1 = ctx.getChild(1);
        if (astNode1 instanceof LimitClauseContext) {
            rowCount = null == ctx.limitClause().selectLimitValue() ? null : (LimitValueSegment) visit(ctx.limitClause().selectLimitValue());
        } else {
            offset = (LimitValueSegment) visit(ctx.offsetClause().selectOffsetValue());
        }
        return new LimitSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), offset, rowCount);
    }

    private LimitSegment createLimitSegmentWhenRowCountOrOffsetAbsent(final SelectLimitContext ctx) {
        if (null != ctx.limitClause()) {
            if (null != ctx.limitClause().selectOffsetValue()) {
                LimitValueSegment limit = (LimitValueSegment) visit(ctx.limitClause().selectLimitValue());
                LimitValueSegment offset = (LimitValueSegment) visit(ctx.limitClause().selectOffsetValue());
                return new LimitSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), offset, limit);
            }
            LimitValueSegment limit = (LimitValueSegment) visit(ctx.limitClause().selectLimitValue());
            return new LimitSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), null, limit);
        }
        LimitValueSegment offset = (LimitValueSegment) visit(ctx.offsetClause().selectOffsetValue());
        return new LimitSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), offset, null);
    }
    
    @Override
    public ASTNode visitCall(final CallContext ctx) {
        return new PostgreSQLCallStatement();
    }
    
    @Override
    public ASTNode visitDoStatement(final DoStatementContext ctx) {
        return new PostgreSQLDoStatement();
    }
}
