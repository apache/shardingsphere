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

package org.apache.shardingsphere.sql.parser.core.extractor.impl.dml.update;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.sql.parser.core.extractor.api.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.sql.parser.core.extractor.impl.common.column.ColumnExtractor;
import org.apache.shardingsphere.sql.parser.core.extractor.impl.common.expression.ExpressionExtractor;
import org.apache.shardingsphere.sql.parser.core.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.sql.parser.core.extractor.util.RuleName;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.ExpressionSegment;

import java.util.Map;

/**
 * Assignment extractor.
 *
 * @author zhangliang
 */
public final class AssignmentExtractor implements OptionalSQLSegmentExtractor {
    
    private final ColumnExtractor columnExtractor = new ColumnExtractor();
    
    private final ExpressionExtractor expressionExtractor = new ExpressionExtractor();
    
    @Override
    public Optional<AssignmentSegment> extract(final ParserRuleContext ancestorNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        Optional<ParserRuleContext> assignmentNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.ASSIGNMENT);
        if (!assignmentNode.isPresent()) {
            return Optional.absent();
        }
        Optional<ColumnSegment> columnSegment = columnExtractor.extract((ParserRuleContext) assignmentNode.get().getChild(0), parameterMarkerIndexes);
        Preconditions.checkState(columnSegment.isPresent());
        ExpressionSegment assignmentValue = getAssignmentValue(parameterMarkerIndexes, assignmentNode.get());
        return Optional.of(new AssignmentSegment(assignmentNode.get().getStart().getStartIndex(), assignmentNode.get().getStop().getStopIndex(), columnSegment.get(), assignmentValue));
    }
    
    private ExpressionSegment getAssignmentValue(final Map<ParserRuleContext, Integer> parameterMarkerIndexes, final ParserRuleContext assignmentNode) {
        Optional<? extends ExpressionSegment> result = expressionExtractor.extract((ParserRuleContext) assignmentNode.getChild(2), parameterMarkerIndexes);
        Preconditions.checkState(result.isPresent());
        return result.get();
    }
}

