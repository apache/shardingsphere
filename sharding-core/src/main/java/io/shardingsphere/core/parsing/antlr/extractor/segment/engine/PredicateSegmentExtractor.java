/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.antlr.extractor.segment.engine;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.ParserRuleContext;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import io.shardingsphere.core.parsing.antlr.extractor.segment.OptionalSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.constant.LogicalOperator;
import io.shardingsphere.core.parsing.antlr.extractor.segment.constant.Paren;
import io.shardingsphere.core.parsing.antlr.extractor.segment.constant.RuleName;
import io.shardingsphere.core.parsing.antlr.extractor.statement.engine.ColumnSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.util.ASTUtils;
import io.shardingsphere.core.parsing.antlr.sql.segment.ColumnSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.PredicateSegment;
import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import io.shardingsphere.core.parsing.lexer.token.Symbol;
import io.shardingsphere.core.parsing.parser.clause.condition.NullCondition;
import io.shardingsphere.core.parsing.parser.context.condition.AndCondition;
import io.shardingsphere.core.parsing.parser.context.condition.Column;
import io.shardingsphere.core.parsing.parser.context.condition.Condition;
import io.shardingsphere.core.parsing.parser.context.condition.OrCondition;
import io.shardingsphere.core.parsing.parser.expression.SQLExpression;
import io.shardingsphere.core.parsing.parser.expression.SQLNumberExpression;
import io.shardingsphere.core.parsing.parser.expression.SQLPlaceholderExpression;
import io.shardingsphere.core.parsing.parser.expression.SQLTextExpression;
import io.shardingsphere.core.util.NumberUtil;
import lombok.RequiredArgsConstructor;

/**
 * Predicate segment extractor.
 *
 * @author duhongjun
 */
@RequiredArgsConstructor
public final class PredicateSegmentExtractor implements OptionalSQLSegmentExtractor {
    
    private final Map<String, String> tableAlias;
    
    private final ColumnSegmentExtractor columnHandler = new ColumnSegmentExtractor();
    
    @Override
    public Optional<PredicateSegment> extract(final ParserRuleContext ancestorNode) {
        throw new RuntimeException();
    }
    
    /** Extract SQL segment from SQL AST.
     *
     * @param questionNodeIndexMap question node map
     * @param exprNode expression node of AST
     * @return or condition
     */
    public Optional<OrCondition> extractCondition(final Map<ParserRuleContext, Integer> questionNodeIndexMap, final ParserRuleContext exprNode) {
        int index = -1;
        for (int i = 0; i < exprNode.getChildCount(); i++) {
            if (LogicalOperator.isLogicalOperator(exprNode.getChild(i).getText())) {
                index = i;
                break;
            }
        }
        if (index > 0) {
            Optional<OrCondition> leftOrCondition = extractCondition(questionNodeIndexMap, (ParserRuleContext) exprNode.getChild(index - 1));
            Optional<OrCondition> rightOrCondition = extractCondition(questionNodeIndexMap, (ParserRuleContext) exprNode.getChild(index + 1));
            return mergeCondition(leftOrCondition, rightOrCondition, exprNode.getChild(index).getText());
        } else {
            return extractConditionForParen(questionNodeIndexMap, exprNode);
        }
    }
    
    private Optional<OrCondition> mergeCondition(final Optional<OrCondition> leftOrCondition, final Optional<OrCondition> rightOrCondition, final String operator) {
        if (!leftOrCondition.get().getAndConditions().isEmpty() && !rightOrCondition.get().getAndConditions().isEmpty()) {
            if (LogicalOperator.isOrOperator(operator)) {
                leftOrCondition.get().getAndConditions().addAll(rightOrCondition.get().getAndConditions());
                return leftOrCondition;
            }
            if (LogicalOperator.isAndOperator(operator)) {
                OrCondition result = new OrCondition();
                for (AndCondition each : leftOrCondition.get().getAndConditions()) {
                    for (AndCondition eachRightOr : rightOrCondition.get().getAndConditions()) {
                        AndCondition tempList = new AndCondition();
                        tempList.getConditions().addAll(each.getConditions());
                        tempList.getConditions().addAll(eachRightOr.getConditions());
                        result.getAndConditions().add(tempList);
                    }
                }
                return Optional.of(result);
            }
        }
        if (!leftOrCondition.get().getAndConditions().isEmpty()) {
            return leftOrCondition;
        }
        if (!rightOrCondition.get().getAndConditions().isEmpty()) {
            return rightOrCondition;
        }
        return Optional.of(new OrCondition());
    }
    
    private Optional<OrCondition> extractConditionForParen(final Map<ParserRuleContext, Integer> questionNodeIndexMap, final ParserRuleContext exprNode) {
        int index = -1;
        for (int i = 0; i < exprNode.getChildCount(); i++) {
            if (Paren.isLeftParen(exprNode.getChild(i).getText())) {
                index = i;
                break;
            }
        }
        if (0 <= index) {
            Preconditions.checkState(exprNode.getChildCount() == index + 3, "Invalid expression.");
            Preconditions.checkState(Paren.match(exprNode.getChild(index).getText(), exprNode.getChild(index + 2).getText()), "Missing right paren.");
            if (index >= 0 && "ExprContext".equals(exprNode.getChild(index + 1).getClass().getSimpleName())) {
                return extractCondition(questionNodeIndexMap, (ParserRuleContext) exprNode.getChild(index + 1));
            }
            return Optional.absent();
        }
        OrCondition result = new OrCondition();
        AndCondition newAndCondition = new AndCondition();
        newAndCondition.getConditions().add(buildCondition(questionNodeIndexMap, exprNode).get());
        result.getAndConditions().add(newAndCondition);
        return Optional.of(result);
    }
    
    private Optional<Condition> buildCondition(final Map<ParserRuleContext, Integer> questionNodeIndexMap, final ParserRuleContext exprNode) {
        Optional<Condition> result = buildEqualCondition(questionNodeIndexMap, exprNode);
        if (result.isPresent()) {
            return result;
        }
        result = buildPredicateCondition(questionNodeIndexMap, exprNode);
        if (!result.isPresent()) {
            return Optional.<Condition>of(new NullCondition());
        }
        return result;
    }
    
    private Optional<Condition> buildEqualCondition(final Map<ParserRuleContext, Integer> questionNodeIndexMap, final ParserRuleContext exprNode) {
        Optional<ParserRuleContext> comparisionNode = ASTUtils.findFirstChildNode(exprNode, RuleName.COMPARSION_OPERATOR);
        if (!comparisionNode.isPresent()) {
            return Optional.absent();
        }
        if (3 != comparisionNode.get().getParent().getChildCount()) {
            return Optional.absent();
        }
        if (!Symbol.EQ.getLiterals().equalsIgnoreCase(comparisionNode.get().getText())) {
            return Optional.absent();
        }
        Optional<ParserRuleContext> leftNode = ASTUtils.findFirstChildNode((ParserRuleContext) comparisionNode.get().parent.getChild(0), RuleName.COLUMN_NAME);
        Optional<ParserRuleContext> rightNode = ASTUtils.findFirstChildNode((ParserRuleContext) comparisionNode.get().parent.getChild(2), RuleName.COLUMN_NAME);
        if (!leftNode.isPresent() && !rightNode.isPresent()) {
            return Optional.absent();
        }
        if (leftNode.isPresent() && rightNode.isPresent()) {
            return Optional.absent();
        }
        ParserRuleContext valueNode = null;
        if (leftNode.isPresent()) {
            valueNode = (ParserRuleContext) comparisionNode.get().parent.getChild(2);
        } else if (rightNode.isPresent()) {
            valueNode = (ParserRuleContext) comparisionNode.get().parent.getChild(0);
        }
        Optional<Column> column = buildColumn(exprNode);
        if (!column.isPresent()) {
            return Optional.absent();
        }
        Optional<SQLExpression> sqlExpression = buildExperssion(questionNodeIndexMap, valueNode);
        if (!sqlExpression.isPresent()) {
            return Optional.absent();
        }
        return Optional.of(new Condition(column.get(), sqlExpression.get()));
    }
    
    private Optional<SQLExpression> buildExperssion(final Map<ParserRuleContext, Integer> questionNodeIndexMap, final ParserRuleContext valueNode) {
        Optional<ParserRuleContext> experssionNode = ASTUtils.findFirstChildNode(valueNode, RuleName.STRING);
        if (experssionNode.isPresent()) {
            return Optional.<SQLExpression>of(new SQLTextExpression(experssionNode.get().getText()));
        }
        experssionNode = ASTUtils.findFirstChildNode(valueNode, RuleName.NUMBER);
        if (experssionNode.isPresent()) {
            return Optional.<SQLExpression>of(new SQLNumberExpression(NumberUtil.getExactlyNumber(experssionNode.get().getText(), 10)));
        }
        experssionNode = ASTUtils.findFirstChildNode(valueNode, RuleName.QUESTION);
        if (experssionNode.isPresent()) {
            Integer index = questionNodeIndexMap.get(experssionNode.get());
            if (null != index) {
                return Optional.<SQLExpression>of(new SQLPlaceholderExpression(index.intValue()));
            }
        }
        return Optional.absent();
    }
    
    private Optional<Condition> buildPredicateCondition(final Map<ParserRuleContext, Integer> questionNodeIndexMap, final ParserRuleContext exprNode) {
        Optional<ParserRuleContext> predicateNode = ASTUtils.findFirstChildNode(exprNode, RuleName.PREDICATE);
        if (!predicateNode.isPresent()) {
            return Optional.absent();
        }
        if (1 != predicateNode.get().getParent().getChildCount()) {
            return Optional.absent();
        }
        if (5 == predicateNode.get().getChildCount() && DefaultKeyword.BETWEEN.name().equalsIgnoreCase(predicateNode.get().getChild(1).getText())) {
            Optional<Condition> result = buildBetweenCondition(questionNodeIndexMap, predicateNode.get());
            if (result.isPresent()) {
                return result;
            }
        }
        if (5 <= predicateNode.get().getChildCount() && DefaultKeyword.IN.name().equalsIgnoreCase(predicateNode.get().getChild(1).getText())) {
            Optional<Condition> result = buildInCondition(questionNodeIndexMap, predicateNode.get());
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.absent();
    }
    
    private Optional<Condition> buildBetweenCondition(final Map<ParserRuleContext, Integer> questionNodeIndexMap, final ParserRuleContext predicateNode) {
        Optional<Column> column = buildColumn((ParserRuleContext) predicateNode.getChild(0));
        if (!column.isPresent()) {
            return Optional.absent();
        }
        Optional<SQLExpression> beginSQLExpression = buildExperssion(questionNodeIndexMap, (ParserRuleContext) predicateNode.getChild(2));
        Optional<SQLExpression> endSQLExpression = buildExperssion(questionNodeIndexMap, (ParserRuleContext) predicateNode.getChild(4));
        if (beginSQLExpression.isPresent() && endSQLExpression.isPresent()) {
            return Optional.of(new Condition(column.get(), beginSQLExpression.get(), endSQLExpression.get()));
        }
        return Optional.absent();
    }
    
    private Optional<Condition> buildInCondition(final Map<ParserRuleContext, Integer> questionNodeIndexMap, final ParserRuleContext predicateNode) {
        Optional<Column> column = buildColumn((ParserRuleContext) predicateNode.getChild(0));
        if (!column.isPresent()) {
            return Optional.absent();
        }
        Collection<ParserRuleContext> expressionNodes = ASTUtils.getAllDescendantNodes((ParserRuleContext) predicateNode.getChild(0), RuleName.SIMPLE_EXPR);
        List<SQLExpression> sqlExpressions = new LinkedList<>();
        for (ParserRuleContext each : expressionNodes) {
            Optional<SQLExpression> expression = buildExperssion(questionNodeIndexMap, each);
            if (!expression.isPresent()) {
                sqlExpressions.clear();
                break;
            }
            sqlExpressions.add(expression.get());
        }
        if (!sqlExpressions.isEmpty()) {
            return Optional.of(new Condition(column.get(), sqlExpressions));
        }
        return Optional.absent();
    }
    
    private Optional<Column> buildColumn(final ParserRuleContext parentNode) {
        if (tableAlias.isEmpty()) {
            return Optional.absent();
        }
        Optional<ParserRuleContext> column = ASTUtils.findFirstChildNode(parentNode, RuleName.COLUMN_NAME);
        if (!column.isPresent()) {
            return Optional.absent();
        }
        Optional<ColumnSegment> columnExtractResult = columnHandler.extract(column.get());
        if (!columnExtractResult.isPresent()) {
            return Optional.absent();
        }
        String ownerName = columnExtractResult.get().getOwner().isPresent() ? columnExtractResult.get().getOwner().get() : "";
        if (columnExtractResult.get().getOwner().isPresent()) {
            ownerName = tableAlias.get(ownerName);
        }
        return Optional.of(new Column(columnExtractResult.get().getName(), ownerName));
    }
}
