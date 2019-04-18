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

package org.apache.shardingsphere.core.parse.antlr.extractor.impl.dml;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.constant.ShardingOperator;
import org.apache.shardingsphere.core.parse.antlr.constant.LogicalOperator;
import org.apache.shardingsphere.core.parse.antlr.constant.Paren;
import org.apache.shardingsphere.core.parse.antlr.extractor.impl.common.column.ColumnExtractor;
import org.apache.shardingsphere.core.parse.antlr.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parse.antlr.extractor.util.RuleName;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.condition.AndConditionSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.condition.ConditionSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.condition.OrConditionSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.expr.BetweenValueExpressionSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.expr.CompareValueExpressionSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.expr.InValueExpressionSegment;
import org.apache.shardingsphere.core.parse.old.lexer.token.DefaultKeyword;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Predicate extractor.
 *
 * @author duhongjun
 */
public final class PredicateExtractor {
    
    private final ExpressionExtractor expressionExtractor = new ExpressionExtractor();
    
    /**
     * Extract.
     *
     * @param parameterMarkerIndexes parameter marker indexes
     * @param exprNode expression node of AST
     * @return or condition
     */
    public Optional<OrConditionSegment> extract(final Map<ParserRuleContext, Integer> parameterMarkerIndexes, final ParserRuleContext exprNode) {
        return extractConditionInternal(parameterMarkerIndexes, exprNode);
    }
    
    private Optional<OrConditionSegment> extractConditionInternal(final Map<ParserRuleContext, Integer> parameterMarkerIndexes, final ParserRuleContext exprNode) {
        int index = -1;
        for (int i = 0; i < exprNode.getChildCount(); i++) {
            if (LogicalOperator.isLogicalOperator(exprNode.getChild(i).getText())) {
                index = i;
                break;
            }
        }
        if (index > 0) {
            Optional<OrConditionSegment> leftOrCondition = extractConditionInternal(parameterMarkerIndexes, (ParserRuleContext) exprNode.getChild(index - 1));
            Optional<OrConditionSegment> rightOrCondition = extractConditionInternal(parameterMarkerIndexes, (ParserRuleContext) exprNode.getChild(index + 1));
            if (leftOrCondition.isPresent() && rightOrCondition.isPresent()) {
                return Optional.of(mergeCondition(leftOrCondition.get(), rightOrCondition.get(), exprNode.getChild(index).getText()));
            }
            return leftOrCondition.isPresent() ? leftOrCondition : rightOrCondition;
        }
        return extractConditionForParen(parameterMarkerIndexes, exprNode);
    }
    
    private OrConditionSegment mergeCondition(final OrConditionSegment leftOrCondition, final OrConditionSegment rightOrCondition, final String operator) {
        if (LogicalOperator.isOrOperator(operator)) {
            leftOrCondition.getAndConditions().addAll(rightOrCondition.getAndConditions());
            return leftOrCondition;
        }
        OrConditionSegment result = new OrConditionSegment();
        for (AndConditionSegment each : leftOrCondition.getAndConditions()) {
            for (AndConditionSegment eachRightOr : rightOrCondition.getAndConditions()) {
                AndConditionSegment tempList = new AndConditionSegment();
                tempList.getConditions().addAll(each.getConditions());
                tempList.getConditions().addAll(eachRightOr.getConditions());
                result.getAndConditions().add(tempList);
            }
        }
        return result;
    }
    
    private Optional<OrConditionSegment> extractConditionForParen(final Map<ParserRuleContext, Integer> parameterMarkerIndexes, final ParserRuleContext exprNode) {
        int index = -1;
        for (int i = 0; i < exprNode.getChildCount(); i++) {
            if (Paren.isLeftParen(exprNode.getChild(i).getText())) {
                index = i;
                break;
            }
        }
        if (-1 != index) {
            Preconditions.checkState(Paren.match(exprNode.getChild(index).getText(), exprNode.getChild(index + 2).getText()), "Missing right paren.");
            if (RuleName.EXPR.getName().equals(exprNode.getChild(index + 1).getClass().getSimpleName())) {
                return extractConditionInternal(parameterMarkerIndexes, (ParserRuleContext) exprNode.getChild(index + 1));
            }
            return Optional.absent();
        }
        Optional<ConditionSegment> condition = buildCondition(parameterMarkerIndexes, exprNode);
        if (!condition.isPresent()) {
            return Optional.absent();
        }
        OrConditionSegment result = new OrConditionSegment();
        AndConditionSegment newAndCondition = new AndConditionSegment();
        newAndCondition.getConditions().add(condition.get());
        result.getAndConditions().add(newAndCondition);
        return Optional.of(result);
    }
    
    private Optional<ConditionSegment> buildCondition(final Map<ParserRuleContext, Integer> parameterMarkerIndexes, final ParserRuleContext exprNode) {
        Optional<ConditionSegment> result = buildCompareCondition(parameterMarkerIndexes, exprNode);
        if (result.isPresent()) {
            return result;
        }
        Optional<ParserRuleContext> predicateNode = ExtractorUtils.findFirstChildNode(exprNode, RuleName.PREDICATE);
        if (!predicateNode.isPresent()) {
            return Optional.absent();
        }
        if (1 != predicateNode.get().getParent().getChildCount()) {
            return Optional.absent();
        }
        if (5 == predicateNode.get().getChildCount() && DefaultKeyword.BETWEEN.name().equalsIgnoreCase(predicateNode.get().getChild(1).getText())) {
            result = buildBetweenCondition(parameterMarkerIndexes, predicateNode.get());
            if (result.isPresent()) {
                return result;
            }
        }
        if (5 <= predicateNode.get().getChildCount() && DefaultKeyword.IN.name().equalsIgnoreCase(predicateNode.get().getChild(1).getText())) {
            result = buildInCondition(parameterMarkerIndexes, predicateNode.get());
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.absent();
    }
    
    private Optional<ConditionSegment> buildCompareCondition(final Map<ParserRuleContext, Integer> parameterMarkerIndexes, final ParserRuleContext exprNode) {
        Optional<ParserRuleContext> comparisionNode = ExtractorUtils.findFirstChildNode(exprNode, RuleName.COMPARISON_OPERATOR);
        if (!comparisionNode.isPresent()) {
            return Optional.absent();
        }
        ParserRuleContext predicateNode = comparisionNode.get().getParent();
        Optional<ParserRuleContext> leftNode = ExtractorUtils.findFirstChildNode((ParserRuleContext) predicateNode.getChild(0), RuleName.COLUMN_NAME);
        Optional<ParserRuleContext> rightNode = ExtractorUtils.findFirstChildNode((ParserRuleContext) predicateNode.getChild(2), RuleName.COLUMN_NAME);
        if (!leftNode.isPresent() && !rightNode.isPresent()) {
            return Optional.absent();
        }
        if (leftNode.isPresent() && rightNode.isPresent()) {
            Optional<ColumnSegment> leftColumn = buildColumn(leftNode.get(), parameterMarkerIndexes);
            Optional<ColumnSegment> rightColumn = buildColumn(rightNode.get(), parameterMarkerIndexes);
            Preconditions.checkState(leftColumn.isPresent() && rightColumn.isPresent());
            return Optional.of(new ConditionSegment(leftColumn.get(), comparisionNode.get().getText(), rightColumn.get(), predicateNode.getStop().getStopIndex()));
        }
        Optional<ColumnSegment> column = buildColumn(exprNode, parameterMarkerIndexes);
        Preconditions.checkState(column.isPresent());
        ParserRuleContext valueNode = leftNode.isPresent() ? (ParserRuleContext) comparisionNode.get().parent.getChild(2) : (ParserRuleContext) comparisionNode.get().parent.getChild(0);
        Optional<? extends ExpressionSegment> sqlExpression = expressionExtractor.extract(parameterMarkerIndexes, valueNode);
        return sqlExpression.isPresent() ? Optional.of(new ConditionSegment(column.get(), comparisionNode.get().getText(), 
                new CompareValueExpressionSegment(sqlExpression.get(), comparisionNode.get().getText()), predicateNode.getStop().getStopIndex())) : Optional.<ConditionSegment>absent();
    }
    
    private Optional<ConditionSegment> buildBetweenCondition(final Map<ParserRuleContext, Integer> parameterMarkerIndexes, final ParserRuleContext predicateNode) {
        Optional<ColumnSegment> column = buildColumn((ParserRuleContext) predicateNode.getChild(0), parameterMarkerIndexes);
        if (!column.isPresent()) {
            return Optional.absent();
        }
        Optional<? extends ExpressionSegment> beginSQLExpression = expressionExtractor.extract(parameterMarkerIndexes, (ParserRuleContext) predicateNode.getChild(2));
        Optional<? extends ExpressionSegment> endSQLExpression = expressionExtractor.extract(parameterMarkerIndexes, (ParserRuleContext) predicateNode.getChild(4));
        if (beginSQLExpression.isPresent() && endSQLExpression.isPresent()) {
            return Optional.of(new ConditionSegment(column.get(), ShardingOperator.BETWEEN.name(), 
                    new BetweenValueExpressionSegment(beginSQLExpression.get(), endSQLExpression.get()), predicateNode.getStop().getStopIndex()));
        }
        return Optional.absent();
    }
    
    private Optional<ConditionSegment> buildInCondition(final Map<ParserRuleContext, Integer> parameterMarkerIndexes, final ParserRuleContext predicateNode) {
        Optional<ColumnSegment> column = buildColumn((ParserRuleContext) predicateNode.getChild(0), parameterMarkerIndexes);
        if (!column.isPresent()) {
            return Optional.absent();
        }
        List<ExpressionSegment> sqlExpressions = new LinkedList<>();
        for (int i = 3; i < predicateNode.getChildCount(); i++) {
            if (RuleName.EXPR.getName().equals(predicateNode.getChild(i).getClass().getSimpleName())) {
                Optional<? extends ExpressionSegment> expression = expressionExtractor.extract(parameterMarkerIndexes, (ParserRuleContext) predicateNode.getChild(i));
                if (!expression.isPresent()) {
                    sqlExpressions.clear();
                    break;
                }
                sqlExpressions.add(expression.get());
            }
        }
        if (!sqlExpressions.isEmpty()) {
            InValueExpressionSegment inExpressionSegment = new InValueExpressionSegment();
            inExpressionSegment.getSqlExpressions().addAll(sqlExpressions);
            return Optional.of(new ConditionSegment(column.get(), ShardingOperator.IN.name(), inExpressionSegment, predicateNode.getStop().getStopIndex()));
        }
        return Optional.absent();
    }
    
    private Optional<ColumnSegment> buildColumn(final ParserRuleContext parentNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        return new ColumnExtractor().extract(parentNode, parameterMarkerIndexes);
    }
}
