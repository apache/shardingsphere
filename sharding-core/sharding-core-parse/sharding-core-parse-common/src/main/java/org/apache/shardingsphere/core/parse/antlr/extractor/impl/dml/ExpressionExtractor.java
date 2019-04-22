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
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.shardingsphere.core.parse.antlr.extractor.impl.common.column.ColumnExtractor;
import org.apache.shardingsphere.core.parse.antlr.extractor.impl.dml.select.SubqueryExtractor;
import org.apache.shardingsphere.core.parse.antlr.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parse.antlr.extractor.util.RuleName;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.expr.CommonExpressionSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.expr.FunctionExpressionSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.expr.PropertyExpressionSegment;
import org.apache.shardingsphere.core.util.NumberUtil;

import java.util.Map;

/**
 * Expression extractor.
 *
 * @author duhongjun
 */
public final class ExpressionExtractor {
    
    /**
     * Extract expression.
     *
     * @param parameterMarkerIndexes  parameter marker indexes
     * @param expressionNode expression node
     * @return expression segment
     */
    public Optional<? extends ExpressionSegment> extract(final Map<ParserRuleContext, Integer> parameterMarkerIndexes, final ParserRuleContext expressionNode) {
        Optional<ParserRuleContext> subqueryNode = ExtractorUtils.findFirstChildNode(expressionNode, RuleName.SUBQUERY);
        return subqueryNode.isPresent() ? new SubqueryExtractor().extract(subqueryNode.get(), parameterMarkerIndexes) : Optional.of(extractExpression(parameterMarkerIndexes, expressionNode));
    }
    
    private ExpressionSegment extractExpression(final Map<ParserRuleContext, Integer> parameterMarkerIndexes, final ParserRuleContext expressionNode) {
        Optional<ParserRuleContext> functionNode = ExtractorUtils.findFirstChildNode(expressionNode, RuleName.FUNCTION_CALL);
        if (functionNode.isPresent()) {
            return extractFunctionExpressionSegment(functionNode.get());
        }
        if (RuleName.COLUMN_NAME.getName().equals(expressionNode.getChild(0).getClass().getSimpleName())) {
            return extractPropertyExpressionSegment(expressionNode, parameterMarkerIndexes);
        }
        return extractCommonExpressionSegment(parameterMarkerIndexes, expressionNode);
    }
    
    // TODO extract column name and value from function
    private ExpressionSegment extractFunctionExpressionSegment(final ParserRuleContext functionNode) {
        return new FunctionExpressionSegment(functionNode.getStart().getStartIndex(), functionNode.getStop().getStopIndex(), functionNode.getChild(0).getText(), 
                ((TerminalNode) functionNode.getChild(1)).getSymbol().getStartIndex(), functionNode.getStop().getStopIndex(), -1);
    }
    
    private ExpressionSegment extractPropertyExpressionSegment(final ParserRuleContext expressionNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        ParserRuleContext columnNode = (ParserRuleContext) expressionNode.getChild(0);
        Optional<ColumnSegment> columnSegment = new ColumnExtractor().extract(columnNode, parameterMarkerIndexes);
        Preconditions.checkState(columnSegment.isPresent());
        return new PropertyExpressionSegment(columnNode.getStart().getStartIndex(), columnNode.getStop().getStopIndex(), columnSegment.get().getName(), columnSegment.get().getOwner().orNull());
    }
    
    /**
     * Extract common expression segment.
     *
     * @param parameterMarkerIndexes parameter marker indexes
     * @param expressionNode expression node
     * @return common expression segment
     */
    public CommonExpressionSegment extractCommonExpressionSegment(final Map<ParserRuleContext, Integer> parameterMarkerIndexes, final ParserRuleContext expressionNode) {
        CommonExpressionSegment result = new CommonExpressionSegment(expressionNode.getStart().getStartIndex(), expressionNode.getStop().getStopIndex());
        Optional<ParserRuleContext> questionNode = ExtractorUtils.findFirstChildNode(expressionNode, RuleName.PARAMETER_MARKER);
        if (questionNode.isPresent()) {
            Integer index = parameterMarkerIndexes.get(questionNode.get());
            result.setPlaceholderIndex(index);
            return result;
        }
        Optional<ParserRuleContext> bitExprNode = ExtractorUtils.findFirstChildNode(expressionNode, RuleName.BIT_EXPR);
        if (bitExprNode.isPresent() && 1 != bitExprNode.get().getChildCount()) {
            return result;
        }
        Optional<ParserRuleContext> numberNode = ExtractorUtils.findFirstChildNode(expressionNode, RuleName.NUMBER);
        if (numberNode.isPresent()) {
            result.setLiterals(NumberUtil.getExactlyNumber(numberNode.get().getText(), 10));
        }
        Optional<ParserRuleContext> stringNode = ExtractorUtils.findFirstChildNode(expressionNode, RuleName.STRING);
        if (stringNode.isPresent()) {
            String text = stringNode.get().getText();
            result.setLiterals(text.substring(1, text.length() - 1));
        }
        return result;
    }
}
