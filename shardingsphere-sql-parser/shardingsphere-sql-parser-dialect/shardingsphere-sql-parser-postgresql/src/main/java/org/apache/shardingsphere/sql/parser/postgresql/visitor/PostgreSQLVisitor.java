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

package org.apache.shardingsphere.sql.parser.postgresql.visitor;

import com.google.common.base.Joiner;
import lombok.AccessLevel;
import lombok.Getter;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
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
import org.apache.shardingsphere.sql.parser.sql.constant.AggregationType;
import org.apache.shardingsphere.sql.parser.sql.constant.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.predicate.PredicateBuilder;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.AggregationDistinctProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateBetweenRightValue;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateBracketValue;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateCompareRightValue;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateInRightValue;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateLeftBracketValue;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateRightBracketValue;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateRightValue;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.DataTypeLengthSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.DataTypeSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.sql.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.value.keyword.KeywordValue;
import org.apache.shardingsphere.sql.parser.sql.value.literal.LiteralValue;
import org.apache.shardingsphere.sql.parser.sql.value.literal.impl.BooleanLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.value.literal.impl.NumberLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.value.literal.impl.StringLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.value.parametermarker.ParameterMarkerValue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * PostgreSQL visitor.
 */
@Getter(AccessLevel.PROTECTED)
public abstract class PostgreSQLVisitor extends PostgreSQLStatementBaseVisitor<ASTNode> {
    
    private int currentParameterIndex;
    
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
        
        if (null != ctx.BETWEEN() && null == ctx.NOT()) {
            return createBetweenSegment(ctx);
        }
        if (null != ctx.IN() && null == ctx.NOT()) {
            return createInSegment(ctx);
        }
        
        if (null != ctx.comparisonOperator()) {
            ASTNode left = visit(ctx.aExpr(0));
            if (left instanceof ColumnSegment) {
                ColumnSegment columnSegment = (ColumnSegment) left;
                ASTNode right = visit(ctx.aExpr(1));
                PredicateRightValue value = right instanceof ColumnSegment ? (PredicateRightValue) right
                        : new PredicateCompareRightValue(ctx.comparisonOperator().getText(), (ExpressionSegment) right);
                PredicateSegment predicate = new PredicateSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), columnSegment, value);
                return predicate;
            }
            visit(ctx.aExpr(1));
            return new LiteralExpressionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText());
        }
        
        if (null != ctx.logicalOperator()) {
            return new PredicateBuilder(visit(ctx.aExpr(0)), visit(ctx.aExpr(1)), ctx.logicalOperator().getText()).mergePredicate();
        }
        
        if (null != ctx.optIndirection()) {
            return visit(ctx.aExpr(0));
        }
        super.visitAExpr(ctx);
        return new CommonExpressionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText());
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
        ColumnSegment result = new ColumnSegment(ctx.colId().start.getStartIndex(), ctx.colId().stop.getStopIndex(), new IdentifierValue(ctx.colId().getText()));
        return result;
    }
    
    private PredicateSegment createInSegment(final AExprContext ctx) {
        ColumnSegment column = (ColumnSegment) visit(ctx.aExpr(0).cExpr().columnref());
        PredicateBracketValue predicateBracketValue = createBracketValue(ctx.inExpr());
        return new PredicateSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), column, new PredicateInRightValue(predicateBracketValue, getExpressionSegments(ctx)));
    }
    
    private Collection<ExpressionSegment> getExpressionSegments(final AExprContext ctx) {
        Collection<ExpressionSegment> result = new LinkedList<>();
//        if (null != ctx.cExpr() && null != ctx.cExpr().select_with_parens()) {
//            Select_with_parensContext subquery = ctx.cExpr().select_with_parens();
//            result.add(new SubqueryExpressionSegment(new SubquerySegment(subquery.getStart().getStartIndex(), subquery.getStop().getStopIndex(),
//                    (SelectStatement) visit(ctx.cExpr().select_with_parens()))));
//            return result;
//        }
//        for (AExprContext each : ctx.aExpr()) {
//            result.add(new CommonExpressionSegment(each.start.getStartIndex(), each.stop.getStopIndex(), each.getText()));
//        }
        return result;
    }
    
    private PredicateBracketValue createBracketValue(final InExprContext ctx) {
        PredicateLeftBracketValue predicateLeftBracketValue = new PredicateLeftBracketValue(ctx.LP_().getSymbol().getStartIndex(), ctx.LP_().getSymbol().getStopIndex());
        PredicateRightBracketValue predicateRightBracketValue = new PredicateRightBracketValue(ctx.RP_().getSymbol().getStartIndex(), ctx.RP_().getSymbol().getStopIndex());
        visit(ctx);
        return new PredicateBracketValue(predicateLeftBracketValue, predicateRightBracketValue);
    }

    private PredicateSegment createBetweenSegment(final AExprContext ctx) {
        ColumnSegment column = (ColumnSegment) visit(ctx.aExpr().get(0));
        return new PredicateSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), column, new PredicateBetweenRightValue((ExpressionSegment) visit(ctx.bExpr()),
                (ExpressionSegment) visit(ctx.aExpr(1))));
    }
    
    @Override
    public ASTNode visitBExpr(final BExprContext ctx) {
        if (null != ctx.cExpr()) {
            return visit(ctx.cExpr());
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
        int innerExpressionStartIndex = ((TerminalNode) ctx.getChild(1)).getSymbol().getStartIndex();
        if (null == ctx.DISTINCT()) {
            return new AggregationProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), type, innerExpressionStartIndex);
        }
        return new AggregationDistinctProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), type, innerExpressionStartIndex, getDistinctExpression(ctx));
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
            return new IndexOrderByItemSegment(index.getStartIndex(), index.getStopIndex(),
                    Integer.valueOf(index.getLiterals().toString()), orderDirection);
        }
        return new ExpressionOrderByItemSegment(ctx.aExpr().getStart().getStartIndex(), ctx.aExpr().getStop().getStopIndex(), ctx.aExpr().getText(), orderDirection);
    }
    
    private OrderDirection generateOrderDirection(final AscDescContext ctx) {
        OrderDirection result = null != ctx.DESC() ? OrderDirection.DESC : OrderDirection.ASC;
        return result;
    }
    
    @Override
    public final ASTNode visitDataType(final DataTypeContext ctx) {
        DataTypeSegment dataTypeSegment = new DataTypeSegment();
        dataTypeSegment.setDataTypeName(((KeywordValue) visit(ctx.dataTypeName())).getValue());
        dataTypeSegment.setStartIndex(ctx.start.getStartIndex());
        dataTypeSegment.setStopIndex(ctx.stop.getStopIndex());
        if (null != ctx.dataTypeLength()) {
            DataTypeLengthSegment dataTypeLengthSegment = (DataTypeLengthSegment) visit(ctx.dataTypeLength());
            dataTypeSegment.setDataLength(dataTypeLengthSegment);
        }
        return dataTypeSegment;
    }
    
    @Override
    public final ASTNode visitDataTypeLength(final DataTypeLengthContext ctx) {
        DataTypeLengthSegment dataTypeLengthSegment = new DataTypeLengthSegment();
        dataTypeLengthSegment.setStartIndex(ctx.start.getStartIndex());
        dataTypeLengthSegment.setStopIndex(ctx.stop.getStartIndex());
        List<TerminalNode> numbers = ctx.NUMBER_();
        if (numbers.size() == 1) {
            dataTypeLengthSegment.setPrecision(Integer.parseInt(numbers.get(0).getText()));
        }
        if (numbers.size() == 2) {
            dataTypeLengthSegment.setPrecision(Integer.parseInt(numbers.get(0).getText()));
            dataTypeLengthSegment.setScale(Integer.parseInt(numbers.get(1).getText()));
        }
        return dataTypeLengthSegment;
    }
}
