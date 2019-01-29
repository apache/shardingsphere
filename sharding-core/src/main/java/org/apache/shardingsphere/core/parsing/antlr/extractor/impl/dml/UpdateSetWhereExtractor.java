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

package org.apache.shardingsphere.core.parsing.antlr.extractor.impl.dml;

import java.util.Map;

import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.parsing.antlr.extractor.impl.ColumnSegmentExtractor;
import org.apache.shardingsphere.core.parsing.antlr.extractor.impl.dql.AbstractFromWhereExtractor;
import org.apache.shardingsphere.core.parsing.antlr.extractor.impl.expression.ExpressionExtractor;
import org.apache.shardingsphere.core.parsing.antlr.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parsing.antlr.extractor.util.RuleName;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.FromWhereSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.column.ColumnSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.dml.UpdateSetWhereSegment;

import com.google.common.base.Optional;

/**
 * Update set where extractor.
 *
 * @author duhongjun
 */
public final class UpdateSetWhereExtractor extends AbstractFromWhereExtractor {
    
    private final ExpressionExtractor expressionExtractor = new ExpressionExtractor();
    
    protected FromWhereSegment createSegment() {
        return new UpdateSetWhereSegment();
    }
    
    @Override
    protected Optional<ParserRuleContext> extractTable(final FromWhereSegment fromWhereSegment, final ParserRuleContext ancestorNode, final Map<ParserRuleContext, Integer> placeholderIndexes) {
        Optional<ParserRuleContext> tableReferenceNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.TABLE_REFERENCE);
        if (!tableReferenceNode.isPresent()) {
            return Optional.absent();
        }
        this.extractTableReference(fromWhereSegment, tableReferenceNode.get(), placeholderIndexes);
        extractSetColumns(ancestorNode, (UpdateSetWhereSegment) fromWhereSegment, placeholderIndexes);
        return ExtractorUtils.findFirstChildNodeNoneRecursive(ancestorNode, RuleName.WHERE_CLAUSE);
    }
    
    private void extractSetColumns(final ParserRuleContext ancestorNode, final UpdateSetWhereSegment updateSetWhereSegment, final Map<ParserRuleContext, Integer> placeholderIndexes) {
        Optional<ParserRuleContext> setClauseNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.SET_CLAUSE);
        if (!setClauseNode.isPresent()) {
            return;
        }
        ColumnSegmentExtractor columnSegmentExtractor = new ColumnSegmentExtractor();
        for (ParserRuleContext each : ExtractorUtils.getAllDescendantNodes(setClauseNode.get(), RuleName.ASSIGNMENT)) {
            Optional<ColumnSegment> columnSegment = columnSegmentExtractor.extract((ParserRuleContext) each.getChild(0));
            updateSetWhereSegment.getUpdateColumns().put(columnSegment.get().getName(), expressionExtractor.extract(placeholderIndexes, (ParserRuleContext) each.getChild(2)).get());
        }
    }
}
