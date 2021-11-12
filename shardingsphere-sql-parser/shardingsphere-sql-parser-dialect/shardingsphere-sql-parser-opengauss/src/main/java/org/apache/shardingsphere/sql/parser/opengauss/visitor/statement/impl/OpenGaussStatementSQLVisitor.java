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

package org.apache.shardingsphere.sql.parser.opengauss.visitor.statement.impl;

import com.google.common.base.Joiner;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementBaseVisitor;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AExprContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AexprConstContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AliasClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AscDescContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AttrNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.BExprContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CExprContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ColIdContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ColumnNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ColumnNamesContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ColumnrefContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ConstraintNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DataTypeContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DataTypeLengthContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DataTypeNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DeleteContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ExecuteStmtContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ExprListContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ForLockingClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.FromClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.FromListContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.FuncApplicationContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.FuncExprContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.FunctionExprCommonSubexprContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.GroupByItemContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.GroupClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.HavingClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.IdentifierContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.InExprContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.IndexNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.InsertColumnItemContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.InsertColumnListContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.InsertContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.InsertRestContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.InsertTargetContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.JoinQualContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.JoinedTableContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.LimitClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.NameListContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.NumberLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.OwnerContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ParameterMarkerContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.QualifiedNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.RelationExprContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.RelationExprOptAliasContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.SchemaNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.SelectClauseNContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.SelectContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.SelectLimitContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.SelectLimitValueContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.SelectNoParensContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.SelectOffsetValueContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.SelectWithParensContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.SetClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.SetClauseListContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.SetTargetContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.SimpleSelectContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.SortClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.SortbyContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.TableNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.TableNamesContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.TableReferenceContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.TargetElContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.TargetListContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.UnreservedWordContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.UpdateContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ValuesClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.WhereClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.WhereOrCurrentClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.WindowClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.OptOnDuplicateKeyContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AssignmentContext;
import org.apache.shardingsphere.sql.parser.sql.common.constant.AggregationType;
import org.apache.shardingsphere.sql.parser.sql.common.constant.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.ConstraintSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExistsSubqueryExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubqueryExpressionSegment;
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
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.HavingSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.LockSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.AliasAvailable;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DataTypeLengthSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DataTypeSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.WindowSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.keyword.KeywordValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.LiteralValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.BooleanLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.NumberLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.StringLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.parametermarker.ParameterMarkerValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussExecuteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.dml.OpenGaussDeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.dml.OpenGaussInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.dml.OpenGaussSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.dml.OpenGaussUpdateStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * OpenGauss Statement SQL visitor.
 */
@NoArgsConstructor
@Getter(AccessLevel.PROTECTED)
public abstract class OpenGaussStatementSQLVisitor extends OpenGaussStatementBaseVisitor<ASTNode> {
    
    private int currentParameterIndex;
    
    public OpenGaussStatementSQLVisitor(final Properties props) {
    }
    
    @Override
    public final ASTNode visitParameterMarker(final ParameterMarkerContext ctx) {
        return new ParameterMarkerValue(currentParameterIndex++);
    }
    
    @Override
    public final ASTNode visitNumberLiterals(final NumberLiteralsContext ctx) {
        return new NumberLiteralValue(ctx.getText());
    }
    
    @Override
    public final ASTNode visitIdentifier(final IdentifierContext ctx) {
        UnreservedWordContext unreservedWord = ctx.unreservedWord();
        return null != unreservedWord ? visit(unreservedWord) : new IdentifierValue(ctx.getText());
    }
    
    @Override
    public final ASTNode visitUnreservedWord(final UnreservedWordContext ctx) {
        return new IdentifierValue(ctx.getText());
    }
    
    @Override
    public final ASTNode visitSchemaName(final SchemaNameContext ctx) {
        return visit(ctx.identifier());
    }
    
    @Override
    public final ASTNode visitTableName(final TableNameContext ctx) {
        SimpleTableSegment result = new SimpleTableSegment(new TableNameSegment(ctx.name().getStart().getStartIndex(), ctx.name().getStop().getStopIndex(), (IdentifierValue) visit(ctx.name())));
        OwnerContext owner = ctx.owner();
        if (null != owner) {
            result.setOwner(new OwnerSegment(owner.getStart().getStartIndex(), owner.getStop().getStopIndex(), (IdentifierValue) visit(owner.identifier())));
        }
        return result;
    }
    
    @Override
    public final ASTNode visitColumnName(final ColumnNameContext ctx) {
        ColumnSegment result = new ColumnSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (IdentifierValue) visit(ctx.name()));
        OwnerContext owner = ctx.owner();
        if (null != owner) {
            result.setOwner(new OwnerSegment(owner.getStart().getStartIndex(), owner.getStop().getStopIndex(), (IdentifierValue) visit(owner.identifier())));
        }
        return result;
    }
    
    @Override
    public final ASTNode visitIndexName(final IndexNameContext ctx) {
        return new IndexSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (IdentifierValue) visit(ctx.identifier()));
    }
    
    @Override
    public final ASTNode visitConstraintName(final ConstraintNameContext ctx) {
        return new ConstraintSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (IdentifierValue) visit(ctx.identifier()));
    }
    
    @Override
    public final ASTNode visitTableNames(final TableNamesContext ctx) {
        CollectionValue<SimpleTableSegment> result = new CollectionValue<>();
        for (TableNameContext each : ctx.tableName()) {
            result.getValue().add((SimpleTableSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public final ASTNode visitColumnNames(final ColumnNamesContext ctx) {
        CollectionValue<ColumnSegment> result = new CollectionValue<>();
        for (ColumnNameContext each : ctx.columnName()) {
            result.getValue().add((ColumnSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitAExpr(final AExprContext ctx) {
        if (null != ctx.cExpr()) {
            return visit(ctx.cExpr());
        }
        if (null != ctx.BETWEEN()) {
            return createBetweenSegment(ctx);
        }
        if (null != ctx.IN()) {
            return createInSegment(ctx);
        }
        if (null != ctx.patternMatchingOperator()) {
            return createPatternMatchingOperationSegment(ctx);
        }
        if (null != ctx.comparisonOperator()) {
            return createCommonBinaryOperationSegment(ctx, ctx.comparisonOperator().getText());
        }
        if (null != ctx.andOperator()) {
            return createCommonBinaryOperationSegment(ctx, ctx.andOperator().getText());
        }
        if (null != ctx.orOperator()) {
            return createCommonBinaryOperationSegment(ctx, ctx.orOperator().getText());
        }
        super.visitAExpr(ctx);
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new CommonExpressionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), text);
    }
        
    private BinaryOperationExpression createPatternMatchingOperationSegment(final AExprContext ctx) {
        String operator = ctx.patternMatchingOperator().getText();
        ExpressionSegment left = (ExpressionSegment) visit(ctx.aExpr(0));
        ListExpression right = new ListExpression(ctx.aExpr(1).start.getStartIndex(), ctx.aExpr().get(ctx.aExpr().size() - 1).stop.getStopIndex());
        for (int i = 1; i < ctx.aExpr().size(); i++) {
            right.getItems().add((ExpressionSegment) visit(ctx.aExpr().get(i)));
        }
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new BinaryOperationExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, operator, text);
    }
    
    private BinaryOperationExpression createCommonBinaryOperationSegment(final AExprContext ctx, final String operator) {
        ExpressionSegment left = (ExpressionSegment) visit(ctx.aExpr(0));
        ExpressionSegment right = (ExpressionSegment) visit(ctx.aExpr(1));
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new BinaryOperationExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, operator, text);
    }
    
    @Override
    public ASTNode visitCExpr(final CExprContext ctx) {
        if (null != ctx.columnref()) {
            return visit(ctx.columnref());
        }
        if (null != ctx.parameterMarker()) {
            return new ParameterMarkerExpressionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ((ParameterMarkerValue) visit(ctx.parameterMarker())).getValue());
        }
        if (null != ctx.aexprConst()) {
            ASTNode astNode = visit(ctx.aexprConst());
            if (astNode instanceof StringLiteralValue || astNode instanceof BooleanLiteralValue || astNode instanceof NumberLiteralValue) {
                return new LiteralExpressionSegment(ctx.aexprConst().start.getStartIndex(), ctx.aexprConst().stop.getStopIndex(), ((LiteralValue) astNode).getValue());
            }
            return astNode;
        }
        if (null != ctx.aExpr()) {
            return visit(ctx.aExpr());
        }
        if (null != ctx.funcExpr()) {
            return visit(ctx.funcExpr());
        }
        if (null != ctx.selectWithParens()) {
            return createSubqueryExpressionSegment(ctx);
        }
        super.visitCExpr(ctx);
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new CommonExpressionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), text);
    }
    
    private ExpressionSegment createSubqueryExpressionSegment(final CExprContext ctx) {
        SubquerySegment subquerySegment = new SubquerySegment(ctx.selectWithParens().getStart().getStartIndex(), 
                ctx.selectWithParens().getStop().getStopIndex(), (OpenGaussSelectStatement) visit(ctx.selectWithParens()));
        if (null != ctx.EXISTS()) {
            return new ExistsSubqueryExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), subquerySegment);
        }
        return new SubqueryExpressionSegment(subquerySegment);
    }
    
    @Override
    public ASTNode visitFuncExpr(final FuncExprContext ctx) {
        if (null != ctx.functionExprCommonSubexpr()) {
            return visit(ctx.functionExprCommonSubexpr());
        }
        calculateParameterCount(getTargetRuleContextFromParseTree(ctx, CExprContext.class));
        // TODO replace aggregation segment
        String aggregationType = ctx.funcApplication().funcName().getText();
        if (AggregationType.isAggregationType(aggregationType)) {
            return createAggregationSegment(ctx.funcApplication(), aggregationType);
        }
        return new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.funcApplication().funcName().getText(), getOriginalText(ctx));
    }
    
    @Override
    public ASTNode visitFunctionExprCommonSubexpr(final FunctionExprCommonSubexprContext ctx) {
        calculateParameterCount(getTargetRuleContextFromParseTree(ctx, CExprContext.class));
        return new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getChild(0).getText(), getOriginalText(ctx));
    }
    
    private <T extends ParseTree> Collection<T> getTargetRuleContextFromParseTree(final ParseTree parseTree, final Class<? extends T> clazz) {
        Collection<T> result = new LinkedList<>();
        for (int index = 0; index < parseTree.getChildCount(); index++) {
            ParseTree child = parseTree.getChild(index);
            if (clazz.isInstance(child)) {
                result.add(clazz.cast(child));
            } else {
                result.addAll(getTargetRuleContextFromParseTree(child, clazz));
            }
        }
        return result;
    }
    
    private void calculateParameterCount(final Collection<CExprContext> cexprContexts) {
        for (CExprContext each : cexprContexts) {
            visit(each);
        }
    }
    
    @Override
    public ASTNode visitAexprConst(final AexprConstContext ctx) {
        if (null != ctx.NUMBER_()) {
            return new NumberLiteralValue(ctx.NUMBER_().getText());
        }
        if (null != ctx.STRING_()) {
            return new StringLiteralValue(ctx.STRING_().getText());
        }
        if (null != ctx.FALSE()) {
            return new BooleanLiteralValue(ctx.FALSE().getText());
        }
        if (null != ctx.TRUE()) {
            return new BooleanLiteralValue(ctx.TRUE().getText());
        }
        return new CommonExpressionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.getText());
    }
    
    @Override
    public ASTNode visitColumnref(final ColumnrefContext ctx) {
        if (null != ctx.indirection()) {
            AttrNameContext attrName = ctx.indirection().indirectionEl().attrName();
            ColumnSegment result = new ColumnSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), new IdentifierValue(attrName.getText()));
            OwnerSegment owner = new OwnerSegment(ctx.colId().start.getStartIndex(), ctx.colId().stop.getStopIndex(), new IdentifierValue(ctx.colId().getText()));
            result.setOwner(owner);
            return result;
        }
        return new ColumnSegment(ctx.colId().start.getStartIndex(), ctx.colId().stop.getStopIndex(), new IdentifierValue(ctx.colId().getText()));
    }
    
    private InExpression createInSegment(final AExprContext ctx) {
        ExpressionSegment left = (ExpressionSegment) visit(ctx.aExpr(0));
        ExpressionSegment right = createInExpressionSegment(ctx.inExpr());
        boolean not = null != ctx.NOT();
        return new InExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, not);
    }
    
    @SuppressWarnings("unchecked")
    private ExpressionSegment createInExpressionSegment(final InExprContext ctx) {
        if (null != ctx.selectWithParens()) {
            OpenGaussSelectStatement select = (OpenGaussSelectStatement) visit(ctx.selectWithParens());
            SubquerySegment subquerySegment = new SubquerySegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), select);
            return new SubqueryExpressionSegment(subquerySegment);
        }
        ListExpression result = new ListExpression(ctx.LP_().getSymbol().getStartIndex(), ctx.RP_().getSymbol().getStopIndex());
        result.getItems().addAll(((CollectionValue<ExpressionSegment>) visit(ctx.exprList())).getValue());
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitExprList(final ExprListContext ctx) {
        CollectionValue<ExpressionSegment> result = new CollectionValue<>();
        if (null != ctx.exprList()) {
            result.combine((CollectionValue<ExpressionSegment>) visitExprList(ctx.exprList()));
        }
        result.getValue().add((ExpressionSegment) visit(ctx.aExpr()));
        return result;
    }
    
    private BetweenExpression createBetweenSegment(final AExprContext ctx) {
        ExpressionSegment left = (ExpressionSegment) visit(ctx.aExpr(0));
        ExpressionSegment between = (ExpressionSegment) visit(ctx.bExpr());
        ExpressionSegment and = (ExpressionSegment) visit(ctx.aExpr(1));
        boolean not = null != ctx.NOT();
        return new BetweenExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, between, and, not);
    }
    
    @Override
    public ASTNode visitBExpr(final BExprContext ctx) {
        if (null != ctx.cExpr()) {
            return visit(ctx.cExpr());
        }
        if (null != ctx.TYPE_CAST_() || null != ctx.qualOp()) {
            ExpressionSegment left = (ExpressionSegment) visit(ctx.bExpr(0));
            ExpressionSegment right;
            String operator;
            if (null != ctx.TYPE_CAST_()) {
                operator = ctx.TYPE_CAST_().getText();
                right = new CommonExpressionSegment(ctx.typeName().start.getStartIndex(), ctx.typeName().stop.getStopIndex(), ctx.typeName().getText());
            } else {
                operator = ctx.qualOp().getText();
                right = (ExpressionSegment) visit(ctx.bExpr(1));
            }
            String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
            return new BinaryOperationExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, operator, text);
        }
        for (BExprContext each : ctx.bExpr()) {
            visit(each);
        }
        return new LiteralExpressionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.getText());
    }
    
    private ProjectionSegment createAggregationSegment(final FuncApplicationContext ctx, final String aggregationType) {
        AggregationType type = AggregationType.valueOf(aggregationType.toUpperCase());
        String innerExpression = ctx.start.getInputStream().getText(new Interval(ctx.LP_().getSymbol().getStartIndex(), ctx.stop.getStopIndex()));
        if (null == ctx.DISTINCT()) {
            return new AggregationProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), type, innerExpression);
        }
        return new AggregationDistinctProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), type, innerExpression, getDistinctExpression(ctx));
    }
    
    private String getDistinctExpression(final FuncApplicationContext ctx) {
        StringBuilder result = new StringBuilder();
        result.append(ctx.funcArgList().getText());
        if (null != ctx.sortClause()) {
            result.append(ctx.sortClause().getText());
        }
        return result.toString();
    }
    
    @Override
    public final ASTNode visitDataTypeName(final DataTypeNameContext ctx) {
        IdentifierContext identifierContext = ctx.identifier();
        if (null != identifierContext) {
            return new KeywordValue(identifierContext.getText());
        }
        Collection<String> dataTypeNames = new LinkedList<>();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            dataTypeNames.add(ctx.getChild(i).getText());
        }
        return new KeywordValue(Joiner.on(" ").join(dataTypeNames));
    }
    
    @Override
    public final ASTNode visitSortClause(final SortClauseContext ctx) {
        Collection<OrderByItemSegment> items = new LinkedList<>();
        for (SortbyContext each : ctx.sortbyList().sortby()) {
            items.add((OrderByItemSegment) visit(each));
        }
        return new OrderBySegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), items);
    }
    
    @Override
    public final ASTNode visitSortby(final SortbyContext ctx) {
        OrderDirection orderDirection = null != ctx.ascDesc() ? generateOrderDirection(ctx.ascDesc()) : OrderDirection.ASC;
        ASTNode expr = visit(ctx.aExpr());
        if (expr instanceof ColumnSegment) {
            ColumnSegment column = (ColumnSegment) expr;
            return new ColumnOrderByItemSegment(column, orderDirection);
        }
        if (expr instanceof LiteralExpressionSegment) {
            LiteralExpressionSegment index = (LiteralExpressionSegment) expr;
            return new IndexOrderByItemSegment(index.getStartIndex(), index.getStopIndex(), Integer.parseInt(index.getLiterals().toString()), orderDirection);
        }
        if (expr instanceof ExpressionSegment) {
            return new ExpressionOrderByItemSegment(ctx.aExpr().getStart().getStartIndex(), ctx.aExpr().getStop().getStopIndex(), ctx.aExpr().getText(), orderDirection, (ExpressionSegment) expr);
        }
        return new ExpressionOrderByItemSegment(ctx.aExpr().getStart().getStartIndex(), ctx.aExpr().getStop().getStopIndex(), ctx.aExpr().getText(), orderDirection);
    }
    
    private OrderDirection generateOrderDirection(final AscDescContext ctx) {
        return null == ctx.DESC() ? OrderDirection.ASC : OrderDirection.DESC;
    }
    
    @Override
    public final ASTNode visitDataType(final DataTypeContext ctx) {
        DataTypeSegment result = new DataTypeSegment();
        result.setDataTypeName(((KeywordValue) visit(ctx.dataTypeName())).getValue());
        result.setStartIndex(ctx.start.getStartIndex());
        result.setStopIndex(ctx.stop.getStopIndex());
        if (null != ctx.dataTypeLength()) {
            DataTypeLengthSegment dataTypeLengthSegment = (DataTypeLengthSegment) visit(ctx.dataTypeLength());
            result.setDataLength(dataTypeLengthSegment);
        }
        return result;
    }
    
    @Override
    public final ASTNode visitDataTypeLength(final DataTypeLengthContext ctx) {
        DataTypeLengthSegment result = new DataTypeLengthSegment();
        result.setStartIndex(ctx.start.getStartIndex());
        result.setStopIndex(ctx.stop.getStartIndex());
        List<TerminalNode> numbers = ctx.NUMBER_();
        if (1 == numbers.size()) {
            result.setPrecision(Integer.parseInt(numbers.get(0).getText()));
        }
        if (2 == numbers.size()) {
            result.setPrecision(Integer.parseInt(numbers.get(0).getText()));
            result.setScale(Integer.parseInt(numbers.get(1).getText()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitInsert(final InsertContext ctx) {
        // TODO :FIXME, since there is no segment for insertValuesClause, InsertStatement is created by sub rule.
        // TODO :deal with insert select
        OpenGaussInsertStatement result = (OpenGaussInsertStatement) visit(ctx.insertRest());
        result.setTable((SimpleTableSegment) visit(ctx.insertTarget()));
        if (null != ctx.optOnDuplicateKey()) {
            result.setOnDuplicateKeyColumnsSegment((OnDuplicateKeyColumnsSegment) visit(ctx.optOnDuplicateKey()));
        }
        result.setParameterCount(getCurrentParameterIndex());
        return result;
    }
    
    @Override
    public ASTNode visitInsertTarget(final InsertTargetContext ctx) {
        QualifiedNameContext qualifiedName = ctx.qualifiedName();
        OwnerSegment owner = null;
        TableNameSegment tableName;
        if (null != qualifiedName.indirection()) {
            ColIdContext colId = ctx.qualifiedName().colId();
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
        OpenGaussInsertStatement result = new OpenGaussInsertStatement();
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
    public ASTNode visitOptOnDuplicateKey(final OptOnDuplicateKeyContext ctx) {
        if (null != ctx.NOTHING()) {
            return new OnDuplicateKeyColumnsSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), Collections.emptyList());
        }
        Collection<AssignmentSegment> columns = new LinkedList<>();
        for (AssignmentContext each : ctx.assignment()) {
            columns.add((AssignmentSegment) visit(each));
        }
        return new OnDuplicateKeyColumnsSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), columns);
    }
    
    @Override
    public ASTNode visitAssignment(final AssignmentContext ctx) {
        List<ColumnSegment> columnSegments = Collections.singletonList((ColumnSegment) visit(ctx.setTarget()));
        ExpressionSegment expressionSegment;
        if (null != ctx.aExpr()) {
            expressionSegment = (ExpressionSegment) visit(ctx.aExpr());
        } else {
            String value = ctx.start.getInputStream().getText(new Interval(ctx.VALUES().getSymbol().getStartIndex(), ctx.stop.getStopIndex()));
            expressionSegment = new CommonExpressionSegment(ctx.VALUES().getSymbol().getStartIndex(), ctx.getStop().getStopIndex(), value);
        }
        return new ColumnAssignmentSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), columnSegments, expressionSegment);
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
        List<ColumnSegment> columnSegments = new LinkedList<>();
        columnSegments.add(columnSegment);
        ExpressionSegment expressionSegment = (ExpressionSegment) visit(ctx.aExpr());
        return new ColumnAssignmentSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), columnSegments, expressionSegment);
    }
    
    @Override
    public ASTNode visitSetTarget(final SetTargetContext ctx) {
        IdentifierValue identifierValue = new IdentifierValue(ctx.colId().getText());
        return new ColumnSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), identifierValue);
    }
    
    @Override
    public ASTNode visitRelationExprOptAlias(final RelationExprOptAliasContext ctx) {
        SimpleTableSegment result = generateTableFromRelationExpr(ctx.relationExpr());
        if (null != ctx.colId()) {
            result.setAlias(new AliasSegment(ctx.colId().start.getStartIndex(), ctx.stop.getStopIndex(), new IdentifierValue(ctx.colId().getText())));
        }
        return result;
    }
    
    @Override
    public ASTNode visitUpdate(final UpdateContext ctx) {
        OpenGaussUpdateStatement result = new OpenGaussUpdateStatement();
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
        OpenGaussDeleteStatement result = new OpenGaussDeleteStatement();
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
        OpenGaussSelectStatement result = (OpenGaussSelectStatement) visit(ctx.selectNoParens());
        result.setParameterCount(getCurrentParameterIndex());
        return result;
    }
    
    @Override
    public ASTNode visitSelectNoParens(final SelectNoParensContext ctx) {
        OpenGaussSelectStatement result = (OpenGaussSelectStatement) visit(ctx.selectClauseN());
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
        return null == ctx.simpleSelect() ? new OpenGaussSelectStatement() : visit(ctx.simpleSelect());
    }
    
    @Override
    public ASTNode visitSimpleSelect(final SimpleSelectContext ctx) {
        OpenGaussSelectStatement result = new OpenGaussSelectStatement();
        if (null != ctx.targetList()) {
            ProjectionsSegment projects = (ProjectionsSegment) visit(ctx.targetList());
            if (null != ctx.distinctClause()) {
                projects.setDistinctRow(true);
            }
            result.setProjections(projects);
        } else {
            result.setProjections(new ProjectionsSegment(-1, -1));
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
        if (null != ctx.havingClause()) {
            result.setHaving((HavingSegment) visit(ctx.havingClause()));
        }
        if (null != ctx.windowClause()) {
            result.setWindow((WindowSegment) visit(ctx.windowClause()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitHavingClause(final HavingClauseContext ctx) {
        ExpressionSegment expr = (ExpressionSegment) visit(ctx.aExpr());
        return new HavingSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), expr);
    }
    
    @Override
    public ASTNode visitWindowClause(final WindowClauseContext ctx) {
        return new WindowSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
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
            ProjectionsSegment projections = (ProjectionsSegment) visit(ctx.targetList());
            result.getProjections().addAll(projections.getProjections());
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
        ProjectionSegment result = new ExpressionProjectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), getOriginalText(expr));
        if (null != expr.cExpr()) {
            ASTNode projection = visit(expr.cExpr());
            if (projection instanceof ColumnSegment) {
                result = new ColumnProjectionSegment((ColumnSegment) projection);
            }
            if (projection instanceof FunctionSegment) {
                result = new ExpressionProjectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), getOriginalText(expr), (FunctionSegment) projection);
            }
            if (projection instanceof AggregationProjectionSegment) {
                result = (AggregationProjectionSegment) projection;
            }
            if (projection instanceof SubqueryExpressionSegment) {
                SubqueryExpressionSegment subqueryExpression = (SubqueryExpressionSegment) projection;
                String text = ctx.start.getInputStream().getText(new Interval(subqueryExpression.getStartIndex(), subqueryExpression.getStopIndex()));
                result = new SubqueryProjectionSegment(subqueryExpression.getSubquery(), text);
            }
            if (projection instanceof ExistsSubqueryExpression) {
                ExistsSubqueryExpression existsSubqueryExpression = (ExistsSubqueryExpression) projection;
                String text = ctx.start.getInputStream().getText(new Interval(existsSubqueryExpression.getStartIndex(), existsSubqueryExpression.getStopIndex()));
                result = new SubqueryProjectionSegment(existsSubqueryExpression.getSubquery(), text);
            }
        }
        if (result instanceof AliasAvailable && null != ctx.identifier()) {
            ((AliasAvailable) result).setAlias(new AliasSegment(ctx.identifier().start.getStartIndex(), ctx.identifier().stop.getStopIndex(), new IdentifierValue(ctx.identifier().getText())));
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
        return visit(ctx.tableReference());
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
            OpenGaussSelectStatement select = (OpenGaussSelectStatement) visit(ctx.selectWithParens());
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
        TableNameSegment tableName = new TableNameSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), new IdentifierValue("not support"));
        return new SimpleTableSegment(tableName);
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
            SimpleTableSegment table = new SimpleTableSegment(new TableNameSegment(tableName.start.getStartIndex(), 
                    tableName.stop.getStopIndex(), new IdentifierValue(tableName.getText())));
            table.setOwner(new OwnerSegment(qualifiedName.colId().start.getStartIndex(), qualifiedName.colId().stop.getStopIndex(), new IdentifierValue(qualifiedName.colId().getText())));
            return table;
        }
        return new SimpleTableSegment(new TableNameSegment(qualifiedName.colId().start.getStartIndex(), 
                qualifiedName.colId().stop.getStopIndex(), new IdentifierValue(qualifiedName.colId().getText())));
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
    public ASTNode visitExecuteStmt(final ExecuteStmtContext ctx) {
        return new OpenGaussExecuteStatement();
    }
    
    /**
     * Get original text.
     *
     * @param ctx context
     * @return original text
     */
    protected String getOriginalText(final ParserRuleContext ctx) {
        return ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
    }
}
