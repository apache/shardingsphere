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

package io.shardingsphere.core.parsing.antlr.extractor.segment.engine;

import com.google.common.base.Optional;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.parsing.antlr.extractor.segment.OptionalSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.constant.RuleName;
import io.shardingsphere.core.parsing.antlr.extractor.util.ASTUtils;
import io.shardingsphere.core.parsing.antlr.sql.segment.LimitSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.LimitValueSegment;
import io.shardingsphere.core.parsing.lexer.token.Symbol;
import io.shardingsphere.core.util.NumberUtil;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Limit extractor.
 *
 * @author duhongjun
 */
public class LimitExtractor implements OptionalSQLSegmentExtractor {
    
    @Override
    public Optional<LimitSegment> extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> limitNode = ASTUtils.findFirstChildNode(ancestorNode, RuleName.LIMIT_CLAUSE);
        if (!limitNode.isPresent()) {
            return Optional.absent();
        }
        Optional<ParserRuleContext> rangeNode = ASTUtils.findFirstChildNode(limitNode.get(), RuleName.RANGE_CLAUSE);
        if (!rangeNode.isPresent()) {
            return Optional.absent();
        }
        Collection<ParserRuleContext> questionNodes = ASTUtils.getAllDescendantNodes(ancestorNode, RuleName.QUESTION);
        Map<ParserRuleContext, Integer> questionNodeIndexMap = new HashMap<>();
        int index = 0;
        for (ParserRuleContext each : questionNodes) {
            questionNodeIndexMap.put(each, index++);
        }
        LimitValueSegment firstLimitValue = addLimitExtractResult(questionNodeIndexMap, (ParserRuleContext) rangeNode.get().getChild(0));
        LimitValueSegment secondLimitValue;
        if (rangeNode.get().getChildCount() >= 3) {
            secondLimitValue = addLimitExtractResult(questionNodeIndexMap, (ParserRuleContext) rangeNode.get().getChild(2));
            return Optional.of(new LimitSegment(DatabaseType.MySQL, Optional.of(firstLimitValue), Optional.of(secondLimitValue)));
        } else {
            return Optional.of(new LimitSegment(DatabaseType.MySQL, Optional.of(firstLimitValue)));
        }
    }
    
    private LimitValueSegment addLimitExtractResult(final Map<ParserRuleContext, Integer> questionNodeIndexMap, final ParserRuleContext node) {
        if (node.getText().equals(Symbol.QUESTION.getLiterals())) {
            if (questionNodeIndexMap.containsKey(node.getChild(0))) {
                return new LimitValueSegment(-1, questionNodeIndexMap.get(node.getChild(0)), ((ParserRuleContext) node.getChild(0)).getStart().getStartIndex());
            }
        } else {
            return new LimitValueSegment(NumberUtil.getExactlyNumber(node.getText(), 10).intValue(), -1, node.getStart().getStartIndex());
        }
        return null;
    }
}
