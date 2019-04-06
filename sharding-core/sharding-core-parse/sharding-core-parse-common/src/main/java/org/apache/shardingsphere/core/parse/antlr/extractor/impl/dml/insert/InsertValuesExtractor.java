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
import org.apache.shardingsphere.core.parse.antlr.extractor.api.CollectionSQLSegmentExtractor;
import org.apache.shardingsphere.core.parse.antlr.extractor.impl.dml.ExpressionExtractor;
import org.apache.shardingsphere.core.parse.antlr.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parse.antlr.extractor.util.RuleName;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.InsertValuesSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.expr.CommonExpressionSegment;
import org.apache.shardingsphere.core.parse.lexer.token.DefaultKeyword;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Insert values group extractor.
 *
 * @author zhangliang
 */
public final class InsertValuesExtractor implements CollectionSQLSegmentExtractor {
    
    private final ExpressionExtractor expressionExtractor = new ExpressionExtractor();
    
    @Override
    public Collection<InsertValuesSegment> extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> insertValuesClauseNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.INSERT_VALUES_CLAUSE);
        if (!insertValuesClauseNode.isPresent()) {
            return Collections.emptyList();
        }
        Collection<InsertValuesSegment> result = new LinkedList<>();
        Map<ParserRuleContext, Integer> placeholderIndexes = getPlaceholderIndexes(ancestorNode);
        for (ParserRuleContext each : ExtractorUtils.getAllDescendantNodes(insertValuesClauseNode.get(), RuleName.ASSIGNMENT_VALUES)) {
            result.add(new InsertValuesSegment(DefaultKeyword.VALUES, extractCommonExpressionSegments(each, placeholderIndexes)));
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
    
    private Collection<CommonExpressionSegment> extractCommonExpressionSegments(final ParserRuleContext assignmentValuesNode, final Map<ParserRuleContext, Integer> placeholderIndexes) {
        Collection<CommonExpressionSegment> result = new LinkedList<>();
        for (ParserRuleContext each : ExtractorUtils.getAllDescendantNodes(assignmentValuesNode, RuleName.ASSIGNMENT_VALUE)) {
            result.add(expressionExtractor.extractCommonExpressionSegment(placeholderIndexes, each));
        }
        return result;
    }
}
