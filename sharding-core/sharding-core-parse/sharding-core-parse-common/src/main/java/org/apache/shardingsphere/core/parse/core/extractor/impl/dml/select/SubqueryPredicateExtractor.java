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
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.parse.core.extractor.api.CollectionSQLSegmentExtractor;
import org.apache.shardingsphere.core.parse.core.extractor.impl.dml.PredicateExtractor;
import org.apache.shardingsphere.core.parse.core.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parse.core.extractor.util.RuleName;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.OrPredicateSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.SubqueryPredicateSegment;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * Subquery predicate extractor.
 *
 * @author duhongjun
 */
public final class SubqueryPredicateExtractor implements CollectionSQLSegmentExtractor {
    
    private final PredicateExtractor predicateExtractor = new PredicateExtractor();
    
    @Override
    public Collection<SubqueryPredicateSegment> extract(final ParserRuleContext ancestorNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        Collection<ParserRuleContext> subqueryNodes = ExtractorUtils.getAllDescendantNodes(ancestorNode, RuleName.SUBQUERY);
        Collection<SubqueryPredicateSegment> result = new LinkedList<>();
        for (ParserRuleContext each : subqueryNodes) {
            Optional<OrPredicateSegment> orPredicateSegment = predicateExtractor.extract(each, parameterMarkerIndexes);
            if (orPredicateSegment.isPresent()) {
                result.add(new SubqueryPredicateSegment(orPredicateSegment.get().getAndPredicates()));
            }
        }
        return result;
    }
}
