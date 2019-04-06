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

package org.apache.shardingsphere.core.parse.antlr.extractor.impl.dml.insert;

import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.parse.antlr.extractor.CollectionSQLSegmentExtractor;
import org.apache.shardingsphere.core.parse.antlr.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parse.antlr.extractor.util.RuleName;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.InsertValuesSegment;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Insert assignment values group extractor.
 *
 * @author zhangliang
 */
public final class InsertAssignmentValuesGroupExtractor implements CollectionSQLSegmentExtractor {
    
    private InsertAssignmentValuesExtractor insertAssignmentValuesExtractor;
    
    @Override
    public Collection<InsertValuesSegment> extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> insertValuesClauseNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.INSERT_VALUES_CLAUSE);
        if (!insertValuesClauseNode.isPresent()) {
            return Collections.emptyList();
        }
        Collection<InsertValuesSegment> result = new LinkedList<>();
        insertAssignmentValuesExtractor = new InsertAssignmentValuesExtractor(getPlaceholderIndexes(ancestorNode));
        for (ParserRuleContext each : ExtractorUtils.getAllDescendantNodes(insertValuesClauseNode.get(), RuleName.ASSIGNMENT_VALUES)) {
            Optional<InsertValuesSegment> insertValuesSegment = insertAssignmentValuesExtractor.extract(each);
            if (insertValuesSegment.isPresent()) {
                result.add(insertValuesSegment.get());
            }
        }
        return result;
    }
    
    private Map<ParserRuleContext, Integer> getPlaceholderIndexes(final ParserRuleContext rootNode) {
        Collection<ParserRuleContext> placeholderNodes = ExtractorUtils.getAllDescendantNodes(rootNode, RuleName.QUESTION);
        Map<ParserRuleContext, Integer> result = new HashMap<>(placeholderNodes.size(), 1);
        int index = 0;
        for (ParserRuleContext each : placeholderNodes) {
            result.put(each, index++);
        }
        return result;
    }
}
