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

package org.apache.shardingsphere.core.parsing.antlr.extractor.impl;

import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.shardingsphere.core.parsing.antlr.extractor.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.core.parsing.antlr.extractor.impl.expression.ExpressionExtractor;
import org.apache.shardingsphere.core.parsing.antlr.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parsing.antlr.extractor.util.RuleName;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.InsertSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.InsertValuesSegment;
import org.apache.shardingsphere.core.parsing.lexer.token.DefaultKeyword;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Insert extractor.
 *
 * @author duhongjun
 */
public final class InsertExtractor implements OptionalSQLSegmentExtractor {
    
    private ExpressionExtractor expressionExtractor;
    
    private ColumnSegmentExtractor columnSegmentExtractor;
    
    @Override
    public Optional<InsertSegment> extract(final ParserRuleContext ancestorNode) {
        InsertSegment result = new InsertSegment();
        expressionExtractor = new ExpressionExtractor(getPlaceholderIndexes(ancestorNode));
        columnSegmentExtractor = new ColumnSegmentExtractor();
        extractValuesColumn(ancestorNode, result);
        if (result.getValuesList().isEmpty()) {
            extractSetColumn(ancestorNode, result);
        }
        extractDuplicateKeys(ancestorNode, result);
        return Optional.of(result);
    }
    
    private Map<ParserRuleContext, Integer> getPlaceholderIndexes(final ParserRuleContext rootNode) {
        Collection<ParserRuleContext> questionNodes = ExtractorUtils.getAllDescendantNodes(rootNode, RuleName.QUESTION);
        Map<ParserRuleContext, Integer> result = new HashMap<>(questionNodes.size(), 1);
        int index = 0;
        for (ParserRuleContext each : questionNodes) {
            result.put(each, index++);
        }
        return result;
    }
    
    private void extractValuesColumn(final ParserRuleContext ancestorNode, final InsertSegment insertSegment) {
        Optional<ParserRuleContext> columnClauseNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.COLUMN_CLAUSE);
        if (!columnClauseNode.isPresent()) {
            return;
        }
        insertSegment.setColumnClauseStartPosition(columnClauseNode.get().getStart().getStartIndex() - 1);
        Optional<ParserRuleContext> columnListNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.COLUMN_LIST);
        if (columnListNode.isPresent()) {
            insertSegment.setColumnsListLastPosition(columnListNode.get().getStop().getStopIndex());
        } else {
            insertSegment.setColumnsListLastPosition(insertSegment.getColumnClauseStartPosition());
        }
        for (ParserRuleContext each : ExtractorUtils.getAllDescendantNodes(columnClauseNode.get(), RuleName.COLUMN_NAME)) {
            insertSegment.getColumns().add(columnSegmentExtractor.extract(each).get());
        }
        Optional<ParserRuleContext> valueClauseNode = ExtractorUtils.findFirstChildNode(columnClauseNode.get(), RuleName.VALUE_CLAUSE);
        if (!valueClauseNode.isPresent()) {
            return;
        }
        Collection<ParserRuleContext> assignmentValueListNodes = ExtractorUtils.getAllDescendantNodes(valueClauseNode.get(), RuleName.ASSIGNMENT_VALUE_LIST);
        insertSegment.setInsertValueStartPosition(((TerminalNode) assignmentValueListNodes.iterator().next().getChild(0)).getSymbol().getStartIndex());
        for (ParserRuleContext each : assignmentValueListNodes) {
            Collection<ParserRuleContext> questionNodes = ExtractorUtils.getAllDescendantNodes(each, RuleName.QUESTION);
            InsertValuesSegment insertValuesSegment = new InsertValuesSegment(DefaultKeyword.VALUES, each.getStart().getStartIndex(), each.getStop().getStopIndex(), questionNodes.size());
            insertSegment.getValuesList().add(insertValuesSegment);
            for (ParserRuleContext eachValue : ExtractorUtils.getAllDescendantNodes(each, RuleName.ASSIGNMENT_VALUE)) {
                insertValuesSegment.getValues().add(expressionExtractor.extractCommonExpressionSegment(eachValue));
            }
        }
    }
    
    private void extractSetColumn(final ParserRuleContext ancestorNode, final InsertSegment insertSegment) {
        Optional<ParserRuleContext> setClauseNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.SET_CLAUSE);
        if (!setClauseNode.isPresent()) {
            return;
        }
        Optional<ParserRuleContext> assignmentListNode = ExtractorUtils.findFirstChildNode(setClauseNode.get(), RuleName.ASSIGNMENT_LIST);
        if (!assignmentListNode.isPresent()) {
            return;
        }
        insertSegment.setInsertValueStartPosition(assignmentListNode.get().getStart().getStartIndex());
        Collection<ParserRuleContext> questionNodes = ExtractorUtils.getAllDescendantNodes(assignmentListNode.get(), RuleName.QUESTION);
        InsertValuesSegment insertValuesSegment = new InsertValuesSegment(DefaultKeyword.SET, assignmentListNode.get().getStart().getStartIndex(),
                assignmentListNode.get().getStop().getStopIndex(), questionNodes.size());
        insertSegment.getValuesList().add(insertValuesSegment);
        Collection<ParserRuleContext> assignments = ExtractorUtils.getAllDescendantNodes(assignmentListNode.get(), RuleName.ASSIGNMENT);
        insertSegment.setInsertValuesListLastPosition(assignmentListNode.get().getStop().getStopIndex());
        for (ParserRuleContext each : assignments) {
            ParserRuleContext columnNode = (ParserRuleContext) each.getChild(0);
            insertSegment.getColumns().add(columnSegmentExtractor.extract(columnNode).get());
            insertValuesSegment.getValues().add(expressionExtractor.extractCommonExpressionSegment((ParserRuleContext) each.getChild(2)));
        }
    }
    
    private void extractDuplicateKeys(final ParserRuleContext ancestorNode, final InsertSegment insertSegment) {
        Optional<ParserRuleContext> onDuplicateClauseNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.ON_DUPLICATE_CLAUSE);
        if (!onDuplicateClauseNode.isPresent()) {
            return;
        }
        for (ParserRuleContext each : ExtractorUtils.getAllDescendantNodes(onDuplicateClauseNode.get(), RuleName.COLUMN_NAME)) {
            insertSegment.getDuplicateKeyColumns().add(each.getText());
        }
    }
}
