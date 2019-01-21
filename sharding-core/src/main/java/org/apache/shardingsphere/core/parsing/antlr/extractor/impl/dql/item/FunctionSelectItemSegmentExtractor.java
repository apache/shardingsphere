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

package org.apache.shardingsphere.core.parsing.antlr.extractor.impl.dql.item;

import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.constant.AggregationType;
import org.apache.shardingsphere.core.parsing.antlr.extractor.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.core.parsing.antlr.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parsing.antlr.extractor.util.RuleName;
import org.apache.shardingsphere.core.parsing.antlr.sql.AliasAvailable;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.select.AggregationDistinctSelectItemSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.select.AggregationSelectItemSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.select.ExpressionSelectItemSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.select.SelectItemSegment;

/**
 * Function select item segment extractor.
 *
 * @author zhangliang
 */
public final class FunctionSelectItemSegmentExtractor implements OptionalSQLSegmentExtractor {
    
    @Override
    public Optional<SelectItemSegment> extract(final ParserRuleContext expressionNode) {
        Optional<ParserRuleContext> functionNode = ExtractorUtils.findFirstChildNode(expressionNode, RuleName.FUNCTION_CALL);
        if (!functionNode.isPresent()) {
            return Optional.absent();
        }
        return Optional.of(extractFunctionSelectItemSegment(expressionNode, functionNode.get()));
    }
    
    private SelectItemSegment extractFunctionSelectItemSegment(final ParserRuleContext expressionNode, final ParserRuleContext functionNode) {
        String functionName = functionNode.getChild(0).getText();
        Optional<AggregationType> aggregationType = findAggregationType(functionName);
        AliasAvailable result = aggregationType.isPresent() ? extractAggregationSelectItemSegment(aggregationType.get(), functionNode)
                : new ExpressionSelectItemSegment(functionNode.getText(), functionNode.getStart().getStartIndex(), functionNode.getStop().getStopIndex());
        Optional<ParserRuleContext> aliasNode = ExtractorUtils.findFirstChildNode(expressionNode, RuleName.ALIAS);
        if (aliasNode.isPresent()) {
            result.setAlias(aliasNode.get().getText());
        }
        return (SelectItemSegment) result;
    }
    
    private Optional<AggregationType> findAggregationType(final String functionName) {
        try {
            return Optional.of(AggregationType.valueOf(functionName.toUpperCase()));
        } catch (final IllegalArgumentException ignore) {
            return Optional.absent();
        }
    }
    
    private AggregationSelectItemSegment extractAggregationSelectItemSegment(final AggregationType type, final ParserRuleContext functionNode) {
        return ExtractorUtils.findFirstChildNode(functionNode, RuleName.DISTINCT).isPresent()
                ? new AggregationDistinctSelectItemSegment(
                        type, getInnerExpression(functionNode), functionNode.getStart().getStartIndex(), functionNode.getStop().getStopIndex(), getDistinctExpression(functionNode)) 
                : new AggregationSelectItemSegment(type, getInnerExpression(functionNode), functionNode.getStart().getStartIndex(), functionNode.getStop().getStopIndex()); 
    }
    
    private String getInnerExpression(final ParserRuleContext functionNode) {
        StringBuilder result = new StringBuilder();
        for (int i = 1; i < functionNode.getChildCount(); i++) {
            String text = functionNode.getChild(i).getText();
            result.append(text);
            if ("DISTINCT".equals(text)) {
                result.append(" ");
            }
        }
        return result.toString();
    }
    
    private String getDistinctExpression(final ParserRuleContext functionNode) {
        StringBuilder result = new StringBuilder();
        for (int i = 3; i < functionNode.getChildCount() - 1; i++) {
            result.append(functionNode.getChild(i).getText());
        }
        return result.toString();
    }
}
