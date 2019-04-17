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
import lombok.Setter;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.parse.antlr.extractor.api.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.core.parse.antlr.extractor.api.PlaceholderIndexesAware;
import org.apache.shardingsphere.core.parse.antlr.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parse.antlr.extractor.util.RuleName;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.WhereSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.condition.OrConditionSegment;

import java.util.Collection;
import java.util.Map;

/**
 * Where extractor.
 *
 * @author duhongjun
 * @author zhangliang
 */
@Setter
public final class WhereExtractor implements OptionalSQLSegmentExtractor, PlaceholderIndexesAware {
    
    private final PredicateExtractor predicateExtractor = new PredicateExtractor();
    
    private Map<ParserRuleContext, Integer> placeholderIndexes;
    
    @Override
    public Optional<WhereSegment> extract(final ParserRuleContext ancestorNode) {
        WhereSegment result = new WhereSegment();
        result.setParameterCount(placeholderIndexes.size());
        Optional<ParserRuleContext> whereNode = ExtractorUtils.findFirstChildNodeNoneRecursive(ancestorNode, RuleName.WHERE_CLAUSE);
        if (whereNode.isPresent()) {
            setPropertiesForRevert(result, placeholderIndexes, whereNode.get());
            Optional<OrConditionSegment> orConditionSegment = extractOrConditionSegment(placeholderIndexes, whereNode.get());
            if (orConditionSegment.isPresent()) {
                result.getConditions().getAndConditions().addAll(orConditionSegment.get().getAndConditions());
            }
        }
        return Optional.of(result);
    }
    
    private void setPropertiesForRevert(final WhereSegment whereSegment, final Map<ParserRuleContext, Integer> placeholderIndexes, final ParserRuleContext whereNode) {
        whereSegment.setWhereStartIndex(whereNode.getStart().getStartIndex());
        whereSegment.setWhereStopIndex(whereNode.getStop().getStopIndex());
        if (placeholderIndexes.isEmpty()) {
            return;
        }
        Collection<ParserRuleContext> questionNodes = ExtractorUtils.getAllDescendantNodes(whereNode, RuleName.QUESTION);
        if (questionNodes.isEmpty()) {
            return;
        }
        int whereParameterStartIndex = placeholderIndexes.get(questionNodes.iterator().next());
        whereSegment.setWhereParameterStartIndex(whereParameterStartIndex);
        whereSegment.setWhereParameterEndIndex(whereParameterStartIndex + questionNodes.size() - 1);
    }
    
    private Optional<OrConditionSegment> extractOrConditionSegment(final Map<ParserRuleContext, Integer> placeholderIndexes, final ParserRuleContext whereNode) {
        Optional<ParserRuleContext> exprNode = ExtractorUtils.findFirstChildNode((ParserRuleContext) whereNode.getChild(1), RuleName.EXPR);
        return exprNode.isPresent() ? predicateExtractor.extract(placeholderIndexes, exprNode.get()) : Optional.<OrConditionSegment>absent();
    }
}
