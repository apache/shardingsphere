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

package org.apache.shardingsphere.core.parse.antlr.extractor.impl.dml.select;

import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.parse.antlr.extractor.api.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.core.parse.antlr.extractor.impl.dml.select.item.SelectItemExtractor;
import org.apache.shardingsphere.core.parse.antlr.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parse.antlr.extractor.util.RuleName;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.SelectItemsSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.item.SelectItemSegment;

/**
 * Select items extractor.
 *
 * @author duhongjun
 * @author panjuan
 */
public final class SelectItemsExtractor implements OptionalSQLSegmentExtractor {
    
    private final SelectItemExtractor selectItemExtractor = new SelectItemExtractor();
    
    @Override
    public Optional<SelectItemsSegment> extract(final ParserRuleContext ancestorNode) {
        ParserRuleContext selectItemsNode = ExtractorUtils.getFirstChildNode(ancestorNode, RuleName.SELECT_ITEMS);
        SelectItemsSegment result = new SelectItemsSegment(selectItemsNode.getStart().getStartIndex(), selectItemsNode.getStop().getStopIndex(), extractDistinct(ancestorNode));
        Optional<ParserRuleContext> unqualifiedShorthandNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.UNQUALIFIED_SHORTHAND);
        if (unqualifiedShorthandNode.isPresent()) {
            setUnqualifiedShorthandSelectItemSegment(unqualifiedShorthandNode.get(), result);
        }
        setSelectItemSegment(ancestorNode, result);
        return Optional.of(result);
    }
    
    private void setUnqualifiedShorthandSelectItemSegment(final ParserRuleContext unqualifiedShorthandNode, final SelectItemsSegment selectItemsSegment) {
        Optional<? extends SelectItemSegment> unqualifiedShorthandSelectItemSegment = selectItemExtractor.extract(unqualifiedShorthandNode);
        if (unqualifiedShorthandSelectItemSegment.isPresent()) {
            selectItemsSegment.getSelectItems().add(unqualifiedShorthandSelectItemSegment.get());
        }
    }
    
    private void setSelectItemSegment(final ParserRuleContext ancestorNode, final SelectItemsSegment selectItemsSegment) {
        for (ParserRuleContext each : ExtractorUtils.getAllDescendantNodes(ancestorNode, RuleName.SELECT_ITEM)) {
            Optional<? extends SelectItemSegment> selectItemSegment = selectItemExtractor.extract(each);
            if (selectItemSegment.isPresent()) {
                selectItemsSegment.getSelectItems().add(selectItemSegment.get());
            }
        }
    }
    
    private boolean extractDistinct(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> duplicateSpecificationNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.DUPLICATE_SPECIFICATION);
        return duplicateSpecificationNode.isPresent()
                && (duplicateSpecificationNode.get().getText().equalsIgnoreCase("DISTINCT") || duplicateSpecificationNode.get().getText().equalsIgnoreCase("DISTINCTROW"));
    }
}
