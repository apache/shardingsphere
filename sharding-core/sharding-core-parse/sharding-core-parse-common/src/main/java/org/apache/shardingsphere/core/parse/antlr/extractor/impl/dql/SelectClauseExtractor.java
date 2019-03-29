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

package org.apache.shardingsphere.core.parse.antlr.extractor.impl.dql;

import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.parse.antlr.extractor.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.core.parse.antlr.extractor.impl.dql.item.SelectItemExtractor;
import org.apache.shardingsphere.core.parse.antlr.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parse.antlr.extractor.util.RuleName;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.SelectClauseSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.select.SelectItemSegment;

/**
 * Select clause extractor.
 *
 * @author duhongjun
 * @author panjuan
 */
public final class SelectClauseExtractor implements OptionalSQLSegmentExtractor {
    
    private final SelectItemExtractor selectItemExtractor = new SelectItemExtractor();
    
    @Override
    public Optional<SelectClauseSegment> extract(final ParserRuleContext ancestorNode) {
        ParserRuleContext selectClauseNode = ExtractorUtils.getFirstChildNode(ancestorNode, RuleName.SELECT_CLAUSE);
        ParserRuleContext selectExpressionsNode = ExtractorUtils.getFirstChildNode(selectClauseNode, RuleName.SELECT_EXPRS);
        SelectClauseSegment result = new SelectClauseSegment(selectExpressionsNode.getStart().getStartIndex(), selectExpressionsNode.getStop().getStopIndex(), hasDistinct(selectClauseNode));
        Optional<ParserRuleContext> unqualifiedShorthandNode = ExtractorUtils.findFirstChildNode(selectClauseNode, RuleName.UNQUALIFIED_SHORTHAND);
        if (unqualifiedShorthandNode.isPresent()) {
            setUnqualifiedShorthandSelectItemSegment(unqualifiedShorthandNode.get(), result);
        }
        setSelectItemSegment(selectClauseNode, result);
        return Optional.of(result);
    }
    
    private void setUnqualifiedShorthandSelectItemSegment(final ParserRuleContext unqualifiedShorthandNode, final SelectClauseSegment selectClauseSegment) {
        Optional<? extends SelectItemSegment> unqualifiedShorthandSelectItemSegment = selectItemExtractor.extract(unqualifiedShorthandNode);
        if (unqualifiedShorthandSelectItemSegment.isPresent()) {
            selectClauseSegment.getSelectItems().add(unqualifiedShorthandSelectItemSegment.get());
        }
    }
    
    private void setSelectItemSegment(final ParserRuleContext selectClauseNode, final SelectClauseSegment selectClauseSegment) {
        for (ParserRuleContext each : ExtractorUtils.getAllDescendantNodes(selectClauseNode, RuleName.SELECT_EXPR)) {
            Optional<? extends SelectItemSegment> selectItemSegment = selectItemExtractor.extract(each);
            if (selectItemSegment.isPresent()) {
                selectClauseSegment.getSelectItems().add(selectItemSegment.get());
            }
        }
    }
    
    private boolean hasDistinct(final ParserRuleContext selectClauseNode) {
        return ExtractorUtils.findFirstChildNode(ExtractorUtils.getFirstChildNode(selectClauseNode, RuleName.SELECT_SPECIFICATION), RuleName.DISTINCT).isPresent();
    }
}
