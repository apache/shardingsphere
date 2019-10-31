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

package org.apache.shardingsphere.core.parse.core.extractor.impl.dml.update;

import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.parse.core.extractor.api.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.core.parse.core.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parse.core.extractor.util.RuleName;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.SetAssignmentsSegment;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * Set assignments extractor.
 *
 * @author zhangliang
 */
public final class SetAssignmentsExtractor implements OptionalSQLSegmentExtractor {
    
    private final AssignmentExtractor assignmentExtractor = new AssignmentExtractor();
    
    @Override
    public Optional<SetAssignmentsSegment> extract(final ParserRuleContext ancestorNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        Optional<ParserRuleContext> setAssignmentsClauseNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.SET_ASSIGNMENTS_CLAUSE);
        if (!setAssignmentsClauseNode.isPresent()) {
            return Optional.absent();
        }
        Collection<AssignmentSegment> assignmentSegments = new LinkedList<>();
        for (ParserRuleContext each : ExtractorUtils.getAllDescendantNodes(ancestorNode, RuleName.ASSIGNMENT)) {
            Optional<AssignmentSegment> assignmentSegment = assignmentExtractor.extract(each, parameterMarkerIndexes);
            if (assignmentSegment.isPresent()) {
                assignmentSegments.add(assignmentSegment.get());
            }
        }
        return Optional.of(new SetAssignmentsSegment(setAssignmentsClauseNode.get().getStart().getStartIndex(), setAssignmentsClauseNode.get().getStop().getStopIndex(), assignmentSegments));
    }
}
