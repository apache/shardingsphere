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
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.parse.antlr.extractor.api.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.core.parse.antlr.extractor.impl.dml.ExpressionExtractor;
import org.apache.shardingsphere.core.parse.antlr.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parse.antlr.extractor.util.RuleName;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.InsertValuesSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.expr.CommonExpressionSegment;
import org.apache.shardingsphere.core.parse.lexer.token.DefaultKeyword;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * Insert assignment values extractor.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class InsertAssignmentValuesExtractor implements OptionalSQLSegmentExtractor {
    
    private final Map<ParserRuleContext, Integer> placeholderIndexes;
    
    private final ExpressionExtractor expressionExtractor = new ExpressionExtractor();
    
    @Override
    public Optional<InsertValuesSegment> extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> assignmentValuesNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.ASSIGNMENT_VALUES);
        if (!assignmentValuesNode.isPresent()) {
            return Optional.absent();
        }
        return Optional.of(new InsertValuesSegment(
                DefaultKeyword.VALUES, ExtractorUtils.getAllDescendantNodes(assignmentValuesNode.get(), RuleName.QUESTION).size(), extractCommonExpressionSegments(assignmentValuesNode.get())));
    }
    
    private Collection<CommonExpressionSegment> extractCommonExpressionSegments(final ParserRuleContext assignmentValuesNode) {
        Collection<CommonExpressionSegment> result = new LinkedList<>();
        for (ParserRuleContext each : ExtractorUtils.getAllDescendantNodes(assignmentValuesNode, RuleName.ASSIGNMENT_VALUE)) {
            result.add(expressionExtractor.extractCommonExpressionSegment(placeholderIndexes, each));
        }
        return result;
    }
}
