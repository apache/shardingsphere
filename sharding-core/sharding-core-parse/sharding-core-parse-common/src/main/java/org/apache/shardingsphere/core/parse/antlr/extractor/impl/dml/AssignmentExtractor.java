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
import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.parse.antlr.extractor.impl.common.column.ColumnExtractor;
import org.apache.shardingsphere.core.parse.antlr.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parse.antlr.extractor.util.RuleName;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.expr.CommonExpressionSegment;

import java.util.Map;

/**
 * Assignment extractor.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class AssignmentExtractor {
    
    private final ColumnExtractor columnExtractor = new ColumnExtractor();
    
    private final ExpressionExtractor expressionExtractor = new ExpressionExtractor();
    
    /**
     * Extract.
     * 
     * @param parameterMarkerIndexes parameter marker indexes
     * @param ancestorNode ancestor node
     * @return assignment segment
     */
    public Optional<AssignmentSegment> extract(final Map<ParserRuleContext, Integer> parameterMarkerIndexes, final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> assignmentNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.ASSIGNMENT);
        if (!assignmentNode.isPresent()) {
            return Optional.absent();
        }
        Optional<ColumnSegment> columnSegment = columnExtractor.extract((ParserRuleContext) assignmentNode.get().getChild(0), parameterMarkerIndexes);
        Preconditions.checkState(columnSegment.isPresent());
        CommonExpressionSegment expressionSegment = expressionExtractor.extractCommonExpressionSegment(parameterMarkerIndexes, (ParserRuleContext) assignmentNode.get().getChild(2));
        return Optional.of(new AssignmentSegment(columnSegment.get(), expressionSegment));
    }
}
