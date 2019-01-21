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

package org.apache.shardingsphere.core.parsing.antlr.extractor.impl.dql;

import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.apache.shardingsphere.core.parsing.antlr.extractor.OptionalSQLSegmentExtractor;
import org.apache.shardingsphere.core.parsing.antlr.extractor.impl.dql.item.SelectItemExtractor;
import org.apache.shardingsphere.core.parsing.antlr.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parsing.antlr.extractor.util.RuleName;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.SelectClauseSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.select.SelectItemSegment;
import org.apache.shardingsphere.core.parsing.lexer.token.DefaultKeyword;

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
        for (int i = 0; i < selectExpressionsNode.getChildCount(); i++) {
            ParseTree selectExpressionNode = selectExpressionsNode.getChild(i);
            if (selectExpressionNode instanceof TerminalNodeImpl) {
                continue;
            }
            Optional<? extends SelectItemSegment> selectItemSegment = selectItemExtractor.extract((ParserRuleContext) selectExpressionNode);
            if (selectItemSegment.isPresent()) {
                result.getSelectItems().add(selectItemSegment.get());
            }
        }
        return Optional.of(result);
    }
    
    private boolean hasDistinct(final ParserRuleContext selectClauseNode) {
        return selectClauseNode.getChildCount() > 2 && DefaultKeyword.DISTINCT.name().equalsIgnoreCase(selectClauseNode.getChild(1).getText());
    }
}
