/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.antlr.extractor.impl;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import com.google.common.base.Optional;

import io.shardingsphere.core.parsing.antlr.extractor.OptionalSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.util.ExtractorUtils;
import io.shardingsphere.core.parsing.antlr.extractor.util.RuleName;
import io.shardingsphere.core.parsing.antlr.sql.segment.SelectClauseSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.ExpressionSegment;
import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;

/**
 * Select clause extractor.
 *
 * @author duhongjun
 * @author panjuan
 */
public final class SelectClauseExtractor implements OptionalSQLSegmentExtractor {
    
    @Override
    public Optional<SelectClauseSegment> extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> selectClauseNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.SELECT_CLAUSE);
        if (!selectClauseNode.isPresent()) {
            return Optional.absent();
        }
        boolean hasDistinct = false;
        if (2 < selectClauseNode.get().getChildCount()) {
            if (DefaultKeyword.DISTINCT.name().equalsIgnoreCase(selectClauseNode.get().getChild(1).getText())) {
                hasDistinct = true;
            }
        }
        Optional<ParserRuleContext> selectExpressionNode = ExtractorUtils.findFirstChildNode(selectClauseNode.get(), RuleName.SELECT_EXPRS);
        if (!selectExpressionNode.isPresent()) {
            return Optional.absent();
        }
        SelectClauseSegment result = new SelectClauseSegment(selectExpressionNode.get().getStart().getStartIndex(), selectExpressionNode.get().getStop().getStopIndex() + 2);
        result.setHasDistinct(hasDistinct);
        ExpressionExtractor expressionExtractor = new ExpressionExtractor();
        for (int i = 0; i < selectExpressionNode.get().getChildCount(); i++) {
            ParseTree childNode = selectExpressionNode.get().getChild(i);
            if (childNode instanceof TerminalNodeImpl) {
                continue;
            }
            Optional<ExpressionSegment> expressionSegment = expressionExtractor.extract((ParserRuleContext) childNode);
            if (expressionSegment.isPresent()) {
                result.getExpressions().add(expressionSegment.get());
            }
        }
        return Optional.of(result);
    }
}
