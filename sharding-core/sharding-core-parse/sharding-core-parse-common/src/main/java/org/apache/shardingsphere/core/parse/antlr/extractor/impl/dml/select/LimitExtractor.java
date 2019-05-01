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

package org.apache.shardingsphere.core.parse.antlr.extractor.impl.dml.select;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.parse.antlr.extractor.api.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.core.parse.antlr.extractor.impl.common.expression.impl.ParameterMarkerExpressionExtractor;
import org.apache.shardingsphere.core.parse.antlr.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parse.antlr.extractor.util.RuleName;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.limit.LimitSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.limit.LimitValueSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.limit.LiteralLimitValueSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.limit.PlaceholderLimitValueSegment;
import org.apache.shardingsphere.core.util.NumberUtil;

import java.util.Map;

/**
 * Limit extractor.
 *
 * @author duhongjun
 * @author panjuan
 * @author zhangliang
 */
public final class LimitExtractor implements OptionalSQLSegmentExtractor {
    
    private final ParameterMarkerExpressionExtractor parameterMarkerExpressionExtractor = new ParameterMarkerExpressionExtractor();
    
    @Override
    public Optional<LimitSegment> extract(final ParserRuleContext ancestorNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        Optional<ParserRuleContext> limitNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.LIMIT_CLAUSE);
        if (!limitNode.isPresent()) {
            return Optional.absent();
        }
        LimitValueSegment rowCount = getRowCount(limitNode.get(), parameterMarkerIndexes);
        Optional<LimitValueSegment> offset = getOffset(limitNode.get(), parameterMarkerIndexes);
        return offset.isPresent() ? Optional.of(new LimitSegment(rowCount, offset.get())) : Optional.of(new LimitSegment(rowCount));
    }
    
    private LimitValueSegment getRowCount(final ParserRuleContext limitNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        Optional<ParserRuleContext> rowCountNode = ExtractorUtils.findFirstChildNode(limitNode, RuleName.LIMIT_ROW_COUNT);
        Preconditions.checkState(rowCountNode.isPresent());
        return extractLimitValue(rowCountNode.get(), parameterMarkerIndexes);
    }
    
    private Optional<LimitValueSegment> getOffset(final ParserRuleContext limitNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        Optional<ParserRuleContext> offsetNode = ExtractorUtils.findFirstChildNode(limitNode, RuleName.LIMIT_OFFSET);
        return offsetNode.isPresent() ? Optional.of(extractLimitValue(offsetNode.get(), parameterMarkerIndexes)) : Optional.<LimitValueSegment>absent();
    }
    
    private LimitValueSegment extractLimitValue(final ParserRuleContext limitValueNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        Optional<ParameterMarkerExpressionSegment> parameterMarkerExpression = parameterMarkerExpressionExtractor.extract(limitValueNode, parameterMarkerIndexes);
        if (parameterMarkerExpression.isPresent()) {
            return new PlaceholderLimitValueSegment(parameterMarkerExpression.get().getParameterMarkerIndex(), limitValueNode.getStart().getStartIndex(), limitValueNode.getStop().getStopIndex());
        }
        Optional<ParserRuleContext> numberLiteralsNode = ExtractorUtils.findFirstChildNode(limitValueNode, RuleName.NUMBER_LITERALS);
        Preconditions.checkState(numberLiteralsNode.isPresent());
        return new LiteralLimitValueSegment(
                NumberUtil.getExactlyNumber(numberLiteralsNode.get().getText(), 10).intValue(), limitValueNode.getStart().getStartIndex(), limitValueNode.getStop().getStopIndex());
    }
}
