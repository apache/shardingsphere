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

package org.apache.shardingsphere.core.parsing.antlr.extractor.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.constant.ShardingOperator;
import org.apache.shardingsphere.core.parsing.antlr.extractor.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.core.parsing.antlr.extractor.impl.expression.ExpressionExtractor;
import org.apache.shardingsphere.core.parsing.antlr.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parsing.antlr.extractor.util.LogicalOperator;
import org.apache.shardingsphere.core.parsing.antlr.extractor.util.Paren;
import org.apache.shardingsphere.core.parsing.antlr.extractor.util.RuleName;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.column.ColumnSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.condition.AndConditionSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.condition.ConditionSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.condition.OrConditionSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.condition.PredicateSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.expr.BetweenValueExpressionSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.expr.EqualsValueExpressionSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.expr.InValueExpressionSegment;
import org.apache.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parsing.lexer.token.Symbol;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Predicate extractor.
 *
 * @author duhongjun
 */
@RequiredArgsConstructor
public final class PredicateExtractor implements OptionalSQLSegmentExtractor {
    
    private ExpressionExtractor expressionExtractor = new ExpressionExtractor();
    
    @Override
    public Optional<PredicateSegment> extract(final ParserRuleContext ancestorNode) {
        throw new RuntimeException();
    }
    
    /**
     * Extract SQL segment from SQL AST.
     *
     * @param placeholderIndexes question node map
     * @param exprNode expression node of AST
     * @return or condition
     */
    public Optional<OrConditionSegment> extractCondition(final Map<ParserRuleContext, Integer> placeholderIndexes, final ParserRuleContext exprNode) {
        return extractConditionInternal(placeholderIndexes, exprNode);
    }
    
    private Optional<OrConditionSegment> extractConditionInternal(final Map<ParserRuleContext, Integer> placeholderIndexes, final ParserRuleContext exprNode) {
        int index = -1;
        for (int i = 0; i < exprNode.getChildCount(); i++) {
            if (LogicalOperator.isLogicalOperator(exprNode.getChild(i).getText())) {
                index = i;
                break;
            }
        }
        if (index > 0) {
            Optional<OrConditionSegment> leftOrCondition = extractConditionInternal(placeholderIndexes, (ParserRuleContext) exprNode.getChild(index - 1));
            Optional<OrConditionSegment> rightOrCondition = extractConditionInternal(placeholderIndexes, (ParserRuleContext) exprNode.getChild(index + 1));
            if (leftOrCondition.isPresent() && rightOrCondition.isPresent()) {
                return Optional.of(mergeCondition(placeholderIndexes, leftOrCondition.get(), rightOrCondition.get(), exprNode.getChild(index).getText()));
            }
            return leftOrCondition.isPresent() ? leftOrCondition : rightOrCondition;
        }
        return extractConditionForParen(placeholderIndexes, exprNode);
    }
    
    private OrConditionSegment mergeCondition(final Map<ParserRuleContext, Integer> placeholderIndexes, final OrConditionSegment leftOrCondition,
                                              final OrConditionSegment rightOrCondition, final String operator) {
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
    
    private Optional<OrConditionSegment> extractConditionForParen(final Map<ParserRuleContext, Integer> placeholderIndexes, final ParserRuleContext exprNode) {
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
                return extractConditionInternal(placeholderIndexes, (ParserRuleContext) exprNode.getChild(index + 1));
            }
            return Optional.absent();
        }
        Optional<ConditionSegment> condition = buildCondition(placeholderIndexes, exprNode);
        if (!condition.isPresent()) {
            return Optional.absent();
        }
        OrConditionSegment result = new OrConditionSegment();
        AndConditionSegment newAndCondition = new AndConditionSegment();
        newAndCondition.getConditions().add(condition.get());
        result.getAndConditions().add(newAndCondition);
        return Optional.of(result);
    }
    
    private Optional<ConditionSegment> buildCondition(final Map<ParserRuleContext, Integer> placeholderIndexes, final ParserRuleContext exprNode) {
        Optional<ConditionSegment> result = buildEqualCondition(placeholderIndexes, exprNode);
        if (result.isPresent()) {
            return result;
        }
        return buildPredicateCondition(placeholderIndexes, exprNode);
    }
    
    private Optional<ConditionSegment> buildEqualCondition(final Map<ParserRuleContext, Integer> placeholderIndexes, final ParserRuleContext exprNode) {
        Optional<ParserRuleContext> comparisionNode = ExtractorUtils.findFirstChildNode(exprNode, RuleName.COMPARISON_OPERATOR);
        if (!comparisionNode.isPresent() || !isValidEqualCondition(comparisionNode.get())) {
            return Optional.absent();
        }
        Optional<ParserRuleContext> leftNode = ExtractorUtils.findFirstChildNode((ParserRuleContext) comparisionNode.get().parent.getChild(0), RuleName.COLUMN_NAME);
        Optional<ParserRuleContext> rightNode = ExtractorUtils.findFirstChildNode((ParserRuleContext) comparisionNode.get().parent.getChild(2), RuleName.COLUMN_NAME);
        if (!leftNode.isPresent() && !rightNode.isPresent()) {
            return Optional.absent();
        }
        if (leftNode.isPresent() && rightNode.isPresent()) {
            Optional<ColumnSegment> leftColumn = buildColumn(leftNode.get());
            Optional<ColumnSegment> rightColumn = buildColumn(rightNode.get());
            Preconditions.checkState(leftColumn.isPresent() && rightColumn.isPresent());
            return Optional.of(new ConditionSegment(leftColumn.get(), ShardingOperator.EQUAL, rightColumn.get()));
        }
        Optional<ColumnSegment> column = buildColumn(exprNode);
        Preconditions.checkState(column.isPresent());
        ParserRuleContext valueNode = leftNode.isPresent() ? (ParserRuleContext) comparisionNode.get().parent.getChild(2) : (ParserRuleContext) comparisionNode.get().parent.getChild(0);
        Optional<? extends ExpressionSegment> sqlExpression = expressionExtractor.extract(placeholderIndexes, valueNode);
        return sqlExpression.isPresent()
                ? Optional.of(new ConditionSegment(column.get(), ShardingOperator.EQUAL, new EqualsValueExpressionSegment(sqlExpression.get()))) : Optional.<ConditionSegment>absent();
    }
    
    private boolean isValidEqualCondition(final ParserRuleContext comparisionNode) {
        return Symbol.EQ.getLiterals().equalsIgnoreCase(comparisionNode.getText()) && 3 == comparisionNode.getParent().getChildCount();
    }
    
    private Optional<ConditionSegment> buildPredicateCondition(final Map<ParserRuleContext, Integer> placeholderIndexes, final ParserRuleContext exprNode) {
        Optional<ParserRuleContext> predicateNode = ExtractorUtils.findFirstChildNode(exprNode, RuleName.PREDICATE);
        if (!predicateNode.isPresent()) {
            return Optional.absent();
        }
        if (1 != predicateNode.get().getParent().getChildCount()) {
            return Optional.absent();
        }
        if (5 == predicateNode.get().getChildCount() && DefaultKeyword.BETWEEN.name().equalsIgnoreCase(predicateNode.get().getChild(1).getText())) {
            Optional<ConditionSegment> result = buildBetweenCondition(placeholderIndexes, predicateNode.get());
            if (result.isPresent()) {
                return result;
            }
        }
        if (5 <= predicateNode.get().getChildCount() && DefaultKeyword.IN.name().equalsIgnoreCase(predicateNode.get().getChild(1).getText())) {
            Optional<ConditionSegment> result = buildInCondition(placeholderIndexes, predicateNode.get());
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.absent();
    }
    
    private Optional<ConditionSegment> buildBetweenCondition(final Map<ParserRuleContext, Integer> placeholderIndexes, final ParserRuleContext predicateNode) {
        Optional<ColumnSegment> column = buildColumn((ParserRuleContext) predicateNode.getChild(0));
        if (!column.isPresent()) {
            return Optional.absent();
        }
        Optional<? extends ExpressionSegment> beginSQLExpression = expressionExtractor.extract(placeholderIndexes, (ParserRuleContext) predicateNode.getChild(2));
        Optional<? extends ExpressionSegment> endSQLExpression = expressionExtractor.extract(placeholderIndexes, (ParserRuleContext) predicateNode.getChild(4));
        if (beginSQLExpression.isPresent() && endSQLExpression.isPresent()) {
            return Optional.of(new ConditionSegment(column.get(), ShardingOperator.BETWEEN, new BetweenValueExpressionSegment(beginSQLExpression.get(), endSQLExpression.get())));
        }
        return Optional.absent();
    }
    
    private Optional<ConditionSegment> buildInCondition(final Map<ParserRuleContext, Integer> placeholderIndexes, final ParserRuleContext predicateNode) {
        Optional<ColumnSegment> column = buildColumn((ParserRuleContext) predicateNode.getChild(0));
        if (!column.isPresent()) {
            return Optional.absent();
        }
        List<ExpressionSegment> sqlExpressions = new LinkedList<>();
        for (int i = 3; i < predicateNode.getChildCount(); i++) {
            if (RuleName.SIMPLE_EXPR.getName().equals(predicateNode.getChild(i).getClass().getSimpleName())) {
                Optional<? extends ExpressionSegment> expression = expressionExtractor.extract(placeholderIndexes, (ParserRuleContext) predicateNode.getChild(i));
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
            return Optional.of(new ConditionSegment(column.get(), ShardingOperator.IN, inExpressionSegment));
        }
        return Optional.absent();
    }
    
    private Optional<ColumnSegment> buildColumn(final ParserRuleContext parentNode) {
        return new ColumnSegmentExtractor().extract(parentNode);
    }
}
