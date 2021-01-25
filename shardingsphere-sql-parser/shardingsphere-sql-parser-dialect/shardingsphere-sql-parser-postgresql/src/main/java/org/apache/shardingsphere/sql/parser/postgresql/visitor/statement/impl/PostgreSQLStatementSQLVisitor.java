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
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementBaseVisitor;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AExprContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AexprConstContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AscDescContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AttrNameContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.BExprContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CExprContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ColumnNameContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ColumnNamesContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ColumnrefContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DataTypeContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DataTypeLengthContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DataTypeNameContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ExprListContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.FuncApplicationContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.FuncExprContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.FunctionExprCommonSubexprContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.IdentifierContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.InExprContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.IndexNameContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.NumberLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.OwnerContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ParameterMarkerContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.SchemaNameContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.SortClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.SortbyContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.TableNameContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.TableNamesContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.UnreservedWordContext;
import org.apache.shardingsphere.sql.parser.sql.common.constant.AggregationType;
import org.apache.shardingsphere.sql.parser.sql.common.constant.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationDistinctProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
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
import org.apache.shardingsphere.sql.parser.sql.common.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.keyword.KeywordValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.LiteralValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.BooleanLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.NumberLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.StringLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.parametermarker.ParameterMarkerValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLSelectStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * PostgreSQL Statement SQL visitor.
 */
@NoArgsConstructor
@Getter(AccessLevel.PROTECTED)
public abstract class PostgreSQLStatementSQLVisitor extends PostgreSQLStatementBaseVisitor<ASTNode> {
    
    private int currentParameterIndex;
    
    public PostgreSQLStatementSQLVisitor(final Properties props) {
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
        if (null != ctx.comparisonOperator()) {
            ExpressionSegment left = (ExpressionSegment) visit(ctx.aExpr(0));
            ExpressionSegment right = (ExpressionSegment) visit(ctx.aExpr(1));
            String operator = ctx.comparisonOperator().getText();
            String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
            return new BinaryOperationExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, operator, text);
        }
        if (null != ctx.logicalOperator()) {
            ExpressionSegment left = (ExpressionSegment) visit(ctx.aExpr(0));
            ExpressionSegment right = (ExpressionSegment) visit(ctx.aExpr(1));
            String operator = ctx.logicalOperator().getText();
            String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
            return new BinaryOperationExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, operator, text);
        }
        super.visitAExpr(ctx);
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new CommonExpressionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), text);
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
        super.visitCExpr(ctx);
        return new CommonExpressionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.getText());
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
        ExpressionSegment right = visitInExpression(ctx.inExpr());
        boolean not = null != ctx.NOT();
        return new InExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), left, right, not);
    }
    
    private ExpressionSegment visitInExpression(final InExprContext ctx) {
        if (null != ctx.selectWithParens()) {
            PostgreSQLSelectStatement select = (PostgreSQLSelectStatement) visit(ctx.selectWithParens());
            SubquerySegment subquerySegment = new SubquerySegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), select);
            return new SubqueryExpressionSegment(subquerySegment);
        }
        return (ExpressionSegment) visit(ctx.exprList());
    }
    
    @Override
    public ASTNode visitExprList(final ExprListContext ctx) {
        ListExpression result = new ListExpression(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
        if (null != ctx.exprList()) {
            result.getItems().addAll(((ListExpression) visitExprList(ctx.exprList())).getItems());
        }
        result.getItems().add((ExpressionSegment) visit(ctx.aExpr()));
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
    
    protected ProjectionSegment generateProjectFromFuncExpr(final FuncExprContext ctx) {
        if (null != ctx.funcApplication()) {
            return generateProjectFromFuncApplication(ctx.funcApplication());
        }
        return generateProjectFromFunctionExprCommonSubexpr(ctx.functionExprCommonSubexpr());
    }
    
    private ProjectionSegment generateProjectFromFuncApplication(final FuncApplicationContext ctx) {
        String aggregationType = ctx.funcName().getText();
        if (AggregationType.isAggregationType(aggregationType)) {
            return createAggregationSegment(ctx, aggregationType);
        }
        return new ExpressionProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText());
    }
    
    private ProjectionSegment generateProjectFromFunctionExprCommonSubexpr(final FunctionExprCommonSubexprContext ctx) {
        return new ExpressionProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText());
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
        ASTNode astNode = visit(ctx.aExpr());
        if (astNode instanceof ColumnSegment) {
            ColumnSegment column = (ColumnSegment) astNode;
            return new ColumnOrderByItemSegment(column, orderDirection);
        }
        if (astNode instanceof LiteralExpressionSegment) {
            LiteralExpressionSegment index = (LiteralExpressionSegment) astNode;
            return new IndexOrderByItemSegment(index.getStartIndex(), index.getStopIndex(), Integer.parseInt(index.getLiterals().toString()), orderDirection);
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
}
