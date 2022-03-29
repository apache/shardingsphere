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

import com.google.common.base.Joiner;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AExprContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AexprConstContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AliasClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AscDescContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AttrNameContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.BExprContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CExprContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ColIdContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ColumnNameContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ColumnNamesContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ColumnrefContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ConstraintNameContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DataTypeContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DataTypeLengthContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DataTypeNameContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DeleteContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ExecuteStmtContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ExprListContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ForLockingClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.FromClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.FromListContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.FuncApplicationContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.FuncExprContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.FunctionExprCommonSubexprContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.GroupByItemContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.GroupClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.HavingClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.IdentifierContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.InExprContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.IndexNameContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.IndirectionContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.InsertColumnItemContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.InsertColumnListContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.InsertContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.InsertRestContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.InsertTargetContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.JoinQualContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.JoinedTableContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.LimitClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.NameListContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.NumberLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.OwnerContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ParameterMarkerContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.QualifiedNameContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.RelationExprContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.RelationExprOptAliasContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.SchemaNameContext;
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
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.SortClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.SortbyContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.TableNameContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.TableNamesContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.TableReferenceContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.TargetElContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.TargetListContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.UnreservedWordContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.UpdateContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ValuesClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.WhereClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.WhereOrCurrentClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.WindowClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParserBaseVisitor;
import org.apache.shardingsphere.sql.parser.sql.common.constant.AggregationType;
import org.apache.shardingsphere.sql.parser.sql.common.constant.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.common.constant.ParameterMarkerType;
import org.apache.shardingsphere.sql.parser.sql.common.constant.UnionType;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.ConstraintSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.InsertColumnsSegment;
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
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.union.UnionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.AliasAvailable;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DataTypeLengthSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DataTypeSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.ParameterMarkerSegment;
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
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLExecuteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLDeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLUpdateStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * PostgreSQL Statement SQL visitor.
 */
@NoArgsConstructor
@Getter(AccessLevel.PROTECTED)
public abstract class PostgreSQLStatementSQLVisitor extends PostgreSQLStatementParserBaseVisitor<ASTNode> {
    
    private int currentParameterIndex;
    
    private final Collection<ParameterMarkerSegment> parameterMarkerSegments = new LinkedList<>();
    
    public PostgreSQLStatementSQLVisitor(final Properties props) {
    }
    
    @Override
    public final ASTNode visitParameterMarker(final ParameterMarkerContext ctx) {
        if (null != ctx.DOLLAR_()) {
            int parameterIndex = ((NumberLiteralValue) visit(ctx.numberLiterals())).getValue().intValue();
            if (parameterIndex > currentParameterIndex) {
                currentParameterIndex = parameterIndex;
            }
            return new ParameterMarkerValue(parameterIndex - 1, ParameterMarkerType.DOLLAR);
        }
        return new ParameterMarkerValue(currentParameterIndex++, ParameterMarkerType.QUESTION);
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
            ParameterMarkerValue parameterMarker = (ParameterMarkerValue) visit(ctx.parameterMarker());
            ParameterMarkerExpressionSegment segment = new ParameterMarkerExpressionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), parameterMarker.getValue(), parameterMarker.getType());
            parameterMarkerSegments.add(segment);
            return segment;
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
                ctx.selectWithParens().getStop().getStopIndex(), (PostgreSQLSelectStatement) visit(ctx.selectWithParens()));
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
        Collection<ExpressionSegment> expressionSegments = getExpressionSegments(getTargetRuleContextFromParseTree(ctx, CExprContext.class));
        // TODO replace aggregation segment
        String aggregationType = ctx.funcApplication().funcName().getText();
        if (AggregationType.isAggregationType(aggregationType)) {
            return createAggregationSegment(ctx.funcApplication(), aggregationType);
        }
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.funcApplication().funcName().getText(), getOriginalText(ctx));
        result.getParameters().addAll(expressionSegments);
        return result;
    }
    
    @Override
    public ASTNode visitFunctionExprCommonSubexpr(final FunctionExprCommonSubexprContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getChild(0).getText(), getOriginalText(ctx));
        Collection<ExpressionSegment> expressionSegments = getExpressionSegments(getTargetRuleContextFromParseTree(ctx, CExprContext.class));
        result.getParameters().addAll(expressionSegments);
        return result;
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
    
    private Collection<ExpressionSegment> getExpressionSegments(final Collection<CExprContext> cexprContexts) {
        Collection<ExpressionSegment> result = new LinkedList<>();
        for (CExprContext each : cexprContexts) {
            result.add((ExpressionSegment) visit(each));
        }
        return result;
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
            PostgreSQLSelectStatement select = (PostgreSQLSelectStatement) visit(ctx.selectWithParens());
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
            return new ExpressionOrderByItemSegment(ctx.aExpr().getStart().getStartIndex(), 
                    ctx.aExpr().getStop().getStopIndex(), getOriginalText(ctx.aExpr()), orderDirection, (ExpressionSegment) expr);
        }
        return new ExpressionOrderByItemSegment(ctx.aExpr().getStart().getStartIndex(), ctx.aExpr().getStop().getStopIndex(), getOriginalText(ctx.aExpr()), orderDirection);
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
        PostgreSQLInsertStatement result = (PostgreSQLInsertStatement) visit(ctx.insertRest());
        result.setTable((SimpleTableSegment) visit(ctx.insertTarget()));
        result.setParameterCount(getCurrentParameterIndex());
        result.getParameterMarkerSegments().addAll(getParameterMarkerSegments());
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
        SimpleTableSegment result = createTableFromRelationExpr(ctx.relationExpr());
        if (null != ctx.colId()) {
            result.setAlias(new AliasSegment(ctx.colId().start.getStartIndex(), ctx.stop.getStopIndex(), new IdentifierValue(ctx.colId().getText())));
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
        result.getParameterMarkerSegments().addAll(getParameterMarkerSegments());
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
        result.getParameterMarkerSegments().addAll(getParameterMarkerSegments());
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
        result.getParameterMarkerSegments().addAll(getParameterMarkerSegments());
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
        if (null != ctx.simpleSelect()) {
            return visit(ctx.simpleSelect());
        } else if (null != ctx.selectClauseN() && !ctx.selectClauseN().isEmpty()) {
            PostgreSQLSelectStatement result = (PostgreSQLSelectStatement) visit(ctx.selectClauseN(0));
            UnionSegment unionSegment = new UnionSegment(getUnionType(ctx), (PostgreSQLSelectStatement) visit(ctx.selectClauseN(1)),
                    ((TerminalNode) ctx.getChild(1)).getSymbol().getStartIndex(), ctx.getStop().getStopIndex());
            result.getUnionSegments().add(unionSegment);
            return result;
        } else {
            return visit(ctx.selectWithParens());
        }
    }
    
    private UnionType getUnionType(final SelectClauseNContext ctx) {
        boolean isDistinct = null == ctx.allOrDistinct() || null != ctx.allOrDistinct().DISTINCT();
        if (null != ctx.UNION()) {
            return isDistinct ? UnionType.UNION_DISTINCT : UnionType.UNION_ALL;
        } else if (null != ctx.INTERSECT()) {
            return isDistinct ? UnionType.INTERSECT_DISTINCT : UnionType.INTERSECT_ALL;
        } else {
            return isDistinct ? UnionType.EXCEPT_DISTINCT : UnionType.EXCEPT_ALL;
        }
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
            return new ExpressionOrderByItemSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), getOriginalText(ctx), OrderDirection.ASC);
        }
        return new ExpressionOrderByItemSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), getOriginalText(ctx), OrderDirection.ASC);
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
            SimpleTableSegment result = createTableFromRelationExpr(ctx.relationExpr());
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
    
    private SimpleTableSegment createTableFromRelationExpr(final RelationExprContext ctx) {
        QualifiedNameContext qualifiedName = ctx.qualifiedName();
        if (null != qualifiedName.indirection()) {
            AttrNameContext tableName = qualifiedName.indirection().indirectionEl().attrName();
            SimpleTableSegment table = new SimpleTableSegment(new TableNameSegment(tableName.start.getStartIndex(), 
                    tableName.stop.getStopIndex(), new IdentifierValue(tableName.getText())));
            OwnerSegment owner = new OwnerSegment(qualifiedName.colId().start.getStartIndex(), qualifiedName.colId().stop.getStopIndex(), new IdentifierValue(qualifiedName.colId().getText()));
            if (null != qualifiedName.indirection().indirection()) {
                OwnerSegment tableOwner = createTableOwner(qualifiedName.indirection().indirection());
                tableOwner.setOwner(owner);
                table.setOwner(tableOwner);
            } else {
                table.setOwner(owner);
            }
            return table;
        }
        return new SimpleTableSegment(new TableNameSegment(qualifiedName.colId().start.getStartIndex(), 
                qualifiedName.colId().stop.getStopIndex(), new IdentifierValue(qualifiedName.colId().getText())));
    }
    
    private OwnerSegment createTableOwner(final IndirectionContext ctx) {
        AttrNameContext attrName = ctx.indirectionEl().attrName();
        return new OwnerSegment(attrName.start.getStartIndex(), attrName.stop.getStopIndex(), new IdentifierValue(attrName.getText()));
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
        return new PostgreSQLExecuteStatement();
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
