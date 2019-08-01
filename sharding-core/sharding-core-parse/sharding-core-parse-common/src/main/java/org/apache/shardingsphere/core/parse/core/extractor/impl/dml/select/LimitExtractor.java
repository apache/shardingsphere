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

package org.apache.shardingsphere.core.parse.core.extractor.impl.dml.select;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.parse.core.extractor.api.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.core.parse.core.extractor.impl.common.expression.impl.ParameterMarkerExpressionExtractor;
import org.apache.shardingsphere.core.parse.core.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parse.core.extractor.util.RuleName;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.limit.LimitValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.limit.ParameterMarkerLimitValueSegment;
import org.apache.shardingsphere.core.parse.util.SQLUtil;

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
        return limitNode.isPresent()
                ? Optional.of(new LimitSegment(limitNode.get().getStart().getStartIndex(), limitNode.get().getStop().getStopIndex(),
                extractOffset(limitNode.get(), parameterMarkerIndexes).orNull(), extractRowCount(limitNode.get(), parameterMarkerIndexes).orNull())) : Optional.<LimitSegment>absent();
    }
    
    private Optional<LimitValueSegment> extractOffset(final ParserRuleContext limitNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        Optional<ParserRuleContext> offsetNode = ExtractorUtils.findFirstChildNode(limitNode, RuleName.LIMIT_OFFSET);
        return offsetNode.isPresent() ? Optional.of(extractLimitValue(offsetNode.get(), parameterMarkerIndexes)) : Optional.<LimitValueSegment>absent();
    }
    
    private Optional<LimitValueSegment> extractRowCount(final ParserRuleContext limitNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        Optional<ParserRuleContext> rowCountNode = ExtractorUtils.findFirstChildNode(limitNode, RuleName.LIMIT_ROW_COUNT);
        return rowCountNode.isPresent() ? Optional.of(extractLimitValue(rowCountNode.get(), parameterMarkerIndexes)) : Optional.<LimitValueSegment>absent();
    }
    
    private LimitValueSegment extractLimitValue(final ParserRuleContext limitValueNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        Optional<ParameterMarkerExpressionSegment> parameterMarkerExpression = parameterMarkerExpressionExtractor.extract(limitValueNode, parameterMarkerIndexes);
        if (parameterMarkerExpression.isPresent()) {
            return new ParameterMarkerLimitValueSegment(
                    limitValueNode.getStart().getStartIndex(), limitValueNode.getStop().getStopIndex(), parameterMarkerExpression.get().getParameterMarkerIndex());
        }
        Optional<ParserRuleContext> numberLiteralsNode = ExtractorUtils.findFirstChildNode(limitValueNode, RuleName.NUMBER_LITERALS);
        Preconditions.checkState(numberLiteralsNode.isPresent());
        return new NumberLiteralLimitValueSegment(
                limitValueNode.getStart().getStartIndex(), limitValueNode.getStop().getStopIndex(), SQLUtil.getExactlyNumber(numberLiteralsNode.get().getText(), 10).intValue());
    }
}
