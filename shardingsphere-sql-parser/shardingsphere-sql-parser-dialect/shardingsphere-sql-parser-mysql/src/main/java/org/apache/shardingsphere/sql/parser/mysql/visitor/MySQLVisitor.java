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

package org.apache.shardingsphere.sql.parser.mysql.visitor;

import lombok.AccessLevel;
import lombok.Getter;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementBaseVisitor;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AggregationFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.BitExprContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.BitValueLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.BooleanLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.BooleanPrimaryContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CastFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CharFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ColumnNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ColumnNamesContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ConvertFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DataTypeContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DataTypeLengthContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DataTypeNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DateTimeLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ExprContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ExtractFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.FunctionCallContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.GroupConcatFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.HexadecimalLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.IdentifierContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.IndexNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.IntervalExpressionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.LiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.MatchExpression_Context;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.NullValueLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.NumberLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.OrderByClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.OrderByItemContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.OwnerContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ParameterMarkerContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.PositionFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.PredicateContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RegularFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SchemaNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SimpleExprContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SpecialFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StringLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SubqueryContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SubstringFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TableNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TableNamesContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.UnreservedWordContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.WeightStringFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.WindowFunctionContext;
import org.apache.shardingsphere.sql.parser.sql.common.constant.AggregationType;
import org.apache.shardingsphere.sql.parser.sql.common.constant.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.NotExpression;
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
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.util.SQLUtil;
import org.apache.shardingsphere.sql.parser.sql.common.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.keyword.KeywordValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.BooleanLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.NumberLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.OtherLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.StringLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.parametermarker.ParameterMarkerValue;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * MySQL visitor.
 */
@Getter(AccessLevel.PROTECTED)
public abstract class MySQLVisitor extends MySQLStatementBaseVisitor<ASTNode> {
    
    private int currentParameterIndex;
    
    @Override
    public final ASTNode visitParameterMarker(final ParameterMarkerContext ctx) {
        return new ParameterMarkerValue(currentParameterIndex++);
    }
    
    @Override
    public final ASTNode visitLiterals(final LiteralsContext ctx) {
        if (null != ctx.stringLiterals()) {
            return visit(ctx.stringLiterals());
        }
        if (null != ctx.numberLiterals()) {
            return visit(ctx.numberLiterals());
        }
        if (null != ctx.dateTimeLiterals()) {
            return visit(ctx.dateTimeLiterals());
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
    public final ASTNode visitDateTimeLiterals(final DateTimeLiteralsContext ctx) {
        // TODO deal with dateTimeLiterals
        return new OtherLiteralValue(ctx.getText());
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
        // TODO deal with nullValueLiterals
        return new OtherLiteralValue(ctx.getText());
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
        SimpleTableSegment result = new SimpleTableSegment(new TableNameSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (IdentifierValue) visit(ctx.name())));
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
        if (null != ctx.XOR()) {
            BinaryOperationExpression result = new BinaryOperationExpression();
            result.setStartIndex(ctx.start.getStartIndex());
            result.setStopIndex(ctx.stop.getStopIndex());
            result.setLeft((ExpressionSegment) visit(ctx.expr(0)));
            result.setRight((ExpressionSegment) visit(ctx.expr(1)));
            result.setOperator("XOR");
            String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
            result.setText(text);
            return result;
        }
        if (null != ctx.logicalOperator()) {
            BinaryOperationExpression result = new BinaryOperationExpression();
            result.setStartIndex(ctx.start.getStartIndex());
            result.setStopIndex(ctx.stop.getStopIndex());
            result.setLeft((ExpressionSegment) visit(ctx.expr(0)));
            result.setRight((ExpressionSegment) visit(ctx.expr(1)));
            result.setOperator(ctx.logicalOperator().getText());
            String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
            result.setText(text);
            return result;
        }
        NotExpression result = new NotExpression();
        result.setStartIndex(ctx.start.getStartIndex());
        result.setStopIndex(ctx.stop.getStopIndex());
        result.setExpression((ExpressionSegment) visit(ctx.expr(0)));
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        result.setText(text);
        return result;
    }
    
    @Override
    public final ASTNode visitBooleanPrimary(final BooleanPrimaryContext ctx) {
        if (null != ctx.IS()) {
            BinaryOperationExpression result = new BinaryOperationExpression();
            result.setStartIndex(ctx.start.getStartIndex());
            result.setStopIndex(ctx.stop.getStopIndex());
            result.setLeft((ExpressionSegment) visit(ctx.booleanPrimary()));
            result.setRight(new LiteralExpressionSegment(ctx.IS().getSymbol().getStopIndex() + 1, ctx.stop.getStopIndex(), new Interval(ctx.IS().getSymbol().getStopIndex() + 1,
                    ctx.stop.getStopIndex())));
            result.setOperator("IS");
            String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
            result.setText(text);
            return result;
        }
        if (null != ctx.comparisonOperator() || null != ctx.SAFE_EQ_()) {
            return createCompareSegment(ctx);
        }
        return visit(ctx.predicate());
    }
    
    private ASTNode createCompareSegment(final BooleanPrimaryContext ctx) {
        BinaryOperationExpression result = new BinaryOperationExpression();
        result.setStartIndex(ctx.start.getStartIndex());
        result.setStopIndex(ctx.stop.getStopIndex());
        result.setLeft((ExpressionSegment) visit(ctx.booleanPrimary()));
        if (null != ctx.predicate()) {
            result.setRight((ExpressionSegment) visit(ctx.predicate()));
        } else {
            result.setRight((ExpressionSegment) visit(ctx.subquery()));
        }
        String operator = null != ctx.SAFE_EQ_() ? ctx.SAFE_EQ_().getText() : ctx.comparisonOperator().getText();
        result.setOperator(operator);
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        result.setText(text);
        return result;
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
        if (null != ctx.REGEXP()) {
            return createBinaryOperationExpressionFromRegexp(ctx);
        }
        return visit(ctx.bitExpr(0));
    }
    
    private InExpression createInSegment(final PredicateContext ctx) {
        InExpression result = new InExpression();
        result.setStartIndex(ctx.start.getStartIndex());
        result.setStopIndex(ctx.stop.getStopIndex());
        result.setLeft((ExpressionSegment) visit(ctx.bitExpr(0)));
        if (null != ctx.subquery()) {
            result.setRight(new SubqueryExpressionSegment(new SubquerySegment(ctx.subquery().start.getStartIndex(), ctx.subquery().stop.getStopIndex(), (SelectStatement) visit(ctx.subquery()))));
        } else {
            ListExpression listExpression = new ListExpression();
            listExpression.setStartIndex(ctx.LP_().getSymbol().getStartIndex());
            listExpression.setStopIndex(ctx.RP_().getSymbol().getStopIndex());
            String text = ctx.start.getInputStream().getText(new Interval(ctx.LP_().getSymbol().getStartIndex(), ctx.RP_().getSymbol().getStopIndex()));
            listExpression.setText(text);
            for (ExprContext each : ctx.expr()) {
                listExpression.getItems().add((ExpressionSegment) visit(each));
            }
            result.setRight(listExpression);
        }
        Boolean operator = null != ctx.NOT() ? true : false;
        result.setNot(operator);
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        result.setText(text);
        return result;
    }
    
    private BinaryOperationExpression createBinaryOperationExpressionFromLike(final PredicateContext ctx) {
        BinaryOperationExpression result = new BinaryOperationExpression();
        result.setStartIndex(ctx.start.getStartIndex());
        result.setStopIndex(ctx.stop.getStopIndex());
        result.setLeft((ExpressionSegment) visit(ctx.bitExpr(0)));
        String operator;
        if (null != ctx.SOUNDS()) {
            
            result.setRight((ExpressionSegment) visit(ctx.bitExpr(1)));
            operator = "SOUNDS LIKE";
        } else {
            ListExpression listExpression = new ListExpression();
            String text = ctx.start.getInputStream().getText(new Interval(ctx.LIKE().getSymbol().getStartIndex() + 1, ctx.stop.getStopIndex()));
            listExpression.setText(text);
            for (SimpleExprContext each : ctx.simpleExpr()) {
                listExpression.getItems().add((ExpressionSegment) visit(each));
            }
            result.setRight(listExpression);
            operator = null != ctx.NOT() ? "NOT LIKE" : "LIKE";
        }
        result.setOperator(operator);
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        result.setText(text);
        return result;
    }
    
    private BinaryOperationExpression createBinaryOperationExpressionFromRegexp(final PredicateContext ctx) {
        BinaryOperationExpression result = new BinaryOperationExpression();
        result.setStartIndex(ctx.start.getStartIndex());
        result.setStopIndex(ctx.stop.getStopIndex());
        result.setLeft((ExpressionSegment) visit(ctx.bitExpr(0)));
        result.setRight((ExpressionSegment) visit(ctx.bitExpr(1)));
        String operator = null != ctx.NOT() ? "NOT REGEXP" : "REGEXP";
        result.setOperator(operator);
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        result.setText(text);
        return result;
    }
    
    private BetweenExpression createBetweenSegment(final PredicateContext ctx) {
        BetweenExpression result = new BetweenExpression();
        result.setStartIndex(ctx.start.getStartIndex());
        result.setStopIndex(ctx.stop.getStopIndex());
        result.setLeft((ExpressionSegment) visit(ctx.bitExpr(0)));
        result.setBetweenExpr((ExpressionSegment) visit(ctx.bitExpr(1)));
        result.setAndExpr((ExpressionSegment) visit(ctx.predicate()));
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        result.setText(text);
    
        return result;
    }
    
    @Override
    public final ASTNode visitBitExpr(final BitExprContext ctx) {
        if (null != ctx.simpleExpr()) {
            return createExpressionSegment(visit(ctx.simpleExpr()), ctx);
        }
        BinaryOperationExpression result = new BinaryOperationExpression();
        result.setStartIndex(ctx.start.getStartIndex());
        result.setStopIndex(ctx.stop.getStopIndex());
        result.setLeft((ExpressionSegment) visit(ctx.getChild(0)));
        result.setRight((ExpressionSegment) visit(ctx.getChild(2)));
        result.setOperator(ctx.getChild(1).getText());
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        result.setText(text);
        return result;
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
            return new ParameterMarkerExpressionSegment(context.start.getStartIndex(), context.stop.getStopIndex(), ((ParameterMarkerValue) astNode).getValue());
        }
        if (astNode instanceof SubquerySegment) {
            return new SubqueryExpressionSegment((SubquerySegment) astNode);
        }
        if (astNode instanceof OtherLiteralValue) {
            return new CommonExpressionSegment(context.getStart().getStartIndex(), context.getStop().getStopIndex(), ((OtherLiteralValue) astNode).getValue());
        }
        return astNode;
    }
    
    @Override
    public final ASTNode visitSimpleExpr(final SimpleExprContext ctx) {
        if (null != ctx.subquery()) {
            return new SubquerySegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (SelectStatement) visit(ctx.subquery()));
        }
        if (null != ctx.parameterMarker()) {
            return visit(ctx.parameterMarker());
        }
        if (null != ctx.literals()) {
            return visit(ctx.literals());
        }
        if (null != ctx.intervalExpression()) {
            return visit(ctx.intervalExpression());
        }
        if (null != ctx.functionCall()) {
            return visit(ctx.functionCall());
        }
        if (null != ctx.columnName()) {
            return visit(ctx.columnName());
        }
        if (null != ctx.matchExpression_()) {
            return visit(ctx.matchExpression_());
        }
        return visitRemainSimpleExpr(ctx);
    }
    
    @Override
    public ASTNode visitSubquery(final SubqueryContext ctx) {
        return visit(ctx.unionClause());
    }
    
    @Override
    public final ASTNode visitIntervalExpression(final IntervalExpressionContext ctx) {
        calculateParameterCount(Collections.singleton(ctx.intervalValue().expr()));
        return new ExpressionProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText());
    }
    
    @Override
    public final ASTNode visitFunctionCall(final FunctionCallContext ctx) {
        if (null != ctx.aggregationFunction()) {
            return visit(ctx.aggregationFunction());
        }
        if (null != ctx.specialFunction()) {
            return visit(ctx.specialFunction());
        }
        if (null != ctx.regularFunction()) {
            return visit(ctx.regularFunction());
        }
        throw new IllegalStateException("FunctionCallContext must have aggregationFunction, regularFunction or specialFunction.");
    }
    
    @Override
    public final ASTNode visitAggregationFunction(final AggregationFunctionContext ctx) {
        String aggregationType = ctx.aggregationFunctionName().getText();
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return AggregationType.isAggregationType(aggregationType)
                ? createAggregationSegment(ctx, aggregationType) : new ExpressionProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), text);
    }
    
    private ASTNode createAggregationSegment(final AggregationFunctionContext ctx, final String aggregationType) {
        AggregationType type = AggregationType.valueOf(aggregationType.toUpperCase());
        String innerExpression = ctx.start.getInputStream().getText(new Interval(ctx.LP_().getSymbol().getStartIndex(), ctx.stop.getStopIndex()));
        if (null == ctx.distinct()) {
            return new AggregationProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), type, innerExpression);
        }
        return new AggregationDistinctProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), type, innerExpression, getDistinctExpression(ctx));
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
        if (null != ctx.groupConcatFunction()) {
            return visit(ctx.groupConcatFunction());
        }
        if (null != ctx.windowFunction()) {
            return visit(ctx.windowFunction());
        }
        if (null != ctx.castFunction()) {
            return visit(ctx.castFunction());
        }
        if (null != ctx.convertFunction()) {
            return visit(ctx.convertFunction());
        }
        if (null != ctx.positionFunction()) {
            return visit(ctx.positionFunction());
        }
        if (null != ctx.substringFunction()) {
            return visit(ctx.substringFunction());
        }
        if (null != ctx.extractFunction()) {
            return visit(ctx.extractFunction());
        }
        if (null != ctx.charFunction()) {
            return visit(ctx.charFunction());
        }
        if (null != ctx.weightStringFunction()) {
            return visit(ctx.weightStringFunction());
        }
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new ExpressionProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), text);
    }
    
    @Override
    public final ASTNode visitGroupConcatFunction(final GroupConcatFunctionContext ctx) {
        calculateParameterCount(ctx.expr());
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new ExpressionProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), text);
    }
    
    @Override
    public final ASTNode visitWindowFunction(final WindowFunctionContext ctx) {
        calculateParameterCount(ctx.expr());
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new ExpressionProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), text);
    }
    
    @Override
    public final ASTNode visitCastFunction(final CastFunctionContext ctx) {
        calculateParameterCount(Collections.singleton(ctx.expr()));
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new ExpressionProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), text);
    }
    
    @Override
    public final ASTNode visitConvertFunction(final ConvertFunctionContext ctx) {
        calculateParameterCount(Collections.singleton(ctx.expr()));
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new ExpressionProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), text);
    }
    
    @Override
    public final ASTNode visitPositionFunction(final PositionFunctionContext ctx) {
        calculateParameterCount(ctx.expr());
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new ExpressionProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), text);
    }
    
    @Override
    public final ASTNode visitSubstringFunction(final SubstringFunctionContext ctx) {
        calculateParameterCount(Collections.singleton(ctx.expr()));
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new ExpressionProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), text);
    }
    
    @Override
    public final ASTNode visitExtractFunction(final ExtractFunctionContext ctx) {
        calculateParameterCount(Collections.singleton(ctx.expr()));
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new ExpressionProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), text);
    }
    
    @Override
    public final ASTNode visitCharFunction(final CharFunctionContext ctx) {
        calculateParameterCount(ctx.expr());
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new ExpressionProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), text);
    }
    
    @Override
    public final ASTNode visitWeightStringFunction(final WeightStringFunctionContext ctx) {
        calculateParameterCount(Collections.singleton(ctx.expr()));
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new ExpressionProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), text);
    }
    
    @Override
    public final ASTNode visitRegularFunction(final RegularFunctionContext ctx) {
        if (null != ctx.completeRegularFunction()) {
            calculateParameterCount(ctx.completeRegularFunction().expr());
        }
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new ExpressionProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), text);
    }
    
    private ASTNode visitRemainSimpleExpr(final SimpleExprContext ctx) {
        if (null != ctx.caseExpression()) {
            visit(ctx.caseExpression());
            String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
            return new OtherLiteralValue(text);
        }
        for (ExprContext each : ctx.expr()) {
            visit(each);
        }
        for (SimpleExprContext each : ctx.simpleExpr()) {
            visit(each);
        }
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new CommonExpressionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), text);
    }
    
    @Override
    public final ASTNode visitMatchExpression_(final MatchExpression_Context ctx) {
        visit(ctx.expr());
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new CommonExpressionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), text);
    }
    
    @Override
    public final ASTNode visitDataTypeName(final DataTypeNameContext ctx) {
        return new KeywordValue(ctx.getText());
    }
    
    // TODO :FIXME, sql case id: insert_with_str_to_date
    private void calculateParameterCount(final Collection<ExprContext> exprContexts) {
        for (ExprContext each : exprContexts) {
            visit(each);
        }
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
        if (numbers.size() == 1) {
            result.setPrecision(Integer.parseInt(numbers.get(0).getText()));
        }
        if (numbers.size() == 2) {
            result.setPrecision(Integer.parseInt(numbers.get(0).getText()));
            result.setScale(Integer.parseInt(numbers.get(1).getText()));
        }
        return result;
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
        if (null != ctx.columnName()) {
            ColumnSegment column = (ColumnSegment) visit(ctx.columnName());
            return new ColumnOrderByItemSegment(column, orderDirection);
        }
        if (null != ctx.numberLiterals()) {
            return new IndexOrderByItemSegment(ctx.numberLiterals().getStart().getStartIndex(), ctx.numberLiterals().getStop().getStopIndex(),
                    SQLUtil.getExactlyNumber(ctx.numberLiterals().getText(), 10).intValue(), orderDirection);
        }
        return new ExpressionOrderByItemSegment(ctx.expr().getStart().getStartIndex(), ctx.expr().getStop().getStopIndex(), ctx.expr().getText(), orderDirection);
    }
}
