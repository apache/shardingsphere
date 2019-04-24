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
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.parse.antlr.extractor.api.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.core.parse.antlr.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parse.antlr.extractor.util.RuleName;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.WhereSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.predicate.OrPredicateSegment;

import java.util.Collection;
import java.util.Map;

/**
 * Where extractor.
 *
 * @author duhongjun
 * @author zhangliang
 */
public final class WhereExtractor implements OptionalSQLSegmentExtractor {
    
    private final PredicateExtractor predicateExtractor = new PredicateExtractor();
    
    @Override
    public Optional<WhereSegment> extract(final ParserRuleContext ancestorNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        WhereSegment result = new WhereSegment();
        result.setParameterCount(parameterMarkerIndexes.size());
        Optional<ParserRuleContext> whereNode = ExtractorUtils.findFirstChildNodeNoneRecursive(ancestorNode, RuleName.WHERE_CLAUSE);
        if (whereNode.isPresent()) {
            setPropertiesForRevert(result, whereNode.get(), parameterMarkerIndexes);
            Optional<OrPredicateSegment> orConditionSegment = extractOrConditionSegment(whereNode.get(), parameterMarkerIndexes);
            if (orConditionSegment.isPresent()) {
                result.getOrPredicate().getAndPredicates().addAll(orConditionSegment.get().getAndPredicates());
            }
        }
        return Optional.of(result);
    }
    
    private void setPropertiesForRevert(final WhereSegment whereSegment, final ParserRuleContext whereNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        whereSegment.setWhereStartIndex(whereNode.getStart().getStartIndex());
        whereSegment.setWhereStopIndex(whereNode.getStop().getStopIndex());
        if (parameterMarkerIndexes.isEmpty()) {
            return;
        }
        Collection<ParserRuleContext> parameterMarkerNodes = ExtractorUtils.getAllDescendantNodes(whereNode, RuleName.PARAMETER_MARKER);
        if (parameterMarkerNodes.isEmpty()) {
            return;
        }
        int whereParameterStartIndex = parameterMarkerIndexes.get(parameterMarkerNodes.iterator().next());
        whereSegment.setWhereParameterStartIndex(whereParameterStartIndex);
        whereSegment.setWhereParameterEndIndex(whereParameterStartIndex + parameterMarkerNodes.size() - 1);
    }
    
    private Optional<OrPredicateSegment> extractOrConditionSegment(final ParserRuleContext whereNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        Optional<ParserRuleContext> exprNode = ExtractorUtils.findFirstChildNode((ParserRuleContext) whereNode.getChild(1), RuleName.EXPR);
        return exprNode.isPresent() ? predicateExtractor.extract(exprNode.get(), parameterMarkerIndexes) : Optional.<OrPredicateSegment>absent();
    }
}
