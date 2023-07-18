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

package org.apache.shardingsphere.sql.parser.oracle.visitor.statement;

import lombok.AccessLevel;
import lombok.Getter;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementBaseVisitor;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AggregationFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AnalyticFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.BitExprContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.BitValueLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.BooleanLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.BooleanPrimaryContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CastFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CharFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ColumnNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ColumnNamesContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ConstraintNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DataTypeContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DataTypeLengthContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DataTypeNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DatetimeExprContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ExprContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.FunctionCallContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.FunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.HexadecimalLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.IdentifierContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.IndexNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.IndexTypeNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.LiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.NullValueLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.NumberLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.OrderByClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.OrderByItemContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.OwnerContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.PackageNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ParameterMarkerContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.PredicateContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.RegularFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.SchemaNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.SimpleExprContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.SpecialFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.StringLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.SynonymNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.TableNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.TableNamesContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.TypeNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.UnreservedWordContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ViewNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.XmlAggFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.XmlColattvalFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.XmlExistsFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.XmlForestFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.XmlFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.XmlNameSpaceStringAsIdentifierContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.XmlNameSpacesClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.XmlParseFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.XmlPiFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.XmlQueryFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.XmlRootFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.XmlSerializeFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.XmlTableColumnContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.XmlTableFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.XmlTableOptionsContext;
import org.apache.shardingsphere.sql.parser.sql.common.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.sql.common.enums.NullsOrderType;
import org.apache.shardingsphere.sql.parser.sql.common.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.common.enums.ParameterMarkerType;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.ConstraintSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexTypeSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.packages.PackageSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.type.TypeSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.DatetimeExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.NotExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.XmlNameSpaceStringAsIdentifierSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.XmlNameSpacesClauseSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.XmlPiFunctionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.XmlQueryAndExistsFunctionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.XmlSerializeFunctionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.XmlTableColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.XmlTableFunctionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.XmlTableOptionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationDistinctProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DataTypeLengthSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DataTypeSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.ParameterMarkerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.util.SQLUtils;
import org.apache.shardingsphere.sql.parser.sql.common.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.keyword.KeywordValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.BooleanLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.NullLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.NumberLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.OtherLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.StringLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.parametermarker.ParameterMarkerValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleSelectStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Statement visitor for Oracle.
 */
@Getter(AccessLevel.PROTECTED)
public abstract class OracleStatementVisitor extends OracleStatementBaseVisitor<ASTNode> {
    
    private final Collection<ParameterMarkerSegment> parameterMarkerSegments = new LinkedList<>();
    
    @Override
    public final ASTNode visitParameterMarker(final ParameterMarkerContext ctx) {
        return new ParameterMarkerValue(parameterMarkerSegments.size(), ParameterMarkerType.QUESTION);
    }
    
    @Override
    public final ASTNode visitLiterals(final LiteralsContext ctx) {
        if (null != ctx.stringLiterals()) {
            return visit(ctx.stringLiterals());
        }
        if (null != ctx.numberLiterals()) {
            return visit(ctx.numberLiterals());
        }
        if (null != ctx.hexadecimalLiterals()) {
            return visit(ctx.hexadecimalLiterals());
        }
        if (null != ctx.bitValueLiterals()) {
            return visit(ctx.bitValueLiterals());
        }
        if (null != ctx.booleanLiterals()) {
            return visit(ctx.booleanLiterals());
        }
        if (null != ctx.nullValueLiterals()) {
            return visit(ctx.nullValueLiterals());
        }
        if (null != ctx.dateTimeLiterals()) {
            return visit(ctx.dateTimeLiterals());
        }
        throw new IllegalStateException("Literals must have string, number, dateTime, hex, bit, boolean or null.");
    }
    
    @Override
    public final ASTNode visitStringLiterals(final StringLiteralsContext ctx) {
        return new StringLiteralValue(ctx.getText());
    }
    
    @Override
    public final ASTNode visitNumberLiterals(final NumberLiteralsContext ctx) {
        return new NumberLiteralValue(ctx.getText());
    }
    
    @Override
    public final ASTNode visitHexadecimalLiterals(final HexadecimalLiteralsContext ctx) {
        // TODO deal with hexadecimalLiterals
        return new OtherLiteralValue(ctx.getText());
    }
    
    @Override
    public final ASTNode visitBitValueLiterals(final BitValueLiteralsContext ctx) {
        // TODO deal with bitValueLiterals
        return new OtherLiteralValue(ctx.getText());
    }
    
    @Override
    public final ASTNode visitBooleanLiterals(final BooleanLiteralsContext ctx) {
        return new BooleanLiteralValue(ctx.getText());
    }
    
    @Override
    public final ASTNode visitNullValueLiterals(final NullValueLiteralsContext ctx) {
        return new NullLiteralValue(ctx.getText());
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
    public final ASTNode visitSynonymName(final SynonymNameContext ctx) {
        return visit(ctx.identifier());
    }
    
    @Override
    public final ASTNode visitTableName(final TableNameContext ctx) {
        SimpleTableSegment result = new SimpleTableSegment(new TableNameSegment(ctx.name().getStart().getStartIndex(),
                ctx.name().getStop().getStopIndex(), new IdentifierValue(ctx.name().identifier().getText())));
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
    public final ASTNode visitViewName(final ViewNameContext ctx) {
        SimpleTableSegment result = new SimpleTableSegment(new TableNameSegment(ctx.name().getStart().getStartIndex(),
                ctx.name().getStop().getStopIndex(), new IdentifierValue(ctx.name().identifier().getText())));
        OwnerContext owner = ctx.owner();
        if (null != owner) {
            result.setOwner(new OwnerSegment(owner.getStart().getStartIndex(), owner.getStop().getStopIndex(), (IdentifierValue) visit(owner.identifier())));
        }
        return result;
    }
    
    @Override
    public final ASTNode visitIndexName(final IndexNameContext ctx) {
        IndexNameSegment indexName = new IndexNameSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), (IdentifierValue) visit(ctx.name()));
        return new IndexSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), indexName);
    }
    
    @Override
    public final ASTNode visitFunction(final FunctionContext ctx) {
        return new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ((IdentifierValue) visit(ctx.name())).getValue(), ctx.getText());
    }
    
    @Override
    public final ASTNode visitPackageName(final PackageNameContext ctx) {
        return new PackageSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (IdentifierValue) visit(ctx.name()));
    }
    
    @Override
    public final ASTNode visitTypeName(final TypeNameContext ctx) {
        return new TypeSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (IdentifierValue) visit(ctx.name()));
    }
    
    @Override
    public final ASTNode visitIndexTypeName(final IndexTypeNameContext ctx) {
        return new IndexTypeSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (IdentifierValue) visit(ctx.name()));
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
    public final ASTNode visitExpr(final ExprContext ctx) {
        if (null != ctx.booleanPrimary()) {
            return visit(ctx.booleanPrimary());
        }
        if (null != ctx.LP_()) {
            return visit(ctx.expr(0));
        }
        if (null != ctx.andOperator()) {
            return createBinaryOperationExpression(ctx, ctx.andOperator().getText());
        }
        if (null != ctx.orOperator()) {
            return createBinaryOperationExpression(ctx, ctx.orOperator().getText());
        }
        if (null != ctx.datetimeExpr()) {
            return createDatetimeExpression(ctx, ctx.datetimeExpr());
        }
        return new NotExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), (ExpressionSegment) visit(ctx.expr(0)), false);
    }
    
    private ASTNode createDatetimeExpression(final ExprContext ctx, final DatetimeExprContext datetimeExpr) {
        ExpressionSegment left = (ExpressionSegment) visit(ctx.expr(0));
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        if (null != datetimeExpr.expr()) {
            ExpressionSegment right = new ExpressionProjectionSegment(datetimeExpr.getStart().getStartIndex(),
                    datetimeExpr.getStop().getStopIndex(), datetimeExpr.getText(), (ExpressionSegment) visit(datetimeExpr.expr()));
            return new DatetimeExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, text);
        }
        return new DatetimeExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, text);
    }
    
    private ASTNode createBinaryOperationExpression(final ExprContext ctx, final String operator) {
        ExpressionSegment left = (ExpressionSegment) visit(ctx.expr(0));
        ExpressionSegment right = (ExpressionSegment) visit(ctx.expr(1));
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new BinaryOperationExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, operator, text);
    }
    
    @Override
    public final ASTNode visitBooleanPrimary(final BooleanPrimaryContext ctx) {
        if (null == ctx.IS()) {
            return null == ctx.comparisonOperator() && null == ctx.SAFE_EQ_() ? visit(ctx.predicate()) : createCompareSegment(ctx);
        }
        String rightText = "";
        if (null != ctx.NOT()) {
            rightText = rightText.concat(ctx.start.getInputStream().getText(new Interval(ctx.NOT().getSymbol().getStartIndex(), ctx.NOT().getSymbol().getStopIndex()))).concat(" ");
        }
        Token operatorToken = null;
        if (null != ctx.NULL()) {
            operatorToken = ctx.NULL().getSymbol();
        }
        if (null != ctx.TRUE()) {
            operatorToken = ctx.TRUE().getSymbol();
        }
        if (null != ctx.FALSE()) {
            operatorToken = ctx.FALSE().getSymbol();
        }
        int startIndex = null == operatorToken ? ctx.IS().getSymbol().getStopIndex() + 2 : operatorToken.getStartIndex();
        rightText = rightText.concat(ctx.start.getInputStream().getText(new Interval(startIndex, ctx.stop.getStopIndex())));
        ExpressionSegment right = new LiteralExpressionSegment(ctx.IS().getSymbol().getStopIndex() + 2, ctx.stop.getStopIndex(), rightText);
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        ExpressionSegment left = (ExpressionSegment) visit(ctx.booleanPrimary());
        String operator = "IS";
        return new BinaryOperationExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, operator, text);
    }
    
    private ASTNode createCompareSegment(final BooleanPrimaryContext ctx) {
        ExpressionSegment left = (ExpressionSegment) visit(ctx.booleanPrimary());
        String operator = null == ctx.SAFE_EQ_() ? ctx.comparisonOperator().getText() : ctx.SAFE_EQ_().getText();
        ExpressionSegment right = null != ctx.predicate() ? (ExpressionSegment) visit(ctx.predicate()) : (ExpressionSegment) visit(ctx.subquery());
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new BinaryOperationExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, operator, text);
    }
    
    @Override
    public final ASTNode visitPredicate(final PredicateContext ctx) {
        if (null != ctx.IN()) {
            return createInSegment(ctx);
        }
        if (null != ctx.BETWEEN()) {
            return createBetweenSegment(ctx);
        }
        if (null != ctx.LIKE()) {
            return createBinaryOperationExpressionFromLike(ctx);
        }
        return visit(ctx.bitExpr(0));
    }
    
    private InExpression createInSegment(final PredicateContext ctx) {
        ExpressionSegment left = (ExpressionSegment) visit(ctx.bitExpr(0));
        ExpressionSegment right;
        if (null == ctx.subquery()) {
            ListExpression listExpression = new ListExpression(ctx.LP_().getSymbol().getStartIndex(), ctx.RP_().getSymbol().getStopIndex());
            for (ExprContext each : ctx.expr()) {
                listExpression.getItems().add((ExpressionSegment) visit(each));
            }
            right = listExpression;
        } else {
            right = new SubqueryExpressionSegment(new SubquerySegment(ctx.subquery().start.getStartIndex(), ctx.subquery().stop.getStopIndex(), (OracleSelectStatement) visit(ctx.subquery())));
        }
        boolean not = null != ctx.NOT();
        return new InExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, not);
    }
    
    private BinaryOperationExpression createBinaryOperationExpressionFromLike(final PredicateContext ctx) {
        ExpressionSegment left = (ExpressionSegment) visit(ctx.bitExpr(0));
        ListExpression right = new ListExpression(ctx.simpleExpr(0).start.getStartIndex(), ctx.simpleExpr().get(ctx.simpleExpr().size() - 1).stop.getStopIndex());
        for (SimpleExprContext each : ctx.simpleExpr()) {
            right.getItems().add((ExpressionSegment) visit(each));
        }
        String operator = null != ctx.NOT() ? "NOT LIKE" : "LIKE";
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new BinaryOperationExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, operator, text);
    }
    
    private BetweenExpression createBetweenSegment(final PredicateContext ctx) {
        ExpressionSegment left = (ExpressionSegment) visit(ctx.bitExpr(0));
        ExpressionSegment between = (ExpressionSegment) visit(ctx.bitExpr(1));
        ExpressionSegment and = (ExpressionSegment) visit(ctx.predicate());
        boolean not = null != ctx.NOT();
        return new BetweenExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, between, and, not);
    }
    
    @Override
    public final ASTNode visitBitExpr(final BitExprContext ctx) {
        if (null != ctx.simpleExpr()) {
            return createExpressionSegment(visit(ctx.simpleExpr()), ctx);
        }
        ExpressionSegment left = (ExpressionSegment) visit(ctx.getChild(0));
        ExpressionSegment right = (ExpressionSegment) visit(ctx.getChild(2));
        String operator = ctx.getChild(1).getText();
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new BinaryOperationExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, operator, text);
    }
    
    private ASTNode createExpressionSegment(final ASTNode astNode, final ParserRuleContext context) {
        if (astNode instanceof StringLiteralValue) {
            return new LiteralExpressionSegment(context.start.getStartIndex(), context.stop.getStopIndex(), ((StringLiteralValue) astNode).getValue());
        }
        if (astNode instanceof NumberLiteralValue) {
            return new LiteralExpressionSegment(context.start.getStartIndex(), context.stop.getStopIndex(), ((NumberLiteralValue) astNode).getValue());
        }
        if (astNode instanceof BooleanLiteralValue) {
            return new LiteralExpressionSegment(context.start.getStartIndex(), context.stop.getStopIndex(), ((BooleanLiteralValue) astNode).getValue());
        }
        if (astNode instanceof ParameterMarkerValue) {
            ParameterMarkerValue parameterMarker = (ParameterMarkerValue) astNode;
            ParameterMarkerExpressionSegment segment = new ParameterMarkerExpressionSegment(context.start.getStartIndex(), context.stop.getStopIndex(),
                    parameterMarker.getValue(), parameterMarker.getType());
            parameterMarkerSegments.add(segment);
            return segment;
        }
        if (astNode instanceof SubquerySegment) {
            return new SubqueryExpressionSegment((SubquerySegment) astNode);
        }
        if (astNode instanceof OtherLiteralValue) {
            return new CommonExpressionSegment(context.getStart().getStartIndex(), context.getStop().getStopIndex(), context.getText());
        }
        return astNode;
    }
    
    @Override
    public final ASTNode visitSimpleExpr(final SimpleExprContext ctx) {
        int startIndex = ctx.getStart().getStartIndex();
        int stopIndex = ctx.getStop().getStopIndex();
        if (null != ctx.subquery()) {
            return new SubquerySegment(startIndex, stopIndex, (OracleSelectStatement) visit(ctx.subquery()));
        }
        if (null != ctx.parameterMarker()) {
            ParameterMarkerValue parameterMarker = (ParameterMarkerValue) visit(ctx.parameterMarker());
            ParameterMarkerExpressionSegment segment = new ParameterMarkerExpressionSegment(startIndex, stopIndex, parameterMarker.getValue(), parameterMarker.getType());
            parameterMarkerSegments.add(segment);
            return segment;
        }
        if (null != ctx.literals()) {
            return SQLUtils.createLiteralExpression(visit(ctx.literals()), startIndex, stopIndex, ctx.literals().start.getInputStream().getText(new Interval(startIndex, stopIndex)));
        }
        if (null != ctx.functionCall()) {
            return visit(ctx.functionCall());
        }
        if (null != ctx.columnName()) {
            return visit(ctx.columnName());
        }
        return new CommonExpressionSegment(startIndex, stopIndex, ctx.getText());
    }
    
    @Override
    public final ASTNode visitFunctionCall(final FunctionCallContext ctx) {
        if (null != ctx.aggregationFunction()) {
            return visit(ctx.aggregationFunction());
        }
        if (null != ctx.specialFunction()) {
            return visit(ctx.specialFunction());
        }
        if (null != ctx.analyticFunction()) {
            return visit(ctx.analyticFunction());
        }
        if (null != ctx.regularFunction()) {
            return visit(ctx.regularFunction());
        }
        if (null != ctx.xmlFunction()) {
            return visit(ctx.xmlFunction());
        }
        throw new IllegalStateException("FunctionCallContext must have aggregationFunction, regularFunction, analyticFunction or specialFunction.");
    }
    
    @Override
    public ASTNode visitAnalyticFunction(final AnalyticFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.analyticFunctionName().getText(), getOriginalText(ctx));
        for (DataTypeContext each : ctx.dataType()) {
            result.getParameters().add((DataTypeSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public final ASTNode visitAggregationFunction(final AggregationFunctionContext ctx) {
        String aggregationType = ctx.aggregationFunctionName().getText();
        return AggregationType.isAggregationType(aggregationType)
                ? createAggregationSegment(ctx, aggregationType)
                : new ExpressionProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), getOriginalText(ctx));
    }
    
    private ASTNode createAggregationSegment(final AggregationFunctionContext ctx, final String aggregationType) {
        AggregationType type = AggregationType.valueOf(aggregationType.toUpperCase());
        String innerExpression = ctx.start.getInputStream().getText(new Interval(ctx.LP_().get(0).getSymbol().getStartIndex(), ctx.stop.getStopIndex()));
        if (null != ctx.DISTINCT()) {
            AggregationDistinctProjectionSegment result = new AggregationDistinctProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(),
                    type, innerExpression, getDistinctExpression(ctx));
            result.getParameters().addAll(getExpressions(ctx));
            return result;
        }
        AggregationProjectionSegment result = new AggregationProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), type, innerExpression);
        result.getParameters().addAll(getExpressions(ctx));
        return result;
    }
    
    @Override
    public ASTNode visitXmlFunction(final XmlFunctionContext ctx) {
        if (null != ctx.xmlAggFunction()) {
            return visit(ctx.xmlAggFunction());
        }
        if (null != ctx.xmlColattvalFunction()) {
            return visit(ctx.xmlColattvalFunction());
        }
        if (null != ctx.xmlExistsFunction()) {
            return visit(ctx.xmlExistsFunction());
        }
        if (null != ctx.xmlForestFunction()) {
            return visit(ctx.xmlForestFunction());
        }
        if (null != ctx.xmlParseFunction()) {
            return visit(ctx.xmlParseFunction());
        }
        if (null != ctx.xmlPiFunction()) {
            return visit(ctx.xmlPiFunction());
        }
        if (null != ctx.xmlQueryFunction()) {
            return visit(ctx.xmlQueryFunction());
        }
        if (null != ctx.xmlRootFunction()) {
            return visit(ctx.xmlRootFunction());
        }
        if (null != ctx.xmlSerializeFunction()) {
            return visit(ctx.xmlSerializeFunction());
        }
        if (null != ctx.xmlIsSchemaValidFunction()) {
            return visit(ctx.xmlIsSchemaValidFunction());
        }
        return visit(ctx.xmlTableFunction());
    }
    
    @Override
    public ASTNode visitXmlAggFunction(final XmlAggFunctionContext ctx) {
        return new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.XMLAGG().getText(), getOriginalText(ctx));
    }
    
    @Override
    public ASTNode visitXmlColattvalFunction(final XmlColattvalFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.XMLCOLATTVAL().getText(), getOriginalText(ctx));
        Collection<ExpressionSegment> expressionSegments = ctx.expr().stream().map(each -> (ExpressionSegment) visit(each)).collect(Collectors.toList());
        result.getParameters().addAll(expressionSegments);
        return result;
    }
    
    @Override
    public ASTNode visitXmlExistsFunction(final XmlExistsFunctionContext ctx) {
        XmlQueryAndExistsFunctionSegment result =
                new XmlQueryAndExistsFunctionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.XMLEXISTS().getText(), ctx.STRING_().getText(), getOriginalText(ctx));
        Collection<ExpressionSegment> expressionSegments = ctx.xmlPassingClause().expr().stream().map(each -> (ExpressionSegment) visit(each)).collect(Collectors.toList());
        result.getParameters().addAll(expressionSegments);
        return result;
    }
    
    @Override
    public ASTNode visitXmlForestFunction(final XmlForestFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.XMLFOREST().getText(), getOriginalText(ctx));
        Collection<ExpressionSegment> expressionSegments = ctx.expr().stream().map(each -> (ExpressionSegment) visit(each)).collect(Collectors.toList());
        result.getParameters().addAll(expressionSegments);
        return result;
    }
    
    @Override
    public ASTNode visitXmlParseFunction(final XmlParseFunctionContext ctx) {
        return new ExpressionProjectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), getOriginalText(ctx), (ExpressionSegment) visit(ctx.expr()));
    }
    
    @Override
    public ASTNode visitXmlPiFunction(final XmlPiFunctionContext ctx) {
        if (null != ctx.identifier()) {
            return new XmlPiFunctionSegment(ctx.start.getStopIndex(), ctx.stop.getStopIndex(), ctx.XMLPI().getText(),
                    ctx.identifier().getText(), (ExpressionSegment) visit(ctx.expr(0)), getOriginalText(ctx));
        }
        return new XmlPiFunctionSegment(ctx.start.getStopIndex(), ctx.stop.getStopIndex(), ctx.XMLPI().getText(),
                (ExpressionSegment) visit(ctx.expr(0)), (ExpressionSegment) visit(ctx.expr(1)), getOriginalText(ctx));
    }
    
    @Override
    public ASTNode visitXmlQueryFunction(final XmlQueryFunctionContext ctx) {
        XmlQueryAndExistsFunctionSegment result =
                new XmlQueryAndExistsFunctionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.XMLQUERY().getText(), ctx.STRING_().getText(), getOriginalText(ctx));
        Collection<ExpressionSegment> expressionSegments = ctx.xmlPassingClause().expr().stream().map(each -> (ExpressionSegment) visit(each)).collect(Collectors.toList());
        result.getParameters().addAll(expressionSegments);
        return result;
    }
    
    @Override
    public ASTNode visitXmlRootFunction(final XmlRootFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.XMLROOT().getText(), getOriginalText(ctx));
        Collection<ExpressionSegment> expressionSegments = ctx.expr().stream().map(each -> (ExpressionSegment) visit(each)).collect(Collectors.toList());
        result.getParameters().addAll(expressionSegments);
        return result;
    }
    
    @Override
    public ASTNode visitXmlSerializeFunction(final XmlSerializeFunctionContext ctx) {
        String dataType = null == ctx.dataType() ? null : ctx.dataType().getText();
        String encoding = null == ctx.STRING_() ? null : ctx.STRING_().getText();
        String version = null == ctx.stringLiterals() ? null : ctx.stringLiterals().getText();
        String identSize = null == ctx.INTEGER_() ? null : ctx.INTEGER_().getText();
        return new XmlSerializeFunctionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.XMLSERIALIZE().getText(), (ExpressionSegment) visit(ctx.expr()),
                dataType, encoding, version, identSize, getOriginalText(ctx));
    }
    
    @Override
    public ASTNode visitXmlTableFunction(final XmlTableFunctionContext ctx) {
        XmlNameSpacesClauseSegment xmlNameSpacesClause = null == ctx.xmlNameSpacesClause() ? null : (XmlNameSpacesClauseSegment) visit(ctx.xmlNameSpacesClause());
        return new XmlTableFunctionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.XMLTABLE().getText(),
                xmlNameSpacesClause, ctx.STRING_().getText(), (XmlTableOptionsSegment) visit(ctx.xmlTableOptions()), getOriginalText(ctx));
    }
    
    @Override
    public ASTNode visitXmlNameSpacesClause(final XmlNameSpacesClauseContext ctx) {
        // TODO : throw exception if more than one defaultString exists in a xml name space clause
        String defaultString = null == ctx.defaultString() ? null : ctx.defaultString(0).STRING_().getText();
        Collection<XmlNameSpaceStringAsIdentifierSegment> xmlNameSpaceStringAsIdentifierSegments = null == ctx.xmlNameSpaceStringAsIdentifier() ? Collections.emptyList()
                : ctx.xmlNameSpaceStringAsIdentifier().stream().map(each -> (XmlNameSpaceStringAsIdentifierSegment) visit(each)).collect(Collectors.toList());
        XmlNameSpacesClauseSegment result = new XmlNameSpacesClauseSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), defaultString, getOriginalText(ctx));
        result.getStringAsIdentifier().addAll(xmlNameSpaceStringAsIdentifierSegments);
        return result;
    }
    
    @Override
    public ASTNode visitXmlNameSpaceStringAsIdentifier(final XmlNameSpaceStringAsIdentifierContext ctx) {
        return new XmlNameSpaceStringAsIdentifierSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.STRING_().getText(), ctx.identifier().getText(), getOriginalText(ctx));
    }
    
    @Override
    public ASTNode visitXmlTableOptions(final XmlTableOptionsContext ctx) {
        XmlTableOptionsSegment result = new XmlTableOptionsSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), getOriginalText(ctx));
        Collection<ExpressionSegment> expressionSegments = null == ctx.xmlPassingClause().expr() ? Collections.emptyList()
                : ctx.xmlPassingClause().expr().stream().map(each -> (ExpressionSegment) visit(each)).collect(Collectors.toList());
        Collection<XmlTableColumnSegment> xmlTableColumnSegments = null == ctx.xmlTableColumn() ? Collections.emptyList()
                : ctx.xmlTableColumn().stream().map(each -> (XmlTableColumnSegment) visit(each)).collect(Collectors.toList());
        result.getParameters().addAll(expressionSegments);
        result.getXmlTableColumnSegments().addAll(xmlTableColumnSegments);
        return result;
    }
    
    @Override
    public ASTNode visitXmlTableColumn(final XmlTableColumnContext ctx) {
        String dataType = null == ctx.dataType() ? null : ctx.dataType().getText();
        String path = null == ctx.STRING_() ? null : ctx.STRING_().getText();
        ExpressionSegment defaultExpr = null == ctx.expr() ? null : (ExpressionSegment) visit(ctx.expr());
        return new XmlTableColumnSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.columnName().getText(), dataType, path, defaultExpr, getOriginalText(ctx));
    }
    
    @Override
    public ASTNode visitXmlIsSchemaValidFunction(final OracleStatementParser.XmlIsSchemaValidFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.ISSCHEMAVALID().getText(), getOriginalText(ctx));
        if (null != ctx.expr()) {
            for (ExprContext each : ctx.expr()) {
                result.getParameters().add((ExpressionSegment) visit(each));
            }
        }
        return result;
    }
    
    private Collection<ExpressionSegment> getExpressions(final AggregationFunctionContext ctx) {
        return null == ctx.expr() ? Collections.emptyList() : ctx.expr().stream().map(each -> (ExpressionSegment) visit(each)).collect(Collectors.toList());
    }
    
    private String getDistinctExpression(final AggregationFunctionContext ctx) {
        StringBuilder result = new StringBuilder();
        for (int i = 3; i < ctx.getChildCount() - 1; i++) {
            result.append(ctx.getChild(i).getText());
        }
        return result.toString();
    }
    
    @Override
    public final ASTNode visitSpecialFunction(final SpecialFunctionContext ctx) {
        if (null != ctx.castFunction()) {
            return visit(ctx.castFunction());
        }
        if (null != ctx.charFunction()) {
            return visit(ctx.charFunction());
        }
        return new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getChild(0).getChild(0).getText(), getOriginalText(ctx));
    }
    
    @Override
    public final ASTNode visitCastFunction(final CastFunctionContext ctx) {
        calculateParameterCount(Collections.singleton(ctx.expr()));
        FunctionSegment result;
        if (null != ctx.CAST()) {
            result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.CAST().getText(), getOriginalText(ctx));
        } else {
            result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.XMLCAST().getText(), getOriginalText(ctx));
        }
        ASTNode exprSegment = visit(ctx.expr());
        if (exprSegment instanceof ColumnSegment) {
            result.getParameters().add((ColumnSegment) exprSegment);
        } else if (exprSegment instanceof LiteralExpressionSegment) {
            result.getParameters().add((LiteralExpressionSegment) exprSegment);
        }
        result.getParameters().add((DataTypeSegment) visit(ctx.dataType()));
        return result;
    }
    
    @Override
    public final ASTNode visitCharFunction(final CharFunctionContext ctx) {
        calculateParameterCount(ctx.expr());
        return new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.CHAR().getText(), getOriginalText(ctx));
    }
    
    @Override
    public final ASTNode visitRegularFunction(final RegularFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.regularFunctionName().getText(), getOriginalText(ctx));
        Collection<ExpressionSegment> expressionSegments = ctx.expr().stream().map(each -> (ExpressionSegment) visit(each)).collect(Collectors.toList());
        result.getParameters().addAll(expressionSegments);
        return result;
    }
    
    @Override
    public final ASTNode visitDataTypeName(final DataTypeNameContext ctx) {
        Collection<String> dataTypeNames = new LinkedList<>();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            dataTypeNames.add(ctx.getChild(i).getText());
        }
        return new KeywordValue(String.join(" ", dataTypeNames));
    }
    
    // TODO :FIXME, sql case id: insert_with_str_to_date
    private void calculateParameterCount(final Collection<ExprContext> exprContexts) {
        for (ExprContext each : exprContexts) {
            visit(each);
        }
    }
    
    @Override
    public final ASTNode visitOrderByClause(final OrderByClauseContext ctx) {
        Collection<OrderByItemSegment> items = new LinkedList<>();
        for (OrderByItemContext each : ctx.orderByItem()) {
            items.add((OrderByItemSegment) visit(each));
        }
        return new OrderBySegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), items);
    }
    
    @Override
    public final ASTNode visitOrderByItem(final OrderByItemContext ctx) {
        OrderDirection orderDirection = null != ctx.DESC() ? OrderDirection.DESC : OrderDirection.ASC;
        NullsOrderType nullsOrderType = generateNullsOrderType(ctx);
        if (null != ctx.columnName()) {
            ColumnSegment column = (ColumnSegment) visit(ctx.columnName());
            return new ColumnOrderByItemSegment(column, orderDirection, nullsOrderType);
        }
        if (null != ctx.numberLiterals()) {
            return new IndexOrderByItemSegment(ctx.numberLiterals().getStart().getStartIndex(), ctx.numberLiterals().getStop().getStopIndex(),
                    SQLUtils.getExactlyNumber(ctx.numberLiterals().getText(), 10).intValue(), orderDirection, nullsOrderType);
        }
        return new ExpressionOrderByItemSegment(ctx.expr().getStart().getStartIndex(), ctx.expr().getStop().getStopIndex(),
                getOriginalText(ctx.expr()), orderDirection, nullsOrderType, (ExpressionSegment) visit(ctx.expr()));
    }
    
    private NullsOrderType generateNullsOrderType(final OrderByItemContext ctx) {
        if (null == ctx.FIRST() && null == ctx.LAST()) {
            return null;
        }
        return null == ctx.FIRST() ? NullsOrderType.LAST : NullsOrderType.FIRST;
    }
    
    @Override
    public final ASTNode visitDataType(final DataTypeContext ctx) {
        DataTypeSegment result = new DataTypeSegment();
        if (null != ctx.dataTypeName()) {
            result.setDataTypeName(((KeywordValue) visit(ctx.dataTypeName())).getValue());
        }
        if (null != ctx.specialDatatype()) {
            result.setDataTypeName(((KeywordValue) visit(ctx.specialDatatype().dataTypeName())).getValue());
        }
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
        List<TerminalNode> numbers = ctx.INTEGER_();
        if (numbers.size() == 1) {
            result.setPrecision(Integer.parseInt(numbers.get(0).getText()));
        }
        if (numbers.size() == 2) {
            result.setPrecision(Integer.parseInt(numbers.get(0).getText()));
            result.setScale(Integer.parseInt(numbers.get(1).getText()));
        }
        return result;
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
