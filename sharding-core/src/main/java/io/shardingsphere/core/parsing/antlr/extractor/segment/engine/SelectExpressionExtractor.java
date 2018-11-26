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
import java.util.LinkedList;

import org.antlr.v4.runtime.ParserRuleContext;

import com.google.common.base.Optional;

import io.shardingsphere.core.parsing.antlr.extractor.segment.CollectionSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.constant.RuleName;
import io.shardingsphere.core.parsing.antlr.extractor.util.ASTUtils;
import io.shardingsphere.core.parsing.antlr.sql.segment.CommonSelectExpressionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.SelectExpressionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.StarSelectExpressionSegment;
import io.shardingsphere.core.parsing.lexer.token.Symbol;

/**
 * Select expression extractor.
 *
 * @author duhongjun
 */
public class SelectExpressionExtractor implements CollectionSQLSegmentExtractor {
    
    @Override
    public Collection<SelectExpressionSegment> extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> selectClaseNode = ASTUtils.findFirstChildNode(ancestorNode, RuleName.SELECT_CLAUSE);
        if (!selectClaseNode.isPresent()) {
            return Collections.emptyList();
        }
        Collection<ParserRuleContext> selectExprNodes = ASTUtils.getAllDescendantNodes(selectClaseNode.get(), RuleName.SELECT_EXPR);
        if (selectExprNodes.isEmpty()) {
            return Collections.emptyList();
        }
        Collection<SelectExpressionSegment> result = new LinkedList<>();
        for (ParserRuleContext each : selectExprNodes) {
            String firstChildText = each.getChild(0).getText();
            if (firstChildText.endsWith(Symbol.STAR.getLiterals())) {
                int pos = firstChildText.indexOf(Symbol.DOT.getLiterals());
                String owner = "";
                if (0 < pos) {
                    owner = firstChildText.substring(0, pos - 1);
                }
                result.add(new StarSelectExpressionSegment(Optional.of(owner)));
            } else {
                ParserRuleContext firstChild = (ParserRuleContext) each.getChild(0);
                //TODO best choice using index
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < firstChild.getChildCount(); i++) {
                    builder.append(firstChild.getChild(i).getText() + " ");
                }
                String alias = "";
                if (3 == each.getChildCount()) {
                    alias = each.getChild(2).getText();
                }
                result.add(new CommonSelectExpressionSegment(builder.toString(), Optional.of(alias)));
            }
        }
        return Collections.emptyList();
    }
    
}
