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
import com.google.common.base.Preconditions;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.parse.core.extractor.api.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.core.parse.core.extractor.impl.common.expression.ExpressionExtractor;
import org.apache.shardingsphere.core.parse.core.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parse.core.extractor.util.RuleName;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.rownum.NumberLiteralRowNumberValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.rownum.ParameterMarkerRowNumberValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.rownum.RowNumberValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.top.TopSegment;

import java.util.Map;

/**
 * Top select item extractor.
 *
 * @author zhangliang
 */
public final class TopSelectItemExtractor implements OptionalSQLSegmentExtractor {
    
    private final ExpressionExtractor expressionExtractor = new ExpressionExtractor();
    
    @Override
    public Optional<TopSegment> extract(final ParserRuleContext expressionNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        Optional<ParserRuleContext> topNode = ExtractorUtils.findFirstChildNode(expressionNode, RuleName.TOP);
        if (!topNode.isPresent()) {
            return Optional.absent();
        }
        ParserRuleContext topExprNode = ExtractorUtils.getFirstChildNode(topNode.get(), RuleName.EXPR);
        Optional<? extends ExpressionSegment> topExpr = expressionExtractor.extract(topExprNode, parameterMarkerIndexes);
        Preconditions.checkState(topExpr.isPresent());
        Optional<RowNumberValueSegment> rowNumberValueSegment = createRowNumberValueSegment(topExpr.get());
        Preconditions.checkState(rowNumberValueSegment.isPresent());
        ParserRuleContext rowNumberAliasNode = ExtractorUtils.getFirstChildNode(topNode.get().getParent(), RuleName.ALIAS);
        return Optional.of(
                new TopSegment(topNode.get().getStart().getStartIndex(), topNode.get().getStop().getStopIndex(), topNode.get().getText(), rowNumberValueSegment.get(), rowNumberAliasNode.getText()));
    }
    
    private Optional<RowNumberValueSegment> createRowNumberValueSegment(final ExpressionSegment topExpr) {
        if (topExpr instanceof ParameterMarkerExpressionSegment) {
            return Optional.<RowNumberValueSegment>of(
                    new ParameterMarkerRowNumberValueSegment(topExpr.getStartIndex(), topExpr.getStopIndex(), ((ParameterMarkerExpressionSegment) topExpr).getParameterMarkerIndex(), false));
        }
        if (topExpr instanceof LiteralExpressionSegment && ((LiteralExpressionSegment) topExpr).getLiterals() instanceof Number) {
            return Optional.<RowNumberValueSegment>of(
                    new NumberLiteralRowNumberValueSegment(topExpr.getStartIndex(), topExpr.getStopIndex(), ((Number) ((LiteralExpressionSegment) topExpr).getLiterals()).intValue(), false));
        }
        return Optional.absent();
    }
}
