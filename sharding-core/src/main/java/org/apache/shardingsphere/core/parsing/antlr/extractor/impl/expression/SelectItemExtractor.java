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

package org.apache.shardingsphere.core.parsing.antlr.extractor.impl.expression;

import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.parsing.antlr.extractor.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.core.parsing.antlr.extractor.impl.dql.SubqueryExtractor;
import org.apache.shardingsphere.core.parsing.antlr.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parsing.antlr.extractor.util.RuleName;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.expr.SubquerySegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.select.ColumnSelectItemSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.select.ExpressionSelectItemSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.select.FunctionSelectItemSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.select.SelectItemSegment;
import org.apache.shardingsphere.core.util.SQLUtil;

/**
 * Select item extractor.
 *
 * @author zhangliang
 */
public final class SelectItemExtractor implements OptionalSQLSegmentExtractor {
    
    private final StarSelectItemSegmentExtractor starItemExpressionExtractor = new StarSelectItemSegmentExtractor();
    
    private final ColumnSelectItemSegmentExtractor columnSelectItemSegmentExtractor = new ColumnSelectItemSegmentExtractor();
    
    private final FunctionSelectItemSegmentExtractor functionSelectItemSegmentExtractor = new FunctionSelectItemSegmentExtractor();
    
    private final ExpressionSelectItemSegmentExtractor expressionSelectItemSegmentExtractor = new ExpressionSelectItemSegmentExtractor();
    
    private final SubqueryExtractor subqueryExtractor = new SubqueryExtractor();
    
    @Override
    public Optional<? extends SelectItemSegment> extract(final ParserRuleContext expressionNode) {
        Optional<? extends SelectItemSegment> result;
        result = starItemExpressionExtractor.extract(expressionNode);
        if (result.isPresent()) {
            return result;
        }
        result = extractSubquerySegment(expressionNode);
        if (result.isPresent()) {
            return result;
        }
        result = extractColumnSelectItemSegment(expressionNode);
        if (result.isPresent()) {
            return result;
        }
        result = extractFunctionSelectItemSegment(expressionNode);
        if (result.isPresent()) {
            return result;
        }
        return extractExpressionSelectItemSegment(expressionNode);
    }
    
    private Optional<SubquerySegment> extractSubquerySegment(final ParserRuleContext expressionNode) {
        Optional<ParserRuleContext> subqueryNode = ExtractorUtils.findFirstChildNode(expressionNode, RuleName.SUBQUERY);
        if (subqueryNode.isPresent()) {
            Optional<SubquerySegment> result = subqueryExtractor.extract(subqueryNode.get());
            if (result.isPresent()) {
                setAlias(expressionNode, result.get());
            }
            return result;
        }
        return Optional.absent();
    }
    
    private Optional<ColumnSelectItemSegment> extractColumnSelectItemSegment(final ParserRuleContext expressionNode) {
        Optional<ColumnSelectItemSegment> result = columnSelectItemSegmentExtractor.extract(expressionNode);
        if (result.isPresent()) {
            setAlias(expressionNode, result.get());
            return result;
        }
        return Optional.absent();
    }
    
    private Optional<FunctionSelectItemSegment> extractFunctionSelectItemSegment(final ParserRuleContext expressionNode) {
        Optional<FunctionSelectItemSegment> result = functionSelectItemSegmentExtractor.extract(expressionNode);
        if (result.isPresent()) {
            setAlias(expressionNode, result.get());
        }
        return result;
    }
    
    private Optional<ExpressionSelectItemSegment> extractExpressionSelectItemSegment(final ParserRuleContext expressionNode) {
        Optional<ExpressionSelectItemSegment> result = expressionSelectItemSegmentExtractor.extract(expressionNode);
        if (result.isPresent()) {
            setAlias(expressionNode, result.get());
        }
        return result;
    }
    
    private void setAlias(final ParserRuleContext expressionNode, final SelectItemSegment selectItemSegment) {
        Optional<ParserRuleContext> aliasNode = ExtractorUtils.findFirstChildNode(expressionNode, RuleName.ALIAS);
        if (aliasNode.isPresent()) {
            selectItemSegment.setAlias(SQLUtil.getExactlyValue(aliasNode.get().getText()));
        }
    }
}
