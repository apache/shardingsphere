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
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.shardingsphere.core.parsing.antlr.extractor.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.core.parsing.antlr.extractor.impl.dql.SubqueryExtractor;
import org.apache.shardingsphere.core.parsing.antlr.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parsing.antlr.extractor.util.RuleName;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.column.ColumnSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.expr.CommonExpressionSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.expr.ExpressionWithAliasSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.expr.FunctionExpressionSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.expr.PropertyExpressionSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.expr.StarExpressionSegment;
import org.apache.shardingsphere.core.parsing.lexer.token.Symbol;
import org.apache.shardingsphere.core.util.NumberUtil;
import org.apache.shardingsphere.core.util.SQLUtil;

import java.util.Map;

/**
 * Expression extractor.
 *
 * @author duhongjun
 */
@RequiredArgsConstructor
public final class ExpressionExtractor implements OptionalSQLSegmentExtractor {
    
    private final SubqueryExtractor subqueryExtractor = new SubqueryExtractor();
    
    private final Map<ParserRuleContext, Integer> questionNodeIndexMap;
    
    @Override
    public Optional<? extends ExpressionSegment> extract(final ParserRuleContext expressionNode) {
        String firstChildText = expressionNode.getText();
        if (firstChildText.endsWith(Symbol.STAR.getLiterals())) {
            return Optional.of(extractStarExpressionSegment(expressionNode, firstChildText));
        }
        Optional<ParserRuleContext> subqueryNode = ExtractorUtils.findFirstChildNode(expressionNode, RuleName.SUBQUERY);
        return subqueryNode.isPresent() ? subqueryExtractor.extract(subqueryNode.get()) : Optional.of(extractExpressionWithAliasSegment(expressionNode));
    }
    
    private ExpressionSegment extractStarExpressionSegment(final ParserRuleContext expressionNode, final String text) {
        StarExpressionSegment result = new StarExpressionSegment(expressionNode.getStart().getStartIndex());
        if (text.contains(Symbol.DOT.getLiterals())) {
            result.setOwner(SQLUtil.getExactlyValue(text.substring(0, text.indexOf(Symbol.DOT.getLiterals()))));
        }
        return result;
    }
    
    private ExpressionWithAliasSegment extractExpressionWithAliasSegment(final ParserRuleContext expressionNode) {
        ExpressionWithAliasSegment result = extractExpressionWithoutAlias(expressionNode);
        Optional<String> alias = getAlias(expressionNode);
        if (alias.isPresent()) {
            result.setAlias(alias.get());
        }
        return result;
    }
    
    private ExpressionWithAliasSegment extractExpressionWithoutAlias(final ParserRuleContext expressionNode) {
        Optional<ParserRuleContext> functionNode = ExtractorUtils.findFirstChildNode(expressionNode, RuleName.FUNCTION_CALL);
        if (functionNode.isPresent()) {
            return extractFunctionExpressionSegment(expressionNode, functionNode.get());
        }
        if (RuleName.COLUMN_NAME.getName().equals(expressionNode.getChild(0).getClass().getSimpleName())) {
            return extractPropertyExpressionSegment(expressionNode);
        }
        return extractCommonExpressionSegment(expressionNode);
    }
    
    private ExpressionWithAliasSegment extractFunctionExpressionSegment(final ParserRuleContext expressionNode, final ParserRuleContext functionNode) {
        return new FunctionExpressionSegment(functionNode.getChild(0).getText(), functionNode.getStart().getStartIndex(),
                ((TerminalNode) functionNode.getChild(1)).getSymbol().getStartIndex(), functionNode.getStop().getStopIndex(), 
                ExtractorUtils.findFirstChildNode(expressionNode, RuleName.DISTINCT).isPresent() ? getDistinctExpressionStartIndex(functionNode) : -1);
    }
    
    private int getDistinctExpressionStartIndex(final ParserRuleContext functionNode) {
        ParseTree distinctItemNode = functionNode.getChild(3);
        if (distinctItemNode instanceof TerminalNode) {
            return ((TerminalNode) distinctItemNode).getSymbol().getStartIndex();
        }
        if (distinctItemNode instanceof ParserRuleContext) {
            return ((ParserRuleContext) distinctItemNode).getStart().getStartIndex();
        }
        return -1;
    }
    
    private ExpressionWithAliasSegment extractPropertyExpressionSegment(final ParserRuleContext expressionNode) {
        ParserRuleContext columnNode = (ParserRuleContext) expressionNode.getChild(0);
        Optional<ColumnSegment> columnSegment = new ColumnSegmentExtractor().extract(columnNode);
        Preconditions.checkState(columnSegment.isPresent());
        return new PropertyExpressionSegment(columnSegment.get().getOwner(), columnSegment.get().getName(), columnNode.getStart().getStartIndex(), columnNode.getStop().getStopIndex());
    }
    
    /**
     * Extract expression segment from SQL AST.
     *
     * @param expressionNode expression node of AST
     * @return CommonExpressionSegment
     */
    public CommonExpressionSegment extractCommonExpressionSegment(final ParserRuleContext expressionNode) {
        CommonExpressionSegment result = new CommonExpressionSegment(expressionNode.getStart().getStartIndex(), expressionNode.getStop().getStopIndex());
        Optional<ParserRuleContext> questionNode = ExtractorUtils.findFirstChildNode(expressionNode, RuleName.QUESTION);
        if (questionNode.isPresent()) {
            Integer index = questionNodeIndexMap.get(questionNode.get());
            result.setIndex(index);
        } else {
            Optional<ParserRuleContext> bitExprNode = ExtractorUtils.findFirstChildNode(expressionNode, RuleName.BIT_EXPR);
            Optional<ParserRuleContext> numberNode = ExtractorUtils.findFirstChildNode(expressionNode, RuleName.NUMBER);
            if (numberNode.isPresent() && (!bitExprNode.isPresent() || 1 == bitExprNode.get().getChildCount())) {
                result.setValue(NumberUtil.getExactlyNumber(numberNode.get().getText(), 10));
            }
            Optional<ParserRuleContext> stringNode = ExtractorUtils.findFirstChildNode(expressionNode, RuleName.STRING);
            if (stringNode.isPresent() && (!bitExprNode.isPresent() || 1 == bitExprNode.get().getChildCount())) {
                result.setText(true);
            }
        }
        return result;
    }
    
    private Optional<String> getAlias(final ParserRuleContext expressionNode) {
        Optional<ParserRuleContext> aliasNode = ExtractorUtils.findFirstChildNode(expressionNode, RuleName.ALIAS);
        return aliasNode.isPresent() ? Optional.of(SQLUtil.getExactlyValue(aliasNode.get().getText())) : Optional.<String>absent();
    }
}
