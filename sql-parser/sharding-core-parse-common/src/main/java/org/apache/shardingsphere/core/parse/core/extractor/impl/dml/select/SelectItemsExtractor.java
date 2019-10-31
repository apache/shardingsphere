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

package org.apache.shardingsphere.core.parse.core.extractor.impl.dml.select;

import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.parse.core.extractor.api.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.core.parse.core.extractor.impl.dml.select.item.SelectItemExtractor;
import org.apache.shardingsphere.core.parse.core.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parse.core.extractor.util.RuleName;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.SelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.SelectItemsSegment;

import java.util.Collection;
import java.util.Map;
import java.util.TreeSet;

/**
 * Select items extractor.
 *
 * @author duhongjun
 * @author panjuan
 */
public final class SelectItemsExtractor implements OptionalSQLSegmentExtractor {
    
    // TODO recognize database type, only oracle and sqlserver can use row number
    private final Collection<String> rowNumberIdentifiers;
    
    private final SelectItemExtractor selectItemExtractor = new SelectItemExtractor();
    
    public SelectItemsExtractor() {
        rowNumberIdentifiers = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        rowNumberIdentifiers.add("rownum");
        rowNumberIdentifiers.add("ROW_NUMBER");
    }
    
    @Override
    public Optional<SelectItemsSegment> extract(final ParserRuleContext ancestorNode, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        ParserRuleContext selectItemsNode = ExtractorUtils.getFirstChildNode(findMainQueryNode(ancestorNode), RuleName.SELECT_ITEMS);
        SelectItemsSegment result = new SelectItemsSegment(selectItemsNode.getStart().getStartIndex(), selectItemsNode.getStop().getStopIndex(), extractDistinct(ancestorNode));
        Optional<ParserRuleContext> unqualifiedShorthandNode = ExtractorUtils.findFirstChildNode(selectItemsNode, RuleName.UNQUALIFIED_SHORTHAND);
        if (unqualifiedShorthandNode.isPresent()) {
            setUnqualifiedShorthandSelectItemSegment(unqualifiedShorthandNode.get(), result, parameterMarkerIndexes);
        }
        setSelectItemSegment(selectItemsNode, result, parameterMarkerIndexes);
        return Optional.of(result);
    }
    
    private void setUnqualifiedShorthandSelectItemSegment(final ParserRuleContext unqualifiedShorthandNode,
                                                          final SelectItemsSegment selectItemsSegment, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        Optional<? extends SelectItemSegment> unqualifiedShorthandSelectItemSegment = selectItemExtractor.extract(unqualifiedShorthandNode, parameterMarkerIndexes);
        if (unqualifiedShorthandSelectItemSegment.isPresent()) {
            selectItemsSegment.getSelectItems().add(unqualifiedShorthandSelectItemSegment.get());
        }
    }
    
    private void setSelectItemSegment(final ParserRuleContext selectItemsNode, final SelectItemsSegment selectItemsSegment, final Map<ParserRuleContext, Integer> parameterMarkerIndexes) {
        for (ParserRuleContext each : ExtractorUtils.getAllDescendantNodes(selectItemsNode, RuleName.SELECT_ITEM)) {
            Optional<? extends SelectItemSegment> selectItemSegment = selectItemExtractor.extract(each, parameterMarkerIndexes);
            if (selectItemSegment.isPresent()) {
                selectItemsSegment.getSelectItems().add(selectItemSegment.get());
            }
        }
    }
    
    private boolean extractDistinct(final ParserRuleContext selectItemsNode) {
        Optional<ParserRuleContext> duplicateSpecificationNode = ExtractorUtils.findFirstChildNode(selectItemsNode, RuleName.DUPLICATE_SPECIFICATION);
        if (duplicateSpecificationNode.isPresent()) {
            String text = duplicateSpecificationNode.get().getText();
            return "DISTINCT".equalsIgnoreCase(text) || "DISTINCTROW".equalsIgnoreCase(text);
        }
        return false;
    }
    
    private ParserRuleContext findMainQueryNode(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> tableReferencesNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.TABLE_REFERENCES);
        if (!tableReferencesNode.isPresent()) {
            return ancestorNode;
        }
        Optional<ParserRuleContext> subqueryNode = ExtractorUtils.findSingleNodeFromFirstDescendant(tableReferencesNode.get(), RuleName.SUBQUERY);
        if (subqueryNode.isPresent()) {
            return findMainQueryNode(subqueryNode.get());
        }
        return ancestorNode;
    }
}
