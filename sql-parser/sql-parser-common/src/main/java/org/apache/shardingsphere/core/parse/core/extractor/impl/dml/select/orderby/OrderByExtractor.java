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

package org.apache.shardingsphere.core.parse.core.extractor.impl.dml.select.orderby;

import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.parse.core.extractor.api.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.core.parse.core.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parse.core.extractor.util.RuleName;
import org.apache.shardingsphere.core.parse.sql.segment.dml.order.OrderBySegment;

import java.util.Map;

/**
 * Order by extractor.
 *
 * @author duhongjun
 * @author zhangliang
 */
@RequiredArgsConstructor
public abstract class OrderByExtractor implements OptionalSQLSegmentExtractor {
    
    private final OrderByItemExtractor orderByItemExtractor;
    
    @Override
    public final Optional<OrderBySegment> extract(final ParserRuleContext ancestorNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        Optional<ParserRuleContext> orderByNode = ExtractorUtils.findFirstChildNode(findMainQueryNode(ancestorNode), RuleName.ORDER_BY_CLAUSE);
        return orderByNode.isPresent() ? Optional.of(new OrderBySegment(orderByNode.get().getStart().getStartIndex(), orderByNode.get().getStop().getStopIndex(), 
                orderByItemExtractor.extract(orderByNode.get(), parameterMarkerIndexes))) : Optional.<OrderBySegment>absent();
    }
    
    private ParserRuleContext findMainQueryNode(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> tableReferencesNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.TABLE_REFERENCES);
        if (!tableReferencesNode.isPresent()) {
            return ancestorNode;
        }
        Optional<ParserRuleContext> subqueryNode = ExtractorUtils.findSingleNodeFromFirstDescendant(tableReferencesNode.get(), RuleName.SUBQUERY);
        if (subqueryNode.isPresent()) {
            return findMainQueryNode(subqueryNode.get());
        }
        return ancestorNode;
    }
}
