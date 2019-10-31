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

package org.apache.shardingsphere.core.parse.core.extractor.impl.common.expression.impl;

import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.parse.core.extractor.api.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.core.parse.core.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parse.core.extractor.util.RuleName;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.util.SQLUtil;

import java.util.Map;

/**
 * Literal expression extractor.
 *
 * @author zhangliang
 */
public final class LiteralExpressionExtractor implements OptionalSQLSegmentExtractor {
    
    @Override
    public Optional<LiteralExpressionSegment> extract(final ParserRuleContext expressionNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        Optional<ParserRuleContext> literalsNode = ExtractorUtils.findSingleNodeFromFirstDescendant(expressionNode, RuleName.LITERALS);
        if (!literalsNode.isPresent()) {
            return Optional.absent();
        }
        Optional<?> literals = getLiterals(literalsNode.get());
        return literals.isPresent() ? Optional.of(new LiteralExpressionSegment(literalsNode.get().getStart().getStartIndex(), literalsNode.get().getStop().getStopIndex(), literals.get()))
                : Optional.<LiteralExpressionSegment>absent();
    }
    
    private Optional<?> getLiterals(final ParserRuleContext literalsNode) {
        Optional<Number> numberLiterals = getNumberLiterals(literalsNode);
        if (numberLiterals.isPresent()) {
            return numberLiterals;
        }
        return getStringLiterals(literalsNode);
    }
    
    private Optional<Number> getNumberLiterals(final ParserRuleContext literalsNode) {
        Optional<ParserRuleContext> numberLiteralsNode = ExtractorUtils.findFirstChildNode(literalsNode, RuleName.NUMBER_LITERALS);
        return numberLiteralsNode.isPresent() ? Optional.of(SQLUtil.getExactlyNumber(numberLiteralsNode.get().getText(), 10)) : Optional.<Number>absent();
    }
    
    private Optional<String> getStringLiterals(final ParserRuleContext literalsNode) {
        Optional<ParserRuleContext> stringLiteralsNode = ExtractorUtils.findFirstChildNode(literalsNode, RuleName.STRING_LITERALS);
        if (stringLiteralsNode.isPresent()) {
            String text = stringLiteralsNode.get().getText();
            return Optional.of(text.substring(1, text.length() - 1));
        }
        return Optional.absent();
    }
}
