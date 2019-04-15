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
import lombok.Setter;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.parse.antlr.extractor.api.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.core.parse.antlr.extractor.api.PlaceholderIndexesAware;
import org.apache.shardingsphere.core.parse.antlr.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parse.antlr.extractor.util.RuleName;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.WhereSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.condition.SubqueryConditionSegment;

import java.util.Collection;
import java.util.Map;

/**
 * Subquery condition extractor.
 *
 * @author duhongjun
 */
@Setter
public final class SubqueryConditionExtractor implements OptionalSQLSegmentExtractor, PlaceholderIndexesAware {
    
    private final SelectWhereExtractor selectWhereExtractor = new SelectWhereExtractor();
    
    private Map<ParserRuleContext, Integer> placeholderIndexes;
    
    @Override
    public Optional<SubqueryConditionSegment> extract(final ParserRuleContext ancestorNode) {
        selectWhereExtractor.setPlaceholderIndexes(placeholderIndexes);
        Collection<ParserRuleContext> suQueryNodes = ExtractorUtils.getAllDescendantNodes(ancestorNode, RuleName.SUBQUERY);
        SubqueryConditionSegment result = new SubqueryConditionSegment();
        for (ParserRuleContext each : suQueryNodes) {
            Optional<WhereSegment> condition = selectWhereExtractor.extract(each, ancestorNode);
            if (condition.isPresent()) {
                result.getOrConditions().add(condition.get().getConditions());
            }
        }
        return Optional.of(result);
    }
}
