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

package org.apache.shardingsphere.sql.parser.engine.opengauss.visitor.statement;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AExprContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AexprConstContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AliasClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AliasContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AnyNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AscDescContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AssignmentContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AttrNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AttrsContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.BExprContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CExprContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CaseExprContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ColIdContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ColumnNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ColumnNamesContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ColumnrefContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CommonTableExprContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ConstraintNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DataTypeContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DataTypeLengthContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DataTypeNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DeleteContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ExecuteStmtContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ExprListContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ExtractArgContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ForLockingClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.FromClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.FromListContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.FuncApplicationContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.FuncExprContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.FuncNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.FunctionExprCommonSubexprContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.GroupByItemContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.GroupClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.HavingClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.IdentifierContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.InExprContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.IndexNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.IndirectionContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.InsertColumnItemContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.InsertColumnListContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.InsertContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.InsertRestContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.InsertTargetContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.IntoClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.JoinQualContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.JoinedTableContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.LimitClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.NameListContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.NaturalJoinTypeContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.NullsOrderContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.NumberLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.OptOnDuplicateKeyContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.OuterJoinTypeContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.OwnerContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ParameterMarkerContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.QualifiedNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.QualifiedNameListContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.RelationExprOptAliasContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.SchemaNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.SelectClauseNContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.SelectContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.SelectFetchValueContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.SelectLimitContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.SelectLimitValueContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.SelectNoParensContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.SelectOffsetValueContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.SelectWithParensContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.SetClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.SetClauseListContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.SetTargetContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.SignedIconstContext;
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
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.WhenClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.WhereClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.WhereOrCurrentClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.WindowClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.WindowDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.WindowDefinitionListContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.WithClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParserBaseVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.CombineType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.JoinType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.statement.core.enums.ParameterMarkerType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.SubqueryType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.ConstraintSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.ReturningSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.combine.CombineSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.CaseWhenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExistsSubqueryExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExtractArgExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.TypeCastExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationDistinctProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.LimitValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.ParameterMarkerLimitValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.HavingSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.LockSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasAvailable;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DataTypeLengthSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DataTypeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.NameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.ParameterMarkerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WindowItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WindowSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.FunctionTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.ExecuteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.util.SQLUtils;
import org.apache.shardingsphere.sql.parser.statement.core.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.keyword.KeywordValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.LiteralValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.BooleanLiteralValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.NullLiteralValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.NumberLiteralValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.OtherLiteralValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.StringLiteralValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.parametermarker.ParameterMarkerValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Statement visitor for openGauss.
 */
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public abstract class OpenGaussStatementVisitor extends OpenGaussStatementParserBaseVisitor<ASTNode> {
    
    private final DatabaseType databaseType;
    
    private final Collection<ParameterMarkerSegment> parameterMarkerSegments = new LinkedList<>();
    
    @Override
    public final ASTNode visitParameterMarker(final ParameterMarkerContext ctx) {
        if (null == ctx.DOLLAR_()) {
            return new ParameterMarkerValue(parameterMarkerSegments.size(), ParameterMarkerType.QUESTION);
        }
        return new ParameterMarkerValue(new NumberLiteralValue(ctx.NUMBER_().getText()).getValue().intValue() - 1, ParameterMarkerType.DOLLAR);
    }
    
    @Override
    public final ASTNode visitNumberLiterals(final NumberLiteralsContext ctx) {
        return new NumberLiteralValue(ctx.NUMBER_().getText());
    }
    
    @Override
    public final ASTNode visitIdentifier(final IdentifierContext ctx) {
        UnreservedWordContext unreservedWord = ctx.unreservedWord();
        return null == unreservedWord ? new IdentifierValue(ctx.getText()) : visit(unreservedWord);
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
        IndexNameSegment indexName = new IndexNameSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), (IdentifierValue) visit(ctx.identifier()));
        return new IndexSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), indexName);
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
        if (null != ctx.TYPE_CAST_()) {
            return new TypeCastExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.getText(), (ExpressionSegment) visit(ctx.aExpr(0)), ctx.typeName().getText());
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
        Optional<String> binaryOperator = findBinaryOperator(ctx);
        if (binaryOperator.isPresent()) {
            return createBinaryOperationSegment(ctx, binaryOperator.get());
        }
        super.visitAExpr(ctx);
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new CommonExpressionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), text);
    }
    
    private Optional<String> findBinaryOperator(final AExprContext ctx) {
        if (null != ctx.IS()) {
            return Optional.of(ctx.IS().getText());
        }
        if (null != ctx.ISNULL()) {
            return Optional.of("IS");
        }
        if (1 == ctx.aExpr().size()) {
            return Optional.empty();
        }
        if (null != ctx.comparisonOperator()) {
            return Optional.of(ctx.comparisonOperator().getText());
        }
        if (null != ctx.andOperator()) {
            return Optional.of(ctx.andOperator().getText());
        }
        if (null != ctx.orOperator()) {
            return Optional.of(ctx.orOperator().getText());
        }
        if (null != ctx.PLUS_()) {
            return Optional.of(ctx.PLUS_().getText());
        }
        if (null != ctx.MINUS_()) {
            return Optional.of(ctx.MINUS_().getText());
        }
        if (null != ctx.ASTERISK_()) {
            return Optional.of(ctx.ASTERISK_().getText());
        }
        if (null != ctx.SLASH_()) {
            return Optional.of(ctx.SLASH_().getText());
        }
        return Optional.empty();
    }
    
    private BinaryOperationExpression createPatternMatchingOperationSegment(final AExprContext ctx) {
        String operator = getOriginalText(ctx.patternMatchingOperator()).toUpperCase();
        ExpressionSegment left = (ExpressionSegment) visit(ctx.aExpr(0));
        ListExpression right = new ListExpression(ctx.aExpr(1).start.getStartIndex(), ctx.aExpr().get(ctx.aExpr().size() - 1).stop.getStopIndex());
        for (int i = 1; i < ctx.aExpr().size(); i++) {
            right.getItems().add((ExpressionSegment) visit(ctx.aExpr().get(i)));
        }
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new BinaryOperationExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, operator, text);
    }
    
    private BinaryOperationExpression createBinaryOperationSegment(final AExprContext ctx, final String operator) {
        if ("IS".equalsIgnoreCase(operator)) {
            ExpressionSegment left = (ExpressionSegment) visit(ctx.aExpr(0));
            String rightText;
            ExpressionSegment right;
            if (null != ctx.IS()) {
                rightText = ctx.start.getInputStream().getText(new Interval(ctx.IS().getSymbol().getStopIndex() + 2, ctx.stop.getStopIndex())).trim();
                right = new LiteralExpressionSegment(ctx.IS().getSymbol().getStopIndex() + 2, ctx.stop.getStopIndex(), rightText);
            } else {
                rightText = ctx.start.getInputStream().getText(new Interval(ctx.ISNULL().getSymbol().getStartIndex() + 2, ctx.stop.getStopIndex())).trim();
                right = new LiteralExpressionSegment(ctx.ISNULL().getSymbol().getStartIndex() + 2, ctx.stop.getStopIndex(), rightText);
            }
            return new BinaryOperationExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, "IS",
                    ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex())));
        }
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
            ParameterMarkerExpressionSegment result = new ParameterMarkerExpressionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), parameterMarker.getValue(), parameterMarker.getType());
            parameterMarkerSegments.add(result);
            return result;
        }
        if (null != ctx.aexprConst()) {
            return visit(ctx.aexprConst());
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
        if (null != ctx.caseExpr()) {
            return visit(ctx.caseExpr());
        }
        super.visitCExpr(ctx);
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new CommonExpressionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), text);
    }
    
    private ExpressionSegment createSubqueryExpressionSegment(final CExprContext ctx) {
        SubquerySegment subquerySegment = new SubquerySegment(ctx.selectWithParens().getStart().getStartIndex(),
                ctx.selectWithParens().getStop().getStopIndex(), (SelectStatement) visit(ctx.selectWithParens()), getOriginalText(ctx.selectWithParens()));
        if (null != ctx.EXISTS()) {
            subquerySegment.getSelect().setSubqueryType(SubqueryType.EXISTS);
            return new ExistsSubqueryExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), subquerySegment);
        }
        return new SubqueryExpressionSegment(subquerySegment);
    }
    
    @Override
    public ASTNode visitCaseExpr(final CaseExprContext ctx) {
        Collection<ExpressionSegment> whenExprs = new LinkedList<>();
        Collection<ExpressionSegment> thenExprs = new LinkedList<>();
        for (WhenClauseContext each : ctx.whenClauseList().whenClause()) {
            whenExprs.add((ExpressionSegment) visit(each.aExpr(0)));
            thenExprs.add((ExpressionSegment) visit(each.aExpr(1)));
        }
        ExpressionSegment caseExpr = null == ctx.caseArg() ? null : (ExpressionSegment) visit(ctx.caseArg().aExpr());
        ExpressionSegment elseExpr = null == ctx.caseDefault() ? null : (ExpressionSegment) visit(ctx.caseDefault().aExpr());
        return new CaseWhenExpression(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), caseExpr, whenExprs, thenExprs, elseExpr);
    }
    
    @Override
    public ASTNode visitFuncExpr(final FuncExprContext ctx) {
        if (null != ctx.functionExprCommonSubexpr()) {
            return visit(ctx.functionExprCommonSubexpr());
        }
        return visit(ctx.funcApplication());
    }
    
    @Override
    public ASTNode visitFunctionExprCommonSubexpr(final FunctionExprCommonSubexprContext ctx) {
        if (null != ctx.CAST()) {
            return new TypeCastExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.getText(), (ExpressionSegment) visit(ctx.aExpr(0)), ctx.typeName().getText());
        }
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getChild(0).getText(), getOriginalText(ctx));
        Collection<ExpressionSegment> expressionSegments = getExpressionSegments(getTargetRuleContextFromParseTree(ctx, AExprContext.class));
        if ("EXTRACT".equalsIgnoreCase(ctx.getChild(0).getText())) {
            result.getParameters().add((ExpressionSegment) visit(getTargetRuleContextFromParseTree(ctx, ExtractArgContext.class).iterator().next()));
        }
        result.getParameters().addAll(expressionSegments);
        return result;
    }
    
    @Override
    public ASTNode visitExtractArg(final ExtractArgContext ctx) {
        return new ExtractArgExpression(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getChild(0).getText());
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
    
    private Collection<ExpressionSegment> getExpressionSegments(final Collection<AExprContext> aExprContexts) {
        Collection<ExpressionSegment> result = new LinkedList<>();
        for (AExprContext each : aExprContexts) {
            result.add((ExpressionSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitAexprConst(final AexprConstContext ctx) {
        LiteralValue<?> value;
        if (null != ctx.numberConst()) {
            value = new NumberLiteralValue(ctx.numberConst().getText());
        } else if (null != ctx.STRING_()) {
            value = new StringLiteralValue(ctx.STRING_().getText());
        } else if (null != ctx.FALSE()) {
            value = new BooleanLiteralValue(ctx.FALSE().getText());
        } else if (null != ctx.TRUE()) {
            value = new BooleanLiteralValue(ctx.TRUE().getText());
        } else if (null != ctx.NULL()) {
            value = new NullLiteralValue(ctx.getText());
        } else {
            value = new OtherLiteralValue(ctx.getText());
        }
        if (null != ctx.constTypeName() || null != ctx.funcName() && null == ctx.LP_()) {
            LiteralExpressionSegment expression = new LiteralExpressionSegment(ctx.STRING_().getSymbol().getStartIndex(), ctx.STRING_().getSymbol().getStopIndex(), value.getValue().toString());
            String dataType = null == ctx.constTypeName() ? ctx.funcName().getText() : ctx.constTypeName().getText();
            return new TypeCastExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.getText(), expression, dataType);
        }
        return SQLUtils.createLiteralExpression(value, ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.getText());
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
            SelectStatement select = (SelectStatement) visit(ctx.selectWithParens());
            SubquerySegment subquerySegment = new SubquerySegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), select, getOriginalText(ctx.selectWithParens()));
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
        if (null != ctx.TYPE_CAST_()) {
            return new TypeCastExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.getText(), (ExpressionSegment) visit(ctx.bExpr(0)), ctx.typeName().getText());
        }
        if (null != ctx.qualOp()) {
            ExpressionSegment left = (ExpressionSegment) visit(ctx.bExpr(0));
            ExpressionSegment right = (ExpressionSegment) visit(ctx.bExpr(1));
            String operator = ctx.qualOp().getText();
            String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
            return new BinaryOperationExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, operator, text);
        }
        for (BExprContext each : ctx.bExpr()) {
            visit(each);
        }
        return new LiteralExpressionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.getText());
    }
    
    private ProjectionSegment createAggregationSegment(final FuncApplicationContext ctx, final String aggregationType, final Collection<ExpressionSegment> expressionSegments) {
        AggregationType type = AggregationType.valueOf(aggregationType.toUpperCase());
        String separator = null;
        if (null != ctx.separatorName()) {
            separator = new StringLiteralValue(ctx.separatorName().STRING_().getText()).getValue();
        }
        if (null == ctx.DISTINCT()) {
            AggregationProjectionSegment result = new AggregationProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), type, getOriginalText(ctx), separator);
            result.getParameters().addAll(expressionSegments);
            return result;
        }
        AggregationDistinctProjectionSegment result =
                new AggregationDistinctProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), type, getOriginalText(ctx), getDistinctExpression(ctx), separator);
        result.getParameters().addAll(expressionSegments);
        return result;
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
        return new KeywordValue(String.join(" ", dataTypeNames));
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
        OrderDirection orderDirection = null == ctx.ascDesc() ? OrderDirection.ASC : generateOrderDirection(ctx.ascDesc());
        NullsOrderType nullsOrderType = generateNullsOrderType(ctx.nullsOrder());
        ASTNode expr = visit(ctx.aExpr());
        if (expr instanceof ColumnSegment) {
            ColumnSegment column = (ColumnSegment) expr;
            return new ColumnOrderByItemSegment(column, orderDirection, nullsOrderType);
        }
        if (expr instanceof LiteralExpressionSegment) {
            LiteralExpressionSegment index = (LiteralExpressionSegment) expr;
            return new IndexOrderByItemSegment(index.getStartIndex(), index.getStopIndex(), Integer.parseInt(index.getLiterals().toString()), orderDirection, nullsOrderType);
        }
        if (expr instanceof ExpressionSegment) {
            return new ExpressionOrderByItemSegment(ctx.aExpr().getStart().getStartIndex(),
                    ctx.aExpr().getStop().getStopIndex(), getOriginalText(ctx.aExpr()), orderDirection, nullsOrderType, (ExpressionSegment) expr);
        }
        return new ExpressionOrderByItemSegment(ctx.aExpr().getStart().getStartIndex(), ctx.aExpr().getStop().getStopIndex(), getOriginalText(ctx.aExpr()), orderDirection, nullsOrderType);
    }
    
    private NullsOrderType generateNullsOrderType(final NullsOrderContext ctx) {
        if (null == ctx) {
            return null;
        }
        return null == ctx.FIRST() ? NullsOrderType.LAST : NullsOrderType.FIRST;
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
        InsertStatement result = (InsertStatement) visit(ctx.insertRest());
        result.setTable((SimpleTableSegment) visit(ctx.insertTarget()));
        if (null != ctx.optOnDuplicateKey()) {
            result.setOnDuplicateKeyColumns((OnDuplicateKeyColumnsSegment) visit(ctx.optOnDuplicateKey()));
        }
        if (null != ctx.returningClause()) {
            result.setReturning((ReturningSegment) visit(ctx.returningClause()));
        }
        result.addParameterMarkers(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitInsertTarget(final InsertTargetContext ctx) {
        SimpleTableSegment result = (SimpleTableSegment) visit(ctx.qualifiedName());
        if (null != ctx.AS()) {
            ColIdContext colId = ctx.colId();
            result.setAlias(new AliasSegment(colId.start.getStartIndex(), colId.stop.getStopIndex(), new IdentifierValue(colId.getText())));
        }
        return result;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public ASTNode visitQualifiedNameList(final QualifiedNameListContext ctx) {
        CollectionValue<SimpleTableSegment> result = new CollectionValue<>();
        if (null != ctx.qualifiedName()) {
            result.getValue().add((SimpleTableSegment) visit(ctx.qualifiedName()));
        }
        if (null != ctx.qualifiedNameList()) {
            result.combine((CollectionValue) visit(ctx.qualifiedNameList()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitQualifiedName(final QualifiedNameContext ctx) {
        if (null == ctx.indirection()) {
            return new SimpleTableSegment(new TableNameSegment(ctx.colId().start.getStartIndex(), ctx.colId().stop.getStopIndex(), new IdentifierValue(ctx.colId().getText())));
        }
        AttrNameContext attrName = ctx.indirection().indirectionEl().attrName();
        TableNameSegment tableName = new TableNameSegment(attrName.start.getStartIndex(), attrName.stop.getStopIndex(), new IdentifierValue(attrName.getText()));
        OwnerSegment owner = new OwnerSegment(ctx.colId().start.getStartIndex(), ctx.colId().stop.getStopIndex(), new IdentifierValue(ctx.colId().getText()));
        SimpleTableSegment result = new SimpleTableSegment(tableName);
        if (null == ctx.indirection().indirection()) {
            result.setOwner(owner);
        } else {
            OwnerSegment tableOwner = createTableOwner(ctx.indirection().indirection());
            tableOwner.setOwner(owner);
            result.setOwner(tableOwner);
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitInsertRest(final InsertRestContext ctx) {
        InsertStatement result = new InsertStatement(databaseType);
        ValuesClauseContext valuesClause = ctx.select().selectNoParens().selectClauseN().simpleSelect().valuesClause();
        if (null == valuesClause) {
            SelectStatement selectStatement = (SelectStatement) visit(ctx.select());
            result.setInsertSelect(new SubquerySegment(ctx.select().start.getStartIndex(), ctx.select().stop.getStopIndex(), selectStatement, getOriginalText(ctx.select())));
        } else {
            result.getValues().addAll(createInsertValuesSegments(valuesClause));
        }
        if (null == ctx.insertColumnList()) {
            result.setInsertColumns(new InsertColumnsSegment(ctx.start.getStartIndex() - 1, ctx.start.getStartIndex() - 1, Collections.emptyList()));
        } else {
            InsertColumnListContext insertColumns = ctx.insertColumnList();
            CollectionValue<ColumnSegment> columns = (CollectionValue<ColumnSegment>) visit(insertColumns);
            InsertColumnsSegment insertColumnsSegment = new InsertColumnsSegment(insertColumns.start.getStartIndex() - 1, insertColumns.stop.getStopIndex() + 1, columns.getValue());
            result.setInsertColumns(insertColumnsSegment);
        }
        return result;
    }
    
    @Override
    public ASTNode visitOptOnDuplicateKey(final OptOnDuplicateKeyContext ctx) {
        if (null != ctx.NOTHING()) {
            return new OnDuplicateKeyColumnsSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), Collections.emptyList());
        }
        Collection<ColumnAssignmentSegment> columns = new LinkedList<>();
        for (AssignmentContext each : ctx.assignment()) {
            columns.add((ColumnAssignmentSegment) visit(each));
        }
        return new OnDuplicateKeyColumnsSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), columns);
    }
    
    @Override
    public ASTNode visitAssignment(final AssignmentContext ctx) {
        List<ColumnSegment> columnSegments = Collections.singletonList((ColumnSegment) visit(ctx.setTarget()));
        ExpressionSegment expressionSegment;
        if (null == ctx.aExpr()) {
            String value = ctx.start.getInputStream().getText(new Interval(ctx.VALUES().getSymbol().getStartIndex(), ctx.stop.getStopIndex()));
            FunctionSegment functionSegment = new FunctionSegment(ctx.VALUES().getSymbol().getStartIndex(), ctx.getStop().getStopIndex(), ctx.VALUES().getText(), value);
            functionSegment.getParameters().add(new ColumnSegment(ctx.name().getStart().getStartIndex(), ctx.name().getStop().getStopIndex(), new IdentifierValue(ctx.name().getText())));
            expressionSegment = functionSegment;
        } else {
            expressionSegment = (ExpressionSegment) visit(ctx.aExpr());
        }
        return new ColumnAssignmentSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), columnSegments, expressionSegment);
    }
    
    @SuppressWarnings("unchecked")
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
        if (null == ctx.optIndirection().indirectionEl()) {
            return new ColumnSegment(ctx.colId().start.getStartIndex(), ctx.colId().stop.getStopIndex(), new IdentifierValue(ctx.colId().getText()));
        }
        ColumnSegment result = new ColumnSegment(
                ctx.colId().start.getStartIndex(), ctx.optIndirection().stop.getStopIndex(), new IdentifierValue(ctx.optIndirection().indirectionEl().attrName().getText()));
        result.setOwner(new OwnerSegment(ctx.colId().start.getStartIndex(), ctx.colId().stop.getStopIndex(), new IdentifierValue(ctx.colId().getText())));
        return result;
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
    
    private Collection<ColumnAssignmentSegment> generateAssignmentSegments(final SetClauseListContext ctx) {
        Collection<ColumnAssignmentSegment> result = new LinkedList<>();
        if (null != ctx.setClauseList()) {
            Collection<ColumnAssignmentSegment> tmpResult = generateAssignmentSegments(ctx.setClauseList());
            result.addAll(tmpResult);
        }
        ColumnAssignmentSegment assignmentSegment = (ColumnAssignmentSegment) visit(ctx.setClause());
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
        List<ColIdContext> colIdContexts = ctx.colId();
        if (2 == colIdContexts.size()) {
            ColIdContext columnName = ctx.colId().get(1);
            ColumnSegment result = new ColumnSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), new IdentifierValue(columnName.getText()));
            ColIdContext ownerName = ctx.colId().get(0);
            result.setOwner(new OwnerSegment(ownerName.start.getStartIndex(), ownerName.stop.getStopIndex(), new IdentifierValue(ownerName.getText())));
            return result;
        }
        return new ColumnSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), new IdentifierValue(ctx.getText()));
    }
    
    @Override
    public ASTNode visitRelationExprOptAlias(final RelationExprOptAliasContext ctx) {
        SimpleTableSegment result = (SimpleTableSegment) visit(ctx.relationExpr().qualifiedName());
        if (null != ctx.colId()) {
            result.setAlias(new AliasSegment(ctx.colId().start.getStartIndex(), ctx.stop.getStopIndex(), new IdentifierValue(ctx.colId().getText())));
        }
        return result;
    }
    
    @Override
    public ASTNode visitUpdate(final UpdateContext ctx) {
        UpdateStatement result = new UpdateStatement(databaseType);
        SimpleTableSegment tableSegment = (SimpleTableSegment) visit(ctx.relationExprOptAlias());
        result.setTable(tableSegment);
        result.setSetAssignment((SetAssignmentSegment) visit(ctx.setClauseList()));
        if (null != ctx.whereOrCurrentClause()) {
            result.setWhere((WhereSegment) visit(ctx.whereOrCurrentClause()));
        }
        if (null != ctx.fromClause()) {
            result.setFrom((TableSegment) visit(ctx.fromClause()));
        }
        result.addParameterMarkers(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitSetClauseList(final SetClauseListContext ctx) {
        Collection<ColumnAssignmentSegment> assignments = generateAssignmentSegments(ctx);
        return new SetAssignmentSegment(ctx.start.getStartIndex() - 4, ctx.stop.getStopIndex(), assignments);
    }
    
    @Override
    public ASTNode visitDelete(final DeleteContext ctx) {
        DeleteStatement result = new DeleteStatement(databaseType);
        SimpleTableSegment tableSegment = (SimpleTableSegment) visit(ctx.relationExprOptAlias());
        result.setTable(tableSegment);
        if (null != ctx.whereOrCurrentClause()) {
            result.setWhere((WhereSegment) visit(ctx.whereOrCurrentClause()));
        }
        result.addParameterMarkers(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitWhereOrCurrentClause(final WhereOrCurrentClauseContext ctx) {
        return visit(ctx.whereClause());
    }
    
    @Override
    public ASTNode visitSelect(final SelectContext ctx) {
        // TODO :Unsupported for withClause.
        SelectStatement result = (SelectStatement) visit(ctx.selectNoParens());
        result.addParameterMarkers(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitSelectNoParens(final SelectNoParensContext ctx) {
        SelectStatement result = (SelectStatement) visit(ctx.selectClauseN());
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
        if (null != ctx.withClause()) {
            WithSegment withSegment = (WithSegment) visit(ctx.withClause());
            result.setWith(withSegment);
        }
        return result;
    }
    
    @Override
    public ASTNode visitWithClause(final WithClauseContext ctx) {
        Collection<CommonTableExpressionSegment> commonTableExpressions = new LinkedList<>();
        for (CommonTableExprContext each : ctx.cteList().commonTableExpr()) {
            commonTableExpressions.add((CommonTableExpressionSegment) visit(each));
        }
        return new WithSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), commonTableExpressions, null != ctx.RECURSIVE());
    }
    
    @Override
    public ASTNode visitCommonTableExpr(final CommonTableExprContext ctx) {
        return new CommonTableExpressionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (AliasSegment) visit(ctx.alias()),
                new SubquerySegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (SelectStatement) visit(ctx.preparableStmt().select()),
                        getOriginalText(ctx.preparableStmt().select())));
    }
    
    @Override
    public ASTNode visitAlias(final AliasContext ctx) {
        return new AliasSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), new IdentifierValue(ctx.identifier().getText()));
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
        }
        if (null != ctx.selectClauseN() && !ctx.selectClauseN().isEmpty()) {
            SelectStatement result = new SelectStatement(databaseType);
            SelectStatement left = (SelectStatement) visit(ctx.selectClauseN(0));
            result.setProjections(left.getProjections());
            left.getFrom().ifPresent(result::setFrom);
            CombineSegment combineSegment = new CombineSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                    createSubquerySegment(ctx.selectClauseN(0), left), getCombineType(ctx), createSubquerySegment(ctx.selectClauseN(1), (SelectStatement) visit(ctx.selectClauseN(1))));
            result.setCombine(combineSegment);
            return result;
        }
        return visit(ctx.selectWithParens());
    }
    
    private SubquerySegment createSubquerySegment(final SelectClauseNContext ctx, final SelectStatement selectStatement) {
        return new SubquerySegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), selectStatement, getOriginalText(ctx));
    }
    
    private CombineType getCombineType(final SelectClauseNContext ctx) {
        boolean isDistinct = null == ctx.allOrDistinct() || null != ctx.allOrDistinct().DISTINCT();
        if (null != ctx.UNION()) {
            return isDistinct ? CombineType.UNION : CombineType.UNION_ALL;
        }
        if (null != ctx.INTERSECT()) {
            return isDistinct ? CombineType.INTERSECT : CombineType.INTERSECT_ALL;
        }
        if (null != ctx.MINUS()) {
            return isDistinct ? CombineType.MINUS : CombineType.MINUS_ALL;
        }
        return isDistinct ? CombineType.EXCEPT : CombineType.EXCEPT_ALL;
    }
    
    @Override
    public ASTNode visitSimpleSelect(final SimpleSelectContext ctx) {
        SelectStatement result = new SelectStatement(databaseType);
        if (null != ctx.targetList()) {
            ProjectionsSegment projects = (ProjectionsSegment) visit(ctx.targetList());
            if (null != ctx.distinctClause()) {
                projects.setDistinctRow(true);
            }
            result.setProjections(projects);
        } else {
            result.setProjections(new ProjectionsSegment(-1, -1));
        }
        if (null != ctx.intoClause()) {
            result.setInto((TableSegment) visit(ctx.intoClause()));
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
    public ASTNode visitIntoClause(final IntoClauseContext ctx) {
        return visit(ctx.optTempTableName().qualifiedName());
    }
    
    @Override
    public ASTNode visitHavingClause(final HavingClauseContext ctx) {
        ExpressionSegment expr = (ExpressionSegment) visit(ctx.aExpr());
        return new HavingSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), expr);
    }
    
    @Override
    public ASTNode visitWindowClause(final WindowClauseContext ctx) {
        WindowSegment result = new WindowSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
        appendWindowItems(ctx.windowDefinitionList(), result.getItemSegments());
        return result;
    }
    
    private void appendWindowItems(final WindowDefinitionListContext ctx, final Collection<WindowItemSegment> windowItems) {
        if (null != ctx.windowDefinitionList()) {
            appendWindowItems(ctx.windowDefinitionList(), windowItems);
            windowItems.add((WindowItemSegment) visit(ctx.windowDefinition()));
            return;
        }
        windowItems.add((WindowItemSegment) visit(ctx.windowDefinition()));
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitWindowDefinition(final WindowDefinitionContext ctx) {
        WindowItemSegment result = new WindowItemSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
        result.setWindowName(new IdentifierValue(ctx.colId().getText()));
        if (null != ctx.windowSpecification().partitionClause()) {
            CollectionValue<ExpressionSegment> value = (CollectionValue<ExpressionSegment>) visit(ctx.windowSpecification().partitionClause().exprList());
            result.setPartitionListSegments(value.getValue());
        }
        if (null != ctx.windowSpecification().sortClause()) {
            OrderBySegment orderBySegment = (OrderBySegment) visit(ctx.windowSpecification().sortClause());
            result.setOrderBySegment(orderBySegment);
        }
        if (null != ctx.windowSpecification().frameClause()) {
            result.setFrameClause(new CommonExpressionSegment(ctx.windowSpecification().frameClause().start.getStartIndex(), ctx.windowSpecification().frameClause().stop.getStopIndex(),
                    ctx.windowSpecification().frameClause().getText()));
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
                return new ColumnOrderByItemSegment((ColumnSegment) astNode, OrderDirection.ASC, null);
            }
            if (astNode instanceof LiteralExpressionSegment) {
                LiteralExpressionSegment index = (LiteralExpressionSegment) astNode;
                return new IndexOrderByItemSegment(index.getStartIndex(), index.getStopIndex(),
                        Integer.parseInt(index.getLiterals().toString()), OrderDirection.ASC, null);
            }
            return new ExpressionOrderByItemSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), getOriginalText(ctx), OrderDirection.ASC, null, (ExpressionSegment) visit(ctx.aExpr()));
        }
        return new ExpressionOrderByItemSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), getOriginalText(ctx), OrderDirection.ASC, null);
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
        ProjectionSegment result = createProjectionSegment(ctx, ctx.aExpr());
        if (null != ctx.identifier()) {
            ((AliasAvailable) result).setAlias(new AliasSegment(ctx.identifier().start.getStartIndex(), ctx.identifier().stop.getStopIndex(), new IdentifierValue(ctx.identifier().getText())));
        }
        return result;
    }
    
    private ProjectionSegment createProjectionSegment(final TargetElContext ctx, final AExprContext expr) {
        if (null != ctx.ASTERISK_()) {
            return new ShorthandProjectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
        }
        if (null != ctx.DOT_ASTERISK_()) {
            ShorthandProjectionSegment result = new ShorthandProjectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
            result.setOwner(new OwnerSegment(ctx.colId().start.getStartIndex(), ctx.colId().stop.getStopIndex(), new IdentifierValue(ctx.colId().getText())));
            return result;
        }
        if (null != ctx.aExpr()) {
            ASTNode projection = visit(ctx.aExpr());
            return createProjectionSegment(ctx, expr, projection);
        }
        return new ExpressionProjectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), getOriginalText(expr), null);
    }
    
    private ProjectionSegment createProjectionSegment(final TargetElContext ctx, final AExprContext expr, final ASTNode projection) {
        if (projection instanceof ColumnSegment) {
            return new ColumnProjectionSegment((ColumnSegment) projection);
        }
        if (projection instanceof AggregationProjectionSegment) {
            return (AggregationProjectionSegment) projection;
        }
        if (projection instanceof SubqueryExpressionSegment) {
            SubqueryExpressionSegment subqueryExpression = (SubqueryExpressionSegment) projection;
            String text = ctx.start.getInputStream().getText(new Interval(subqueryExpression.getStartIndex(), subqueryExpression.getStopIndex()));
            return new SubqueryProjectionSegment(subqueryExpression.getSubquery(), text);
        }
        if (projection instanceof ExpressionSegment) {
            return new ExpressionProjectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), getOriginalText(expr), (ExpressionSegment) projection);
        }
        return new ExpressionProjectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), getOriginalText(expr), null);
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
            result.setJoinType(JoinType.COMMA.name());
            return result;
        }
        return visit(ctx.tableReference());
    }
    
    @Override
    public ASTNode visitTableReference(final TableReferenceContext ctx) {
        if (null != ctx.relationExpr()) {
            return getSimpleTableSegment(ctx);
        }
        if (null != ctx.selectWithParens()) {
            return getSubqueryTableSegment(ctx);
        }
        if (null != ctx.tableReference()) {
            return getJoinTableSegment(ctx);
        }
        if (null != ctx.functionTable() && null != ctx.functionTable().functionExprWindowless() && null != ctx.functionTable().functionExprWindowless().funcApplication()) {
            return getFunctionTableSegment(ctx);
        }
        // TODO deal with functionTable and xmlTable
        return new SimpleTableSegment(new TableNameSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), new IdentifierValue("not support")));
    }
    
    private SimpleTableSegment getSimpleTableSegment(final TableReferenceContext ctx) {
        SimpleTableSegment result = (SimpleTableSegment) visit(ctx.relationExpr().qualifiedName());
        if (null != ctx.aliasClause()) {
            result.setAlias((AliasSegment) visit(ctx.aliasClause()));
        }
        return result;
    }
    
    private SubqueryTableSegment getSubqueryTableSegment(final TableReferenceContext ctx) {
        SelectStatement select = (SelectStatement) visit(ctx.selectWithParens());
        SubquerySegment subquery = new SubquerySegment(ctx.selectWithParens().start.getStartIndex(), ctx.selectWithParens().stop.getStopIndex(), select, getOriginalText(ctx.selectWithParens()));
        SubqueryTableSegment result = new SubqueryTableSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), subquery);
        if (null != ctx.aliasClause()) {
            result.setAlias((AliasSegment) visit(ctx.aliasClause()));
        }
        return result;
    }
    
    private JoinTableSegment getJoinTableSegment(final TableReferenceContext ctx) {
        JoinTableSegment result = new JoinTableSegment();
        result.setLeft((TableSegment) visit(ctx.tableReference()));
        int startIndex = null == ctx.LP_() ? ctx.tableReference().start.getStartIndex() : ctx.LP_().getSymbol().getStartIndex();
        int stopIndex = 0;
        AliasSegment alias = null;
        if (null == ctx.aliasClause()) {
            stopIndex = null == ctx.RP_() ? ctx.tableReference().start.getStopIndex() : ctx.RP_().getSymbol().getStopIndex();
        } else {
            alias = (AliasSegment) visit(ctx.aliasClause());
            startIndex = null == ctx.RP_() ? ctx.joinedTable().stop.getStopIndex() : ctx.RP_().getSymbol().getStopIndex();
        }
        result.setStartIndex(startIndex);
        result.setStopIndex(stopIndex);
        visitJoinedTable(ctx.joinedTable(), result);
        result.setAlias(alias);
        return result;
    }
    
    private FunctionTableSegment getFunctionTableSegment(final TableReferenceContext ctx) {
        FunctionSegment functionSegment = (FunctionSegment) visit(ctx.functionTable().functionExprWindowless().funcApplication());
        return new FunctionTableSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), functionSegment);
    }
    
    @Override
    public ASTNode visitFuncApplication(final FuncApplicationContext ctx) {
        Collection<ExpressionSegment> expressionSegments = getExpressionSegments(getTargetRuleContextFromParseTree(ctx, AExprContext.class));
        FuncNameContext funcNameContext = ctx.funcName();
        String functionName = null == funcNameContext.typeFunctionName() ? funcNameContext.indirection().indirectionEl().attrName().getText() : funcNameContext.typeFunctionName().getText();
        if (AggregationType.isAggregationType(functionName)) {
            return createAggregationSegment(ctx, functionName, expressionSegments);
        }
        FunctionSegment result = new FunctionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), functionName, getOriginalText(ctx));
        if (null != funcNameContext.colId()) {
            result.setOwner(new OwnerSegment(funcNameContext.colId().start.getStartIndex(), funcNameContext.colId().stop.getStopIndex(), new IdentifierValue(funcNameContext.colId().getText())));
        }
        result.getParameters().addAll(expressionSegments);
        return result;
    }
    
    private void visitJoinedTable(final JoinedTableContext ctx, final JoinTableSegment tableSegment) {
        TableSegment right = (TableSegment) visit(ctx.tableReference());
        tableSegment.setRight(right);
        tableSegment.setJoinType(getJoinType(ctx));
        tableSegment.setNatural(null != ctx.naturalJoinType());
        if (null != ctx.joinQual()) {
            visitJoinQual(ctx.joinQual(), tableSegment);
        }
    }
    
    private String getJoinType(final JoinedTableContext ctx) {
        if (null != ctx.crossJoinType()) {
            return JoinType.CROSS.name();
        }
        if (null != ctx.innerJoinType()) {
            return JoinType.INNER.name();
        }
        if (null != ctx.outerJoinType()) {
            return getOutJoinType(ctx.outerJoinType());
        }
        if (null != ctx.naturalJoinType()) {
            return getNaturalJoinType(ctx.naturalJoinType());
        }
        return JoinType.COMMA.name();
    }
    
    private String getOutJoinType(final OuterJoinTypeContext ctx) {
        if (null != ctx.FULL()) {
            return JoinType.FULL.name();
        }
        return null == ctx.LEFT() ? JoinType.RIGHT.name() : JoinType.LEFT.name();
    }
    
    private String getNaturalJoinType(final NaturalJoinTypeContext ctx) {
        if (null != ctx.INNER()) {
            return JoinType.INNER.name();
        }
        if (null != ctx.FULL()) {
            return JoinType.FULL.name();
        }
        if (null != ctx.LEFT()) {
            return JoinType.LEFT.name();
        }
        if (null != ctx.RIGHT()) {
            return JoinType.RIGHT.name();
        }
        return JoinType.INNER.name();
    }
    
    private void visitJoinQual(final JoinQualContext ctx, final JoinTableSegment joinTableSource) {
        if (null != ctx.aExpr()) {
            ExpressionSegment condition = (ExpressionSegment) visit(ctx.aExpr());
            joinTableSource.setCondition(condition);
        }
        if (null != ctx.USING()) {
            joinTableSource.setUsing(generateUsingColumn(ctx.nameList()));
        }
    }
    
    private List<ColumnSegment> generateUsingColumn(final NameListContext ctx) {
        List<ColumnSegment> result = new ArrayList<>();
        if (null != ctx.nameList()) {
            result.addAll(generateUsingColumn(ctx.nameList()));
        }
        if (null != ctx.name()) {
            result.add(new ColumnSegment(ctx.name().start.getStartIndex(), ctx.name().stop.getStopIndex(), new IdentifierValue(ctx.name().getText())));
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
        ASTNode astNode = visit(ctx.cExpr());
        if (astNode instanceof ParameterMarkerExpressionSegment) {
            return new ParameterMarkerLimitValueSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ((ParameterMarkerExpressionSegment) astNode).getParameterMarkerIndex());
        }
        return new NumberLiteralLimitValueSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), Long.parseLong(((ExpressionSegment) astNode).getText()));
    }
    
    @Override
    public ASTNode visitSelectOffsetValue(final SelectOffsetValueContext ctx) {
        ASTNode astNode = visit(ctx.cExpr());
        if (astNode instanceof ParameterMarkerExpressionSegment) {
            return new ParameterMarkerLimitValueSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ((ParameterMarkerExpressionSegment) astNode).getParameterMarkerIndex());
        }
        return new NumberLiteralLimitValueSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), Long.parseLong(((ExpressionSegment) astNode).getText()));
    }
    
    @Override
    public ASTNode visitSelectFetchValue(final SelectFetchValueContext ctx) {
        ASTNode astNode = visit(ctx.cExpr());
        if (astNode instanceof ParameterMarkerExpressionSegment) {
            return new ParameterMarkerLimitValueSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ((ParameterMarkerExpressionSegment) astNode).getParameterMarkerIndex());
        }
        return new NumberLiteralLimitValueSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), Long.parseLong(((ExpressionSegment) astNode).getText()));
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
            if (null != ctx.limitClause().selectFetchValue()) {
                LimitValueSegment limit = (LimitValueSegment) visit(ctx.limitClause().selectFetchValue());
                return new LimitSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), null, limit);
            }
            LimitValueSegment limit = (LimitValueSegment) visit(ctx.limitClause().selectLimitValue());
            return new LimitSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), null, limit);
        }
        LimitValueSegment offset = (LimitValueSegment) visit(ctx.offsetClause().selectOffsetValue());
        return new LimitSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), offset, null);
    }
    
    @Override
    public ASTNode visitExecuteStmt(final ExecuteStmtContext ctx) {
        return new ExecuteStatement(databaseType);
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
    
    @Override
    @SuppressWarnings("unchecked")
    public ASTNode visitAnyName(final AnyNameContext ctx) {
        CollectionValue<NameSegment> result = new CollectionValue<>();
        if (null != ctx.attrs()) {
            result.combine((CollectionValue<NameSegment>) visit(ctx.attrs()));
        }
        result.getValue().add(new NameSegment(ctx.colId().getStart().getStartIndex(), ctx.colId().getStop().getStopIndex(), new IdentifierValue(ctx.colId().getText())));
        return result;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public ASTNode visitAttrs(final AttrsContext ctx) {
        CollectionValue<NameSegment> result = new CollectionValue<>();
        result.getValue().add(new NameSegment(ctx.attrName().getStart().getStartIndex(), ctx.attrName().getStop().getStopIndex(), new IdentifierValue(ctx.attrName().getText())));
        if (null != ctx.attrs()) {
            result.combine((CollectionValue<NameSegment>) visit(ctx.attrs()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitSignedIconst(final SignedIconstContext ctx) {
        return new NumberLiteralValue(ctx.getText());
    }
}
