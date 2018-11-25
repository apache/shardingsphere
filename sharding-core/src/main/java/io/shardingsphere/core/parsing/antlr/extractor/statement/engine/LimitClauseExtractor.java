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

package io.shardingsphere.core.parsing.antlr.extractor.statement.engine;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import com.google.common.base.Optional;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.parsing.antlr.extractor.segment.CollectionSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.constant.RuleName;
import io.shardingsphere.core.parsing.antlr.extractor.util.ASTUtils;
import io.shardingsphere.core.parsing.antlr.sql.segment.LimitSegment;
import io.shardingsphere.core.parsing.lexer.token.Symbol;
import io.shardingsphere.core.util.NumberUtil;

/**
 * Limit clause extractor.
 *
 * @author duhongjun
 */
public class LimitClauseExtractor implements CollectionSQLSegmentExtractor {
    
    @Override
    public Collection<LimitSegment> extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> limitNode = ASTUtils.findFirstChildNode(ancestorNode, RuleName.LIMIT_CLAUSE);
        if (!limitNode.isPresent()) {
            return Collections.emptyList();
        }
        Optional<ParserRuleContext> rangeNode = ASTUtils.findFirstChildNode(limitNode.get(), RuleName.RANGE_CLAUSE);
        if (!rangeNode.isPresent()) {
            return Collections.emptyList();
        }
        Collection<LimitSegment> result = new LinkedList<>();
        Collection<ParserRuleContext> questionNodes = ASTUtils.getAllDescendantNodes(ancestorNode, RuleName.QUESTION);
        Map<ParserRuleContext, Integer> questionNodeIndexMap = new HashMap<>();
        int index = 0;
        for (ParserRuleContext each : questionNodes) {
            questionNodeIndexMap.put(each, index++);
        }
        addLimitExtractResult(result, questionNodeIndexMap, rangeNode.get().getChild(0));
        if (rangeNode.get().getChildCount() >= 3) {
            addLimitExtractResult(result, questionNodeIndexMap, rangeNode.get().getChild(2));
        }
        return result;
    }
    
    private void addLimitExtractResult(final Collection<LimitSegment> limitResult, final Map<ParserRuleContext, Integer> questionNodeIndexMap, final ParseTree node) {
        if (node.getText().equals(Symbol.QUESTION.getLiterals())) {
            if (questionNodeIndexMap.containsKey(node)) {
                limitResult.add(new LimitSegment(DatabaseType.MySQL, -1, questionNodeIndexMap.get(node)));
            }
        } else {
            limitResult.add(new LimitSegment(DatabaseType.MySQL, NumberUtil.getExactlyNumber(node.getText(), 10).intValue(), -1));
        }
    }
}
