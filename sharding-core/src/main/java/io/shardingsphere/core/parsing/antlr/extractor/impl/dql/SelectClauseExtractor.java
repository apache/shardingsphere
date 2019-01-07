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

package io.shardingsphere.core.parsing.antlr.extractor.impl.dql;

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.antlr.extractor.OptionalSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.impl.ExpressionExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.util.ExtractorUtils;
import io.shardingsphere.core.parsing.antlr.extractor.util.RuleName;
import io.shardingsphere.core.parsing.antlr.sql.segment.SelectClauseSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.ExpressionSegment;
import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

/**
 * Select clause extractor.
 *
 * @author duhongjun
 * @author panjuan
 */
public final class SelectClauseExtractor implements OptionalSQLSegmentExtractor {
    
    private final ExpressionExtractor expressionExtractor = new ExpressionExtractor();
    
    @Override
    public Optional<SelectClauseSegment> extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> selectClauseNode = ExtractorUtils.findFirstChildNode(ancestorNode, RuleName.SELECT_CLAUSE);
        if (!selectClauseNode.isPresent()) {
            return Optional.absent();
        }
        Optional<ParserRuleContext> selectExpressionsNode = ExtractorUtils.findFirstChildNode(selectClauseNode.get(), RuleName.SELECT_EXPRS);
        if (!selectExpressionsNode.isPresent()) {
            return Optional.absent();
        }
        SelectClauseSegment result = new SelectClauseSegment(
                selectExpressionsNode.get().getStart().getStartIndex(), selectExpressionsNode.get().getStop().getStopIndex() + 2, hasDistinct(selectClauseNode.get()));
        for (int i = 0; i < selectExpressionsNode.get().getChildCount(); i++) {
            ParseTree childNode = selectExpressionsNode.get().getChild(i);
            if (childNode instanceof TerminalNodeImpl) {
                continue;
            }
            Optional<? extends ExpressionSegment> expressionSegment = expressionExtractor.extract((ParserRuleContext) childNode);
            if (expressionSegment.isPresent()) {
                result.getExpressions().add(expressionSegment.get());
            }
        }
        return Optional.of(result);
    }
    
    private boolean hasDistinct(final ParserRuleContext selectClauseNode) {
        return selectClauseNode.getChildCount() > 2 && DefaultKeyword.DISTINCT.name().equalsIgnoreCase(selectClauseNode.getChild(1).getText());
    }
}
