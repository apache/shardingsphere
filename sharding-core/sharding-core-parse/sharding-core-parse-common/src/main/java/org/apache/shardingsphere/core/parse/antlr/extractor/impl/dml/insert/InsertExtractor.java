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
import org.apache.shardingsphere.core.parse.antlr.extractor.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.core.parse.antlr.extractor.impl.ColumnExtractor;
import org.apache.shardingsphere.core.parse.antlr.extractor.impl.expression.ExpressionExtractor;
import org.apache.shardingsphere.core.parse.antlr.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parse.antlr.extractor.util.RuleName;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.InsertSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.InsertValuesSegment;
import org.apache.shardingsphere.core.parse.lexer.token.DefaultKeyword;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Insert extractor.
 *
 * @author duhongjun
 * @author panjuan
 */
public final class InsertExtractor implements OptionalSQLSegmentExtractor {
    
    private ExpressionExtractor expressionExtractor = new ExpressionExtractor();
    
    private ColumnExtractor columnExtractor = new ColumnExtractor();
    
    @Override
    public Optional<InsertSegment> extract(final ParserRuleContext ancestorNode) {
        InsertSegment result = new InsertSegment();
        Map<ParserRuleContext, Integer> placeholderIndexes = getPlaceholderIndexes(ancestorNode);
        extractValuesColumn(placeholderIndexes, ancestorNode, result);
        if (result.getValuesList().isEmpty()) {
            extractSetColumn(placeholderIndexes, ancestorNode, result);
        }
        result.setColumnClauseStartIndex(ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.TABLE_NAME).get().getStop().getStopIndex() + 1);
        result.setInsertValuesListLastIndex(ancestorNode.getStop().getStopIndex());
        return Optional.of(result);
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
    
    private void extractValuesColumn(final Map<ParserRuleContext, Integer> placeholderIndexes, final ParserRuleContext ancestorNode, final InsertSegment insertSegment) {
        Optional<ParserRuleContext> columnClauseNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.INSERT_COLUMNS_CLAUSE);
        if (!columnClauseNode.isPresent()) {
            return;
        }
        Optional<ParserRuleContext> valueClauseNode = ExtractorUtils.findFirstChildNode(columnClauseNode.get(), RuleName.INSERT_VALUES_CLAUSE);
        if (!valueClauseNode.isPresent()) {
            return;
        }
        Collection<ParserRuleContext> insertValuesNodes = ExtractorUtils.getAllDescendantNodes(valueClauseNode.get(), RuleName.ASSIGNMENT_VALUES);
        for (ParserRuleContext each : insertValuesNodes) {
            Collection<ParserRuleContext> questionNodes = ExtractorUtils.getAllDescendantNodes(each, RuleName.QUESTION);
            InsertValuesSegment insertValuesSegment = new InsertValuesSegment(DefaultKeyword.VALUES, each.getStart().getStartIndex(), each.getStop().getStopIndex(), questionNodes.size());
            insertSegment.getValuesList().add(insertValuesSegment);
            for (ParserRuleContext eachValue : ExtractorUtils.getAllDescendantNodes(each, RuleName.ASSIGNMENT_VALUE)) {
                insertValuesSegment.getValues().add(expressionExtractor.extractCommonExpressionSegment(placeholderIndexes, eachValue));
            }
        }
    }
    
    private void extractSetColumn(final Map<ParserRuleContext, Integer> placeholderIndexes, final ParserRuleContext ancestorNode, final InsertSegment insertSegment) {
        Optional<ParserRuleContext> setClauseNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.SET_ASSIGNMENTS_CLAUSE);
        if (!setClauseNode.isPresent()) {
            return;
        }
        Optional<ParserRuleContext> assignmentListNode = ExtractorUtils.findFirstChildNode(setClauseNode.get(), RuleName.ASSIGNMENTS);
        if (!assignmentListNode.isPresent()) {
            return;
        }
        Collection<ParserRuleContext> questionNodes = ExtractorUtils.getAllDescendantNodes(assignmentListNode.get(), RuleName.QUESTION);
        InsertValuesSegment insertValuesSegment = new InsertValuesSegment(DefaultKeyword.SET, assignmentListNode.get().getStart().getStartIndex(),
                assignmentListNode.get().getStop().getStopIndex(), questionNodes.size());
        insertSegment.getValuesList().add(insertValuesSegment);
        Collection<ParserRuleContext> assignments = ExtractorUtils.getAllDescendantNodes(assignmentListNode.get(), RuleName.ASSIGNMENT);
        insertSegment.setInsertValuesListLastIndex(assignmentListNode.get().getStop().getStopIndex());
        for (ParserRuleContext each : assignments) {
            ParserRuleContext columnNode = (ParserRuleContext) each.getChild(0);
            insertSegment.getColumns().add(columnExtractor.extract(columnNode).get());
            insertValuesSegment.getValues().add(expressionExtractor.extractCommonExpressionSegment(placeholderIndexes, (ParserRuleContext) each.getChild(2)));
        }
    }
}
