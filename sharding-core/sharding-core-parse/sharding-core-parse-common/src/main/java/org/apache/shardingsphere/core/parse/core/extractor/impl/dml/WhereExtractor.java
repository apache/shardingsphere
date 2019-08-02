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

package org.apache.shardingsphere.core.parse.core.extractor.impl.dml;

import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.parse.core.extractor.api.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.core.parse.core.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parse.core.extractor.util.RuleName;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.OrPredicateSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.WhereSegment;

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
        Optional<ParserRuleContext> whereNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.WHERE_CLAUSE);
        if (!whereNode.isPresent()) {
            return Optional.absent();
        }
        WhereSegment result = new WhereSegment(whereNode.get().getStart().getStartIndex(), whereNode.get().getStop().getStopIndex(), parameterMarkerIndexes.size());
        Optional<OrPredicateSegment> orPredicateSegment = predicateExtractor.extract(whereNode.get(), parameterMarkerIndexes);
        if (orPredicateSegment.isPresent()) {
            result.getAndPredicates().addAll(orPredicateSegment.get().getAndPredicates());
        }
        Collection<ParserRuleContext> parameterMarkerNodes = ExtractorUtils.getAllDescendantNodes(whereNode.get(), RuleName.PARAMETER_MARKER);
        if (!parameterMarkerNodes.isEmpty()) {
            result.setParameterStartIndex(parameterMarkerIndexes.get(parameterMarkerNodes.iterator().next()));
        }
        return Optional.of(result);
    }
}
