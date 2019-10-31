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

package org.apache.shardingsphere.core.parse.core.extractor.impl.dml.insert;

import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.parse.core.extractor.api.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.core.parse.core.extractor.impl.common.column.ColumnExtractor;
import org.apache.shardingsphere.core.parse.core.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parse.core.extractor.util.RuleName;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.OnDuplicateKeyColumnsSegment;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * On duplicate key columns extractor.
 *
 * @author zhangliang
 */
public final class OnDuplicateKeyColumnsExtractor implements OptionalSQLSegmentExtractor {
    
    private final ColumnExtractor columnExtractor = new ColumnExtractor();
    
    @Override
    public Optional<OnDuplicateKeyColumnsSegment> extract(final ParserRuleContext ancestorNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        Optional<ParserRuleContext> onDuplicateKeyClauseNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.ON_DUPLICATE_KEY_CLAUSE);
        if (!onDuplicateKeyClauseNode.isPresent()) {
            return Optional.absent();
        }
        return Optional.of(new OnDuplicateKeyColumnsSegment(onDuplicateKeyClauseNode.get().getStart().getStartIndex(), onDuplicateKeyClauseNode.get().getStop().getStopIndex(), 
                extractColumnSegments(onDuplicateKeyClauseNode.get(), parameterMarkerIndexes)));
    }
    
    private Collection<ColumnSegment> extractColumnSegments(final ParserRuleContext onDuplicateKeyClauseNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        Collection<ColumnSegment> result = new LinkedList<>();
        for (ParserRuleContext each : ExtractorUtils.getAllDescendantNodes(onDuplicateKeyClauseNode, RuleName.COLUMN_NAME)) {
            Optional<ColumnSegment> columnSegment = columnExtractor.extract(each, parameterMarkerIndexes);
            if (columnSegment.isPresent()) {
                result.add(columnSegment.get());
            }
        }
        return result;
    }
}
