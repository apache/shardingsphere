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

package org.apache.shardingsphere.core.parse.core.extractor.impl.dml.select.item.impl;

import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.shardingsphere.core.parse.core.constant.AggregationType;
import org.apache.shardingsphere.core.parse.core.extractor.api.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.core.parse.core.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parse.core.extractor.util.RuleName;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.AggregationDistinctSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.AggregationSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.ExpressionSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.SelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.generic.AliasAvailable;

import java.util.Map;

/**
 * Function select item extractor.
 *
 * @author zhangliang
 */
public final class FunctionSelectItemExtractor implements OptionalSQLSegmentExtractor {
    
    @Override
    public Optional<SelectItemSegment> extract(final ParserRuleContext expressionNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        Optional<ParserRuleContext> functionCallNode = ExtractorUtils.findFirstChildNode(expressionNode, RuleName.FUNCTION_CALL);
        if (!functionCallNode.isPresent()) {
            return Optional.absent();
        }
        SelectItemSegment result = extractFunctionSelectItemSegment(functionCallNode.get());
        Optional<ParserRuleContext> aliasNode = ExtractorUtils.findFirstChildNodeNoneRecursive(expressionNode, RuleName.ALIAS);
        if (aliasNode.isPresent() && result instanceof AliasAvailable) {
            ((AliasAvailable) result).setAlias(aliasNode.get().getText());
        }
        return Optional.of(result);
    }
    
    private SelectItemSegment extractFunctionSelectItemSegment(final ParserRuleContext functionCallNode) {
        Optional<ParserRuleContext> aggregationFunctionCallNode = ExtractorUtils.findFirstChildNodeNoneRecursive(functionCallNode, RuleName.AGGREGATION_FUNCTION);
        if (!aggregationFunctionCallNode.isPresent()) {
            return new ExpressionSelectItemSegment(functionCallNode.getStart().getStartIndex(), functionCallNode.getStop().getStopIndex(), functionCallNode.getText());
        }
        Optional<AggregationType> aggregationType = findAggregationType(aggregationFunctionCallNode.get().getChild(0).getText());
        return aggregationType.isPresent() ? extractAggregationSelectItemSegment(aggregationType.get(), aggregationFunctionCallNode.get())
                : new ExpressionSelectItemSegment(functionCallNode.getStart().getStartIndex(), functionCallNode.getStop().getStopIndex(), functionCallNode.getText());
    }
    
    private Optional<AggregationType> findAggregationType(final String functionName) {
        try {
            return Optional.of(AggregationType.valueOf(functionName.toUpperCase()));
        } catch (final IllegalArgumentException ignore) {
            return Optional.absent();
        }
    }
    
    private AggregationSelectItemSegment extractAggregationSelectItemSegment(final AggregationType type, final ParserRuleContext aggregationFunctionCallNode) {
        int innerExpressionStartIndex = ((TerminalNode) aggregationFunctionCallNode.getChild(1)).getSymbol().getStartIndex();
        return ExtractorUtils.findFirstChildNode(aggregationFunctionCallNode, RuleName.DISTINCT).isPresent()
                ? new AggregationDistinctSelectItemSegment(aggregationFunctionCallNode.getStart().getStartIndex(), aggregationFunctionCallNode.getStop().getStopIndex(), 
                aggregationFunctionCallNode.getText(), type, innerExpressionStartIndex, getDistinctExpression(aggregationFunctionCallNode))
                : new AggregationSelectItemSegment(aggregationFunctionCallNode.getStart().getStartIndex(), aggregationFunctionCallNode.getStop().getStopIndex(), 
                aggregationFunctionCallNode.getText(), type, innerExpressionStartIndex);
    }
    
    private String getDistinctExpression(final ParserRuleContext functionNode) {
        StringBuilder result = new StringBuilder();
        for (int i = 3; i < functionNode.getChildCount() - 1; i++) {
            result.append(functionNode.getChild(i).getText());
        }
        return result.toString();
    }
}
