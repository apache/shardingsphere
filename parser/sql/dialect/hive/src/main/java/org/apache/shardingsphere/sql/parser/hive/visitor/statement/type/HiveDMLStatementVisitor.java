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

package org.apache.shardingsphere.sql.parser.hive.visitor.statement.type;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DMLStatementVisitor;
import org.apache.shardingsphere.sql.parser.hive.visitor.statement.HiveStatementVisitor;
import org.apache.shardingsphere.sql.parser.sql.common.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.sql.common.enums.CombineType;
import org.apache.shardingsphere.sql.parser.sql.common.enums.JoinType;
import org.apache.shardingsphere.sql.parser.sql.common.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.VariableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.ConstraintSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.combine.CombineSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.CaseWhenExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExistsSubqueryExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ValuesExpression;
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
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.PaginationValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.ParameterMarkerLimitValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.HavingSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.LockSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DataTypeLengthSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DataTypeSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.ParameterMarkerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.WindowItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.WindowSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.DeleteMultiTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.util.SQLUtils;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.BooleanLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.NumberLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.OtherLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.StringLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.parametermarker.ParameterMarkerValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.hive.dml.HiveDeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.hive.dml.HiveInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.hive.dml.HiveSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.hive.dml.HiveUpdateStatement;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DML statement visitor for hive.
 */
public final class HiveDMLStatementVisitor extends HiveStatementVisitor implements DMLStatementVisitor {
    
    @Override
    public ASTNode visitSubquery(final HiveStatementParser.SubqueryContext ctx) {
        return visit(ctx.queryExpressionParens());
    }
    
    @Override
    public ASTNode visitQueryExpressionParens(final HiveStatementParser.QueryExpressionParensContext ctx) {
        if (null != ctx.queryExpressionParens()) {
            return visit(ctx.queryExpressionParens());
        }
        HiveSelectStatement result = (HiveSelectStatement) visit(ctx.queryExpression());
        if (null != ctx.lockClauseList()) {
            result.setLock((LockSegment) visit(ctx.lockClauseList()));
        }
        result.addParameterMarkerSegments(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitLockClauseList(final HiveStatementParser.LockClauseListContext ctx) {
        LockSegment result = new LockSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
        for (HiveStatementParser.LockClauseContext each : ctx.lockClause()) {
            if (null != each.tableLockingList()) {
                result.getTables().addAll(generateTablesFromTableAliasRefList(each.tableLockingList().tableAliasRefList()));
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitQueryExpression(final HiveStatementParser.QueryExpressionContext ctx) {
        HiveSelectStatement result;
        if (null != ctx.queryExpressionBody()) {
            result = (HiveSelectStatement) visit(ctx.queryExpressionBody());
        } else {
            result = (HiveSelectStatement) visit(ctx.queryExpressionParens());
        }
        if (null != ctx.orderByClause()) {
            result.setOrderBy((OrderBySegment) visit(ctx.orderByClause()));
        }
        if (null != ctx.limitClause()) {
            result.setLimit((LimitSegment) visit(ctx.limitClause()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitSelectWithInto(final HiveStatementParser.SelectWithIntoContext ctx) {
        if (null != ctx.selectWithInto()) {
            return visit(ctx.selectWithInto());
        }
        HiveSelectStatement result = (HiveSelectStatement) visit(ctx.queryExpression());
        if (null != ctx.lockClauseList()) {
            result.setLock((LockSegment) visit(ctx.lockClauseList()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitQueryExpressionBody(final HiveStatementParser.QueryExpressionBodyContext ctx) {
        if (1 == ctx.getChildCount() && ctx.getChild(0) instanceof HiveStatementParser.QueryPrimaryContext) {
            return visit(ctx.queryPrimary());
        }
        if (null != ctx.queryExpressionBody()) {
            HiveSelectStatement result = new HiveSelectStatement();
            SubquerySegment left = new SubquerySegment(ctx.queryExpressionBody().start.getStartIndex(), ctx.queryExpressionBody().stop.getStopIndex(),
                    (HiveSelectStatement) visit(ctx.queryExpressionBody()), getOriginalText(ctx.queryExpressionBody()));
            result.setProjections(left.getSelect().getProjections());
            left.getSelect().getFrom().ifPresent(result::setFrom);
            ((HiveSelectStatement) left.getSelect()).getTable().ifPresent(result::setTable);
            result.setCombine(createCombineSegment(ctx.combineClause(), left));
            return result;
        }
        if (null != ctx.queryExpressionParens()) {
            HiveSelectStatement result = new HiveSelectStatement();
            SubquerySegment left = new SubquerySegment(ctx.queryExpressionParens().start.getStartIndex(), ctx.queryExpressionParens().stop.getStopIndex(),
                    (HiveSelectStatement) visit(ctx.queryExpressionParens()), getOriginalText(ctx.queryExpressionParens()));
            result.setProjections(left.getSelect().getProjections());
            left.getSelect().getFrom().ifPresent(result::setFrom);
            ((HiveSelectStatement) left.getSelect()).getTable().ifPresent(result::setTable);
            result.setCombine(createCombineSegment(ctx.combineClause(), left));
            return result;
        }
        return visit(ctx.queryExpressionParens());
    }
    
    private CombineSegment createCombineSegment(final HiveStatementParser.CombineClauseContext ctx, final SubquerySegment left) {
        CombineType combineType;
        if (null != ctx.EXCEPT()) {
            combineType = CombineType.EXCEPT;
        } else {
            combineType = null == ctx.combineOption() || null == ctx.combineOption().ALL() ? CombineType.UNION : CombineType.UNION_ALL;
        }
        ParserRuleContext ruleContext = null == ctx.queryPrimary() ? ctx.queryExpressionParens() : ctx.queryPrimary();
        SubquerySegment right = new SubquerySegment(ruleContext.start.getStartIndex(), ruleContext.stop.getStopIndex(), (HiveSelectStatement) visit(ruleContext), getOriginalText(ruleContext));
        return new CombineSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), left, combineType, right);
    }
    
    @Override
    public ASTNode visitQuerySpecification(final HiveStatementParser.QuerySpecificationContext ctx) {
        HiveSelectStatement result = new HiveSelectStatement();
        result.setProjections((ProjectionsSegment) visit(ctx.projections()));
        if (null != ctx.selectSpecification()) {
            result.getProjections().setDistinctRow(isDistinct(ctx));
        }
        if (null != ctx.fromClause()) {
            if (null != ctx.fromClause().tableReferences()) {
                TableSegment tableSource = (TableSegment) visit(ctx.fromClause().tableReferences());
                result.setFrom(tableSource);
            }
            if (null != ctx.fromClause().DUAL()) {
                TableSegment tableSource = new SimpleTableSegment(new TableNameSegment(ctx.fromClause().DUAL().getSymbol().getStartIndex(),
                        ctx.fromClause().DUAL().getSymbol().getStopIndex(), new IdentifierValue(ctx.fromClause().DUAL().getText())));
                result.setFrom(tableSource);
            }
        }
        if (null != ctx.whereClause()) {
            result.setWhere((WhereSegment) visit(ctx.whereClause()));
        }
        if (null != ctx.groupByClause()) {
            result.setGroupBy((GroupBySegment) visit(ctx.groupByClause()));
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
    public ASTNode visitTableValueConstructor(final HiveStatementParser.TableValueConstructorContext ctx) {
        HiveSelectStatement result = new HiveSelectStatement();
        int startIndex = ctx.getStart().getStartIndex();
        int stopIndex = ctx.getStop().getStopIndex();
        ValuesExpression valuesExpression = new ValuesExpression(startIndex, stopIndex);
        valuesExpression.getRowConstructorList().addAll(createRowConstructorList(ctx.rowConstructorList()));
        result.setProjections(new ProjectionsSegment(startIndex, stopIndex));
        result.getProjections().getProjections().add(new ExpressionProjectionSegment(startIndex, stopIndex, getOriginalText(ctx), valuesExpression));
        return result;
    }
    
    private Collection<InsertValuesSegment> createRowConstructorList(final HiveStatementParser.RowConstructorListContext ctx) {
        Collection<InsertValuesSegment> result = new LinkedList<>();
        for (HiveStatementParser.AssignmentValuesContext each : ctx.assignmentValues()) {
            result.add((InsertValuesSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitTableStatement(final HiveStatementParser.TableStatementContext ctx) {
        HiveSelectStatement result = new HiveSelectStatement();
        if (null != ctx.TABLE()) {
            result.setFrom(new SimpleTableSegment(new TableNameSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                    new IdentifierValue(ctx.tableName().getText()))));
        } else {
            result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitWindowClause(final HiveStatementParser.WindowClauseContext ctx) {
        WindowSegment result = new WindowSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
        for (HiveStatementParser.WindowItemContext each : ctx.windowItem()) {
            result.getItemSegments().add((WindowItemSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitWindowItem(final HiveStatementParser.WindowItemContext ctx) {
        WindowItemSegment result = new WindowItemSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
        result.setWindowName(new IdentifierValue(ctx.identifier().getText()));
        if (null != ctx.windowSpecification().PARTITION()) {
            result.setPartitionListSegments(getExpressionsFromExprList(ctx.windowSpecification().expr()));
        }
        if (null != ctx.windowSpecification().orderByClause()) {
            result.setOrderBySegment((OrderBySegment) visit(ctx.windowSpecification().orderByClause()));
        }
        if (null != ctx.windowSpecification().frameClause()) {
            result.setFrameClause(new CommonExpressionSegment(ctx.windowSpecification().frameClause().start.getStartIndex(), ctx.windowSpecification().frameClause().stop.getStopIndex(),
                    ctx.windowSpecification().frameClause().getText()));
        }
        return result;
    }
    
    private Collection<ExpressionSegment> getExpressionsFromExprList(final List<HiveStatementParser.ExprContext> exprList) {
        if (null == exprList) {
            return Collections.emptyList();
        }
        Collection<ExpressionSegment> result = new ArrayList<>(exprList.size());
        for (HiveStatementParser.ExprContext each : exprList) {
            result.add((ExpressionSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitHavingClause(final HiveStatementParser.HavingClauseContext ctx) {
        ExpressionSegment expr = (ExpressionSegment) visit(ctx.expr());
        return new HavingSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), expr);
    }
    
    @Override
    public ASTNode visitIntervalExpression(final HiveStatementParser.IntervalExpressionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.INTERVAL().getSymbol().getStartIndex(), ctx.INTERVAL().getSymbol().getStopIndex(), ctx.INTERVAL().getText(), ctx.INTERVAL().getText());
        result.getParameters().add((ExpressionSegment) visit(ctx.intervalValue().expr()));
        result.getParameters().add(new LiteralExpressionSegment(ctx.intervalValue().intervalUnit().getStart().getStartIndex(), ctx.intervalValue().intervalUnit().getStop().getStopIndex(),
                ctx.intervalValue().intervalUnit().getText()));
        return result;
    }
    
    @Override
    public ASTNode visitFunctionCall(final HiveStatementParser.FunctionCallContext ctx) {
        if (null != ctx.aggregationFunction()) {
            return visit(ctx.aggregationFunction());
        }
        if (null != ctx.specialFunction()) {
            return visit(ctx.specialFunction());
        }
        if (null != ctx.regularFunction()) {
            return visit(ctx.regularFunction());
        }
        if (null != ctx.jsonFunction()) {
            return visit(ctx.jsonFunction());
        }
        if (null != ctx.udfFunction()) {
            return visit(ctx.udfFunction());
        }
        throw new IllegalStateException("FunctionCallContext must have aggregationFunction, regularFunction, specialFunction, jsonFunction or udfFunction.");
    }
    
    @Override
    public ASTNode visitUdfFunction(final HiveStatementParser.UdfFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), getOriginalText(ctx), getOriginalText(ctx));
        if (null != ctx.expr()) {
            for (HiveStatementParser.ExprContext each : ctx.expr()) {
                result.getParameters().add((ExpressionSegment) visit(each));
            }
        }
        return result;
    }
    
    @Override
    public ASTNode visitAggregationFunction(final HiveStatementParser.AggregationFunctionContext ctx) {
        String aggregationType = ctx.aggregationFunctionName().getText();
        return AggregationType.isAggregationType(aggregationType)
                ? createAggregationSegment(ctx, aggregationType)
                : new ExpressionProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), getOriginalText(ctx));
    }
    
    @Override
    public ASTNode visitJsonFunction(final HiveStatementParser.JsonFunctionContext ctx) {
        HiveStatementParser.JsonFunctionNameContext functionNameContext = ctx.jsonFunctionName();
        String functionName;
        if (null != functionNameContext) {
            functionName = functionNameContext.getText();
            for (HiveStatementParser.ExprContext each : ctx.expr()) {
                visit(each);
            }
        } else if (null != ctx.JSON_SEPARATOR()) {
            functionName = ctx.JSON_SEPARATOR().getText();
        } else {
            functionName = ctx.JSON_UNQUOTED_SEPARATOR().getText();
        }
        return new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), functionName, getOriginalText(ctx));
    }
    
    private ASTNode createAggregationSegment(final HiveStatementParser.AggregationFunctionContext ctx, final String aggregationType) {
        AggregationType type = AggregationType.valueOf(aggregationType.toUpperCase());
        String innerExpression = ctx.start.getInputStream().getText(new Interval(ctx.LP_().getSymbol().getStartIndex(), ctx.stop.getStopIndex()));
        if (null != ctx.distinct()) {
            AggregationDistinctProjectionSegment result = new AggregationDistinctProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(),
                    type, innerExpression, getDistinctExpression(ctx));
            result.getParameters().addAll(getExpressions(ctx));
            return result;
        }
        AggregationProjectionSegment result = new AggregationProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), type, innerExpression);
        result.getParameters().addAll(getExpressions(ctx));
        return result;
    }
    
    private Collection<ExpressionSegment> getExpressions(final HiveStatementParser.AggregationFunctionContext ctx) {
        if (null == ctx.expr()) {
            return Collections.emptyList();
        }
        Collection<ExpressionSegment> result = new LinkedList<>();
        for (HiveStatementParser.ExprContext each : ctx.expr()) {
            result.add((ExpressionSegment) visit(each));
        }
        return result;
    }
    
    private String getDistinctExpression(final HiveStatementParser.AggregationFunctionContext ctx) {
        StringBuilder result = new StringBuilder();
        for (int i = 3; i < ctx.getChildCount() - 1; i++) {
            result.append(ctx.getChild(i).getText());
        }
        return result.toString();
    }
    
    @Override
    public ASTNode visitSpecialFunction(final HiveStatementParser.SpecialFunctionContext ctx) {
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
        if (null != ctx.trimFunction()) {
            return visit(ctx.trimFunction());
        }
        if (null != ctx.weightStringFunction()) {
            return visit(ctx.weightStringFunction());
        }
        if (null != ctx.valuesFunction()) {
            return visit(ctx.valuesFunction());
        }
        if (null != ctx.currentUserFunction()) {
            return visit(ctx.currentUserFunction());
        }
        return new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), getOriginalText(ctx), getOriginalText(ctx));
    }
    
    @Override
    public ASTNode visitGroupConcatFunction(final HiveStatementParser.GroupConcatFunctionContext ctx) {
        calculateParameterCount(ctx.expr());
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.GROUP_CONCAT().getText(), getOriginalText(ctx));
        for (HiveStatementParser.ExprContext each : ctx.expr()) {
            result.getParameters().add((ExpressionSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitWindowFunction(final HiveStatementParser.WindowFunctionContext ctx) {
        super.visitWindowFunction(ctx);
        return new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.funcName.getText(), getOriginalText(ctx));
    }
    
    @Override
    public ASTNode visitCastFunction(final HiveStatementParser.CastFunctionContext ctx) {
        calculateParameterCount(ctx.expr());
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.CAST().getText(), getOriginalText(ctx));
        for (HiveStatementParser.ExprContext each : ctx.expr()) {
            ASTNode expr = visit(each);
            if (expr instanceof ColumnSegment) {
                result.getParameters().add((ColumnSegment) expr);
            } else if (expr instanceof LiteralExpressionSegment) {
                result.getParameters().add((LiteralExpressionSegment) expr);
            }
        }
        if (null != ctx.castType()) {
            result.getParameters().add((DataTypeSegment) visit(ctx.castType()));
        }
        if (null != ctx.DATETIME()) {
            DataTypeSegment dataType = new DataTypeSegment();
            dataType.setDataTypeName(ctx.DATETIME().getText());
            dataType.setStartIndex(ctx.DATETIME().getSymbol().getStartIndex());
            dataType.setStopIndex(ctx.DATETIME().getSymbol().getStopIndex());
            if (null != ctx.typeDatetimePrecision()) {
                dataType.setDataLength((DataTypeLengthSegment) visit(ctx.typeDatetimePrecision()));
            }
            result.getParameters().add(dataType);
        }
        return result;
    }
    
    @Override
    public ASTNode visitCastType(final HiveStatementParser.CastTypeContext ctx) {
        DataTypeSegment result = new DataTypeSegment();
        result.setDataTypeName(ctx.castTypeName.getText());
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
    public ASTNode visitConvertFunction(final HiveStatementParser.ConvertFunctionContext ctx) {
        calculateParameterCount(Collections.singleton(ctx.expr()));
        return new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.CONVERT().getText(), getOriginalText(ctx));
    }
    
    @Override
    public ASTNode visitPositionFunction(final HiveStatementParser.PositionFunctionContext ctx) {
        calculateParameterCount(ctx.expr());
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.POSITION().getText(), getOriginalText(ctx));
        result.getParameters().add((LiteralExpressionSegment) visit(ctx.expr(0)));
        result.getParameters().add((LiteralExpressionSegment) visit(ctx.expr(1)));
        return result;
    }
    
    @Override
    public ASTNode visitSubstringFunction(final HiveStatementParser.SubstringFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(
                ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), null == ctx.SUBSTR() ? ctx.SUBSTRING().getText() : ctx.SUBSTR().getText(), getOriginalText(ctx));
        result.getParameters().add((ExpressionSegment) visit(ctx.expr()));
        for (TerminalNode each : ctx.NUMBER_()) {
            result.getParameters().add(new LiteralExpressionSegment(each.getSymbol().getStartIndex(), each.getSymbol().getStopIndex(), new NumberLiteralValue(each.getText()).getValue()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitExtractFunction(final HiveStatementParser.ExtractFunctionContext ctx) {
        calculateParameterCount(Collections.singleton(ctx.expr()));
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.EXTRACT().getText(), getOriginalText(ctx));
        result.getParameters().add(new LiteralExpressionSegment(ctx.identifier().getStart().getStartIndex(), ctx.identifier().getStop().getStopIndex(), ctx.identifier().getText()));
        result.getParameters().add((LiteralExpressionSegment) visit(ctx.expr()));
        return result;
    }
    
    @Override
    public ASTNode visitCharFunction(final HiveStatementParser.CharFunctionContext ctx) {
        calculateParameterCount(ctx.expr());
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.CHAR().getText(), getOriginalText(ctx));
        for (HiveStatementParser.ExprContext each : ctx.expr()) {
            ASTNode expr = visit(each);
            result.getParameters().add((ExpressionSegment) expr);
        }
        return result;
    }
    
    @Override
    public ASTNode visitTrimFunction(final HiveStatementParser.TrimFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.TRIM().getText(), getOriginalText(ctx));
        if (null != ctx.BOTH()) {
            result.getParameters().add(new LiteralExpressionSegment(ctx.BOTH().getSymbol().getStartIndex(), ctx.BOTH().getSymbol().getStopIndex(),
                    new OtherLiteralValue(ctx.BOTH().getSymbol().getText()).getValue()));
        }
        if (null != ctx.TRAILING()) {
            result.getParameters().add(new LiteralExpressionSegment(ctx.TRAILING().getSymbol().getStartIndex(), ctx.TRAILING().getSymbol().getStopIndex(),
                    new OtherLiteralValue(ctx.TRAILING().getSymbol().getText()).getValue()));
        }
        if (null != ctx.LEADING()) {
            result.getParameters().add(new LiteralExpressionSegment(ctx.LEADING().getSymbol().getStartIndex(), ctx.LEADING().getSymbol().getStopIndex(),
                    new OtherLiteralValue(ctx.LEADING().getSymbol().getText()).getValue()));
        }
        for (HiveStatementParser.ExprContext each : ctx.expr()) {
            result.getParameters().add((ExpressionSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitWeightStringFunction(final HiveStatementParser.WeightStringFunctionContext ctx) {
        calculateParameterCount(Collections.singleton(ctx.expr()));
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.WEIGHT_STRING().getText(), getOriginalText(ctx));
        result.getParameters().add((ExpressionSegment) visit(ctx.expr()));
        return result;
    }
    
    @Override
    public ASTNode visitValuesFunction(final HiveStatementParser.ValuesFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.VALUES().getText(), getOriginalText(ctx));
        if (!ctx.columnRefList().columnRef().isEmpty()) {
            ColumnSegment columnSegment = (ColumnSegment) visit(ctx.columnRefList().columnRef(0));
            result.getParameters().add(columnSegment);
        }
        return result;
    }
    
    @Override
    public ASTNode visitCurrentUserFunction(final HiveStatementParser.CurrentUserFunctionContext ctx) {
        return new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.CURRENT_USER().getText(), getOriginalText(ctx));
    }
    
    @Override
    public ASTNode visitRegularFunction(final HiveStatementParser.RegularFunctionContext ctx) {
        return null == ctx.completeRegularFunction() ? visit(ctx.shorthandRegularFunction()) : visit(ctx.completeRegularFunction());
    }
    
    @Override
    public ASTNode visitCompleteRegularFunction(final HiveStatementParser.CompleteRegularFunctionContext ctx) {
        FunctionSegment result = new FunctionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.regularFunctionName().getText(), getOriginalText(ctx));
        Collection<ExpressionSegment> expressionSegments = ctx.expr().stream().map(each -> (ExpressionSegment) visit(each)).collect(Collectors.toList());
        result.getParameters().addAll(expressionSegments);
        return result;
    }
    
    @Override
    public ASTNode visitShorthandRegularFunction(final HiveStatementParser.ShorthandRegularFunctionContext ctx) {
        String text = getOriginalText(ctx);
        FunctionSegment result;
        if (null != ctx.CURRENT_TIME()) {
            result = new FunctionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.CURRENT_TIME().getText(), text);
            if (null != ctx.NUMBER_()) {
                result.getParameters().add(new LiteralExpressionSegment(ctx.NUMBER_().getSymbol().getStartIndex(), ctx.NUMBER_().getSymbol().getStopIndex(),
                        new NumberLiteralValue(ctx.NUMBER_().getText())));
            }
        } else {
            result = new FunctionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.getText(), text);
        }
        return result;
    }
    
    @Override
    public ASTNode visitCaseExpression(final HiveStatementParser.CaseExpressionContext ctx) {
        Collection<ExpressionSegment> whenExprs = new LinkedList<>();
        Collection<ExpressionSegment> thenExprs = new LinkedList<>();
        for (HiveStatementParser.CaseWhenContext each : ctx.caseWhen()) {
            whenExprs.add((ExpressionSegment) visit(each.expr(0)));
            thenExprs.add((ExpressionSegment) visit(each.expr(1)));
        }
        ExpressionSegment caseExpr = null == ctx.simpleExpr() ? null : (ExpressionSegment) visit(ctx.simpleExpr());
        ExpressionSegment elseExpr = null == ctx.caseElse() ? null : (ExpressionSegment) visit(ctx.caseElse().expr());
        return new CaseWhenExpression(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), caseExpr, whenExprs, thenExprs, elseExpr);
    }
    
    @Override
    public ASTNode visitVariable(final HiveStatementParser.VariableContext ctx) {
        return null == ctx.systemVariable() ? visit(ctx.userVariable()) : visit(ctx.systemVariable());
    }
    
    @Override
    public ASTNode visitUserVariable(final HiveStatementParser.UserVariableContext ctx) {
        return new VariableSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.textOrIdentifier().getText());
    }
    
    @Override
    public ASTNode visitSystemVariable(final HiveStatementParser.SystemVariableContext ctx) {
        VariableSegment result = new VariableSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.rvalueSystemVariable().getText());
        if (null != ctx.systemVariableScope) {
            result.setScope(ctx.systemVariableScope.getText());
        }
        return result;
    }
    
    @Override
    public ASTNode visitMatchExpression(final HiveStatementParser.MatchExpressionContext ctx) {
        visit(ctx.expr());
        String text = ctx.start.getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
        return new CommonExpressionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), text);
    }
    
    // TODO :FIXME, sql case id: insert_with_str_to_date
    private void calculateParameterCount(final Collection<HiveStatementParser.ExprContext> exprContexts) {
        for (HiveStatementParser.ExprContext each : exprContexts) {
            visit(each);
        }
    }
    
    @Override
    public ASTNode visitDataType(final HiveStatementParser.DataTypeContext ctx) {
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
    public ASTNode visitFieldLength(final HiveStatementParser.FieldLengthContext ctx) {
        DataTypeLengthSegment result = new DataTypeLengthSegment();
        result.setStartIndex(ctx.start.getStartIndex());
        result.setStopIndex(ctx.stop.getStartIndex());
        result.setPrecision(new BigDecimal(ctx.length.getText()).intValue());
        return result;
    }
    
    @Override
    public ASTNode visitPrecision(final HiveStatementParser.PrecisionContext ctx) {
        DataTypeLengthSegment result = new DataTypeLengthSegment();
        result.setStartIndex(ctx.start.getStartIndex());
        result.setStopIndex(ctx.stop.getStartIndex());
        List<TerminalNode> numbers = ctx.NUMBER_();
        result.setPrecision(Integer.parseInt(numbers.get(0).getText()));
        result.setScale(Integer.parseInt(numbers.get(1).getText()));
        return result;
    }
    
    @Override
    public ASTNode visitTypeDatetimePrecision(final HiveStatementParser.TypeDatetimePrecisionContext ctx) {
        DataTypeLengthSegment result = new DataTypeLengthSegment();
        result.setStartIndex(ctx.start.getStartIndex());
        result.setStopIndex(ctx.stop.getStartIndex());
        result.setPrecision(Integer.parseInt(ctx.NUMBER_().getText()));
        return result;
    }
    
    @Override
    public ASTNode visitOrderByClause(final HiveStatementParser.OrderByClauseContext ctx) {
        Collection<OrderByItemSegment> items = new LinkedList<>();
        for (HiveStatementParser.OrderByItemContext each : ctx.orderByItem()) {
            items.add((OrderByItemSegment) visit(each));
        }
        return new OrderBySegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), items);
    }
    
    @Override
    public ASTNode visitOrderByItem(final HiveStatementParser.OrderByItemContext ctx) {
        OrderDirection orderDirection;
        if (null != ctx.direction()) {
            orderDirection = null == ctx.direction().DESC() ? OrderDirection.ASC : OrderDirection.DESC;
        } else {
            orderDirection = OrderDirection.ASC;
        }
        if (null != ctx.numberLiterals()) {
            return new IndexOrderByItemSegment(ctx.numberLiterals().getStart().getStartIndex(), ctx.numberLiterals().getStop().getStopIndex(),
                    SQLUtils.getExactlyNumber(ctx.numberLiterals().getText(), 10).intValue(), orderDirection, null);
        } else {
            ASTNode expr = visitExpr(ctx.expr());
            if (expr instanceof ColumnSegment) {
                return new ColumnOrderByItemSegment((ColumnSegment) expr, orderDirection, null);
            } else {
                return new ExpressionOrderByItemSegment(ctx.expr().getStart().getStartIndex(),
                        ctx.expr().getStop().getStopIndex(), getOriginalText(ctx.expr()), orderDirection, null, (ExpressionSegment) expr);
            }
        }
    }
    
    @Override
    public ASTNode visitInsert(final HiveStatementParser.InsertContext ctx) {
        // TODO :FIXME, since there is no segment for insertValuesClause, InsertStatement is created by sub rule.
        HiveInsertStatement result;
        if (null != ctx.insertValuesClause()) {
            result = (HiveInsertStatement) visit(ctx.insertValuesClause());
        } else if (null != ctx.insertSelectClause()) {
            result = (HiveInsertStatement) visit(ctx.insertSelectClause());
        } else {
            result = new HiveInsertStatement();
            result.setSetAssignment((SetAssignmentSegment) visit(ctx.setAssignmentsClause()));
        }
        if (null != ctx.onDuplicateKeyClause()) {
            result.setOnDuplicateKeyColumns((OnDuplicateKeyColumnsSegment) visit(ctx.onDuplicateKeyClause()));
        }
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        result.addParameterMarkerSegments(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitInsertSelectClause(final HiveStatementParser.InsertSelectClauseContext ctx) {
        HiveInsertStatement result = new HiveInsertStatement();
        if (null != ctx.LP_()) {
            if (null != ctx.fields()) {
                result.setInsertColumns(new InsertColumnsSegment(ctx.LP_().getSymbol().getStartIndex(), ctx.RP_().getSymbol().getStopIndex(), createInsertColumns(ctx.fields())));
            } else {
                result.setInsertColumns(new InsertColumnsSegment(ctx.LP_().getSymbol().getStartIndex(), ctx.RP_().getSymbol().getStopIndex(), Collections.emptyList()));
            }
        } else {
            result.setInsertColumns(new InsertColumnsSegment(ctx.start.getStartIndex() - 1, ctx.start.getStartIndex() - 1, Collections.emptyList()));
        }
        result.setInsertSelect(createInsertSelectSegment(ctx));
        return result;
    }
    
    private SubquerySegment createInsertSelectSegment(final HiveStatementParser.InsertSelectClauseContext ctx) {
        HiveSelectStatement selectStatement = (HiveSelectStatement) visit(ctx.select());
        return new SubquerySegment(ctx.select().start.getStartIndex(), ctx.select().stop.getStopIndex(), selectStatement, getOriginalText(ctx.select()));
    }
    
    @Override
    public ASTNode visitInsertValuesClause(final HiveStatementParser.InsertValuesClauseContext ctx) {
        HiveInsertStatement result = new HiveInsertStatement();
        if (null != ctx.LP_()) {
            if (null != ctx.fields()) {
                result.setInsertColumns(new InsertColumnsSegment(ctx.LP_().getSymbol().getStartIndex(), ctx.RP_().getSymbol().getStopIndex(), createInsertColumns(ctx.fields())));
            } else {
                result.setInsertColumns(new InsertColumnsSegment(ctx.LP_().getSymbol().getStartIndex(), ctx.RP_().getSymbol().getStopIndex(), Collections.emptyList()));
            }
        } else {
            result.setInsertColumns(new InsertColumnsSegment(ctx.start.getStartIndex() - 1, ctx.start.getStartIndex() - 1, Collections.emptyList()));
        }
        result.getValues().addAll(createInsertValuesSegments(ctx.assignmentValues()));
        return result;
    }
    
    private Collection<InsertValuesSegment> createInsertValuesSegments(final Collection<HiveStatementParser.AssignmentValuesContext> assignmentValuesContexts) {
        Collection<InsertValuesSegment> result = new LinkedList<>();
        for (HiveStatementParser.AssignmentValuesContext each : assignmentValuesContexts) {
            result.add((InsertValuesSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitOnDuplicateKeyClause(final HiveStatementParser.OnDuplicateKeyClauseContext ctx) {
        Collection<ColumnAssignmentSegment> columns = new LinkedList<>();
        for (HiveStatementParser.AssignmentContext each : ctx.assignment()) {
            columns.add((ColumnAssignmentSegment) visit(each));
        }
        return new OnDuplicateKeyColumnsSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), columns);
    }
    
    private List<ColumnSegment> createInsertColumns(final HiveStatementParser.FieldsContext fields) {
        List<ColumnSegment> result = new LinkedList<>();
        for (HiveStatementParser.InsertIdentifierContext each : fields.insertIdentifier()) {
            result.add((ColumnSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitUpdate(final HiveStatementParser.UpdateContext ctx) {
        HiveUpdateStatement result = new HiveUpdateStatement();
        TableSegment tableSegment = (TableSegment) visit(ctx.tableReferences());
        result.setTable(tableSegment);
        result.setSetAssignment((SetAssignmentSegment) visit(ctx.setAssignmentsClause()));
        if (null != ctx.whereClause()) {
            result.setWhere((WhereSegment) visit(ctx.whereClause()));
        }
        if (null != ctx.orderByClause()) {
            result.setOrderBy((OrderBySegment) visit(ctx.orderByClause()));
        }
        if (null != ctx.limitClause()) {
            result.setLimit((LimitSegment) visit(ctx.limitClause()));
        }
        result.addParameterMarkerSegments(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitSetAssignmentsClause(final HiveStatementParser.SetAssignmentsClauseContext ctx) {
        Collection<ColumnAssignmentSegment> assignments = new LinkedList<>();
        for (HiveStatementParser.AssignmentContext each : ctx.assignment()) {
            assignments.add((ColumnAssignmentSegment) visit(each));
        }
        return new SetAssignmentSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), assignments);
    }
    
    @Override
    public ASTNode visitAssignmentValues(final HiveStatementParser.AssignmentValuesContext ctx) {
        List<ExpressionSegment> segments = new LinkedList<>();
        for (HiveStatementParser.AssignmentValueContext each : ctx.assignmentValue()) {
            segments.add((ExpressionSegment) visit(each));
        }
        return new InsertValuesSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), segments);
    }
    
    @Override
    public ASTNode visitAssignment(final HiveStatementParser.AssignmentContext ctx) {
        ColumnSegment column = (ColumnSegment) visit(ctx.columnRef());
        ExpressionSegment value = (ExpressionSegment) visit(ctx.assignmentValue());
        List<ColumnSegment> columnSegments = new LinkedList<>();
        columnSegments.add(column);
        return new ColumnAssignmentSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), columnSegments, value);
    }
    
    @Override
    public ASTNode visitAssignmentValue(final HiveStatementParser.AssignmentValueContext ctx) {
        HiveStatementParser.ExprContext expr = ctx.expr();
        if (null != expr) {
            ASTNode result = visit(expr);
            if (result instanceof ColumnSegment) {
                return new CommonExpressionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText());
            } else {
                return result;
            }
        }
        return new CommonExpressionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText());
    }
    
    @Override
    public ASTNode visitBlobValue(final HiveStatementParser.BlobValueContext ctx) {
        return new StringLiteralValue(ctx.string_().getText());
    }
    
    @Override
    public ASTNode visitDelete(final HiveStatementParser.DeleteContext ctx) {
        HiveDeleteStatement result = new HiveDeleteStatement();
        if (null != ctx.multipleTablesClause()) {
            result.setTable((TableSegment) visit(ctx.multipleTablesClause()));
        } else {
            result.setTable((TableSegment) visit(ctx.singleTableClause()));
        }
        if (null != ctx.whereClause()) {
            result.setWhere((WhereSegment) visit(ctx.whereClause()));
        }
        if (null != ctx.orderByClause()) {
            result.setOrderBy((OrderBySegment) visit(ctx.orderByClause()));
        }
        if (null != ctx.limitClause()) {
            result.setLimit((LimitSegment) visit(ctx.limitClause()));
        }
        result.addParameterMarkerSegments(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitSingleTableClause(final HiveStatementParser.SingleTableClauseContext ctx) {
        SimpleTableSegment result = (SimpleTableSegment) visit(ctx.tableName());
        if (null != ctx.alias()) {
            result.setAlias((AliasSegment) visit(ctx.alias()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitMultipleTablesClause(final HiveStatementParser.MultipleTablesClauseContext ctx) {
        DeleteMultiTableSegment result = new DeleteMultiTableSegment();
        TableSegment relateTableSource = (TableSegment) visit(ctx.tableReferences());
        result.setRelationTable(relateTableSource);
        result.setActualDeleteTables(generateTablesFromTableAliasRefList(ctx.tableAliasRefList()));
        return result;
    }
    
    private List<SimpleTableSegment> generateTablesFromTableAliasRefList(final HiveStatementParser.TableAliasRefListContext ctx) {
        List<SimpleTableSegment> result = new LinkedList<>();
        for (HiveStatementParser.TableIdentOptWildContext each : ctx.tableIdentOptWild()) {
            result.add((SimpleTableSegment) visit(each.tableName()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitSelect(final HiveStatementParser.SelectContext ctx) {
        // TODO :Unsupported for withClause.
        HiveSelectStatement result;
        if (null != ctx.queryExpression()) {
            result = (HiveSelectStatement) visit(ctx.queryExpression());
            if (null != ctx.lockClauseList()) {
                result.setLock((LockSegment) visit(ctx.lockClauseList()));
            }
        } else if (null != ctx.selectWithInto()) {
            result = (HiveSelectStatement) visit(ctx.selectWithInto());
        } else {
            result = (HiveSelectStatement) visit(ctx.getChild(0));
        }
        result.addParameterMarkerSegments(getParameterMarkerSegments());
        return result;
    }
    
    private boolean isDistinct(final HiveStatementParser.QuerySpecificationContext ctx) {
        for (HiveStatementParser.SelectSpecificationContext each : ctx.selectSpecification()) {
            if (((BooleanLiteralValue) visit(each)).getValue()) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public ASTNode visitSelectSpecification(final HiveStatementParser.SelectSpecificationContext ctx) {
        if (null != ctx.duplicateSpecification()) {
            return visit(ctx.duplicateSpecification());
        }
        return new BooleanLiteralValue(false);
    }
    
    @Override
    public ASTNode visitDuplicateSpecification(final HiveStatementParser.DuplicateSpecificationContext ctx) {
        String text = ctx.getText();
        if ("DISTINCT".equalsIgnoreCase(text) || "DISTINCTROW".equalsIgnoreCase(text)) {
            return new BooleanLiteralValue(true);
        }
        return new BooleanLiteralValue(false);
    }
    
    @Override
    public ASTNode visitProjections(final HiveStatementParser.ProjectionsContext ctx) {
        Collection<ProjectionSegment> projections = new LinkedList<>();
        if (null != ctx.unqualifiedShorthand()) {
            projections.add(new ShorthandProjectionSegment(ctx.unqualifiedShorthand().getStart().getStartIndex(), ctx.unqualifiedShorthand().getStop().getStopIndex()));
        }
        for (HiveStatementParser.ProjectionContext each : ctx.projection()) {
            projections.add((ProjectionSegment) visit(each));
        }
        ProjectionsSegment result = new ProjectionsSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
        result.getProjections().addAll(projections);
        return result;
    }
    
    @Override
    public ASTNode visitProjection(final HiveStatementParser.ProjectionContext ctx) {
        // FIXME :The stop index of project is the stop index of projection, instead of alias.
        if (null != ctx.qualifiedShorthand()) {
            return createShorthandProjection(ctx.qualifiedShorthand());
        }
        AliasSegment alias = null == ctx.alias() ? null : (AliasSegment) visit(ctx.alias());
        ASTNode exprProjection = visit(ctx.expr());
        if (exprProjection instanceof ColumnSegment) {
            ColumnProjectionSegment result = new ColumnProjectionSegment((ColumnSegment) exprProjection);
            result.setAlias(alias);
            return result;
        }
        if (exprProjection instanceof SubquerySegment) {
            SubquerySegment subquerySegment = (SubquerySegment) exprProjection;
            String text = ctx.start.getInputStream().getText(new Interval(subquerySegment.getStartIndex(), subquerySegment.getStopIndex()));
            SubqueryProjectionSegment result = new SubqueryProjectionSegment((SubquerySegment) exprProjection, text);
            result.setAlias(alias);
            return result;
        }
        if (exprProjection instanceof ExistsSubqueryExpression) {
            ExistsSubqueryExpression existsSubqueryExpression = (ExistsSubqueryExpression) exprProjection;
            String text = ctx.start.getInputStream().getText(new Interval(existsSubqueryExpression.getStartIndex(), existsSubqueryExpression.getStopIndex()));
            SubqueryProjectionSegment result = new SubqueryProjectionSegment(((ExistsSubqueryExpression) exprProjection).getSubquery(), text);
            result.setAlias(alias);
            return result;
        }
        return createProjection(ctx, alias, exprProjection);
    }
    
    private ShorthandProjectionSegment createShorthandProjection(final HiveStatementParser.QualifiedShorthandContext shorthand) {
        ShorthandProjectionSegment result = new ShorthandProjectionSegment(shorthand.getStart().getStartIndex(), shorthand.getStop().getStopIndex());
        HiveStatementParser.IdentifierContext identifier = shorthand.identifier().get(shorthand.identifier().size() - 1);
        OwnerSegment owner = new OwnerSegment(identifier.getStart().getStartIndex(), identifier.getStop().getStopIndex(), new IdentifierValue(identifier.getText()));
        result.setOwner(owner);
        if (shorthand.identifier().size() > 1) {
            HiveStatementParser.IdentifierContext schemaIdentifier = shorthand.identifier().get(0);
            owner.setOwner(new OwnerSegment(schemaIdentifier.getStart().getStartIndex(), schemaIdentifier.getStop().getStopIndex(), new IdentifierValue(schemaIdentifier.getText())));
        }
        return result;
    }
    
    @Override
    public ASTNode visitAlias(final HiveStatementParser.AliasContext ctx) {
        return new AliasSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), new IdentifierValue(ctx.textOrIdentifier().getText()));
    }
    
    private ASTNode createProjection(final HiveStatementParser.ProjectionContext ctx, final AliasSegment alias, final ASTNode projection) {
        if (projection instanceof AggregationProjectionSegment) {
            ((AggregationProjectionSegment) projection).setAlias(alias);
            return projection;
        }
        if (projection instanceof ExpressionProjectionSegment) {
            ((ExpressionProjectionSegment) projection).setAlias(alias);
            return projection;
        }
        if (projection instanceof FunctionSegment) {
            FunctionSegment functionSegment = (FunctionSegment) projection;
            ExpressionProjectionSegment result = new ExpressionProjectionSegment(functionSegment.getStartIndex(), functionSegment.getStopIndex(), functionSegment.getText(), functionSegment);
            result.setAlias(alias);
            return result;
        }
        if (projection instanceof CommonExpressionSegment) {
            CommonExpressionSegment segment = (CommonExpressionSegment) projection;
            ExpressionProjectionSegment result = new ExpressionProjectionSegment(segment.getStartIndex(), segment.getStopIndex(), segment.getText(), segment);
            result.setAlias(alias);
            return result;
        }
        // FIXME :For DISTINCT()
        if (projection instanceof ColumnSegment) {
            ExpressionProjectionSegment result = new ExpressionProjectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), getOriginalText(ctx), (ColumnSegment) projection);
            result.setAlias(alias);
            return result;
        }
        if (projection instanceof SubqueryExpressionSegment) {
            SubqueryExpressionSegment subqueryExpressionSegment = (SubqueryExpressionSegment) projection;
            String text = ctx.start.getInputStream().getText(new Interval(subqueryExpressionSegment.getStartIndex(), subqueryExpressionSegment.getStopIndex()));
            SubqueryProjectionSegment result = new SubqueryProjectionSegment(subqueryExpressionSegment.getSubquery(), text);
            result.setAlias(alias);
            return result;
        }
        if (projection instanceof BinaryOperationExpression) {
            int startIndex = ((BinaryOperationExpression) projection).getStartIndex();
            int stopIndex = null == alias ? ((BinaryOperationExpression) projection).getStopIndex() : alias.getStopIndex();
            ExpressionProjectionSegment result = new ExpressionProjectionSegment(startIndex, stopIndex, ((BinaryOperationExpression) projection).getText(), (BinaryOperationExpression) projection);
            result.setAlias(alias);
            return result;
        }
        if (projection instanceof ParameterMarkerExpressionSegment) {
            ParameterMarkerExpressionSegment result = (ParameterMarkerExpressionSegment) projection;
            result.setAlias(alias);
            return projection;
        }
        if (projection instanceof CaseWhenExpression) {
            ExpressionProjectionSegment result = new ExpressionProjectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), getOriginalText(ctx.expr()), (CaseWhenExpression) projection);
            result.setAlias(alias);
            return result;
        }
        if (projection instanceof VariableSegment) {
            ExpressionProjectionSegment result = new ExpressionProjectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), getOriginalText(ctx.expr()), (VariableSegment) projection);
            result.setAlias(alias);
            return result;
        }
        if (projection instanceof BetweenExpression) {
            ExpressionProjectionSegment result = new ExpressionProjectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), getOriginalText(ctx.expr()), (BetweenExpression) projection);
            result.setAlias(alias);
            return result;
        }
        if (projection instanceof InExpression) {
            ExpressionProjectionSegment result = new ExpressionProjectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), getOriginalText(ctx.expr()), (InExpression) projection);
            result.setAlias(alias);
            return result;
        }
        LiteralExpressionSegment column = (LiteralExpressionSegment) projection;
        ExpressionProjectionSegment result = null == alias
                ? new ExpressionProjectionSegment(column.getStartIndex(), column.getStopIndex(), String.valueOf(column.getLiterals()), column)
                : new ExpressionProjectionSegment(column.getStartIndex(), ctx.alias().stop.getStopIndex(), String.valueOf(column.getLiterals()), column);
        result.setAlias(alias);
        return result;
    }
    
    @Override
    public ASTNode visitFromClause(final HiveStatementParser.FromClauseContext ctx) {
        return visit(ctx.tableReferences());
    }
    
    @Override
    public ASTNode visitTableReferences(final HiveStatementParser.TableReferencesContext ctx) {
        TableSegment result = (TableSegment) visit(ctx.tableReference(0));
        if (ctx.tableReference().size() > 1) {
            for (int i = 1; i < ctx.tableReference().size(); i++) {
                result = generateJoinTableSourceFromEscapedTableReference(ctx.tableReference(i), result);
            }
        }
        return result;
    }
    
    private JoinTableSegment generateJoinTableSourceFromEscapedTableReference(final HiveStatementParser.TableReferenceContext ctx, final TableSegment tableSegment) {
        JoinTableSegment result = new JoinTableSegment();
        result.setStartIndex(tableSegment.getStartIndex());
        result.setStopIndex(ctx.stop.getStopIndex());
        result.setLeft(tableSegment);
        result.setJoinType(JoinType.COMMA.name());
        result.setRight((TableSegment) visit(ctx));
        return result;
    }
    
    @Override
    public ASTNode visitEscapedTableReference(final HiveStatementParser.EscapedTableReferenceContext ctx) {
        TableSegment result;
        TableSegment left;
        left = (TableSegment) visit(ctx.tableFactor());
        for (HiveStatementParser.JoinedTableContext each : ctx.joinedTable()) {
            left = visitJoinedTable(each, left);
        }
        result = left;
        return result;
    }
    
    @Override
    public ASTNode visitTableReference(final HiveStatementParser.TableReferenceContext ctx) {
        TableSegment result;
        TableSegment left;
        left = null == ctx.tableFactor() ? (TableSegment) visit(ctx.escapedTableReference()) : (TableSegment) visit(ctx.tableFactor());
        for (HiveStatementParser.JoinedTableContext each : ctx.joinedTable()) {
            left = visitJoinedTable(each, left);
        }
        result = left;
        return result;
    }
    
    @Override
    public ASTNode visitTableFactor(final HiveStatementParser.TableFactorContext ctx) {
        if (null != ctx.subquery()) {
            HiveSelectStatement subquery = (HiveSelectStatement) visit(ctx.subquery());
            SubquerySegment subquerySegment = new SubquerySegment(ctx.subquery().start.getStartIndex(), ctx.subquery().stop.getStopIndex(), subquery, getOriginalText(ctx.subquery()));
            SubqueryTableSegment result = new SubqueryTableSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), subquerySegment);
            if (null != ctx.alias()) {
                result.setAlias((AliasSegment) visit(ctx.alias()));
            }
            return result;
        }
        if (null != ctx.tableName()) {
            SimpleTableSegment result = (SimpleTableSegment) visit(ctx.tableName());
            if (null != ctx.alias()) {
                result.setAlias((AliasSegment) visit(ctx.alias()));
            }
            return result;
        }
        return visit(ctx.tableReferences());
    }
    
    private JoinTableSegment visitJoinedTable(final HiveStatementParser.JoinedTableContext ctx, final TableSegment tableSegment) {
        JoinTableSegment result = new JoinTableSegment();
        result.setLeft(tableSegment);
        result.setStartIndex(tableSegment.getStartIndex());
        result.setStopIndex(ctx.stop.getStopIndex());
        result.setJoinType(getJoinType(ctx));
        result.setNatural(null != ctx.naturalJoinType());
        TableSegment right = null == ctx.tableFactor() ? (TableSegment) visit(ctx.tableReference()) : (TableSegment) visit(ctx.tableFactor());
        result.setRight(right);
        return null == ctx.joinSpecification() ? result : visitJoinSpecification(ctx.joinSpecification(), result);
    }
    
    private String getJoinType(final HiveStatementParser.JoinedTableContext ctx) {
        if (null != ctx.innerJoinType()) {
            return JoinType.INNER.name();
        }
        if (null != ctx.outerJoinType()) {
            return null == ctx.outerJoinType().LEFT() ? JoinType.RIGHT.name() : JoinType.LEFT.name();
        }
        if (null != ctx.naturalJoinType()) {
            return getNaturalJoinType(ctx.naturalJoinType());
        }
        return JoinType.COMMA.name();
    }
    
    private String getNaturalJoinType(final HiveStatementParser.NaturalJoinTypeContext ctx) {
        if (null != ctx.LEFT()) {
            return JoinType.LEFT.name();
        } else if (null != ctx.RIGHT()) {
            return JoinType.RIGHT.name();
        } else {
            return JoinType.INNER.name();
        }
    }
    
    private JoinTableSegment visitJoinSpecification(final HiveStatementParser.JoinSpecificationContext ctx, final JoinTableSegment result) {
        if (null != ctx.expr()) {
            ExpressionSegment condition = (ExpressionSegment) visit(ctx.expr());
            result.setCondition(condition);
        }
        if (null != ctx.USING()) {
            result.setUsing(ctx.columnNames().columnName().stream().map(each -> (ColumnSegment) visit(each)).collect(Collectors.toList()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitWhereClause(final HiveStatementParser.WhereClauseContext ctx) {
        ASTNode segment = visit(ctx.expr());
        return new WhereSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (ExpressionSegment) segment);
    }
    
    @Override
    public ASTNode visitGroupByClause(final HiveStatementParser.GroupByClauseContext ctx) {
        Collection<OrderByItemSegment> items = new LinkedList<>();
        for (HiveStatementParser.OrderByItemContext each : ctx.orderByItem()) {
            items.add((OrderByItemSegment) visit(each));
        }
        return new GroupBySegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), items);
    }
    
    @Override
    public ASTNode visitLimitClause(final HiveStatementParser.LimitClauseContext ctx) {
        if (null == ctx.limitOffset()) {
            return new LimitSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), null, (PaginationValueSegment) visit(ctx.limitRowCount()));
        }
        PaginationValueSegment rowCount;
        PaginationValueSegment offset;
        if (null != ctx.OFFSET()) {
            rowCount = (PaginationValueSegment) visit(ctx.limitRowCount());
            offset = (PaginationValueSegment) visit(ctx.limitOffset());
        } else {
            offset = (PaginationValueSegment) visit(ctx.limitOffset());
            rowCount = (PaginationValueSegment) visit(ctx.limitRowCount());
        }
        return new LimitSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), offset, rowCount);
    }
    
    @Override
    public ASTNode visitLimitRowCount(final HiveStatementParser.LimitRowCountContext ctx) {
        if (null != ctx.numberLiterals()) {
            return new NumberLiteralLimitValueSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ((NumberLiteralValue) visit(ctx.numberLiterals())).getValue().longValue());
        }
        ParameterMarkerSegment result = new ParameterMarkerLimitValueSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(),
                ((ParameterMarkerValue) visit(ctx.parameterMarker())).getValue());
        getParameterMarkerSegments().add(result);
        return result;
    }
    
    @Override
    public ASTNode visitConstraintName(final HiveStatementParser.ConstraintNameContext ctx) {
        return new ConstraintSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (IdentifierValue) visit(ctx.identifier()));
    }
    
    @Override
    public ASTNode visitLimitOffset(final HiveStatementParser.LimitOffsetContext ctx) {
        if (null != ctx.numberLiterals()) {
            return new NumberLiteralLimitValueSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ((NumberLiteralValue) visit(ctx.numberLiterals())).getValue().longValue());
        }
        ParameterMarkerSegment result = new ParameterMarkerLimitValueSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(),
                ((ParameterMarkerValue) visit(ctx.parameterMarker())).getValue());
        getParameterMarkerSegments().add(result);
        return result;
    }
    
    @Override
    public ASTNode visitCollateClause(final HiveStatementParser.CollateClauseContext ctx) {
        if (null != ctx.collationName()) {
            return new LiteralExpressionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.collationName().textOrIdentifier().getText());
        }
        ParameterMarkerExpressionSegment result = new ParameterMarkerExpressionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                ((ParameterMarkerValue) visit(ctx.parameterMarker())).getValue());
        getParameterMarkerSegments().add(result);
        return result;
    }
}
