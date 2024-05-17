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

package org.apache.shardingsphere.sql.parser.presto.visitor.statement;

import lombok.AccessLevel;
import lombok.Getter;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementBaseVisitor;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.BitExprContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.BitValueLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.BooleanLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.BooleanPrimaryContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.ColumnNameContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.ColumnNamesContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.ColumnRefContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.DataTypeContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.DatabaseNameContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.ExprContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.FieldLengthContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.FunctionNameContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.HexadecimalLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.IdentifierContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.IndexNameContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.LiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.NullValueLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.NumberLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.OwnerContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.ParameterMarkerContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.PrecisionContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.PredicateContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.SchemaNameContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.SimpleExprContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.StringLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.String_Context;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.TableListContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.TableNameContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.TemporalLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.ViewNameContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.ViewNamesContext;
import org.apache.shardingsphere.sql.parser.sql.common.enums.ParameterMarkerType;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.CollateExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExistsSubqueryExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.NotExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.SimpleExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DataTypeLengthSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DataTypeSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.ParameterMarkerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.util.SQLUtils;
import org.apache.shardingsphere.sql.parser.sql.common.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.BooleanLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.NullLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.NumberLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.OtherLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.StringLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.parametermarker.ParameterMarkerValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.presto.dml.PrestoSelectStatement;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Statement visitor for presto.
 */
@Getter(AccessLevel.PROTECTED)
public abstract class PrestoStatementVisitor extends PrestoStatementBaseVisitor<ASTNode> {
    
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
        if (null != ctx.temporalLiterals()) {
            return visit(ctx.temporalLiterals());
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
    public ASTNode visitString_(final String_Context ctx) {
        return new StringLiteralValue(ctx.getText());
    }
    
    @Override
    public final ASTNode visitNumberLiterals(final NumberLiteralsContext ctx) {
        return new NumberLiteralValue(ctx.getText());
    }
    
    @Override
    public ASTNode visitTemporalLiterals(final TemporalLiteralsContext ctx) {
        // TODO deal with TemporalLiterals
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
        return new NullLiteralValue(ctx.getText());
    }
    
    @Override
    public final ASTNode visitIdentifier(final IdentifierContext ctx) {
        return new IdentifierValue(ctx.getText());
    }
    
    @Override
    public final ASTNode visitSchemaName(final SchemaNameContext ctx) {
        return new DatabaseSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (IdentifierValue) visit(ctx.identifier()));
    }
    
    @Override
    public final ASTNode visitTableName(final TableNameContext ctx) {
        SimpleTableSegment result = new SimpleTableSegment(new TableNameSegment(ctx.name().getStart().getStartIndex(),
                ctx.name().getStop().getStopIndex(), new IdentifierValue(ctx.name().identifier().getText())));
        OwnerContext owner = ctx.owner();
        if (null != owner) {
            OwnerSegment ownerSegment = (OwnerSegment) visit(owner);
            ownerSegment.setOwner((OwnerSegment) visit(ctx.databaseName()));
            result.setOwner(ownerSegment);
        }
        return result;
    }
    
    @Override
    public ASTNode visitDatabaseName(final DatabaseNameContext ctx) {
        return new OwnerSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (IdentifierValue) visit(ctx.identifier()));
    }
    
    @Override
    public final ASTNode visitViewName(final ViewNameContext ctx) {
        SimpleTableSegment result = new SimpleTableSegment(new TableNameSegment(ctx.identifier().getStart().getStartIndex(),
                ctx.identifier().getStop().getStopIndex(), new IdentifierValue(ctx.identifier().getText())));
        OwnerContext owner = ctx.owner();
        if (null != owner) {
            result.setOwner((OwnerSegment) visit(owner));
        }
        return result;
    }
    
    @Override
    public final ASTNode visitOwner(final OwnerContext ctx) {
        return new OwnerSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (IdentifierValue) visit(ctx.identifier()));
    }
    
    @Override
    public ASTNode visitFunctionName(final FunctionNameContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.identifier().IDENTIFIER_().getText(), ctx.getText());
        if (null != ctx.owner()) {
            result.setOwner((OwnerSegment) visit(ctx.owner()));
        }
        return result;
    }
    
    @Override
    public final ASTNode visitColumnName(final ColumnNameContext ctx) {
        return new ColumnSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (IdentifierValue) visit(ctx.identifier()));
    }
    
    @Override
    public final ASTNode visitIndexName(final IndexNameContext ctx) {
        IndexNameSegment indexName = new IndexNameSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), (IdentifierValue) visit(ctx.identifier()));
        return new IndexSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), indexName);
    }
    
    @Override
    public ASTNode visitTableList(final TableListContext ctx) {
        CollectionValue<SimpleTableSegment> result = new CollectionValue<>();
        for (TableNameContext each : ctx.tableName()) {
            result.getValue().add((SimpleTableSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public final ASTNode visitViewNames(final ViewNamesContext ctx) {
        CollectionValue<SimpleTableSegment> result = new CollectionValue<>();
        for (ViewNameContext each : ctx.viewName()) {
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
        if (null != ctx.XOR()) {
            return createBinaryOperationExpression(ctx, "XOR");
        }
        if (null != ctx.andOperator()) {
            return createBinaryOperationExpression(ctx, ctx.andOperator().getText());
        }
        if (null != ctx.orOperator()) {
            return createBinaryOperationExpression(ctx, ctx.orOperator().getText());
        }
        return new NotExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), (ExpressionSegment) visit(ctx.expr(0)), false);
    }
    
    private BinaryOperationExpression createBinaryOperationExpression(final ExprContext ctx, final String operator) {
        ExpressionSegment left = (ExpressionSegment) visit(ctx.expr(0));
        ExpressionSegment right = (ExpressionSegment) visit(ctx.expr(1));
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new BinaryOperationExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, operator, text);
    }
    
    @Override
    public final ASTNode visitBooleanPrimary(final BooleanPrimaryContext ctx) {
        if (null != ctx.IS()) {
            // TODO optimize operatorToken
            String rightText = "";
            if (null != ctx.NOT()) {
                rightText = rightText.concat(ctx.start.getInputStream().getText(new Interval(ctx.NOT().getSymbol().getStartIndex(),
                        ctx.NOT().getSymbol().getStopIndex()))).concat(" ");
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
        if (null != ctx.comparisonOperator() || null != ctx.SAFE_EQ_()) {
            return createCompareSegment(ctx);
        }
        if (null != ctx.MEMBER()) {
            int startIndex = ctx.MEMBER().getSymbol().getStopIndex() + 5;
            int endIndex = ctx.stop.getStopIndex() - 1;
            String rightText = ctx.start.getInputStream().getText(new Interval(startIndex, endIndex));
            ExpressionSegment right = new ExpressionProjectionSegment(startIndex, endIndex, rightText);
            String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
            ExpressionSegment left = (ExpressionSegment) visit(ctx.booleanPrimary());
            String operator = "MEMBER OF";
            return new BinaryOperationExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, operator, text);
        }
        if (null != ctx.assignmentOperator()) {
            return createAssignmentSegment(ctx);
        }
        return visit(ctx.predicate());
    }
    
    private ASTNode createAssignmentSegment(final BooleanPrimaryContext ctx) {
        ExpressionSegment left = (ExpressionSegment) visit(ctx.booleanPrimary());
        ExpressionSegment right = (ExpressionSegment) visit(ctx.predicate());
        String operator = ctx.assignmentOperator().getText();
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new BinaryOperationExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, operator, text);
    }
    
    private ASTNode createCompareSegment(final BooleanPrimaryContext ctx) {
        ExpressionSegment left = (ExpressionSegment) visit(ctx.booleanPrimary());
        ExpressionSegment right;
        String operator;
        if (null != ctx.ALL()) {
            operator = null == ctx.SAFE_EQ_() ? ctx.comparisonOperator().getText() + " ALL" : ctx.SAFE_EQ_().getText();
        } else {
            operator = null == ctx.SAFE_EQ_() ? ctx.comparisonOperator().getText() : ctx.SAFE_EQ_().getText();
        }
        if (null != ctx.predicate()) {
            right = (ExpressionSegment) visit(ctx.predicate());
        } else {
            right = new SubqueryExpressionSegment(new SubquerySegment(ctx.subquery().start.getStartIndex(), ctx.subquery().stop.getStopIndex(), (PrestoSelectStatement) visit(ctx.subquery()),
                    getOriginalText(ctx.subquery())));
        }
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
        if (null != ctx.REGEXP()) {
            return createBinaryOperationExpressionFromRegexp(ctx);
        }
        if (null != ctx.RLIKE()) {
            return createBinaryOperationExpressionFromRlike(ctx);
        }
        return visit(ctx.bitExpr(0));
    }
    
    private InExpression createInSegment(final PredicateContext ctx) {
        boolean not = null != ctx.NOT();
        ExpressionSegment left = (ExpressionSegment) visit(ctx.bitExpr(0));
        ExpressionSegment right;
        if (null != ctx.subquery()) {
            right = new SubqueryExpressionSegment(new SubquerySegment(ctx.subquery().start.getStartIndex(), ctx.subquery().stop.getStopIndex(), (PrestoSelectStatement) visit(ctx.subquery()),
                    getOriginalText(ctx.subquery())));
        } else {
            right = new ListExpression(ctx.LP_().getSymbol().getStartIndex(), ctx.RP_().getSymbol().getStopIndex());
            for (ExprContext each : ctx.expr()) {
                ((ListExpression) right).getItems().add((ExpressionSegment) visit(each));
            }
        }
        return new InExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, not);
    }
    
    private BinaryOperationExpression createBinaryOperationExpressionFromLike(final PredicateContext ctx) {
        ExpressionSegment left = (ExpressionSegment) visit(ctx.bitExpr(0));
        String operator;
        ExpressionSegment right;
        if (null != ctx.SOUNDS()) {
            right = (ExpressionSegment) visit(ctx.bitExpr(1));
            operator = "SOUNDS LIKE";
        } else {
            ListExpression listExpression = new ListExpression(ctx.simpleExpr(0).start.getStartIndex(), ctx.simpleExpr().get(ctx.simpleExpr().size() - 1).stop.getStopIndex());
            for (SimpleExprContext each : ctx.simpleExpr()) {
                listExpression.getItems().add((ExpressionSegment) visit(each));
            }
            right = listExpression;
            operator = null == ctx.NOT() ? "LIKE" : "NOT LIKE";
        }
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new BinaryOperationExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, operator, text);
    }
    
    private BinaryOperationExpression createBinaryOperationExpressionFromRegexp(final PredicateContext ctx) {
        ExpressionSegment left = (ExpressionSegment) visit(ctx.bitExpr(0));
        ExpressionSegment right = (ExpressionSegment) visit(ctx.bitExpr(1));
        String operator = null == ctx.NOT() ? "REGEXP" : "NOT REGEXP";
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new BinaryOperationExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, operator, text);
    }
    
    private BinaryOperationExpression createBinaryOperationExpressionFromRlike(final PredicateContext ctx) {
        ExpressionSegment left = (ExpressionSegment) visit(ctx.bitExpr(0));
        ExpressionSegment right = (ExpressionSegment) visit(ctx.bitExpr(1));
        String operator = null == ctx.NOT() ? "RLIKE" : "NOT RLIKE";
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
            return visit(ctx.simpleExpr());
        }
        ExpressionSegment left = (ExpressionSegment) visit(ctx.getChild(0));
        ExpressionSegment right = (ExpressionSegment) visit(ctx.getChild(2));
        String operator = ctx.getChild(1).getText();
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new BinaryOperationExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, operator, text);
    }
    
    @Override
    public final ASTNode visitSimpleExpr(final SimpleExprContext ctx) {
        int startIndex = ctx.start.getStartIndex();
        int stopIndex = ctx.stop.getStopIndex();
        if (null != ctx.subquery()) {
            SubquerySegment subquerySegment = new SubquerySegment(ctx.subquery().getStart().getStartIndex(), ctx.subquery().getStop().getStopIndex(), (PrestoSelectStatement) visit(ctx.subquery()),
                    getOriginalText(ctx.subquery()));
            return null == ctx.EXISTS() ? new SubqueryExpressionSegment(subquerySegment) : new ExistsSubqueryExpression(startIndex, stopIndex, subquerySegment);
        }
        if (null != ctx.parameterMarker()) {
            ParameterMarkerValue parameterMarker = (ParameterMarkerValue) visit(ctx.parameterMarker());
            ParameterMarkerExpressionSegment result = new ParameterMarkerExpressionSegment(startIndex, stopIndex, parameterMarker.getValue(), parameterMarker.getType());
            parameterMarkerSegments.add(result);
            return result;
        }
        if (null != ctx.literals()) {
            return SQLUtils.createLiteralExpression(visit(ctx.literals()), startIndex, stopIndex, ctx.literals().start.getInputStream().getText(new Interval(startIndex, stopIndex)));
        }
        if (null != ctx.intervalExpression()) {
            return visit(ctx.intervalExpression());
        }
        if (null != ctx.functionCall()) {
            return visit(ctx.functionCall());
        }
        if (null != ctx.collateClause()) {
            if (null != ctx.simpleExpr()) {
                ExpressionSegment expr = (ExpressionSegment) visit(ctx.simpleExpr(0));
                return new CollateExpression(startIndex, stopIndex, (SimpleExpressionSegment) visit(ctx.collateClause()), expr);
            }
            return new CollateExpression(startIndex, stopIndex, (SimpleExpressionSegment) visit(ctx.collateClause()), null);
        }
        if (null != ctx.columnRef()) {
            return visit(ctx.columnRef());
        }
        if (null != ctx.matchExpression()) {
            return visit(ctx.matchExpression());
        }
        if (null != ctx.notOperator()) {
            ASTNode expression = visit(ctx.simpleExpr(0));
            if (expression instanceof ExistsSubqueryExpression) {
                ((ExistsSubqueryExpression) expression).setNot(true);
                return expression;
            }
            return new NotExpression(startIndex, stopIndex, (ExpressionSegment) expression, "!".equalsIgnoreCase(ctx.notOperator().getText()));
        }
        if (null != ctx.OR_()) {
            ExpressionSegment left = (ExpressionSegment) visit(ctx.simpleExpr(0));
            ExpressionSegment right = (ExpressionSegment) visit(ctx.simpleExpr(1));
            String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
            return new BinaryOperationExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, ctx.OR_().getText(), text);
        }
        return visitRemainSimpleExpr(ctx);
    }
    
    private ASTNode visitRemainSimpleExpr(final SimpleExprContext ctx) {
        if (null != ctx.caseExpression()) {
            return visit(ctx.caseExpression());
        }
        if (null != ctx.BINARY()) {
            return visit(ctx.simpleExpr(0));
        }
        if (null != ctx.variable()) {
            return visit(ctx.variable());
        }
        for (SimpleExprContext each : ctx.simpleExpr()) {
            visit(each);
        }
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new CommonExpressionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), text);
    }
    
    @Override
    public ASTNode visitColumnRef(final ColumnRefContext ctx) {
        int identifierCount = ctx.identifier().size();
        ColumnSegment result;
        if (1 == identifierCount) {
            result = new ColumnSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (IdentifierValue) visit(ctx.identifier(0)));
        } else if (2 == identifierCount) {
            result = new ColumnSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (IdentifierValue) visit(ctx.identifier(1)));
            result.setOwner(new OwnerSegment(ctx.identifier(0).start.getStartIndex(), ctx.identifier(0).stop.getStopIndex(), (IdentifierValue) visit(ctx.identifier(0))));
        } else {
            result = new ColumnSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (IdentifierValue) visit(ctx.identifier(2)));
            OwnerSegment owner = new OwnerSegment(ctx.identifier(1).start.getStartIndex(), ctx.identifier(1).stop.getStopIndex(), (IdentifierValue) visit(ctx.identifier(1)));
            owner.setOwner(new OwnerSegment(ctx.identifier(0).start.getStartIndex(), ctx.identifier(0).stop.getStopIndex(), (IdentifierValue) visit(ctx.identifier(0))));
            result.setOwner(owner);
        }
        return result;
    }
    
    @Override
    public ASTNode visitDataType(final DataTypeContext ctx) {
        DataTypeSegment result = new DataTypeSegment();
        result.setDataTypeName(ctx.dataTypeName.getText());
        result.setStartIndex(ctx.start.getStartIndex());
        result.setStopIndex(ctx.stop.getStopIndex());
        if (null != ctx.fieldLength()) {
            DataTypeLengthSegment dataTypeLengthSegment = (DataTypeLengthSegment) visit(ctx.fieldLength());
            result.setDataLength(dataTypeLengthSegment);
        }
        if (null != ctx.precision()) {
            DataTypeLengthSegment dataTypeLengthSegment = (DataTypeLengthSegment) visit(ctx.precision());
            result.setDataLength(dataTypeLengthSegment);
        }
        return result;
    }
    
    @Override
    public ASTNode visitFieldLength(final FieldLengthContext ctx) {
        DataTypeLengthSegment result = new DataTypeLengthSegment();
        result.setStartIndex(ctx.start.getStartIndex());
        result.setStopIndex(ctx.stop.getStartIndex());
        result.setPrecision(new BigDecimal(ctx.length.getText()).intValue());
        return result;
    }
    
    @Override
    public ASTNode visitPrecision(final PrecisionContext ctx) {
        DataTypeLengthSegment result = new DataTypeLengthSegment();
        result.setStartIndex(ctx.start.getStartIndex());
        result.setStopIndex(ctx.stop.getStartIndex());
        List<TerminalNode> numbers = ctx.NUMBER_();
        result.setPrecision(Integer.parseInt(numbers.get(0).getText()));
        result.setScale(Integer.parseInt(numbers.get(1).getText()));
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
