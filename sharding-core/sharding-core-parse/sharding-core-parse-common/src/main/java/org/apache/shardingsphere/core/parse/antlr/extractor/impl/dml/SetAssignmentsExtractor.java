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
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.assignment.SetAssignmentsSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.expr.CommonExpressionSegment;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Set assignments extractor.
 *
 * @author zhangliang
 */
public final class SetAssignmentsExtractor implements OptionalSQLSegmentExtractor {
    
    private AssignmentExtractor assignmentExtractor;
    
    @Override
    public Optional<SetAssignmentsSegment> extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> setAssignmentsClauseNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.SET_ASSIGNMENTS_CLAUSE);
        if (!setAssignmentsClauseNode.isPresent()) {
            return Optional.absent();
        }
        Collection<ColumnSegment> columnSegments = new LinkedList<>();
        Collection<CommonExpressionSegment> valueSegments = new LinkedList<>();
        assignmentExtractor = new AssignmentExtractor(getPlaceholderIndexes(ancestorNode));
        for (ParserRuleContext each : ExtractorUtils.getAllDescendantNodes(ancestorNode, RuleName.ASSIGNMENT)) {
            Optional<AssignmentSegment> assignmentSegment = assignmentExtractor.extract(each);
            if (assignmentSegment.isPresent()) {
                columnSegments.add(assignmentSegment.get().getColumn());
                valueSegments.add(assignmentSegment.get().getValue());
            }
        }
        return Optional.of(new SetAssignmentsSegment(setAssignmentsClauseNode.get().getStart().getStartIndex(), columnSegments, valueSegments));
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
