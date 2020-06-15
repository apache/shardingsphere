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

package org.apache.shardingsphere.sql.parser.postgresql.visitor.impl;

import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.DMLVisitor;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.Insert_targetContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.Insert_restContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.Qualified_nameContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ColIdContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AttrNameContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.Insert_column_listContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.Values_clauseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.Insert_column_itemContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ExprListContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.Set_clause_listContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.Set_clauseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.Set_targetContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.Relation_expr_opt_aliasContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.Set_target_listContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.Select_no_parensContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.Select_with_parensContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.SelectClauseNContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.Simple_selectContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.Group_clauseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.Group_by_itemContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.Target_listContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.Target_elContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ColumnrefContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.Joined_tableContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.Join_qualContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.NameListContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AliasClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.QualifiedNameContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.RelationExprContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.Select_limitContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.Select_limit_valueContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.Select_offset_valueContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DeleteContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.FromClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.InsertContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.SelectContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.TableReferenceContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.UpdateContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.WhereClauseContext;
import org.apache.shardingsphere.sql.parser.postgresql.visitor.PostgreSQLVisitor;
import org.apache.shardingsphere.sql.parser.sql.constant.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.segment.SQLSegment;
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
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.AggregationDistinctProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.pagination.limit.LimitValueSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.pagination.limit.ParameterMarkerLimitValueSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.LockSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.OrPredicateSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.sql.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * DML visitor for PostgreSQL.
 */
public final class PostgreSQLDMLVisitor extends PostgreSQLVisitor implements DMLVisitor {
    
    @Override
    public ASTNode visitInsert(final InsertContext ctx) {
        // TODO :FIXME, since there is no segment for insertValuesClause, InsertStatement is created by sub rule.
        // TODO :deal with insert select
        InsertStatement result = (InsertStatement) visit(ctx.insert_rest());
        result.setTable((SimpleTableSegment) visit(ctx.insert_target()));
        result.setParameterCount(getCurrentParameterIndex());
        return result;
    }
    
    @Override
    public ASTNode visitInsert_target(final Insert_targetContext ctx) {
        Qualified_nameContext qualifiedName = ctx.qualified_name();
        OwnerSegment owner = null;
        TableNameSegment tableName;
        if (null != qualifiedName.indirection()) {
            ColIdContext colId = ctx.colId();
    
            owner = new OwnerSegment(colId.start.getStartIndex(), colId.stop.getStopIndex(), new IdentifierValue(colId.getText()));
            AttrNameContext attrName = qualifiedName.indirection().indirection_el().attrName();
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
    public ASTNode visitInsert_rest(final Insert_restContext ctx) {
        InsertStatement result = new InsertStatement();
        if (null != ctx.insert_column_list()) {
            Insert_column_listContext insertColumns = ctx.insert_column_list();
            CollectionValue<ColumnSegment> columns = (CollectionValue<ColumnSegment>) visit(insertColumns);
            InsertColumnsSegment insertColumnsSegment = new InsertColumnsSegment(insertColumns.start.getStartIndex() - 1, insertColumns.stop.getStopIndex() + 1, columns.getValue());
            result.setInsertColumns(insertColumnsSegment);
        } else {
            result.setInsertColumns(new InsertColumnsSegment(ctx.start.getStartIndex() - 1, ctx.start.getStartIndex() - 1, Collections.emptyList()));
        }
        Values_clauseContext valuesClause = ctx.select().select_no_parens().selectClauseN().simple_select().values_clause();
        Collection<InsertValuesSegment> insertValuesSegments = createInsertValuesSegments(valuesClause);
        result.getValues().addAll(insertValuesSegments);
        return result;
    }
    
    @Override
    public ASTNode visitInsert_column_list(final Insert_column_listContext ctx) {
        CollectionValue<ColumnSegment> result = new CollectionValue<>();
        if (null != ctx.insert_column_list()) {
            result.getValue().addAll(((CollectionValue<ColumnSegment>) visit(ctx.insert_column_list())).getValue());
        }
        result.getValue().add((ColumnSegment) visit(ctx.insert_column_item()));
        return result;
    }
    
    @Override
    public ASTNode visitInsert_column_item(final Insert_column_itemContext ctx) {
        if (null != ctx.opt_indirection().indirection_el()) {
            ColumnSegment columnSegment = new ColumnSegment(ctx.colId().start.getStartIndex(), ctx.opt_indirection().stop.getStopIndex(),
                    new IdentifierValue(ctx.opt_indirection().indirection_el().attrName().getText()));
            columnSegment.setOwner(new OwnerSegment(ctx.colId().start.getStartIndex(), ctx.colId().stop.getStopIndex(), new IdentifierValue(ctx.colId().getText())));
            return columnSegment;
    
        } else {
            ColumnSegment columnSegment = new ColumnSegment(ctx.colId().start.getStartIndex(), ctx.colId().stop.getStopIndex(), new IdentifierValue(ctx.colId().getText()));
            return columnSegment;
        }
    }
    
    private Collection<InsertValuesSegment> createInsertValuesSegments(final Values_clauseContext ctx) {
        Collection<InsertValuesSegment> result = new LinkedList<>();
        if (null != ctx.values_clause()) {
            Collection<InsertValuesSegment> expressions = createInsertValuesSegments(ctx.values_clause());
            result.addAll(expressions);
        }
        Collection<ExpressionSegment> expressions = createInsertValuesSegments(ctx.exprList());
        InsertValuesSegment insertValuesSegment = new InsertValuesSegment(ctx.exprList().start.getStartIndex(), ctx.exprList().stop.getStopIndex(), (List<ExpressionSegment>) expressions);
        result.add(insertValuesSegment);
        return result;
    }
    
    private Collection<ExpressionSegment> createInsertValuesSegments(final ExprListContext ctx) {
        Collection<ExpressionSegment> result = new LinkedList<>();
        
        if (null != ctx.exprList()) {
            Collection<ExpressionSegment> tmpResult = createInsertValuesSegments(ctx.exprList());
            result.addAll(tmpResult);
        }
        visit(ctx.a_expr());
        CommonExpressionSegment expressionSegment = new CommonExpressionSegment(ctx.a_expr().start.getStartIndex(), ctx.a_expr().stop.getStopIndex(), ctx.a_expr().getText());
        result.add(expressionSegment);
        return result;
    }
    
    private Collection<AssignmentSegment> generateAssignmentSegments(final Set_clause_listContext ctx) {
        Collection<AssignmentSegment> result = new LinkedList<>();
        if (null != ctx.set_clause_list()) {
            Collection<AssignmentSegment> tmpResult = generateAssignmentSegments(ctx.set_clause_list());
            result.addAll(tmpResult);
        }
        AssignmentSegment assignmentSegment = (AssignmentSegment) visit(ctx.set_clause());
        result.add(assignmentSegment);
        return result;
    }
    
    @Override
    public ASTNode visitSet_clause(final Set_clauseContext ctx) {
        ColumnSegment columnSegment = (ColumnSegment) visit(ctx.set_target());
        SQLSegment sqlSegment = (SQLSegment) visit(ctx.a_expr());
        ExpressionSegment expressionSegment = new CommonExpressionSegment(sqlSegment.getStartIndex(), sqlSegment.getStopIndex(), sqlSegment.toString());
        AssignmentSegment result = new AssignmentSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), columnSegment, expressionSegment);
        return result;
    }
    
    @Override
    public ASTNode visitSet_target(final Set_targetContext ctx) {
        OwnerSegment owner = null;
        IdentifierValue identifierValue;
        if (null != ctx.opt_indirection().indirection_el()) {
            owner = new OwnerSegment(ctx.colId().start.getStartIndex(), ctx.colId().stop.getStopIndex(), new IdentifierValue(ctx.colId().getText()));
            identifierValue = new IdentifierValue(ctx.opt_indirection().getText());
        } else {
            identifierValue = new IdentifierValue(ctx.colId().getText());
        }
        ColumnSegment result = new ColumnSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), identifierValue);
        result.setOwner(owner);
        return result;
    }
    
    @Override
    public ASTNode visitRelation_expr_opt_alias(final Relation_expr_opt_aliasContext ctx) {
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
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitUpdate(final UpdateContext ctx) {
        UpdateStatement result = new UpdateStatement();
        SimpleTableSegment tableSegment = (SimpleTableSegment) visit(ctx.relation_expr_opt_alias());
        result.getTables().add(tableSegment);
        result.setSetAssignment((SetAssignmentSegment) visit(ctx.set_clause_list()));
        if (null != ctx.where_or_current_clause()) {
            result.setWhere((WhereSegment) visit(ctx.where_or_current_clause()));
        }
        result.setParameterCount(getCurrentParameterIndex());
        return result;
    }
    
    @Override
    public ASTNode visitSet_clause_list(final Set_clause_listContext ctx) {
        Collection<AssignmentSegment> assignments = generateAssignmentSegments(ctx);
        SetAssignmentSegment result = new SetAssignmentSegment(ctx.start.getStartIndex() - 4, ctx.stop.getStopIndex(), assignments);
        return result;
    }
    
    @Override
    public ASTNode visitSet_target_list(final Set_target_listContext ctx) {
        return super.visitSet_target_list(ctx);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitDelete(final DeleteContext ctx) {
        DeleteStatement result = new DeleteStatement();
        SimpleTableSegment tableSegment = (SimpleTableSegment) visit(ctx.relation_expr_opt_alias());
        result.getTables().add(tableSegment);
        if (null != ctx.where_or_current_clause()) {
            result.setWhere((WhereSegment) visit(ctx.where_or_current_clause()));
        }
        result.setParameterCount(getCurrentParameterIndex());
        return result;
    }
    
    @Override
    public ASTNode visitWhere_or_current_clause(final PostgreSQLStatementParser.Where_or_current_clauseContext ctx) {
        return visit(ctx.whereClause());
    }
    
    @Override
    public ASTNode visitSelect(final SelectContext ctx) {
        // TODO :Unsupported for withClause.
        SelectStatement result = (SelectStatement) visit(ctx.select_no_parens());
        result.setParameterCount(getCurrentParameterIndex());
        return result;
    }
    
    @Override
    public ASTNode visitSelect_no_parens(final Select_no_parensContext ctx) {
        SelectStatement result = (SelectStatement) visit(ctx.selectClauseN());
        if (null != ctx.sort_clause()) {
            OrderBySegment orderBySegment = (OrderBySegment) visit(ctx.sort_clause());
            result.setOrderBy(orderBySegment);
        }
        if (null != ctx.select_limit()) {
            LimitSegment limitSegment = (LimitSegment) visit(ctx.select_limit());
            result.setLimit(limitSegment);
        }
        if (null != ctx.for_locking_clause()) {
            LockSegment lockSegment = (LockSegment) visit(ctx.for_locking_clause());
            result.setLock(lockSegment);
        }
        return result;
    }
    
    @Override
    public ASTNode visitSelect_with_parens(final Select_with_parensContext ctx) {
        
        if (null != ctx.select_with_parens()) {
            return visit(ctx.select_with_parens());
        }
        return visit(ctx.select_no_parens());
    }
    
    @Override
    public ASTNode visitSelectClauseN(final SelectClauseNContext ctx) {
        if (null != ctx.simple_select()) {
            SelectStatement result = (SelectStatement) visit(ctx.simple_select());
            return result;
        }
        return new SelectStatement();
    }
    
    @Override
    public ASTNode visitSimple_select(final Simple_selectContext ctx) {
        SelectStatement result = new SelectStatement();
        if (null != ctx.target_list()) {
            ProjectionsSegment projects = (ProjectionsSegment) visit(ctx.target_list());
            if (null != ctx.distinct_clause()) {
                projects.setDistinctRow(true);
            }
            result.setProjections(projects);
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
        if (null != ctx.group_clause()) {
            result.setGroupBy((GroupBySegment) visit(ctx.group_clause()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitGroup_clause(final Group_clauseContext ctx) {
        Collection<OrderByItemSegment> items = new LinkedList<>();
        for (PostgreSQLStatementParser.Group_by_itemContext each : ctx.group_by_list().group_by_item()) {
            items.add((OrderByItemSegment) visit(each));
        }
        GroupBySegment result = new GroupBySegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), items);
        return result;
    }
    
    @Override
    public ASTNode visitGroup_by_item(final Group_by_itemContext ctx) {
        if (null != ctx.a_expr()) {
            ASTNode astNode = visit(ctx.a_expr());
            if (astNode instanceof ColumnSegment) {
                OrderByItemSegment result = new ColumnOrderByItemSegment((ColumnSegment) astNode, OrderDirection.ASC);
                return result;
            }
            if (astNode instanceof LiteralExpressionSegment) {
                LiteralExpressionSegment index = (LiteralExpressionSegment) astNode;
                return new IndexOrderByItemSegment(index.getStartIndex(), index.getStopIndex(),
                        Integer.valueOf(index.getLiterals().toString()), OrderDirection.ASC);
            }
            return new ExpressionOrderByItemSegment(ctx.start.getStartIndex(), ctx.start.getStopIndex(), ctx.getText(), OrderDirection.ASC);
        }
        return new ExpressionOrderByItemSegment(ctx.start.getStartIndex(), ctx.start.getStopIndex(), ctx.getText(), OrderDirection.ASC);
    }
    
    @Override
    public ASTNode visitTarget_list(final Target_listContext ctx) {
        ProjectionsSegment result = new ProjectionsSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
        if (null != ctx.target_list()) {
            ProjectionsSegment projecs = (ProjectionsSegment) visit(ctx.target_list());
            result.getProjections().addAll(projecs.getProjections());
        }
        ProjectionSegment projection = (ProjectionSegment) visit(ctx.target_el());
        result.getProjections().add(projection);
        return result;
    }
    
    @Override
    public ASTNode visitTarget_el(final Target_elContext ctx) {
        if (null != ctx.ASTERISK_()) {
            return new ShorthandProjectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
        }
        if (null != ctx.DOT_ASTERISK_()) {
            ShorthandProjectionSegment shorthandProjection = new ShorthandProjectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
            shorthandProjection.setOwner(new OwnerSegment(ctx.colId().start.getStartIndex(), ctx.colId().stop.getStopIndex(), new IdentifierValue(ctx.colId().getText())));
            return shorthandProjection;
        }
        PostgreSQLStatementParser.A_exprContext expr = ctx.a_expr();
        if (null != expr.c_expr() && null != expr.c_expr().columnref()) {
            ColumnProjectionSegment projection = generateColumnProjection(expr.c_expr().columnref());
            AliasSegment alias = null != ctx.identifier()
                    ? new AliasSegment(ctx.identifier().start.getStartIndex(), ctx.identifier().stop.getStopIndex(), new IdentifierValue(ctx.identifier().getText())) : null;
            projection.setAlias(alias);
            return projection;
        }
        if (null != expr.c_expr() && null != expr.c_expr().func_expr()) {
            super.visit(expr.c_expr().func_expr());
            ProjectionSegment projection = generateProjectFromFuncExpr(expr.c_expr().func_expr());
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
        
        if (null != expr.c_expr() && null != expr.c_expr().select_with_parens()) {
            SelectStatement select = (SelectStatement) visit(expr.c_expr().select_with_parens());
            SubquerySegment subquery = new SubquerySegment(expr.c_expr().select_with_parens().start.getStartIndex(), expr.c_expr().select_with_parens().stop.getStopIndex(), select);
            SubqueryProjectionSegment projection = new SubqueryProjectionSegment(subquery);
            AliasSegment alias = null != ctx.identifier()
                    ? new AliasSegment(ctx.identifier().start.getStartIndex(), ctx.identifier().stop.getStopIndex(), new IdentifierValue(ctx.identifier().getText())) : null;
            projection.setAlias(alias);
            return projection;
        }
        
        ExpressionProjectionSegment projection = new ExpressionProjectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), expr.getText());
        if (null != ctx.identifier()) {
            AliasSegment alias = new AliasSegment(ctx.identifier().start.getStartIndex(), ctx.identifier().stop.getStopIndex(), new IdentifierValue(ctx.identifier().getText()));
            projection.setAlias(alias);
        }
        return projection;
    }
    
    private ColumnProjectionSegment generateColumnProjection(final ColumnrefContext ctx) {
        if (null != ctx.indirection()) {
            PostgreSQLStatementParser.AttrNameContext attrName = ctx.indirection().indirection_el().attrName();
            ColumnSegment columnSegment = new ColumnSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), new IdentifierValue(attrName.getText()));
            OwnerSegment owner = new OwnerSegment(ctx.colId().start.getStartIndex(), ctx.colId().stop.getStopIndex(), new IdentifierValue(ctx.colId().getText()));
            columnSegment.setOwner(owner);
            ColumnProjectionSegment columnProjection = new ColumnProjectionSegment(columnSegment);
            return columnProjection;
        }
        ColumnSegment columnSegment = new ColumnSegment(ctx.colId().start.getStartIndex(), ctx.colId().stop.getStopIndex(), new IdentifierValue(ctx.colId().getText()));
        ColumnProjectionSegment columnProjection = new ColumnProjectionSegment(columnSegment);
        return columnProjection;
    }
    
    @Override
    public ASTNode visitFromClause(final FromClauseContext ctx) {
        return visit(ctx.fromList());
    }
    
    @Override
    public ASTNode visitFromList(final PostgreSQLStatementParser.FromListContext ctx) {
        CollectionValue<TableReferenceSegment> result = new CollectionValue<>();
        if (null != ctx.fromList()) {
            result.getValue().addAll(((CollectionValue<TableReferenceSegment>) visit(ctx.fromList())).getValue());
        }
        result.getValue().add((TableReferenceSegment) visit(ctx.tableReference()));
        return result;
    }
    
    @Override
    public ASTNode visitTableReference(final TableReferenceContext ctx) {
    
        if (null != ctx.tableReference()) {
    
            TableReferenceSegment result = (TableReferenceSegment) visit(ctx.tableReference());
            if (null != ctx.joined_table()) {
                result.getJoinedTables().add((JoinedTableSegment) visit(ctx.joined_table()));
            }
            return result;
        }
        if (null != ctx.relationExpr()) {
            TableReferenceSegment result = new TableReferenceSegment();
            SimpleTableSegment table = generateTableFromRelationExpr(ctx.relationExpr());
            if (null != ctx.aliasClause()) {
                table.setAlias((AliasSegment) visit(ctx.aliasClause()));
            }
            TableFactorSegment tableFactorSegment = new TableFactorSegment();
            tableFactorSegment.setTable(table);
            result.setTableFactor(tableFactorSegment);
            return result;
        }
        if (null != ctx.select_with_parens()) {
            TableReferenceSegment result = new TableReferenceSegment();
            SelectStatement select = (SelectStatement) visit(ctx.select_with_parens());
            SubquerySegment subquery = new SubquerySegment(ctx.select_with_parens().start.getStartIndex(), ctx.select_with_parens().stop.getStopIndex(), select);
            AliasSegment alias = null != ctx.aliasClause() ? (AliasSegment) visit(ctx.aliasClause()) : null;
            SubqueryTableSegment subqueryTable = new SubqueryTableSegment(subquery);
            subqueryTable.setAlias(alias);
            TableFactorSegment tableFactor = new TableFactorSegment();
            tableFactor.setTable(subqueryTable);
            result.setTableFactor(tableFactor);
            return result;
        }
//        TODO deal with functionTable and xmlTable
        return new TableReferenceSegment();
    }
    
    @Override
    public ASTNode visitJoined_table(final Joined_tableContext ctx) {
        JoinedTableSegment result = new JoinedTableSegment();
        result.setTableFactor(((TableReferenceSegment) visit(ctx.tableReference())).getTableFactor());
        if (null != ctx.join_qual()) {
            JoinSpecificationSegment joinSpecification = (JoinSpecificationSegment) visit(ctx.join_qual());
            result.setJoinSpecification(joinSpecification);
        }
        return result;
    }
    
    @Override
    public ASTNode visitJoin_qual(final Join_qualContext ctx) {
        JoinSpecificationSegment result = new JoinSpecificationSegment();
        if (null != ctx.ON()) {
            ASTNode segment = visit(ctx.a_expr());
            if (segment instanceof OrPredicateSegment) {
                result.getAndPredicates().addAll(((OrPredicateSegment) segment).getAndPredicates());
            } else if (segment instanceof PredicateSegment) {
                AndPredicate andPredicate = new AndPredicate();
                andPredicate.getPredicates().add((PredicateSegment) segment);
                result.getAndPredicates().add(andPredicate);
            }
        }
        if (null != ctx.USING()) {
            result.getUsingColumns().addAll(generateUsingColumn(ctx.nameList()));
        }
        return result;
    }
    
    private Collection<ColumnSegment> generateUsingColumn(final NameListContext ctx) {
        Collection<ColumnSegment> result = new LinkedList<>();
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
        AliasSegment alias = new AliasSegment(ctx.colId().start.getStartIndex(), ctx.stop.getStopIndex(), new IdentifierValue(aliasName.toString()));
        return alias;
    }
    
    private SimpleTableSegment generateTableFromRelationExpr(final RelationExprContext ctx) {
        QualifiedNameContext qualifiedName = ctx.qualifiedName();
    
        if (null != qualifiedName.indirection()) {
            AttrNameContext tableName = qualifiedName.indirection().indirection_el().attrName();
            SimpleTableSegment table = new SimpleTableSegment(tableName.start.getStartIndex(), tableName.stop.getStopIndex(), new IdentifierValue(tableName.getText()));
            table.setOwner(new OwnerSegment(qualifiedName.colId().start.getStartIndex(), qualifiedName.colId().stop.getStopIndex(), new IdentifierValue(qualifiedName.colId().getText())));
            return table;
        }
        return new SimpleTableSegment(qualifiedName.colId().start.getStartIndex(), qualifiedName.colId().stop.getStopIndex(), new IdentifierValue(qualifiedName.colId().getText()));
    }
    
    @Override
    public ASTNode visitWhereClause(final WhereClauseContext ctx) {
        WhereSegment result = new WhereSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
        ASTNode segment = visit(ctx.a_expr());
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
    public ASTNode visitSelect_limit(final Select_limitContext ctx) {
        if (null != ctx.limit_clause() && null != ctx.offset_clause()) {
            return createLimitSegmentWhenLimitAndOffset(ctx);
        }
        return createLimitSegmentWhenRowCountOrOffsetAbsent(ctx);
    }
    
    @Override
    public ASTNode visitSelect_limit_value(final Select_limit_valueContext ctx) {
        if (null != ctx.ALL()) {
            return null;
        }
        ASTNode astNode = visit(ctx.a_expr());
        if (astNode instanceof ParameterMarkerExpressionSegment) {
            return new ParameterMarkerLimitValueSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ((ParameterMarkerExpressionSegment) astNode).getParameterMarkerIndex());
        }
        LimitValueSegment result = new NumberLiteralLimitValueSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                Long.valueOf(((LiteralExpressionSegment) astNode).getLiterals().toString()));
        return result;
    }
    
    @Override
    public ASTNode visitSelect_offset_value(final Select_offset_valueContext ctx) {
        ASTNode astNode = visit(ctx.a_expr());
        if (astNode instanceof ParameterMarkerExpressionSegment) {
            return new ParameterMarkerLimitValueSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ((ParameterMarkerExpressionSegment) astNode).getParameterMarkerIndex());
        }
        LimitValueSegment result = new NumberLiteralLimitValueSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                Long.valueOf(((LiteralExpressionSegment) astNode).getLiterals().toString()));
        return result;
    }
    
    private LimitSegment createLimitSegmentWhenLimitAndOffset(final Select_limitContext ctx) {
        ParseTree astNode0 = ctx.getChild(0);
        LimitValueSegment rowCount = null;
        LimitValueSegment offset = null;
        if (astNode0 instanceof PostgreSQLStatementParser.Limit_clauseContext) {
            rowCount = null == ctx.limit_clause().select_limit_value() ? null : (LimitValueSegment) visit(ctx.limit_clause().select_limit_value());
        } else {
            offset = (LimitValueSegment) visit(ctx.offset_clause().select_offset_value());
        }
        ParseTree astNode1 = ctx.getChild(1);
        if (astNode1 instanceof PostgreSQLStatementParser.Limit_clauseContext) {
            rowCount = null == ctx.limit_clause().select_limit_value() ? null : (LimitValueSegment) visit(ctx.limit_clause().select_limit_value());
        } else {
            offset = (LimitValueSegment) visit(ctx.offset_clause().select_offset_value());
        }
        return new LimitSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), offset, rowCount);
    }

    private LimitSegment createLimitSegmentWhenRowCountOrOffsetAbsent(final Select_limitContext ctx) {
        if (null != ctx.limit_clause()) {
            if (null != ctx.limit_clause().select_offset_value()) {
                LimitValueSegment limit = (LimitValueSegment) visit(ctx.limit_clause().select_limit_value());
                LimitValueSegment offset = (LimitValueSegment) visit(ctx.limit_clause().select_offset_value());
                return new LimitSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), offset, limit);
            }
            LimitValueSegment limit = (LimitValueSegment) visit(ctx.limit_clause().select_limit_value());
            return new LimitSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), null, limit);
        }
        LimitValueSegment offset = (LimitValueSegment) visit(ctx.offset_clause().select_offset_value());
    
        return new LimitSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), offset, null);
    }
}
